package com.afares.emergency.ui.fragments.usertype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afares.emergency.databinding.FragmentUserTypeBinding
import com.afares.emergency.ui.fragments.signup.SignUpFragmentArgs
import com.afares.emergency.util.Constants.CIVIL_DEFENSE
import com.afares.emergency.util.Constants.PARAMEDIC
import com.afares.emergency.util.Constants.USER


class UserTypeFragment : Fragment() {

    private val args by navArgs<UserTypeFragmentArgs>()
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
                    UserTypeFragmentDirections.actionUserTypeFragmentToSignUpFragment(
                        USER,
                        args.userPhone
                    )
                findNavController().navigate(action)
            }
            paramedicBtn.setOnClickListener {
                val action =
                    UserTypeFragmentDirections.actionUserTypeFragmentToSignUpFragment(
                        PARAMEDIC,
                        args.userPhone
                    )
                findNavController().navigate(action)
            }
            firefighterBtn.setOnClickListener {
                val action =
                    UserTypeFragmentDirections.actionUserTypeFragmentToSignUpFragment(
                        CIVIL_DEFENSE,
                        args.userPhone
                    )
                findNavController().navigate(action)
            }
        }

        return binding.root
    }


}