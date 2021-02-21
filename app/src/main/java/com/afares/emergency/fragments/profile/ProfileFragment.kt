package com.afares.emergency.fragments.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import coil.load
import com.afares.emergency.R
import com.afares.emergency.data.Status
import com.afares.emergency.databinding.FragmentLoginBinding
import com.afares.emergency.databinding.FragmentProfileBinding
import com.afares.emergency.viewmodels.LoginViewModel
import com.afares.emergency.viewmodels.UserViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)



        viewModel.fetchUser().observe(viewLifecycleOwner, { user ->
            if (user.status == Status.SUCCESS) {
                Log.d("HHH", user.status.toString())
                binding.apply {

                    userName.text = user.data?.name
                    userPhone.text = user.data?.phone
                    closePersonPhone.text = user.data?.closePersonPhone

                    Glide.with(requireActivity()).load(user.data?.photoUrl).into(userImageView)
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


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}