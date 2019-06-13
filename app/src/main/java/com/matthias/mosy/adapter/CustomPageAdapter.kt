package com.matthias.mosy.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import com.matthias.mosy.fragments.PresetsFragment
import com.matthias.mosy.fragments.WeaterFragment


class CustomPageAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager)
{
    override fun getItem(position: Int): Fragment {
        when(position){
            0-> {return WeaterFragment()}
            else-> {return PresetsFragment()}
        }
    }


    override fun getPageTitle(position: Int): CharSequence {
        when(position){
            0-> {return WeaterFragment.NAME}else-> {return PresetsFragment.NAME}

        }
    }


    override fun getCount(): Int {
        return 2
    }

}