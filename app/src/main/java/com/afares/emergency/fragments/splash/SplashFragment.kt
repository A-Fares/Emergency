package com.afares.emergency.fragments.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_splash, container, false)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthentication()
        }, 1800)

        return view
    }

    private fun checkAuthentication() {
        lifecycleScope.launchWhenStarted {
            authViewModel.readLoginStatus.observe(viewLifecycleOwner, { value ->
                val isLogin = value.isLogin
                val userType = value.userType
                if (isLogin) {
                    when (userType) {
                        "مستخدم" -> {
                            findNavController().navigate(R.id.action_splashFragment_to_homeActivity)
                            activity?.finish();
                        }
                        else -> {
                            findNavController().navigate(R.id.action_splashFragment_to_saviorActivity)
                            activity?.finish();
                        }
                    }
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
            })
        }
    }
}