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
import com.beust.klaxon.Parser
import com.matthias.mosy.MainActivity
import com.matthias.mosy.R
import com.matthias.mosy.adapter.CustomObserver
import com.matthias.mosy.adapter.ScrollAdapter
import com.matthias.mosy.dialog.MESSAGE_ADD_CITY
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
import java.lang.StringBuilder
import java.net.UnknownHostException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class WeaterFragment : android.support.v4.app.Fragment(), CustomObserver {

    /***updateList*/
    override fun apply() {
        initList(view!!)
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
        this.initList(view!!)

        fab.setOnClickListener { myView ->
            var searchWeatherDialog = SearchWeatherDialog(context, this)
            searchWeatherDialog.showDialog()
        }

        swipeLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeLayout.setOnRefreshListener {
            var mHandler = Handler()
            var mRunnable = Runnable{
                initList(view)
            }
            mHandler.post(mRunnable)
        }

    }

    fun initList(view: View)
    {
        var savedCities = MainActivity.prefs?.getSavedCities()
        var call : Call?  = WeatherDataClient.getData(context,listOfIds = savedCities!!)

        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException){
                    //Toast.makeText(parentFragment.context, "Ist das WLAN auf dem Handy aktiviert?", Toast.LENGTH_LONG).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                var handler = Handler(Looper.getMainLooper())
                handler.post{
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
        })
    }

    fun openWeather(weather: Weather){
        val intent = WeatherDetailActivity.newIntent(context,weather)
        startActivity(intent)
    }
}

