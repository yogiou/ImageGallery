package wen.jie.imagegallery

class Config {
    companion object {
        lateinit var instance: Config

        const val NETWORK_TIMEOUT = 15L
        const val BASE_API_DOMAIN = "pixabay.com/"
        const val BASE_API_URL = "https://$BASE_API_DOMAIN"
    }
}