package com.matthias.mosy.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
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
import android.widget.ImageView
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
    private var overlay:Int = Color.argb(155,255,255,255)


    private var lastClickedPreset: Int? = null

    companion object {
        const val NAME = "Presets"
        val LABEL_WEATHER = hashMapOf(
                0 to "Klarer Himmel",
                1 to "Ein paar Wolken",
                2 to "Wolkig",
                3 to "Leichtes Nieseln",
                4 to "Wetter deaktivieren",
                5 to "Mäßiger Regen",
                6 to "Gewitter",
                7 to "Schneefall",
                8 to "Nebel"
        )

        val LABEL_WEATHER_REVERSE = hashMapOf(
                "Klarer Himmel" to 0,
                "Ein paar Wolken" to 1,
                "Wolkig" to 2,
                "Leichtes Nieseln" to 3,
                "Wetter deaktivieren" to 4,
                "Mäßiger Regen" to 5,
                "Gewitter" to 6,
                "Schneefall" to 7,
                "Nebel" to 8
        )

        val SONGS = hashMapOf(
                0 to R.raw.sonne_grillen,
                1 to R.raw.sonne_birds,
                2 to R.raw.windy,
                3 to R.raw.little_rain,
                5 to R.raw.rain,
                6 to R.raw.gewitter

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
        var handler = Handler(Looper.getMainLooper())
        handler.post {
            if(lastClickedPreset != null){
                activate_presets_btn2?.isEnabled = value
            }
            if(value){
                btInfo2.clearColorFilter()
            }else{
                btInfo2.setColorFilter(overlay)
            }
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        gridLayout = view!!.findViewById<GridLayout>(R.id.gridLayout)
        prefs = MainActivity.prefs!!
        prefs.addListener(this)
        bluetoothService = MainActivity.bluetoothLeService
        btInfo2.setColorFilter(overlay)
        initGridItems()

        /**
         * todo: darf auch nur aktiv sein wenn bt vorhanden ist
         */
        activate_presets_btn2.setOnClickListener { myView ->
            if(lastClickedPreset != null){





                bluetoothService.write(lastClickedPreset.toString())
            }
        }

        btInfo2.setOnClickListener{ myView ->
            if(prefs!!.BT_ENABLED){
                Toast.makeText(context,"Bereits mit Sydney verbunden", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,"Versuche mit Sydney zu verbinden", Toast.LENGTH_SHORT).show()
                bluetoothService.connect(MainActivity.HM10_ADDRESS)
            }
        }
    }

    fun initGridItems(){
        for(i in 0 until (gridLayout.childCount)){
            var presetSvg: ImageView = gridLayout.getChildAt(i) as ImageView
            presetSvg.setColorFilter(overlay)
            presetSvg.setOnClickListener { myView ->
                presetSvg.clearColorFilter()
                setBgAndResetOthers()
                if(prefs!!.BT_ENABLED){
                    activate_presets_btn2.isEnabled = true
                }
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
            var presetsSvg : ImageView = gridLayout.getChildAt(lastClickedPreset!!) as ImageView
            presetsSvg.setColorFilter(overlay)
        }
    }
}
