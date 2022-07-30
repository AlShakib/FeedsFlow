package core.model

import com.squareup.moshi.Json

data class Response(@Json(name = "ok") val ok: Boolean, @Json(name = "error_code") val errorCode: Int = 0,
                    @Json(name = "description") val description: String = "")
