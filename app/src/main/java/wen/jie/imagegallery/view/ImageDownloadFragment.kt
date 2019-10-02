package wen.jie.imagegallery.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_download.*
import wen.jie.imagegallery.R
import wen.jie.imagegallery.model.ImageData

class ImageDownloadFragment(private val imageDownloadAdapter: ImageDownloadAdapter) : Fragment() {
    companion object {
        const val TAG = "ImageDownloadFragment"
        const val IMAGE_DATA_KEY = "Image_Data_Key"
    }

    interface ImageDownloadAdapter {
        fun download(imageData: ImageData)
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
        }
    }

    private fun init(imageData: ImageData) {
        Picasso.get()
            .load(imageData.largeImageURL)
            .error(R.drawable.ic_launcher_foreground)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(imageView)

        downloadButton.setOnClickListener {
            imageDownloadAdapter.download(imageData)
        }
    }
}