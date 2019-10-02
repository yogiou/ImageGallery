package wen.jie.imagegallery.utils

import android.content.Context
import android.net.ConnectivityManager

class SystemHelper {
    companion object {
        fun isNetworkConnected(context: Context?): Boolean {
            context?.let {
                val connectivityManager = it.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
            return false
        }
    }
}