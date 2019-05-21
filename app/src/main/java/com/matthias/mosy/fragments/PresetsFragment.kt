package com.matthias.mosy.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
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
import com.matthias.mosy.R
import kotlinx.android.synthetic.main.fragment_presets.*


class PresetsFragment : Fragment() {

    private lateinit var gridLayout: GridLayout
    private var lastClickedPreset: Int? = null
    private val LABEL_WEATHER = hashMapOf(
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
    companion object {
        const val NAME = "Presets"

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_presets, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        gridLayout = view!!.findViewById<GridLayout>(R.id.gridLayout)
        initGridItems()
        activate_presets_btn.setOnClickListener { myView ->
            //todo activate weather via bluetooth
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
