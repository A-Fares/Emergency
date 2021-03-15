package com.afares.emergency.bindingadapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.afares.emergency.R
import com.afares.emergency.util.Constants.FINISHED
import com.afares.emergency.util.Constants.LOADING
import com.afares.emergency.util.Constants.REQUESTED
import java.text.SimpleDateFormat
import java.util.*

class RequestsRowBinding {

    companion object {

        @BindingAdapter("getRequestStatusBackground")
        @JvmStatic
        fun getRequestStatusBackground(view: View, status: String) {
            when (status) {
                REQUESTED ->
                    when (view) {
                        is ImageView -> {
                            view.setImageResource(R.drawable.ic_done)
                        }
                        is TextView -> {
                            view.text = REQUESTED
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
                LOADING ->
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
                FINISHED ->
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