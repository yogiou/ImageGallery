package wen.jie.imagegallery

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import wen.jie.imagegallery.model.DownloadImageService
import wen.jie.imagegallery.model.ImageData
import wen.jie.imagegallery.utils.ProgressResponseBody
import wen.jie.imagegallery.utils.RxBus
import wen.jie.imagegallery.view.ImageDownloadFragment
import wen.jie.imagegallery.view.ImageListFragment
import wen.jie.imagegallery.viewmodel.MainActivityViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), ImageListFragment.ImageListFragmentCallBack, ImageDownloadFragment.ImageDownloadAdapter {
    companion object {
        private const val REQUEST_CODE_WRITE_PERMISSION = 1002
    }

    private val imageDownloadFragmentViewModel = MainActivityViewModel()
    private val hashMap: HashMap<String, NotificationCompat.Builder> = HashMap()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val picSavePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
    private var imageData: ImageData? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_WRITE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageData?.let {
                        download(it)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        init()
        initSubscription()
    }

    private fun init() {
        val newFragment = ImageListFragment(this)
        swapFragment(newFragment, ImageListFragment.TAG)
    }

    private fun swapFragment(fragment: Fragment, fragmentTag: String) {
        val ft = supportFragmentManager?.beginTransaction()
        ft?.addToBackStack(fragmentTag)
        ft?.replace(R.id.container, fragment)
        ft?.commit()
    }

    override fun goToImageDownload(imageData: ImageData) {
        val imageDownloadFragment = ImageDownloadFragment(this)
        val bundle = Bundle()
        bundle.putSerializable(ImageDownloadFragment.IMAGE_DATA_KEY, imageData)
        imageDownloadFragment.arguments = bundle
        swapFragment(imageDownloadFragment, ImageDownloadFragment.TAG)
    }

    private fun startNotification(imageData: ImageData, progress: Int) {
        val notificationID = imageData.id.toInt()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "channel_id")
        notificationBuilder
            .setContentTitle(if (progress < 100) resources.getString(R.string.downloading) else resources.getString(R.string.finish_download))
            .setContentText(imageData.largeImageURL.substring(imageData.largeImageURL.lastIndexOf(
                File.separator
            ) + 1))
            .setSmallIcon(R.drawable.ic_launcher_background)

        notificationBuilder.setProgress(100, progress, false)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val channel =  NotificationChannel(
                "channel_id",
                imageData.largeImageURL.substring(imageData.largeImageURL.lastIndexOf(File.separator) + 1),
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            notificationBuilder.setChannelId("channel_id")
        }

        val notification = notificationBuilder.build()
        hashMap[imageData.id] = notificationBuilder
        notificationManager.notify(notificationID, notification)
    }

    private fun initSubscription() {
        RxBus.getInstance().toObservable()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { rxEvent ->
                when (rxEvent) {
                    is MainActivityViewModel.SaveFileRxEvent -> {
                        save(rxEvent.imageResponse?.responseBody, rxEvent.imageResponse?.name)
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
        sendBroadcast(intent)
    }

    private fun save(body: ResponseBody?, name: String?) {
        if (!File(picSavePath).exists()) {
            File(picSavePath).mkdir()
        }

        val path = "$picSavePath/$name"
        var inputStream: InputStream? = null
        var outputStream = FileOutputStream(path)

        try {
            body?.let { responseBody ->
                inputStream = responseBody.byteStream()
                var c: Int
                val buffer = ByteArray(1024)

                inputStream?.let {
                    do {
                        c = it.read(buffer)

                        if (c == -1) {
                            break
                        }

                        outputStream.write(buffer, 0, c)
                    } while (true)
                }

                broadcastPhotoSaved(File(path))
            }
        } catch (e: IOException) {

        } finally {
            inputStream?.close()
            outputStream.close()
        }
    }

    override fun onDestroy() {
        clearSubscriptions()
        imageDownloadFragmentViewModel.onCleared()
        super.onDestroy()
    }

    override fun download(imageData: ImageData) {
        this.imageData = imageData

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
            }), imageData.largeImageURL.substring(imageData.largeImageURL.lastIndexOf(File.separator) + 1))
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_PERMISSION)
        }
    }
}
