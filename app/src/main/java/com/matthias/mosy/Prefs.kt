package com.matthias.mosy

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context){

    //cities saved with their ids like this 1092|10239480|012987



    val PREFERENCES = "com.matthias.mosy.prefs"
    val SAVED_CITIES = "saved_cities"
    val prefs : SharedPreferences = context.getSharedPreferences(PREFERENCES,0)

    lateinit var saved_cities: ArrayList<Int>

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

    fun deleteCities(){
        setSavedCities(ArrayList<Int>())
    }

    fun addCity(id:Int)
    {
        if (!(saved_cities.contains(id))) {
            saved_cities.add(id)
            setSavedCities(saved_cities)
        }
    }
}