package com.afares.emergency.fragments.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.afares.emergency.R
import com.afares.emergency.data.Status
import com.afares.emergency.databinding.FragmentProfileBinding
import com.afares.emergency.viewmodels.UserViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
    private var mVerificationInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)



        viewModel.fetchUser().observe(viewLifecycleOwner, { user ->
            if (user.status == Status.SUCCESS) {
                binding.apply {
                    userNameTextView.text = user.data?.name
                    userSsnTextView.text = user.data?.ssn
                    phoneTextView.text = user.data?.phone
                    closePersonPhoneTextView.text = user.data?.closePersonPhone
                    Glide.with(requireActivity())
                        .load(user.data?.photoUrl)
                        .placeholder(R.drawable.upload_photo)
                        .into(userImageView)
                    profileContainer.visibility = View.VISIBLE
                    shimmerContainer.visibility = View.GONE
                    shimmerContainer.stopShimmer()
                }
            } else {
                binding.apply {
                    profileContainer.visibility = View.INVISIBLE
                    shimmerContainer.visibility = View.VISIBLE
                }
            }
        })

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}