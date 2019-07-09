package com.matthias.mosy.entity

import android.content.Context
import android.media.MediaPlayer
import android.support.annotation.RawRes
import com.matthias.mosy.fragments.PresetsFragment

class MediaplayerWrapper(context: Context){

    val mediaPlayer:MediaPlayer = MediaPlayer()
    var context:Context = context

    fun playSound(@RawRes resId:Int){
        val assetFileDescriptor = context.resources.openRawResourceFd(resId) ?: return
        mediaPlayer.run {
            reset()
            setDataSource(assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.declaredLength)
            prepare()
            start()
        }
    }
    fun stopPlayer(){
        mediaPlayer.run {
            reset()
        }
    }

    fun handleMusic(state:Int) {
        val source: Int? = PresetsFragment.SONGS.get(state)
        if (source != null) {
            playSound(source)
        } else {
            stopPlayer()
        }
    }


}