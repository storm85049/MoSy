package com.matthias.mosy.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.matthias.mosy.MainActivity
import com.matthias.mosy.Prefs
import com.matthias.mosy.R
import com.matthias.mosy.adapter.CustomObserver
import com.matthias.mosy.entity.Weather
import com.matthias.mosy.entity.WeatherAdapter
import com.matthias.mosy.httpclient.WeatherDataClient
import okhttp3.*
import java.io.IOException
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.StringBuilder
import java.net.UnknownHostException
import java.util.*


const val MESSAGE_ADD_CITY :Int = 0

class SearchWeatherDialog(context: Context, private var customObserver: CustomObserver) : Dialog(context){


    private lateinit var searchText: EditText
    private lateinit var progressIndicator : ProgressBar
    private lateinit var listView: ListView
    private lateinit var alertDialog: android.support.v7.app.AlertDialog



    fun showDialog()
    {

        val builder = android.support.v7.app.AlertDialog.Builder(this.context)
        val layoutInflater = LayoutInflater.from(this.context)
        val view = layoutInflater.inflate(R.layout.search_dialog,null)

        searchText = view.findViewById(R.id.city_user_string) as EditText
        progressIndicator = view.findViewById(R.id.progressBar_cyclic) as ProgressBar
        listView = view.findViewById<ListView>(R.id.weather_result_list) as ListView

        searchText.setOnEditorActionListener{v, actionID, _->
            if(actionID == EditorInfo.IME_ACTION_DONE){
                val place = searchText.text
                this.progressIndicator.visibility = View.VISIBLE
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v?.windowToken,0)
                this.getWeatherDataFor(place.toString(), view)
            }
            true
        }

        with(builder){
            setTitle("Neuen Ort hinzufügen")
            setPositiveButton("Suchen",null)
            setNegativeButton("Zurück",null)
            setView(view)
        }

        alertDialog = builder.create()
        alertDialog.show()


        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener { view ->
            val place = searchText.text
            this.progressIndicator.visibility = View.VISIBLE
            this.getWeatherDataFor(place.toString(), view)
        }
    }

    private fun getWeatherDataFor(city:String, view: View)
    {

     var call: Call? = WeatherDataClient.getData(context,city)

        call?.enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {

                if (e is UnknownHostException){
                    //Toast.makeText(context, "Ist das WLAN auf dem Handy aktiviert?", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                var handler = Handler(Looper.getMainLooper())
                handler.post {
                    progressIndicator.visibility = View.GONE
                    var responseString= response.body()?.string()
                    val parser = Parser()
                    val stringBuilder = StringBuilder(responseString)
                    val jsonObject : JsonObject = parser.parse(stringBuilder) as JsonObject

                    if(response.code() != 200){
                        var message = jsonObject.get("message") as String
                        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
                    }else{
                        val convertedList = Weather.createWeatherListItem(jsonObject)
                        if(convertedList != null) {
                            val adapter = WeatherAdapter(context, convertedList)
                            listView.adapter = adapter

                            listView.setOnItemClickListener { _, _, position, _ ->
                                val selectedWeather = convertedList[position]
                                addToSavedCities(selectedWeather)


                                customObserver.apply()
                                //handler.obtainMessage(MESSAGE_ADD_CITY, -1, -1).sendToTarget()
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
        })
    }

    fun addToSavedCities(weatherEntity: Weather)
    {
        MainActivity.prefs?.addCity(weatherEntity.cityId)
    }


}
