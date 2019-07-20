package com.matthias.mosy

import android.content.Context
import android.content.SharedPreferences
import com.matthias.mosy.adapter.CustomListener

/**
 * Eine Klasse, die die Preferences des Benutzer hält, also gespeicherte Orte etc.
 */
class Prefs(context: Context){


    val PREFERENCES = "com.matthias.mosy.prefs"
    val SAVED_CITIES = "saved_cities"
    val prefs : SharedPreferences = context.getSharedPreferences(PREFERENCES,0)
    var BT_ENABLED: Boolean = false

    private var listeners = arrayListOf<CustomListener>()

    fun setBluetoothEnabled(value: Boolean){
        BT_ENABLED = value
        notifyBtState(BT_ENABLED)
    }
    lateinit var saved_cities: ArrayList<Int>


    fun notifyBtState(value: Boolean){
        for(listener: CustomListener in listeners){
            listener.notifyBlueState(value)
        }
    }

    fun addListener(listener:CustomListener){
        listeners.add(listener)
        notifyBtState(BT_ENABLED)
    }
    fun getSavedCities():ArrayList<Int>
    {
        var raw  = prefs.getString(SAVED_CITIES,"")
        if(raw != ""){
            saved_cities = ArrayList(raw.split("|").map { it.toInt() })
            return saved_cities
        }
        saved_cities = ArrayList<Int>()
        return saved_cities
    }

    fun setSavedCities(list: ArrayList<Int>) {
        var stringToCommit = list.joinToString("|")
        prefs.edit().putString(SAVED_CITIES,stringToCommit).apply()
    }


    fun addCity(id:Int)
    {
        if (!(saved_cities.contains(id))) {
            saved_cities.add(id)
            setSavedCities(saved_cities)
        }
    }

    fun deleteCityById(id: Int){
        if(saved_cities.contains(id)){
            saved_cities.remove(id)
            setSavedCities(saved_cities)
        }

    }
}