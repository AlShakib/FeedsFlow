package core.telegram

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import core.model.Response
import core.telegram.method.Method
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.TimeUnit

object TelegramBot {
    private val apiUrl: String
    private val httpClient: OkHttpClient
    private val moshi: Moshi
    private var lastSentTime: Long = 0

    init {
        val botToken = System.getenv("TELEGRAM_BOT_TOKEN")

        if (botToken.isNullOrBlank()) {
            throw RuntimeException("Telegram Bot Token is not available. The environment variable TELEGRAM_BOT_TOKEN must be defined with a valid Bot Token")
        }
        apiUrl = "https://api.telegram.org/bot$botToken/"
        httpClient = OkHttpClient.Builder()
            .readTimeout(6, TimeUnit.HOURS)
            .writeTimeout(6, TimeUnit.HOURS)
            .connectTimeout(6, TimeUnit.HOURS)
            .callTimeout(6, TimeUnit.HOURS)
            .build()
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    fun execute(method: Method): Response {
        waitIfRequired()
        return method.execute(apiUrl, httpClient, moshi)
    }

    private fun waitIfRequired() {
        val currentTime = Date().time
        val diff = currentTime - lastSentTime
        if (diff < 1000) {
            try {
                Thread.sleep(1000 - diff)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        lastSentTime = currentTime
    }
}
