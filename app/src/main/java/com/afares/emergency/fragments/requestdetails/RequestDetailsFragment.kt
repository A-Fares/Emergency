package com.afares.emergency.fragments.requestdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afares.emergency.R
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentRequestDetailesBinding
import com.afares.emergency.databinding.FragmentRequestsBinding
import com.afares.emergency.fragments.signup.SignUpFragmentArgs
import com.afares.emergency.util.showSnackBar
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.RequestsViewModel
import com.afares.emergency.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Math.round
import kotlin.math.pow
import kotlin.math.roundToInt

@AndroidEntryPoint
class RequestDetailsFragment : Fragment() {

    private var _binding: FragmentRequestDetailesBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val requestsViewModel: RequestsViewModel by viewModels()
    private val args: RequestDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestDetailesBinding.inflate(inflater, container, false)

        getUserInfo(args.currentItem.uid)
        getUserMedical(args.currentItem.uid)

        attachRequestDetails()
        attachUserInfo()
        if (args.currentItem.type == "اسعاف") {
            binding.medicalInfoContainer.visibility = View.VISIBLE
            attachMedicalInfo()
        }
        return binding.root
    }

    private fun attachMedicalInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            requestsViewModel.medicalData.collect { medicalData ->
                when (medicalData) {
                    is NetworkResult.Success -> {
                        binding.apply {
                            if (medicalData.data?.diabetic == true) {
                                diabeticImageView.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                                diabeticTextView.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                            }
                            if (medicalData.data?.heartPatient == true) {
                                heartPatientImageView.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                                heartPatientTextView.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                            }
                            if (medicalData.data?.pressurePatient == true) {
                                pressurePatientImageView.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                                pressurePatientTextView.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                            }
                            bloodTypeTextView.text = medicalData.data?.bloodType
                            ageTextview.text = medicalData.data?.age
                            genderTextView.text = medicalData.data?.gender
                            bmiTextView.text =
                                computeBmi(
                                    medicalData.data?.weight!!,
                                    medicalData.data.height!!
                                ).toString()
                        }
                    }
                }
            }
        }
    }

    private fun computeBmi(weight: String, height: String): Double {
        var computedBmi: Double
        val numerator = weight.toFloat()
        val denominator = (height.toFloat() * 0.01).pow(2.0)
        computedBmi = numerator / denominator
        computedBmi = (computedBmi * 10.0).roundToInt() / 10.0
        return computedBmi
    }

    private fun attachRequestDetails() {
        binding.apply {
            requestTextview.text = args.currentItem.description
            statusTextView.text = args.currentItem.status
            locationBtn.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse("google.navigation:q=${args.currentItem.location}")
                startActivity(intent)
            }
        }
    }

    private fun attachUserInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.userData.collect {
                when (it) {
                    is NetworkResult.Success -> {
                        binding.apply {
                            userName.text = it.data?.name
                            ssnTextView.text = it.data?.ssn
                            phoneTextView.text = it.data?.phone
                            closePhoneTextView.text = it.data?.closePersonPhone
                        }
                    }
                    is NetworkResult.Error -> {
                        toast(requireContext(), it.message.toString())
                    }
                    is NetworkResult.Loading -> {

                    }
                    is NetworkResult.Empty -> {

                    }
                }
            }
        }
    }

    private fun getUserInfo(userId: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (userId != null) {
                userViewModel.getUserInfo(userId)
            }
        }
    }

    private fun getUserMedical(userId: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (userId != null) {
                requestsViewModel.getMedicalHistory(userId)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}