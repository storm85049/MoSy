package com.matthias.mosy.httpclient

import android.content.Context
import com.matthias.mosy.R
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * ein einfacher client, der den aufwändigen call unten wrappt.
 */
class WeatherDataClient
{
    companion object {

        val baseUrlSingle ="http://api.openweathermap.org/data/2.5/weather?q="
        val baseUrlMultiple="http://api.openweathermap.org/data/2.5/group?id="
        val urlOptionalParams = "&units=metric&lang=de"


        fun getData(context: Context, q:String = "", listOfIds: ArrayList<Int> = ArrayList<Int>()): Call?
        {
            var client = OkHttpClient()

            if(q != ""){
                var url = baseUrlSingle + q +
                        urlOptionalParams +
                        "&APPID="+ context.getString(R.string.api_key)
                var request = Request.Builder().url(url).build()
                return client.newCall(request)
            }else if (listOfIds == null || !listOfIds.isEmpty()){
                var stringOfIds = listOfIds.joinToString(",")
                var url = baseUrlMultiple+ stringOfIds  +
                        urlOptionalParams +
                        "&APPID="+ context.getString(R.string.api_key)
                var request = Request.Builder().url(url).build()
                return client.newCall(request)
            }
            return null
        }
    }
}
