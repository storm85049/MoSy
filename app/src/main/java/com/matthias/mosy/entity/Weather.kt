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
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import java.lang.ClassCastException
import kotlin.math.roundToInt


class Weather(

        val city: String,
        val cityId: Int,
        val description: String,
        val humidity: Int,
        val temperature: Double,
        val iconID: String) {

    var sunriseUnix:Int? = null
    var sunsetUnix:Int? = null
    var pressure:Int? = null
    var windspeed:Double? = null
    var windDegree:Int? = null
    var temp_min: Int? = null
    var temp_max : Int?= null



    companion object {
        @Throws(Exception::class)
      fun createWeatherListItem(jsonObject: JsonObject, moreInfo: Boolean = false):ArrayList<Weather>?  {
          var list = ArrayList<Weather>()

          var multiple = jsonObject.get("cnt")
          if(multiple != null){
              var jsonList :JsonArray<JsonObject> = jsonObject.get("list") as JsonArray<JsonObject>
              for (jsonObject in jsonList){
                  var weatherObj = extractDataFromJson(jsonObject,moreInfo)
                  list.add(weatherObj)
              }
          }else{
              list.add(extractDataFromJson(jsonObject,moreInfo))
          }
          return list
      }


      fun extractDataFromJson(jsonObject: JsonObject, moreInfo: Boolean):Weather {
          val city:String = jsonObject.get("name").toString()
          val cityId :Int = jsonObject.get("id") as Int
          val weatherDetails: JsonArray<JsonObject> = jsonObject.get("weather") as JsonArray<JsonObject>
          val weatherDetailsObject: JsonObject = weatherDetails.get(0)
          val description: String = weatherDetailsObject.get("description").toString()

          val weatherSysObject = jsonObject.get("sys") as JsonObject
          val windObject = jsonObject.get("wind") as JsonObject

          val weatherData = jsonObject.get("main") as JsonObject
          var temperature:Double
          try{
              temperature = weatherData.get("temp") as Double
          }catch (e: ClassCastException){
              var temperatureInt: Int= weatherData.get("temp") as Int
              temperature = temperatureInt.toDouble()
          }
          val humidity : Int= weatherData.get("humidity") as Int
          val iconId:  String = weatherDetailsObject.get("icon") as String
          if(moreInfo){
              var weather = Weather(city,cityId,description,humidity,temperature,iconId)

              weather.sunriseUnix = weatherSysObject?.get("sunrise") as Int?
              weather.sunsetUnix =  weatherSysObject?.get("sunset") as Int?

              try{
                  var doubledValue = windObject?.get("deg") as Int?
                  weather.windDegree= doubledValue
              }catch(e:ClassCastException){
                  var doubleValue=  windObject?.get("speed") as Double?
                  weather.windDegree= doubleValue?.toInt()
              }

              //todo manchmal kommt das hier auch als "falscher Datentyp an, das muss unbedingt behoben werden"


              try{
                  var doubledValue = weatherData?.get("pressure") as Int?
                  weather.pressure= doubledValue
              }catch(e:ClassCastException){
                  var doubleValue=  weatherData?.get("pressure") as Double?
                  weather.pressure = doubleValue?.toInt()
              }
              try{
                  var doubledValue = windObject?.get("speed") as Double?
                  weather.windspeed= doubledValue
              }catch(e:ClassCastException){
                  var intValue =  windObject?.get("speed") as Int?
                  weather.windspeed = intValue?.toDouble()
              }


              //gibt es dafür eine andere Lösung ? wenn der wert als zb 22 wiederkommmt, wird er als Int interpretiert, ansonsten hat es initial Double as Datentypen...
              try{
                  var doubledValue = weatherData.get("temp_min") as Double?
                  weather.temp_min = doubledValue?.toInt()
              }catch(e:ClassCastException){
                  weather.temp_min = weatherData.get("temp_min") as Int?
              }

              try{
                  var doubledValue = weatherData.get("temp_max") as Double?
                  weather.temp_max= doubledValue?.toInt()
              }catch(e:ClassCastException){
                  weather.temp_max = weatherData.get("temp_max") as Int?
              }

              return weather
          }

          return Weather(city,cityId,description,humidity,temperature,iconId)
      }
  }
}
