package com.afares.emergency

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentMedicalInfoBinding
import com.afares.emergency.databinding.FragmentRequestsBinding
import com.afares.emergency.fragments.signup.SignUpFragmentArgs
import com.afares.emergency.util.hideKeyboard
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.RequestsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

@AndroidEntryPoint
class MedicalInfoFragment : Fragment() {

    private var _binding: FragmentMedicalInfoBinding? = null
    private val binding get() = _binding!!
    private val requestsViewModel: RequestsViewModel by viewModels()
    private val args by navArgs<MedicalInfoFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicalInfoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        getUserMedical(args.ssn)
        if (args.hasMedicalInfo) {
            binding.medicalInfoContainer.visibility = View.VISIBLE
            attachMedicalInfo()
        } else {
            binding.noDataContainer.visibility = View.VISIBLE
        }
        return binding.root
    }

    private fun attachMedicalInfo() {
        lifecycleScope.launch {
            requestsViewModel.medicalData.collect { medicalData ->
                when (medicalData) {
                    is NetworkResult.Success -> {
                        binding.apply {
                            if (medicalData.data?.diabetic == true) {
                                diabeticImage.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                                diabeticTv.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                            }
                            if (medicalData.data?.heartPatient == true) {
                                heartPatientImage.setColorFilter(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                                heartPatientTv.setTextColor(
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
                                pressurePatientTv.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.green
                                    )
                                )
                            }
                            bloodTypeTv.text = medicalData.data?.bloodType
                            ageTv.text = medicalData.data?.age
                            genderTv.text = medicalData.data?.gender
                            bmiTv.text =
                                computeBmi(
                                    medicalData.data?.weight!!,
                                    medicalData.data.height!!
                                ).toString()
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun getUserMedical(userSnn: String?) {
        lifecycleScope.launch {
            if (userSnn != null) {
                requestsViewModel.getMedicalHistory(userSnn)
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


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}