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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var colorNotPressed = Color.argb(255,150,150,150)
private var colorPressed = Color.argb(50,50,50,50)

/**
 * A simple [Fragment] subclass.
 *
 */
class PresetsFragment : Fragment() {

    private lateinit var gridLayout: GridLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_presets, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {


        gridLayout = view!!.findViewById<GridLayout>(R.id.gridLayout)

        initGridItems()
    }


    fun initGridItems(){
        val count = gridLayout.childCount
            for(i in 0..(count-1)){
                var imageButton: ImageButton = gridLayout.getChildAt(i) as ImageButton
                imageButton.setOnClickListener { myView ->
                    setBgAndResetOthers(myView)
                    imageButton.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    imageButton.setColorFilter(colorPressed)
                }
            }

    }

    fun setBgAndResetOthers(view: View){
        val count = gridLayout.childCount
        for(i in 0..(count-1)){
            var imageButton: ImageButton = gridLayout.getChildAt(i) as ImageButton
            imageButton.setColorFilter(colorNotPressed)

        }
    }

    companion object {
        const val NAME = "Presets"
    }
}
