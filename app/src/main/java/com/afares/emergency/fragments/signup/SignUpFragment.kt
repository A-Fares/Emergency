package com.afares.emergency.fragments.signup

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.data.Status
import com.afares.emergency.data.model.Savior
import com.afares.emergency.data.model.User
import com.afares.emergency.databinding.FragmentSignUpBinding
import com.afares.emergency.util.Constants.RC_SIGN_IN
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.SignUpViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val signupViewModel: SignUpViewModel by viewModels()
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var userType: String

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.userBtn.setOnClickListener {
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

                    TextUtils.isEmpty(phoneClosePersonEt.text) -> {
                        phoneClosePersonEt.error =
                            "field must not be empty"
                        phoneClosePersonEt.requestFocus()
                        return@setOnClickListener
                    }
                    TextUtils.isEmpty(personalPhoneEt.text) -> {
                        personalPhoneEt.error =
                            "field must not be empty"
                        personalPhoneEt.requestFocus()
                        return@setOnClickListener
                    }
                }
            }
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

                if (userType == "مستخدم") {
                    signUpUserWithGoogle(account)
                } else {
                    signUpSaviorWithGoogle(account)
                }


            } catch (e: ApiException) {
                toast(requireContext(), e.message!!)
            }
        }
    }

    private fun signUpUserWithGoogle(account: GoogleSignInAccount) {
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
    }

    private fun signUpSaviorWithGoogle(account: GoogleSignInAccount) {


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
                val action = SignUpFragmentDirections.actionSignUpFragmentToSaviorActivity(userType)
                findNavController().navigate(action)

            } else if (it.status == Status.ERROR) {
                toast(requireContext(), it.message!!)
            }
        })

    }


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}