package com.matthias.mosy.adapter

import android.support.v4.widget.SwipeRefreshLayout
import android.widget.AbsListView
import android.widget.ListView

class ScrollAdapter(
        private var swipeRefreshLayout: SwipeRefreshLayout,
        private var listView:ListView) : AbsListView.OnScrollListener{

    override fun onScroll(p0: AbsListView?, p1: Int, p2: Int, p3: Int) {

        if(listView?.getChildAt(0) != null){
            var flag = (listView.firstVisiblePosition == 0 && listView.getChildAt(0)?.top == 0)
            swipeRefreshLayout.isEnabled = flag
        }
    }

    override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
        println("scroll state has changed")
    }
}