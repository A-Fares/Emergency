package com.afares.emergency.fragments.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.data.Status
import com.afares.emergency.databinding.FragmentLoginBinding
import com.afares.emergency.util.Constants.RC_SIGN_IN
import com.afares.emergency.util.showSnackBar
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.signupBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.loginBtn.setOnClickListener {
            signIn()
        }

        return binding.root
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!

                viewModel.signInWithGoogle(account).observe(viewLifecycleOwner, {
                    if (it.status == Status.SUCCESS) {

                        checkUserValidation()

                    } else if (it.status == Status.ERROR) {
                        toast(requireContext(), it.message!!)
                    }
                })

            } catch (e: ApiException) {
                toast(requireContext(), e.message!!)
            }
        }
    }

    private fun checkUserValidation() {
        viewModel.fetchUserType().observe(viewLifecycleOwner, { type ->
            if (type.isNullOrEmpty()) {
                showSnackBar(authenticationLayout, "يرجى تسجيل الحساب اولا ")
                toast(requireContext(), "يرجى تسجيل الحساب اولا ")
            } else if (type.equals("مستخدم")) {
                findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                activity?.finish();
            } else {
                findNavController().navigate(R.id.action_loginFragment_to_saviorActivity)
                activity?.finish();
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}