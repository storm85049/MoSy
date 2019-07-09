
package com.matthias.mosy.entity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.matthias.mosy.MainActivity
import com.matthias.mosy.R


class WeatherAdapter(private val context: Context,
                     private val dataSource: ArrayList<Weather>) : BaseAdapter() {

  private val inflater: LayoutInflater
      = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


  override fun getCount(): Int {
    return dataSource.size
  }

  override fun getItem(position: Int): Any {
    return dataSource[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view: View
    val holder: ViewHolder

    if (convertView == null) {

      view = inflater.inflate(R.layout.list_item_weather, parent, false)
      holder = ViewHolder()
      holder.cityTextView = view.findViewById(R.id.weather_list_city) as TextView
      holder.descriptionTextView= view.findViewById(R.id.weather_list_description) as TextView
      holder.humidityTextView = view.findViewById(R.id.weather_list_humidity) as TextView
      holder.temperatureTextView = view.findViewById(R.id.weather_list_temperature) as TextView
      holder.weatherIcon = view.findViewById(R.id.weather_list_icon) as ImageView
      holder.activeLabel = view.findViewById(R.id.activeLabel) as TextView
      view.tag = holder
    } else {
      view = convertView
      holder = convertView.tag as ViewHolder
    }

    val cityTextView = holder.cityTextView
    val descriptionTextView = holder.descriptionTextView
    val temperatureTextView = holder.temperatureTextView
    val humidityTextView = holder.humidityTextView
    val weatherIcon = holder.weatherIcon
    val weather = getItem(position) as Weather
    var label = holder.activeLabel
    label.text = ""

    if(MainActivity.prefs?.activeCityID == weather.cityId ){
      label.text = "Aktiv"
    }

    cityTextView.text = weather.city
    descriptionTextView.text = weather.description
    temperatureTextView.text = weather.temperature.toString() + "°"
    humidityTextView.text = weather.humidity.toString() + "%"
    var iconPath = "ic_${weather.iconID}"
    var idPath = context.resources.getIdentifier(iconPath,"drawable", context.packageName)

    weatherIcon.setImageResource(idPath)

    return view
  }

  private class ViewHolder {
    lateinit var cityTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var temperatureTextView: TextView
    lateinit var humidityTextView: TextView
    lateinit var weatherIcon: ImageView
    lateinit var activeLabel: TextView
  }
}
