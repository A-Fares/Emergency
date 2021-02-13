package com.afares.emergency.fragments.splash

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.viewmodels.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : Fragment() {

    private lateinit var userType: String
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_splash, container, false)

        delaySplashScreen()

        return view
    }

    private fun delaySplashScreen() {

        val timer = object : CountDownTimer(1500, 100) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                checkAuthentication(mAuth.currentUser)
            }
        }
        timer.start()
    }

    private fun checkAuthentication(user: FirebaseUser?) {
        if (user == null) {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        } else {
            checkUserType()
        }
    }

    private fun checkUserType() {
        viewModel.fetchUserType().observe(viewLifecycleOwner, { type ->
            if (type != null) {
                if (type.equals("مستخدم")) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeActivity)
                    activity?.finish();
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_saviorActivity)
                    activity?.finish();
                }
            }
        })
    }
}