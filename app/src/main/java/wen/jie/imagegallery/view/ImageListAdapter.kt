package wen.jie.imagegallery.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_list_cell.view.*
import wen.jie.imagegallery.R
import wen.jie.imagegallery.model.ImageData
import java.lang.Exception

class ImageListAdapter(private val adapterCallBack: AdapterCallBack) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TAG = "ImageListAdapter"
    }

    private val imageFileList = mutableListOf<ImageData>()

    interface AdapterCallBack {
        fun onClick(imageData: ImageData)
    }

    fun updateImageList(imageList: List<ImageData>) {
        imageFileList.clear()
        imageFileList.addAll(imageList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ImageListViewHolder(inflater.inflate(R.layout.image_list_cell, parent, false))
    }

    override fun getItemCount(): Int {
        return imageFileList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemData = imageFileList[position]
        (holder as? ImageListViewHolder)?.bind(itemData.previewURL, View.OnClickListener {
            adapterCallBack.onClick(itemData)
        })
    }

    class ImageListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(url: String, onClick: View.OnClickListener) {
            itemView.apply {
                Picasso.get()
                    .load(url)
                    .apply {
                        placeholder(R.drawable.ic_launcher_foreground)
                    }
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(thumbnail, object : Callback {
                        override fun onSuccess() {
                            android.util.Log.v(TAG, "load image success")
                        }

                        override fun onError(e: Exception?) {
                            thumbnail.setImageResource(R.drawable.ic_launcher_foreground)
                        }
                    })

                setOnClickListener(onClick)
            }
        }
    }
}