package com.matthias.mosy.adapter

interface CustomListener{
    //hat sich der bluetoothstatus geändert,
    // bekommen das alle klassen mit,
    // die dieses Interface implementieren und sich bei den Preferences als Listener registriert haben.
    fun notifyBlueState(value: Boolean)
}