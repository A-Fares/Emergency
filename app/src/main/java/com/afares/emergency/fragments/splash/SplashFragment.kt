package com.afares.emergency.fragments.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : Fragment() {


    @Inject
    lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_splash, container, false)

        /**If user is not authenticated, send him to SignInActivity to authenticate first.
         * Else send him to DashboardActivity*/
        GlobalScope.launch(Dispatchers.IO) {
            checkAuthentication(mAuth.currentUser)
        }

        return view
    }

    private suspend fun checkAuthentication(user: FirebaseUser?) {
        delay(1500L)
        if (user != null) {
            findNavController().navigate(R.id.action_splashFragment_to_homeActivity)
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }
}