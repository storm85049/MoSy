package com.matthias.mosy.entity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import java.io.IOException
import java.lang.StringBuilder
import java.util.*


class WeatherDetailActivity : AppCompatActivity(),CustomListener{



   override fun notifyBlueState(value: Boolean) {
     runOnUiThread {
       var colorId = if (value) resources.getColor(R.color.btConnected) else resources.getColor(R.color.btNotConnected)
       btStateBtn.backgroundTintList  = ColorStateList.valueOf(colorId)
       activateButton.isEnabled = value
     }
  }

  private lateinit var description: TextView
  private lateinit var cityName: TextView

  private lateinit var pressure: TextView
  private lateinit var humidity: TextView
  private lateinit var temperature: TextView
  private lateinit var activateButton: Button
  private lateinit var weatherIcon: ImageView
  private lateinit var btStateBtn: ImageButton
  private var observer: CustomObserver? = null

  private var stateNumber : Int? = null
  private var prefs : Prefs? = null
  private var mediaPlayer: MediaPlayer? = null;

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
    activateButton = findViewById(R.id.activate_btn)
    weatherIcon = findViewById(R.id.weather_icon )
    btStateBtn = findViewById(R.id.bluetoothStateBtn)

   /* var colorId = if (prefs!!.BT_ENABLED) resources.getColor(R.color.btConnected) else resources.getColor(R.color.btNotConnected)
    btStateBtn.backgroundTintList  = ColorStateList.valueOf(colorId)
    if(prefs!!.BT_ENABLED){
      activateButton.isEnabled = true
    }*/
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_weather_detail)
    loadViewVars()
    prefs = MainActivity.prefs
    prefs!!.addListener(this)
    mediaPlayer = MediaPlayer.create(this, R.raw.lil_pump_iced_out)

    loadView()

    bluetoothService = MainActivity.bluetoothLeService

    btStateBtn.setOnClickListener{ myView ->
      if(prefs!!.BT_ENABLED){
        Toast.makeText(this,"Bereits mit Sydney verbunden", Toast.LENGTH_SHORT).show()
      }else{
        Toast.makeText(this,"Versuche mit Sydney zu verbinden", Toast.LENGTH_SHORT).show()
        bluetoothService.connect(MainActivity.HM10_ADDRESS)
      }
    }

    activate_btn.setOnClickListener { myView ->
      if(stateNumber != null){
        mediaPlayer?.start()
        bluetoothService.write(stateNumber.toString())
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

  fun addObserver(myObserver:CustomObserver){
    observer = myObserver
  }

  fun loadView(){

    val city = intent.extras.getString(EXTRA_CITY)
    val cityId= intent.extras.getInt(EXTRA_ID)
    var list = ArrayList<Int>()
    list.add(cityId)

    var call = WeatherDataClient.getData(this,listOfIds = list)

    call?.enqueue(object: Callback{
      override fun onFailure(call: okhttp3.Call, e: IOException) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    var iconPath = "ic_${weather?.iconID}"
    var idPath = resources.getIdentifier(iconPath,"drawable", packageName)

    weatherIcon.setImageResource(idPath)
    weatherIcon.adjustViewBounds = true
  }
}
