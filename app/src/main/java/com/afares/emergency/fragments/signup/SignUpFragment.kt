package com.afares.emergency.fragments.signup

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afares.emergency.R
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.User
import com.afares.emergency.databinding.FragmentSignUpBinding
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val args by navArgs<SignUpFragmentArgs>()

    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private var verificationId: String? = null

    private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"


    private var mVerificationInProgress = false


    @Inject
    lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        if (args.userType != "مستخدم") {
            binding.phoneClosePersonEt.visibility = View.GONE
        }
        binding.signupBtn.setOnClickListener {
            if (validateUser(args.userType)) {
                binding.userInfoContainer.visibility = View.GONE
                binding.verifyOtpContainer.visibility = View.VISIBLE
                val phone = "+2${binding.personalPhoneEt.text}"
                sendVerificationCode(phone)
            } else {
                return@setOnClickListener
            }
        }

        binding.buttonVerifyOtp.setOnClickListener {
            binding.apply {
                val code = otpEditText.text.toString()
                if (code.length != 6) {
                    otpEditText.error = "Enter a valid Code!"
                    otpEditText.requestFocus()
                    return@setOnClickListener
                }
                verificationId?.let {
                    val credential = PhoneAuthProvider.getCredential(it, code)
                    signInWithPhoneAuthCredential(userData(), credential)
                }
            }
        }
        signUp()
        return binding.root
    }


    private fun validateUser(userType: String): Boolean {
        binding.apply {
            when {
                TextUtils.isEmpty(nameEt.text) -> {
                    nameEt.error =
                        "برجاء كتابة الاسم بالكامل"
                    nameEt.requestFocus()
                    return false
                }
                TextUtils.isEmpty(ssnEt.text) -> {
                    ssnEt.error =
                        "برجاء كتابة رقم الهوية"
                    ssnEt.requestFocus()
                    return false
                }
                personalPhoneEt.text.length != 11 -> {
                    personalPhoneEt.error =
                        "برجاء ادخال رقم صحيح"
                    personalPhoneEt.requestFocus()
                    return false
                }
                userType == "مستخدم" -> {
                    if (phoneClosePersonEt.text.length != 11
                    ) {
                        phoneClosePersonEt.error =
                            "برجاء ادخال رقم صحيح"
                        phoneClosePersonEt.requestFocus()
                        return false
                    }
                    return true
                }
                else -> return true
            }
        }
    }

    private fun signUp() {
        lifecycleScope.launchWhenStarted {
            authViewModel.userState.collect {
                when (it) {
                    is NetworkResult.Success -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        if (it.data?.type == "مستخدم") {
                            findNavController().navigate(R.id.action_signUpFragment_to_homeActivity)
                            activity?.finish()
                        } else {
                            findNavController().navigate(R.id.action_signUpFragment_to_saviorActivity)
                            activity?.finish()
                        }
                    }
                    is NetworkResult.Error -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        toast(requireContext(), it.message.toString())
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
            signInWithPhoneAuthCredential(userData(), phoneAuthCredential)
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            toast(requireContext(), exception.message!!)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            this@SignUpFragment.verificationId = verificationId
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

    fun signInWithPhoneAuthCredential(user: User, credential: PhoneAuthCredential) {
        authViewModel.signUpWithPhoneAuthCredential(user, credential)
    }

    private fun userData(): User {
        binding.apply {
            val name = nameEt.text.toString()
            val ssn = ssnEt.text.toString()
            val personalPhone = personalPhoneEt.text.toString()
            val closePersonPhone = phoneClosePersonEt.text.toString()
            return User(
                null,
                name,
                ssn,
                personalPhone,
                closePersonPhone,
                args.userType, null
            )
        }
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

