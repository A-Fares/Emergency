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
            userType = binding.firefighterBtn.text.toString()
            /*saveUserInformation(
                userType, null, null, null, null
            )*/
            //  signIn()
        }
        binding.paramedicBtn.setOnClickListener {
            /* saveUserInformation(
                 userType, null, null, null, null
             )*/
            //signIn()
        }

        binding.signupBtn.setOnClickListener {
            binding.apply {
                when {
                    TextUtils.isEmpty(ssnEt.text) -> {
                        ssnEt.error = "field must not be empty"
                        ssnEt.requestFocus()
                        return@setOnClickListener
                    }
                    TextUtils.isEmpty(ageEt.text) -> {
                        ageEt.error = "field must not be empty"
                        ageEt.requestFocus()
                        return@setOnClickListener
                    }
                    TextUtils.isEmpty(bloodTypeEt.text) -> {
                        bloodTypeEt.error = "field must not be empty"
                        bloodTypeEt.requestFocus()
                        return@setOnClickListener
                    }
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

                signUpUserWithGoogle(account)


            } catch (e: ApiException) {
                toast(requireContext(), e.message!!)
            }
        }
    }

    private fun signUpUserWithGoogle(account: GoogleSignInAccount) {
        binding.apply {
            userType = userBtn.text.toString().trim()
            val personalPhone = personalPhoneEt.text.toString()
            val ssn = ssnEt.text.toString().toInt()
            val age = ageEt.text.toString().toInt()
            val bloodType = bloodTypeEt.text.toString()
            val closePersonPhone = phoneClosePersonEt.text.toString()
            signupViewModel.signInWithGoogle(
                account,
                personalPhone,
                userType,
                closePersonPhone,
                age,
                ssn,
                bloodType
            ).observe(viewLifecycleOwner, {
                if (it.status == Status.SUCCESS) {

                    val user = User(
                        mAuth.currentUser!!.uid,
                        mAuth.currentUser?.displayName!!,
                        mAuth.currentUser?.email!!,
                        mAuth.currentUser?.photoUrl!!.toString(),
                        userType,
                        personalPhone,
                        closePersonPhone, age, ssn, bloodType
                    )
                    signupViewModel.saveUser(user)
                    findNavController().navigate(R.id.action_signUpFragment_to_homeActivity)

                } else if (it.status == Status.ERROR) {
                    toast(requireContext(), it.message!!)
                }
            })
        }
    }

    /* private fun checkUserInformation() {
         binding.apply {
             userType = userBtn.text.toString().trim()
             val ssn = ssnEt.text.toString().toInt()
             val age = ageEt.text.toString().toInt()
             val bloodType = bloodTypeEt.text.toString()
             val closePersonPhone = phoneClosePersonEt.text.toString()

             signIn()
             saveUserInformation(
                 userType,
                 closePersonPhone,
                 age,
                 ssn,
                 bloodType
             )
         }
     }*/

    /*private fun saveUserInformation(
        userType: String,
        closePersonPhone: String?,
        age: Int?,
        ssn: Int?,
        bloodType: String?
    ) {
        val uid = mAuth.currentUser!!.uid
        val name = mAuth.currentUser?.displayName!!
        val email = mAuth.currentUser?.email!!
        val photoUrl = mAuth.currentUser?.photoUrl!!.toString()
        val phoneNumber = mAuth.currentUser?.phoneNumber!!
        if (userType == "مستخدم") {
            if (closePersonPhone != null && age != null && ssn != null && bloodType != null) {
                val user = User(
                    uid,
                    name,
                    email,
                    photoUrl,
                    userType,
                    phoneNumber,
                    closePersonPhone,
                    age,
                    ssn,
                    bloodType
                )
                signupViewModel.saveUser(user)
            }
        } else {
            val savior = Savior(uid, name, email, photoUrl, userType)
            signupViewModel.saveSavior(savior)
        }
    }*/

    /*private fun checkUserDirection(userType: String) {
        when (userType) {
            "مستخدم" -> findNavController().navigate(R.id.action_signUpFragment_to_homeActivity)
            "مسعف" -> findNavController().navigate(R.id.action_signUpFragment_to_saviorActivity)
            "حمايةمدنية" -> findNavController().navigate(R.id.action_signUpFragment_to_saviorActivity)
        }
    }*/


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}