package com.afares.emergency.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.databinding.FragmentLoginBinding
import com.afares.emergency.util.showSnackBar
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var verificationId: String? = null
    private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
    private var mVerificationInProgress = false

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
            findNavController().navigate(R.id.action_loginFragment_to_userTypeFragment)
        }

        binding.loginBtn.setOnClickListener {
            binding.apply {
                if (phoneEt.text.length != 11
                ) {
                    phoneEt.error =
                        "برجاء ادخال رقم صحيح"
                    phoneEt.requestFocus()
                    return@setOnClickListener
                }
                loginContainer.visibility = View.GONE
                otpContainer.visibility = View.VISIBLE
                val phone = "+2${phoneEt.text}"
                sendVerificationCode(phone)
            }
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
                    signInWithPhoneAuthCredential(credential)
                }
            }
        }

        return binding.root
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential)
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

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkUserValidation()
            } else {
                // Show Error
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    toast(requireContext(), task.exception?.message ?: "Verification Failed")
                } else {
                    toast(requireContext(), "Verification Failed")
                }
            }
        }
    }

    private fun checkUserValidation() {
        authViewModel.fetchUserType().observe(viewLifecycleOwner, { type ->
            when {
                type.isNullOrEmpty() -> {
                    showSnackBar(binding.loginFragment, "يرجى تسجيل الحساب اولا ")
                    toast(requireContext(), "يرجى تسجيل الحساب اولا ")
                }
                type == "مستخدم" -> {
                    findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                    activity?.finish()
                }
                else -> {
                    val action = LoginFragmentDirections.actionLoginFragmentToSaviorActivity(type)
                    findNavController().navigate(action)
                    activity?.finish()
                }
            }
        })
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