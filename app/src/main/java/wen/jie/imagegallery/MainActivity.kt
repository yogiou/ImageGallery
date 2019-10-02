package wen.jie.imagegallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import kotlinx.android.synthetic.main.activity_main.*
import wen.jie.imagegallery.model.ImageData
import wen.jie.imagegallery.view.ImageDownloadFragment
import wen.jie.imagegallery.view.ImageListFragment

class MainActivity : AppCompatActivity(), ImageListFragment.ImageListFragmentCallBack {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        init()
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
        val imageDownloadFragment = ImageDownloadFragment()
        val bundle = Bundle()
        bundle.putSerializable(ImageDownloadFragment.IMAGE_DATA_KEY, imageData)
        imageDownloadFragment.arguments = bundle
        swapFragment(imageDownloadFragment, ImageDownloadFragment.TAG)
    }
}
