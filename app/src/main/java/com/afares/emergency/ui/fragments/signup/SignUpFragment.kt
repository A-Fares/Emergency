package com.afares.emergency.ui.fragments.signup

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
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
import com.afares.emergency.util.Constants
import com.afares.emergency.util.Constants.CIVIL_DEFENSE
import com.afares.emergency.util.Constants.PARAMEDIC
import com.afares.emergency.util.Constants.USER
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.afares.emergency.viewmodels.RequestsViewModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val args by navArgs<SignUpFragmentArgs>()

    private val authViewModel: AuthViewModel by viewModels()
    private val requestsViewModel: RequestsViewModel by viewModels()

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!


    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var storage: StorageReference

    private var imageUri: Uri? = null
    private var imageUrl = ""

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

        binding.addUserImage.setOnClickListener {
            pickImage()
        }

        if (args.userType == USER) {
            binding.userContainer.visibility = View.VISIBLE
        }

        if (args.userType == PARAMEDIC) {
            binding.apply {
                paramedicContainer.visibility = View.VISIBLE
                progressBarSignUp.visibility = View.VISIBLE
            }
            initHospitalSpinner()
        }

        if (args.userType == CIVIL_DEFENSE) {
            binding.civilDefenceContainer.visibility = View.VISIBLE
            binding.progressBarSignUp.visibility = View.VISIBLE
            initCivilDefenceSpinner()
        }

        binding.signupBtn.setOnClickListener {

            if (validateUser(args.userType)) {
                /**      hna hb3to ll sf7a bta3to   */
                authViewModel.ssnRegistered(binding.ssnEt.text.toString())
                    .observe(viewLifecycleOwner, { ssnHasExist ->
                        if (ssnHasExist) {
                            toast(requireContext(), "رقم الهوية مسجل من قبل")
                        } else {
                            signUp()
                        }
                    })
            } else {
                return@setOnClickListener
            }
        }


        return binding.root
    }

    /** ----------------------------- Spinner Hospital Spinner--------------------------*/
    private fun initHospitalSpinner() {
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
                if (cityId != "none") {
                    requestsViewModel.getHospitalData(cityId)
                }
            }
        }
    }

    /** ----------------------------- Spinner civilDefenseSpinner--------------------------*/
    private fun initCivilDefenceSpinner() {
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
                if (cityId != "none") {
                    requestsViewModel.getCivilDefenseData(cityId)
                }

            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, Constants.PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            uploadImageToStorage()
        }
    }

    private fun uploadImageToStorage() {
        val progressbar = ProgressDialog(requireContext())
        progressbar.setMessage("تحميل ....")
        progressbar.show()

        if (imageUri != null) {
            val fileRef = storage.child(System.currentTimeMillis().toString() + ".jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    imageUrl = downloadUri.toString()
                } else {
                    // Handle failures
                }
                progressbar.dismiss()
            }
        }
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
                ssnEt.text.length != 10 -> {
                    ssnEt.error =
                        "برجاء كتابة رقم الهوية صحيح"
                    ssnEt.requestFocus()
                    return false
                }
                userType == USER -> {
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
        authViewModel.saveUser(userData())
        when (args.userType) {
            USER -> {
                findNavController().navigate(R.id.action_signUpFragment_to_homeActivity)
                activity?.finish()
            }
            else -> {
                findNavController().navigate(R.id.action_signUpFragment_to_saviorActivity)
                activity?.finish()
            }
        }
    }

    private fun userData(): User {
        binding.apply {
            val name = nameEt.text.toString()
            val ssn = ssnEt.text.toString()
            val closePersonPhone = "+566" + phoneClosePersonEt.text.toString()
            return User(
                mAuth.currentUser!!.uid,
                name,
                ssn,
                args.userPhone,
                closePersonPhone,
                args.userType, imageUrl, cityId
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}

