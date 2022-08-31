package com.kodgem.coachingapp.services

import android.app.Activity
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.kodgem.coachingapp.R
import org.json.JSONException
import org.json.JSONObject


class FcmNotificationsSender(
    private var userFcmToken: String?,
    var title: String?,
    private var body: String?,
    private var mActivity: Activity?
) {


    private var requestQueue: RequestQueue? = null
    private val postUrl = "https://fcm.googleapis.com/fcm/send"
    private val fcmServerKey ="YOUR-Server-Key"

    fun sendNotifications() {
        requestQueue = Volley.newRequestQueue(mActivity)
        val mainObj = JSONObject()
        try {
            mainObj.put("to", userFcmToken)
            val notiObject = JSONObject()
            notiObject.put("title", title)
            notiObject.put("body", body)
            notiObject.put(
                "icon", R.drawable.ic_baseline_book_24
            ) // enter icon that exists in drawable only
            mainObj.put("notification", notiObject)
            val request: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, postUrl, mainObj, Response.Listener {
                    // code run is got response
                }, Response.ErrorListener {
                    // code run is got error
                }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val header: MutableMap<String, String> = HashMap()
                        header["content-type"] = "application/json"
                        header["authorization"] = "key=$fcmServerKey"
                        return header
                    }
                }
            requestQueue!!.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

}