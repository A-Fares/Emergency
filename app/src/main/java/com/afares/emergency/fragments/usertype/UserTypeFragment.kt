package com.afares.emergency.fragments.usertype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afares.emergency.databinding.FragmentUserTypeBinding


class UserTypeFragment : Fragment() {


    private var _binding: FragmentUserTypeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserTypeBinding.inflate(inflater, container, false)

        binding.apply {
            userBtn.setOnClickListener {
                val action =
                    UserTypeFragmentDirections.actionUserTypeFragmentToSignUpFragment("مستخدم")
                findNavController().navigate(action)
            }
            paramedicBtn.setOnClickListener {
                val action =
                    UserTypeFragmentDirections.actionUserTypeFragmentToSignUpFragment("اسعاف")
                findNavController().navigate(action)
            }
            firefighterBtn.setOnClickListener {
                val action =
                    UserTypeFragmentDirections.actionUserTypeFragmentToSignUpFragment("دفاع مدني")
                findNavController().navigate(action)
            }
        }

        return binding.root
    }


}