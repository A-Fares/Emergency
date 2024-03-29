package com.afares.emergency.ui.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentLoginBinding
import com.afares.emergency.util.Constants.USER
import com.afares.emergency.util.hideKeyboard
import com.afares.emergency.util.showSnackBar
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var verificationId: String? = null
    private val keyVerifyInProgress = "key_verify_in_progress"
    private var mVerificationInProgress = false
    private var phone = ""

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)


        binding.loginBtn.setOnClickListener {
            binding.apply {
                if (phoneEt.text.length != 9
                ) {
                    phoneEt.error =
                        "برجاء ادخال رقم صحيح"
                    phoneEt.requestFocus()
                    return@setOnClickListener
                }
                loginContainer.visibility = View.GONE
                otpContainer.visibility = View.VISIBLE
                phone = "+966${phoneEt.text}"
                sendVerificationCode(phone)
            }
            hideKeyboard()
        }

        binding.buttonVerify.setOnClickListener {
            binding.apply {
                val code = otpEt.text.toString()
                if (code.length != 6) {
                    otpEt.error = "Enter a valid Code!"
                    otpEt.requestFocus()
                    return@setOnClickListener
                }
                verificationId?.let {
                    val credential = PhoneAuthProvider.getCredential(it, code)
                    authViewModel.signInWithPhoneAuthCredential(credential)
                }
            }
            hideKeyboard()
        }
        signIn()
        return binding.root
    }

    private fun signIn() {
        lifecycleScope.launchWhenStarted {
            authViewModel.userState.collect {
                when (it) {
                    is NetworkResult.Success -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        when (it.data?.type) {
                            USER -> {
                                findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                                activity?.finish()
                            }
                            else -> {
                                findNavController().navigate(R.id.action_loginFragment_to_saviorActivity)
                                activity?.finish()
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        val action =
                            LoginFragmentDirections.actionLoginFragmentToUserTypeFragment(phone)
                        findNavController().navigate(action)
                    }
                    is NetworkResult.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is NetworkResult.Empty -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            authViewModel.signInWithPhoneAuthCredential(phoneAuthCredential)
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            toast(requireContext(), exception.message!!)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            this@LoginFragment.verificationId = verificationId
        }
    }


    private fun sendVerificationCode(phone: String) {
        mAuth.setLanguageCode("ar")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone.trim(),
            30,
            TimeUnit.SECONDS,
            requireActivity(),
            callbacks
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(keyVerifyInProgress, mVerificationInProgress)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}