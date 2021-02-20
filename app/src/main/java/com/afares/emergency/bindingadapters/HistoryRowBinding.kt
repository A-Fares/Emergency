package com.afares.emergency.bindingadapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import com.afares.emergency.R

class HistoryRowBinding {


    companion object {

        @BindingAdapter("loadImageType")
        @JvmStatic
        fun loadImageType(imageView: ImageView, type: String) {
            when (type) {
                "دفاع مدني" -> imageView.setImageResource(R.drawable.ic_firefighter_truck)
                "اسعاف" -> imageView.setImageResource(R.drawable.ic_ambulance)
            }
        }

    }
}