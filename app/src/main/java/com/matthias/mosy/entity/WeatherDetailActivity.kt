package com.matthias.mosy.entity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.text.LocaleDisplayNames
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.telecom.Call
import android.webkit.WebView
import android.widget.*
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.matthias.mosy.MainActivity
import com.matthias.mosy.Prefs
import com.matthias.mosy.R
import com.matthias.mosy.adapter.CustomListener
import com.matthias.mosy.adapter.CustomObserver
import com.matthias.mosy.adapter.ScrollAdapter
import com.matthias.mosy.bluetooth.BluetoothLeService
import com.matthias.mosy.fragments.PresetsFragment
import com.matthias.mosy.fragments.WeaterFragment
import com.matthias.mosy.httpclient.WeatherDataClient
import kotlinx.android.synthetic.main.activity_weather_detail.*
import kotlinx.android.synthetic.main.fragment_weather.*
import okhttp3.Callback
import okhttp3.Response
import org.w3c.dom.Text
import java.io.IOException
import java.lang.StringBuilder
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class WeatherDetailActivity : AppCompatActivity(),CustomListener{


  override fun notifyBlueState(value: Boolean) {
     runOnUiThread {
       if(value){
         btInfo.clearColorFilter()
       }else{
         btInfo.setColorFilter(overlay)
       }
       activateButton.isEnabled = value
     }
  }
  private var overlay:Int = Color.argb(155,255,255,255)

  private lateinit var description: TextView
  private lateinit var cityName: TextView

  private lateinit var pressure: TextView
  private lateinit var humidity: TextView
  private lateinit var temperature: TextView
  private lateinit var sunrise: TextView
  private lateinit var sunset: TextView
  private lateinit var activateButton: Button
  private lateinit var weatherIcon: ImageView
  private lateinit var btInfo: ImageView
  private lateinit var wind: TextView


  private var stateNumber : Int? = null
  private var prefs : Prefs? = null
  private var mediaPlayer: MediaPlayer? = null
  private var temperatureString:String = "0"

  private lateinit var bluetoothService: BluetoothLeService


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
    activateButton = findViewById(R.id.activate_presets_btn)
    weatherIcon = findViewById(R.id.weather_icon )
    btInfo = findViewById(R.id.btInfo)
    sunrise = findViewById(R.id.sunrise_text)
    sunset = findViewById(R.id.sunset_text)
    wind = findViewById(R.id.wind_text)

  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_weather_detail)
    loadViewVars()
    loadView()
    bluetoothService = MainActivity.bluetoothLeService
    prefs = MainActivity.prefs
    prefs!!.addListener(this)

    mediaPlayer = MediaPlayer()



    btInfo.setOnClickListener{ myView ->
      if(prefs!!.BT_ENABLED){
        Toast.makeText(this,"Bereits mit Sydney verbunden", Toast.LENGTH_SHORT).show()
      }else{
        Toast.makeText(this,"Versuche mit Sydney zu verbinden", Toast.LENGTH_SHORT).show()
        bluetoothService.connect(MainActivity.HM10_ADDRESS)
      }
    }

    activate_presets_btn.setOnClickListener { myView ->
      if(stateNumber != null){
        //mediaPlayer?.start()
        //
        val source: Int? = PresetsFragment.SONGS.get(stateNumber!!)
        if(source != null){
          //todo: das hier noch einmal anpassen
          var path = "android.resource://com.matthias.mosy/${source}"
          mediaPlayer?.setDataSource(path)
          mediaPlayer?.start()
        }

        bluetoothService.write(stateNumber.toString() + "|" + temperatureString)
      }
    }

    deleteCityBtn.setOnClickListener{myView ->
        val dialog = AlertDialog.Builder(this).setTitle("${cityName.text} löschen ? ").setMessage("Soll ${cityName.text} aus den gespeicherten Städten gelöscht werden?")
                .setPositiveButton("Bestätigen") { dialog, i ->
                  MainActivity.prefs?.deleteCityById(intent.extras.getInt(EXTRA_ID))
                  finish()
                }
                .setNegativeButton("Zurück", { dialog, i -> })
        dialog.show()
      }
  }


  fun loadView(){

    val cityId= intent.extras.getInt(EXTRA_ID)
    var list = ArrayList<Int>()
    list.add(cityId)

    var call = WeatherDataClient.getData(this,listOfIds = list)

    call?.enqueue(object: Callback{
      override fun onFailure(call: okhttp3.Call, e: IOException) {
        if (e is UnknownHostException){
          var handler = Handler(Looper.getMainLooper())
          handler.post{
            Toast.makeText(this@WeatherDetailActivity.applicationContext, "Hat das Handy eine aktive Internetverbindung?", Toast.LENGTH_LONG).show()
          }
        }
      }

      override fun onResponse(call: okhttp3.Call, response: Response) {
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
    stateNumber = PresetsFragment.LABEL_WEATHER_REVERSE.get(weather?.description)

    cityName.text = weather?.city
    pressure.text = weather?.pressure?.toString() + " hPa"
    humidity.text = weather?.humidity?.toString() + " %"
    temperature.text = weather?.temperature?.toString() + "°"
    temperatureString = weather!!.temperature.toInt().toString()

    val sunsetTime = Date(weather.sunsetUnix!!*1000)
    val sunriseTime = Date(weather.sunriseUnix!!*1000)

    val dateFormat= SimpleDateFormat("HH:mm")
    sunrise.text = dateFormat.format(sunriseTime)
    sunset.text = dateFormat.format(sunsetTime)

    wind.text = weather?.windspeed.toString() + " km/h"


    var iconPath = "ic_${weather?.iconID}"
    var idPath = resources.getIdentifier(iconPath,"drawable", packageName)

    weatherIcon.setImageResource(idPath)
    weatherIcon.adjustViewBounds = true
  }
}
