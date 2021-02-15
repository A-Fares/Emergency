package com.afares.emergency.bindingadapters

import androidx.databinding.BindingAdapter
import coil.load
import com.afares.emergency.R
import de.hdodenhof.circleimageview.CircleImageView

class UserBinding {

    companion object{

        @BindingAdapter("loadImageFromUrl")
        @JvmStatic
        fun loadImageFromUrl(imageView: CircleImageView?, imageUrl: String?) {
            imageView?.load(imageUrl) {
                crossfade(600)
                error(R.drawable.ic_user_placeholder)
            }
        }

    }

}