package com.afares.emergency.fragments.signup

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afares.emergency.R
import com.afares.emergency.data.model.Savior
import com.afares.emergency.databinding.FragmentSignUpBinding
import com.afares.emergency.viewmodels.SignUpViewModel
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val args by navArgs<SignUpFragmentArgs>()

    private val signupViewModel: SignUpViewModel by viewModels()
    private var _binding: FragmentSignUpBinding? = null
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
                    signInWithPhoneAuthCredential(credential)
                }
            }
        }

        /*  binding.userBtn.setOnClickListener {
              binding.userInfoContainer.visibility = View.VISIBLE
              binding.userTypeContainer.visibility = View.GONE
          }

          binding.firefighterBtn.setOnClickListener {
              userType = "دفاع مدني"
              signIn()
          }
          binding.paramedicBtn.setOnClickListener {
              userType = "اسعاف"
              signIn()
          }

          binding.signupBtn.setOnClickListener {
              binding.apply {
                  userType = userBtn.text.toString().trim()
                  when {
                      TextUtils.isEmpty(nameEt.text) -> {
                          nameEt.error =
                              "برجاء كتابة الاسم بالكامل"
                          nameEt.requestFocus()
                          return@setOnClickListener
                      }
                      TextUtils.isEmpty(ssnEt.text) -> {
                          ssnEt.error =
                              "برجاء كتابة رقم الهوية"
                          ssnEt.requestFocus()
                          return@setOnClickListener
                      }
                      TextUtils.isEmpty(phoneClosePersonEt.text)
                              && phoneClosePersonEt.text.length > 10 -> {
                          phoneClosePersonEt.error =
                              "برجاء ادخال رقم صحيح"
                          phoneClosePersonEt.requestFocus()
                          return@setOnClickListener
                      }
                      TextUtils.isEmpty(personalPhoneEt.text)
                              && personalPhoneEt.text.length != 11 -> {
                          personalPhoneEt.error =
                              "برجاء ادخال رقم صحيح"
                          personalPhoneEt.requestFocus()
                          return@setOnClickListener
                      }
                      else -> {
                          val phone = "+2${personalPhoneEt.text}"
                          signupViewModel.sendOtpToPhone(phone)
                          findNavController().navigate(R.id.action_signUpFragment_to_verivyOtpFragment)
                      }
                  }


              }

          }*/

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


    /*   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
           super.onActivityResult(requestCode, resultCode, data)
           if (requestCode == RC_SIGN_IN) {
               val response = IdpResponse.fromResultIntent(data)
               when {
                   resultCode == Activity.RESULT_OK -> {
                       // Successfully signed in
                       // showSnackbar("SignIn successful")
                       return
                   }
                   response == null -> {
                       // Sign in failed
                       // User pressed back button
                       //  showSnackbar("Sign in cancelled")
                       return
                   }
                   response.getError()?.getErrorCode() == ErrorCodes.NO_NETWORK -> {
                      // showSnackbar(R.string.no_internet_connection);
                       return;
                   }
                   else -> {
                       //        showSnackbar("Unknown Response")
                   }
               }
           }
       }*/

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential)
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            showToast(exception.message!!)
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

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userType = args.userType
                if (userType == "مستخدم") {
                    saveUser(userType)
                } else {
                    saveSavior(userType)
                }
            } else {
                // Show Error
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    showToast(task.exception?.message ?: "Verification Failed")
                } else {
                    showToast("Verification Failed")
                }
            }
        }
    }

    private fun saveSavior(userType: String) {
        binding.apply {
            val name = nameEt.text.toString()
            val ssn = ssnEt.text.toString()
            val personalPhone = personalPhoneEt.text.toString()

            val savior = Savior(
                mAuth.currentUser!!.uid,
                name,
                ssn,
                personalPhone,
                userType
            )
            signupViewModel.saveSavior(savior)
            findNavController().navigate(R.id.action_signUpFragment_to_saviorActivity)

        }
    }

    private fun saveUser(userType: String) {
        binding.apply {
            val name = nameEt.text.toString()
            val ssn = ssnEt.text.toString()
            val personalPhone = personalPhoneEt.text.toString()
            val closePersonPhone = phoneClosePersonEt.text.toString()

            val user = com.afares.emergency.data.model.User(
                mAuth.currentUser!!.uid,
                name,
                ssn,
                personalPhone,
                closePersonPhone,
                userType, null
            )
            signupViewModel.saveUser(user)

            findNavController().navigate(R.id.action_signUpFragment_to_homeActivity)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }


    /*private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }*/

/*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RC_SIGN_IN) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!

            if (userType == "مستخدم") {
                //  signUpUserWithGoogle(account)
            } else {
                signUpSaviorWithGoogle(account)
            }


        } catch (e: ApiException) {
            toast(requireContext(), e.message!!)
        }
    }
}*/

/*    private fun signUpUserWithGoogle(account: GoogleSignInAccount) {
        binding.apply {
            val personalPhone = personalPhoneEt.text.toString()
            val closePersonPhone = phoneClosePersonEt.text.toString()
            signupViewModel.signUpUserWithGoogle(
                account,
                personalPhone,
                userType,
                closePersonPhone
            ).observe(viewLifecycleOwner, {
                if (it.status == Status.SUCCESS) {

                    val user = User(
                        mAuth.currentUser!!.uid,
                        mAuth.currentUser?.displayName!!,
                        mAuth.currentUser?.photoUrl!!.toString(),
                        userType,
                        personalPhone,
                        closePersonPhone
                    )
                    signupViewModel.saveUser(user)
                    findNavController().navigate(R.id.action_signUpFragment_to_homeActivity)

                } else if (it.status == Status.ERROR) {
                    toast(requireContext(), it.message!!)
                }
            })
        }
    }*/

/*  private fun signUpSaviorWithGoogle(account: GoogleSignInAccount) {
      signupViewModel.signUpSaviorWithGoogle(
          account,
          userType
      ).observe(viewLifecycleOwner, {
          if (it.status == Status.SUCCESS) {

              val savior = Savior(
                  mAuth.currentUser!!.uid,
                  mAuth.currentUser?.displayName!!,
                  mAuth.currentUser?.photoUrl!!.toString(),
                  userType
              )
              signupViewModel.saveSavior(savior)
              val action =
                  SignUpFragmentDirections.actionSignUpFragmentToSaviorActivity(userType)
              findNavController().navigate(action)

          } else if (it.status == Status.ERROR) {
              toast(requireContext(), it.message!!)
          }
      })
  }*/

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}

