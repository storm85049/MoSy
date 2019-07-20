package com.matthias.mosy.fragments


import android.app.Fragment
import android.os.*
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AbsListView.*
import android.widget.ListView
import android.widget.Toast
import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonException
import com.beust.klaxon.Parser
import com.matthias.mosy.MainActivity
import com.matthias.mosy.R
import com.matthias.mosy.adapter.CustomObserver
import com.matthias.mosy.adapter.ScrollAdapter
import com.matthias.mosy.dialog.SearchWeatherDialog
import com.matthias.mosy.entity.Weather
import com.matthias.mosy.entity.WeatherAdapter
import com.matthias.mosy.entity.WeatherDetailActivity
import com.matthias.mosy.httpclient.WeatherDataClient



import kotlinx.android.synthetic.main.fragment_weather.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.net.UnknownHostException
import java.util.*


/**
 * dieses fragment kümmert sich um das Erstellen der Liste aus den gespeicherten IDs in den SharedPreferences und das
 * Handlen beim Click auf einen Ort oder beim Click auf das Plus-Symbol
 */
class WeatherFragment() : android.support.v4.app.Fragment(), CustomObserver {

    /***updateList wenn über das plus symbol im alert eine neue stadt hinzugefügt wurde*/
    override fun applyChanges() {
        initList()
    }

    private lateinit var listView : ListView
    private lateinit var swipeLayout : SwipeRefreshLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    companion object {
        const val NAME = "Wetter"
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        listView = view!!.findViewById<ListView>(R.id.weather_list)
        this.initList()

        fab.setOnClickListener { myView ->
            var searchWeatherDialog = SearchWeatherDialog(context, this)
            searchWeatherDialog.showDialog()
        }

        swipeLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeLayout.setOnRefreshListener {
            var mHandler = Handler()
            var mRunnable = Runnable{
                initList()
            }
            mHandler.post(mRunnable)
        }

    }

    fun initList()
    {
        var savedCities = MainActivity.prefs?.getSavedCities()
        var call : Call?  = WeatherDataClient.getData(context,listOfIds = savedCities!!)

        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException){
                    var handler = Handler(Looper.getMainLooper())
                    handler.post{
                        Toast.makeText(context, "Hat das Handy eine aktive Internetverbindung?", Toast.LENGTH_LONG).show()
                        swipeLayout.isRefreshing = false
                    }
                }
            }
            override fun onResponse(call: Call, response: Response) {
                handleResponse(response)
            }
        })
    }

    fun openWeather(weather: Weather){
        val intent = WeatherDetailActivity.newIntent(context,weather)
        startActivity(intent)
    }

    /**
     * wenn eine antwort von der api wiederkommt, wird das JSON geparst, in WeatherObjekte umgewandelt
     * und als Liste an den Weatheradapter übergeben, der dann eine ListView erstellt.
     */
    fun handleResponse(response: Response){
        var handler = Handler(Looper.getMainLooper())
        handler.post{
            val parser = Parser()
            val stringBuilder = StringBuilder(response.body()?.string())
            var jsonObject: JsonObject = JsonObject()
            var jsonParseable = false
            try{
                jsonObject = parser.parse(stringBuilder) as JsonObject
                jsonParseable = true
            }catch (e: KlaxonException){
                Toast.makeText(context, "Es besteht ein Problem mit der Wetterdatenschnittstelle", Toast.LENGTH_LONG).show()
            }
            if(response.code() != 200 && jsonParseable){
                var message = jsonObject.get("message") as String
                Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
            }
            else if (jsonParseable){
                var convertedList:ArrayList<Weather>? = null
                try{
                    convertedList = Weather.createWeatherListItem(jsonObject)
                }catch(e:Exception){
                    Toast.makeText(context, "Es ist ein Fehler beim Abrufen der Daten aufgetreten", Toast.LENGTH_LONG).show()
                }
                if(convertedList != null) {
                    val adapter = WeatherAdapter(context, convertedList)
                    listView.adapter = adapter
                    swipeLayout.isRefreshing = false
                    listView.setOnScrollListener(ScrollAdapter(swipeRefreshLayout,listView))
                    listView.setOnItemClickListener { _, _, position, _ ->
                        val selectedWeather = convertedList[position]
                        openWeather(selectedWeather)

                    }
                }
            }
        }

    }

}

