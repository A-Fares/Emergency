package com.afares.emergency.fragments.signup

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afares.emergency.R
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.CivilDefense
import com.afares.emergency.data.model.Hospital
import com.afares.emergency.data.model.User
import com.afares.emergency.databinding.FragmentSignUpBinding
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.QueryDocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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

    private lateinit var mySpinner: Spinner
    private lateinit var adapterSpinnerHospital: ArrayAdapter<Hospital>
    private lateinit var adapterSpinnerCivilDefense: ArrayAdapter<CivilDefense>

    private var cityId: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        authViewModel.queryHospitalData()
        authViewModel.queryCivilDefenseData()

        if (args.userType == "مستخدم") {
            binding.apply {
                closePhoneTv.visibility = View.VISIBLE
                phoneClosePersonEt.visibility = View.VISIBLE
                code2Tv.visibility = View.VISIBLE
                flag2ImageView.visibility = View.VISIBLE
            }
        }

        if (args.userType == "اسعاف") {
            binding.apply {
                hospitalSpinner.visibility = View.VISIBLE
                progressBarSignUp.visibility = View.VISIBLE
            }

            /** ----------------------------- Spinner --------------------------*/
            mySpinner = binding.hospitalSpinner

            authViewModel.hospitalMutableList.observe(viewLifecycleOwner, {
                binding.progressBarSignUp.visibility = View.GONE
                adapterSpinnerHospital =
                    ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, it)
                mySpinner.adapter = adapterSpinnerHospital
            })

            mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // You can define your actions as you want
                }

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    val selectedObject = mySpinner.selectedItem as Hospital
                    cityId = selectedObject.id.toString()
                }
            }
        }
        if (args.userType == "دفاع مدني") {
            binding.civilDefenseSpinner.visibility = View.VISIBLE
            binding.progressBarSignUp.visibility = View.VISIBLE

            /** ----------------------------- Spinner --------------------------*/
            mySpinner = binding.civilDefenseSpinner

            authViewModel.civilDefenseMutableList.observe(viewLifecycleOwner, {
                binding.progressBarSignUp.visibility = View.GONE
                adapterSpinnerCivilDefense =
                    ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, it)
                mySpinner.adapter = adapterSpinnerCivilDefense
            })

            mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // You can define your actions as you want
                }

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    val selectedObject = mySpinner.selectedItem as CivilDefense
                    cityId = selectedObject.id.toString()
                }
            }
        }

        binding.signupBtn.setOnClickListener {
            if (validateUser(args.userType)) {
                binding.userInfoContainer.visibility = View.GONE
                binding.verifyOtpContainer.visibility = View.VISIBLE
                val phone = "+966${binding.personalPhoneEt.text}"
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
                personalPhoneEt.text.length != 9 -> {
                    personalPhoneEt.error =
                        "برجاء ادخال رقم صحيح"
                    personalPhoneEt.requestFocus()
                    return false
                }
                userType == "مستخدم" -> {
                    if (phoneClosePersonEt.text.length != 9
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
            val personalPhone = "+566" + personalPhoneEt.text.toString()
            val closePersonPhone = "+566" + phoneClosePersonEt.text.toString()
            return User(
                null, null,
                name,
                ssn,
                personalPhone,
                closePersonPhone,
                args.userType, null, cityId
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

