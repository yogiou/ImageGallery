package wen.jie.imagegallery.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import wen.jie.imagegallery.utils.BaseErrorHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_image_list.*
import wen.jie.imagegallery.R
import wen.jie.imagegallery.model.ImageData
import wen.jie.imagegallery.utils.RxBus
import wen.jie.imagegallery.viewmodel.ImageListFragmentViewModel

class ImageListFragment(private val imageListFragmentCallBack: ImageListFragmentCallBack) : Fragment(), ImageListAdapter.AdapterCallBack {
    private val compositeDisposable by lazy { CompositeDisposable() }
    private val imageListFragmentViewModel: ImageListFragmentViewModel = ImageListFragmentViewModel()
    private var currentPage: Int = 1

    companion object {
        const val TAG = "ImageListFragment"
    }

    interface ImageListFragmentCallBack {
        fun goToImageDownload(imageData: ImageData)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initSubscription()
    }

    override fun onResume() {
        super.onResume()
        imageListFragmentViewModel.getImageLists(currentPage.toString())
        initUI()
    }

    private fun initUI() {
        if (currentPage == 1) {
            previous.visibility = View.GONE
        } else {
            previous.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        clearSubscriptions()
        imageListFragmentViewModel.onCleared()
        super.onDestroy()
    }

    private fun clearSubscriptions() {
        compositeDisposable.clear()
    }

    private fun initSubscription() {
        RxBus.getInstance().toObservable()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { rxEvent ->
                when (rxEvent) {
                    is ImageListFragmentViewModel.DisplayOnShowImageList -> {
                        setAdapter(rxEvent.responseData.hits)
                        initUI()
                    }
                    is ImageListFragmentViewModel.DisplayOnShowLoading -> {
                        showLoading(rxEvent.isVisible)
                    }
                    is BaseErrorHelper.BaseError -> {
                        currentPage--
                    }
                }
            }
            .addTo(compositeDisposable)
    }

    private fun init() {
        imageList?.layoutManager = GridLayoutManager(context, columnForWidth(resources.displayMetrics.widthPixels))
        next?.setOnClickListener {
            currentPage++
            imageListFragmentViewModel.getImageLists(currentPage.toString())
        }

        previous?.setOnClickListener {
            currentPage--

            if (currentPage >= 1) {
                imageListFragmentViewModel.getImageLists(currentPage.toString())
            }
        }
    }

    private fun setAdapter(list: List<ImageData>) {
        val adapter = ImageListAdapter(this)
        adapter.updateImageList(list)
        imageList.adapter = adapter
    }

    private fun columnForWidth(widthPx: Int): Int {
        val widthDp = dpFromPx(widthPx.toFloat())

        if (widthDp >= 900) {
            return 5
        } else if (widthDp >= 600) {
            return 3
        } else if (widthDp >= 480) {
            return 2
        } else if (widthDp >= 320) {
            return 2
        } else {
            return 2
        }
    }

    private fun dpFromPx(px: Float): Int {
        return (px / resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun showLoading(isVisible: Boolean) {
        if (isVisible) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    override fun onClick(imageData: ImageData) {
        imageListFragmentCallBack.goToImageDownload(imageData)
    }
}