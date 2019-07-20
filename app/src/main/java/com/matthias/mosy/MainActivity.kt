package com.matthias.mosy

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.matthias.mosy.adapter.CustomPageAdapter
import com.matthias.mosy.bluetooth.BluetoothLeService
import com.matthias.mosy.entity.MediaplayerWrapper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  /**
   * Kleiner "Hack"-> die folgenden Objekte sind so in der gesamten Applikation als Singletons verfügbar.
   * Macht man das in der App Entwicklung mit Kotlin so? Es funktioniert aber super
   */
  companion object {
    var prefs : Prefs?  = null
    lateinit var bluetoothLeService: BluetoothLeService
    lateinit var mediaPlayer: MediaplayerWrapper
    val HM10_ADDRESS = "34:03:DE:37:C1:C5"

}

  private lateinit var bluetoothAdapter: BluetoothAdapter
  private val REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1
  private val REQUEST_ENABLE_BT = 1
  private lateinit var bluetoothManager: BluetoothManager

  /**
   * request persmissions, initialize important classes...
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    prefs = Prefs(applicationContext)
    mediaPlayer = MediaplayerWrapper(applicationContext)

    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_ACCESS_FINE_LOCATION)
    setupBluetoothConnection()

    val fragmentAdapter = CustomPageAdapter(supportFragmentManager)
    viewpager_main.adapter = fragmentAdapter
    tabs_main.setupWithViewPager(viewpager_main)
    tabs_main.setSelectedTabIndicatorColor(resources.getColor(R.color.colorPrimary))

  }

  private fun setupBluetoothConnection(){
    bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    bluetoothAdapter = bluetoothManager.adapter

    if (!bluetoothAdapter!!.isEnabled) {
      val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }
    bluetoothLeService = BluetoothLeService(bluetoothManager)
    if(bluetoothLeService.initialize()){
      bluetoothLeService.connect(HM10_ADDRESS)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
      finish()
      return
    }
    bluetoothLeService.connect(HM10_ADDRESS)
    super.onActivityResult(requestCode, resultCode, data)
  }
}



