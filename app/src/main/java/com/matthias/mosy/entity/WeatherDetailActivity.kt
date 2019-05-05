package com.matthias.mosy.entity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.matthias.mosy.R
import com.matthias.mosy.adapter.ScrollAdapter
import com.matthias.mosy.httpclient.WeatherDataClient
import kotlinx.android.synthetic.main.fragment_weather.*
import okhttp3.*
import java.io.IOException
import java.lang.StringBuilder


class WeatherDetailActivity : AppCompatActivity() {

  private lateinit var description: TextView
  private lateinit var cityName: TextView

  private lateinit var pressure: TextView
  private lateinit var humidity: TextView
  private lateinit var temperature: TextView
  private lateinit var activateButton: Button
  private lateinit var weatherIcon: ImageView




  companion object {
    const val EXTRA_CITY = "city"
    const val EXTRA_ID = "id"

    fun newIntent(context: Context, weather: Weather): Intent {
      val intent = Intent(context, WeatherDetailActivity::class.java)
      intent.putExtra(EXTRA_CITY, weather.city)
      intent.putExtra(EXTRA_ID, weather.cityId)
      return intent
    }
  }

  fun loadViewVars() {
    description = findViewById(R.id.weather_description)
    cityName = findViewById(R.id.city_text)
    pressure = findViewById(R.id.pressure_text)
    humidity = findViewById(R.id.humidity_text)
    temperature = findViewById(R.id.temperature_text)
    activateButton = findViewById(R.id.activate_btn)
    weatherIcon = findViewById(R.id.weather_icon )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_weather_detail)
    loadViewVars()
    loadView()

  }

  fun loadView(){

    val city = intent.extras.getString(EXTRA_CITY)
    val cityId= intent.extras.getInt(EXTRA_ID)
    var list = ArrayList<Int>()
    list.add(cityId)

    var call = WeatherDataClient.getData(this,listOfIds = list)

    call?.enqueue(object: Callback{
      override fun onFailure(call: Call, e: IOException) {
        //toast dass die inetverbindung gefailt ist.
      }

      override fun onResponse(call: Call, response: Response) {
        var handler = Handler(Looper.getMainLooper())
        handler.post{
          var responseString= response.body()?.string()
          val parser = Parser()
          val stringBuilder = StringBuilder(responseString)
          val jsonObject : JsonObject = parser.parse(stringBuilder) as JsonObject

          val list: ArrayList<Weather>? = Weather.createWeatherListItem(jsonObject,true)
          var weather:Weather? = list?.get(0)
          populateViewVars(weather)

        }
      }
    })
  }

  fun populateViewVars(weather: Weather?){

    description.text = weather?.description
    cityName.text = weather?.city
    pressure.text = weather?.pressure?.toString() + " hPa"
    humidity.text = weather?.humidity?.toString() + " %"
    temperature.text = weather?.temperature?.toString() + "°"

    var iconPath = "ic_${weather?.iconID}"
    var idPath = resources.getIdentifier(iconPath,"drawable", packageName)
    weatherIcon.setImageResource(idPath)



  }


}
