package com.matthias.mosy.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import com.matthias.mosy.MainActivity
import com.matthias.mosy.Prefs
import com.matthias.mosy.R
import com.matthias.mosy.adapter.CustomListener
import com.matthias.mosy.bluetooth.BluetoothLeService
import kotlinx.android.synthetic.main.fragment_presets.*


class PresetsFragment : Fragment(), CustomListener {


    private lateinit var gridLayout: GridLayout
    private lateinit var bluetoothService  : BluetoothLeService
    private lateinit var prefs : Prefs

    private var lastClickedPreset: Int? = null

    companion object {
        const val NAME = "Presets"
        val LABEL_WEATHER = hashMapOf(
                0 to "Klarer Himmel",
                1 to "Ein paar Wolken",
                2 to "Wolkig",
                3 to "Überwiegend bewölkt",
                4 to "Leichtes Nieseln",
                5 to "Mäßiger Regen",
                6 to "Gewitter",
                7 to "Schneefall",
                8 to "Nebel"
        )

        val LABEL_WEATHER_REVERSE = hashMapOf(
                "Klarer Himmel" to 0,
                "Ein paar Wolken" to 1,
                "Wolkig" to 2,
                "Überwiegend bewölkt" to 3,
                "Leichtes Nieseln" to 4,
                "Mäßiger Regen" to 5,
                "Gewitter" to 6,
                "Schneefall" to 7,
                "Nebel" to 8
        )

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_presets, container, false)
    }

    override fun notifyBlueState(value: Boolean) {
        changeBtButtonsState(value)
    }

    fun changeBtButtonsState(value:Boolean){
        //hijer einseitzen und im looper thread starten
        println("bluetooth ist nun ${value}")
        var handler = Handler(Looper.getMainLooper())
        handler.post {
            activate_presets_btn?.isEnabled = value
            var colorId = if(value) resources.getColor(R.color.btConnected) else resources.getColor(R.color.btNotConnected)
            bluetoothStateBtn.backgroundTintList = ColorStateList.valueOf(colorId)
        }

    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        gridLayout = view!!.findViewById<GridLayout>(R.id.gridLayout)
        prefs = MainActivity.prefs!!
        prefs.addListener(this)
        bluetoothService = MainActivity.bluetoothLeService
        initGridItems()
        activate_presets_btn.setOnClickListener { myView ->

            if(lastClickedPreset != null){
                //todo sichergehen, dass bluetoothservice vorhanden ist.
                bluetoothService.write(lastClickedPreset.toString())
            }
        }
    }

    fun initGridItems(){
        val count = gridLayout.childCount
        for(i in 0..(count-1)){
            var imageButton: ImageButton = gridLayout.getChildAt(i) as ImageButton
            imageButton.setOnClickListener { myView ->
                setBgAndResetOthers()
                imageButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorBtnPressed))
                activate_presets_btn.isEnabled = true
                lastClickedPreset = i
                if(lastClickedPreset != null){
                    presetsDescription.visibility = View.VISIBLE
                    var message = LABEL_WEATHER[lastClickedPreset!!]
                    presetsDescription.text = message
                }
            }
        }
    }


    fun setBgAndResetOthers(){
        if(lastClickedPreset != null){
            var imageButton: ImageButton = gridLayout.getChildAt(lastClickedPreset!!) as ImageButton
            imageButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorBtnNotPressed))
        }
    }
}
