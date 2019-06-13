/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.matthias.mosy.entity

import android.content.Context
import android.media.Image
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.matthias.mosy.R
import com.squareup.picasso.Picasso


class WeatherAdapter(private val context: Context,
                     private val dataSource: ArrayList<Weather>) : BaseAdapter() {

  private val inflater: LayoutInflater
      = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

  companion object {
    private val LABEL_COLORS = hashMapOf(
        "Low-Carb" to R.color.colorLowCarb,
        "Low-Fat" to R.color.colorLowFat,
        "Low-Sodium" to R.color.colorLowSodium,
        "Medium-Carb" to R.color.colorMediumCarb,
        "Vegetarian" to R.color.colorVegetarian,
        "Balanced" to R.color.colorBalanced
    )
  }

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

    cityTextView.text = weather.city
    descriptionTextView.text = weather.description
    temperatureTextView.text = weather.temperature.toString() + "°"
    humidityTextView.text = weather.humidity.toString() + "%"
    var iconPath = "ic_${weather.iconID}"
    var idPath = context.resources.getIdentifier(iconPath,"drawable", context.packageName)
    weatherIcon.setImageResource(idPath)


      //todo: icon aus dem weather.iconId lokal rausziehen, aint no need for http
    //Picasso.with(context).load(recipe.imageUrl).placeholder(R.mipmap.ic_launcher).into(thumbnailImageView)
/*

    val titleTypeFace = ResourcesCompat.getFont(context, R.font.josefinsans_bold)
    titleTextView.typeface = titleTypeFace

    val subtitleTypeFace = ResourcesCompat.getFont(context, R.font.josefinsans_semibolditalic)
    subtitleTextView.typeface = subtitleTypeFace

    val detailTypeFace = ResourcesCompat.getFont(context, R.font.quicksand_bold)
    detailTextView.typeface = detailTypeFace

    detailTextView.setTextColor(
        ContextCompat.getColor(context, LABEL_COLORS[recipe.label] ?: R.color.colorPrimary))
*/

    return view
  }

  private class ViewHolder {
    lateinit var cityTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var temperatureTextView: TextView
    lateinit var humidityTextView: TextView
    lateinit var weatherIcon: ImageView
  }
}
