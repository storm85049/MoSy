package com.matthias.mosy

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import com.matthias.mosy.adapter.CustomPageAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

companion object {
    var prefs : Prefs?  = null
}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    prefs = Prefs(applicationContext)

    val fragmentAdapter = CustomPageAdapter(supportFragmentManager)
    viewpager_main.adapter = fragmentAdapter
    tabs_main.setupWithViewPager(viewpager_main)

  }
}
