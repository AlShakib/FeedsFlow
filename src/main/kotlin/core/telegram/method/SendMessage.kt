package core.telegram.method

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class SendMessage(@Json(name = "chat_id") val chatId: String, @Json(name = "text") val text: String,
                       @Json(name = "parse_mode") val parseMode: String = "",
                       @Json(name = "disable_web_page_preview") val disableWebPagePreview: Boolean = false,
                       @Json(name = "disable_notification") val disableNotification: Boolean = false,
                       @Json(name = "protect_content") val protectContent: Boolean = false) : Method("sendMessage") {

    override fun getRequestBody(moshi: Moshi): RequestBody {
        return moshi.adapter(SendMessage::class.java).toJson(this).toRequestBody("application/json; charset=utf-8".toMediaType())
    }
}
