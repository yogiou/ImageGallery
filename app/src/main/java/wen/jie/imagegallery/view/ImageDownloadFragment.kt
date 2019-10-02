package wen.jie.imagegallery.view

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_image_download.*
import okhttp3.ResponseBody
import wen.jie.imagegallery.R
import wen.jie.imagegallery.model.DownloadImageService
import wen.jie.imagegallery.model.ImageData
import wen.jie.imagegallery.utils.ProgressResponseBody
import wen.jie.imagegallery.utils.RxBus
import wen.jie.imagegallery.viewmodel.ImageDownloadFragmentViewModel
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ImageDownloadFragment : Fragment() {
    companion object {
        const val TAG = "ImageDownloadFragment"
        const val IMAGE_DATA_KEY = "Image_Data_Key"
        private const val REQUEST_CODE_WRITE_PERMISSION = 1002
    }

    private val imageDownloadFragmentViewModel = ImageDownloadFragmentViewModel()
    private val hashMap: HashMap<String, NotificationCompat.Builder> = HashMap()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val picSavePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_WRITE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadButton?.performClick()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_download, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments?.getSerializable(IMAGE_DATA_KEY)
        argument?.let {
            val imageData = it as ImageData
            init(imageData)
            imageInfoTextView?.text = resources.getString(R.string.info, imageData.likes.toString(), imageData.imageWidth.toString(), imageData.imageHeight.toString(), imageData.comments, imageData.tags, imageData.downloads.toString(), imageData.favorites.toString(), imageData.views.toString(), imageData.imageSize.toString(), imageData.user)
            initSubscription(imageData)
        }
    }

    override fun onDestroy() {
        clearSubscriptions()
        imageDownloadFragmentViewModel.onCleared()
        super.onDestroy()
    }

    private fun init(imageData: ImageData) {
        Picasso.get()
            .load(imageData.largeImageURL)
            .error(R.drawable.ic_launcher_foreground)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(imageView)

        downloadButton.setOnClickListener {
            context?.let {
                if (ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    imageDownloadFragmentViewModel.downloadImage(DownloadImageService.create(object : ProgressResponseBody.OnAttachmentDownloadListener {

                        override fun onAttachmentDownloadUpdate(percent: Int) {
                            if (percent % 10 == 0 || percent == 100) {
                                startNotification(imageData, percent)
                            }
                        }

                        override fun onAttachmentDownloadedError() {

                        }

                        override fun onAttachmentDownloadedFinished() {

                        }

                        override fun onAttachmentDownloadedSuccess() {

                        }
                    }), imageData.largeImageURL.substring(imageData.largeImageURL.lastIndexOf(separator) + 1))
                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_PERMISSION)
                }
            }
        }
    }

    private fun startNotification(imageData: ImageData, progress: Int) {
        val notificationID = imageData.id.toInt()

        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        context?.let {
            val notificationBuilder = NotificationCompat.Builder(it, "channel_id")
            notificationBuilder
                .setContentTitle(if (progress < 100) resources.getString(R.string.downloading) else resources.getString(R.string.finish_download))
                .setContentText(imageData.largeImageURL.substring(imageData.largeImageURL.lastIndexOf(separator) + 1))
                .setSmallIcon(R.drawable.ic_launcher_background)

            notificationBuilder.setProgress(100, progress, false)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                val channel =  NotificationChannel(
                    "channel_id",
                    imageData.largeImageURL.substring(imageData.largeImageURL.lastIndexOf(separator) + 1),
                    NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
                notificationBuilder.setChannelId("channel_id")
            }

            val notification = notificationBuilder.build()
            hashMap[imageData.id] = notificationBuilder
            notificationManager.notify(notificationID, notification)
        }
    }

    private fun initSubscription(imageData: ImageData) {
        RxBus.getInstance().toObservable()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { rxEvent ->
                when (rxEvent) {
                    is ImageDownloadFragmentViewModel.SaveFileRxEvent -> {
                        save(rxEvent.responseBody, imageData.largeImageURL.substring(imageData.largeImageURL.lastIndexOf(separator) + 1))
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    private fun clearSubscriptions() {
        compositeDisposable.clear()
    }

    private fun broadcastPhotoSaved(file: File) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        intent.data = contentUri
        context?.sendBroadcast(intent)
    }

    private fun save(body: ResponseBody?, name: String) {
        if (!File(picSavePath).exists()) {
            File(picSavePath).mkdir()
        }

        val path = "$picSavePath/$name"
        val outputStream = FileOutputStream(path)

        try {
            body?.let { responseBody ->
                outputStream.write(responseBody.bytes())
                broadcastPhotoSaved(File(path))
            }
        } catch (e: IOException) {

        } finally {
            outputStream.close()
        }
    }
}