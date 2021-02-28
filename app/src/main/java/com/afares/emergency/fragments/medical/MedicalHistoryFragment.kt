package com.afares.emergency.fragments.medical

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.databinding.FragmentMedicalHistoryBinding
import com.afares.emergency.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MedicalHistoryFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentMedicalHistoryBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicalHistoryBinding.inflate(inflater, container, false)


        binding.saveBtn.setOnClickListener {

            binding.apply {
                val uId = mAuth.currentUser!!.uid
                val diabetic = diabetic.isChecked
                val heartPatient = heartPatient.isChecked
                val pressurePatient = pressurePatient.isChecked
                val age = ageEt.text.toString()
                val height = heightEt.text.toString()
                val weight = weightEt.text.toString()
                val bloodType = bloodTypeSpinner.selectedItem.toString()
                val gender = GenderSpinner.selectedItem.toString()

                if (validateMedicalHistory(height, weight, age)) {
                    val medicalHistory = MedicalHistory(
                        uId,
                        diabetic,
                        heartPatient,
                        pressurePatient,
                        height,
                        weight,
                        age,
                        bloodType,
                        gender
                    )
                    viewModel.addMedicalHistory(medicalHistory)
                    findNavController().navigate(R.id.action_medicalHistoryFragment_to_helpFragment)
                } else {
                    return@setOnClickListener
                }
            }
        }

        return binding.root
    }

    private fun validateMedicalHistory(height: String, weight: String, age: String): Boolean {

        binding.apply {
            if (TextUtils.isEmpty(height)) {
                heightEt.error = "برجاء ادخال الطول "
                heightEt.requestFocus()
                return false
            }

            if (TextUtils.isEmpty(weight)) {
                weightEt.error = "برجاء ادخال الوزن "
                weightEt.requestFocus()
                return false
            }

            if (TextUtils.isEmpty(age)) {
                ageEt.error = "برجاء ادخال العمر "
                ageEt.requestFocus()
                return false
            }
        }
        return true
    }


}