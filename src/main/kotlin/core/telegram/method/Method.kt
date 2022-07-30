package core.telegram.method

import com.squareup.moshi.Moshi
import core.model.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

abstract class Method(@field:Transient private val methodName: String) {

    fun execute(apiUrl: String, client: OkHttpClient, moshi: Moshi): Response {
        val requestBody = getRequestBody(moshi)
        val url: String = if (apiUrl.endsWith("/")) {
            if (methodName.startsWith("/")) {
                apiUrl + methodName.substring(1)
            } else {
                apiUrl + methodName
            }
        } else {
            if (!methodName.startsWith("/")) {
                "$apiUrl/$methodName"
            } else {
                apiUrl + methodName
            }
        }
        val request: Request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        try {
            val call = client.newCall(request)
            val httpResponse = call.execute()
            val responseBody = httpResponse.body ?: return Response(false)
            val response = moshi.adapter(Response::class.java).fromJson(responseBody.string())
            httpResponse.close()
            return response ?: Response(false)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return Response(false)
    }

    protected abstract fun getRequestBody(moshi: Moshi): RequestBody
}
