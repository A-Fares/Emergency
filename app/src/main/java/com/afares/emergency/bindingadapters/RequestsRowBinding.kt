package com.afares.emergency.bindingadapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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

        @BindingAdapter("getRequestStatusBackground")
        @JvmStatic
        fun getRequestStatusBackground(view: View, status: String) {
            when (status) {
                "تم الطلب" ->
                    when (view) {
                        is ImageView -> {
                            view.setImageResource(R.drawable.ic_done)
                        }
                        is TextView -> {
                            view.text = "تم الطلب"
                        }
                        is ConstraintLayout -> {
                            view.setBackgroundColor(
                                ContextCompat.getColor(
                                    view.context,
                                    R.color.green
                                )
                            )
                        }
                    }
                "تم الاستلام" ->
                    when (view) {
                        is ImageView -> {
                            view.setImageResource(R.drawable.ic_loading)
                        }
                        is TextView -> {
                            view.text = "العملية قيد التنفيذ"
                        }
                        is ConstraintLayout -> {
                            view.setBackgroundColor(
                                ContextCompat.getColor(
                                    view.context,
                                    R.color.yellow
                                )
                            )
                        }
                    }
                "تم الانتهاء" ->
                    when (view) {
                        is ImageView -> {
                            view.setImageResource(R.drawable.ic_finish)
                        }
                        is TextView -> {
                            view.text = "تمت العملية بنجاح"
                        }
                        is ConstraintLayout -> {
                            view.setBackgroundColor(
                                ContextCompat.getColor(
                                    view.context,
                                    R.color.red_200
                                )
                            )
                        }
                    }
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
        fun simpleDateFormat(textView: TextView, createdDate: Date?) {
            val spf = SimpleDateFormat("MMM dd, yyyy")
            val date: String = spf.format(createdDate!!)
            textView.text = date
        }

    }
}