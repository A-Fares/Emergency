package com.afares.emergency.bindingadapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.afares.emergency.R
import com.afares.emergency.data.model.Request
import com.afares.emergency.fragments.requests.RequestsFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class RequestsRowBinding {

    companion object {

        @BindingAdapter("loadImageType")
        @JvmStatic
        fun loadImageType(imageView: ImageView, type: String) {
            when (type) {
                "دفاع مدني" -> imageView.setImageResource(R.drawable.ic_firefighter_truck)
                "اسعاف" -> imageView.setImageResource(R.drawable.ic_ambulance)
            }
        }


        @BindingAdapter("onRequestClickListener")
        @JvmStatic
        fun onRequestClickListener(requestRowLayout: ConstraintLayout, currentItem: Request) {
            requestRowLayout.setOnClickListener {
                try {
                    val action =
                        RequestsFragmentDirections.actionRequestsFragmentToRequestDetailesFragment(
                            currentItem
                        )
                    requestRowLayout.findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.d("onRecipeClickListener", e.toString())
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        @BindingAdapter("simpleDateFormat")
        @JvmStatic
        fun simpleDateFormat(textView: TextView, createdDate: Date) {
            val spf = SimpleDateFormat("MMM dd, yyyy")
            val date: String = spf.format(createdDate)
            textView.text = date
        }

    }
}