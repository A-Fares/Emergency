package com.afares.emergency.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import coil.load
import com.afares.emergency.R
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
            binding.apply {
                userName.text = user.data?.name
                userPhone.text = user.data?.phone
                closePersonPhone.text = user.data?.closePersonPhone

                Glide.with(requireActivity()).load(user.data?.photoUrl).into(userImageView)

            }
        })

        return binding.root
    }


}