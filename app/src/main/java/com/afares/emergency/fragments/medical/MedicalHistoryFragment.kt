package com.afares.emergency.fragments.medical

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.afares.emergency.R
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.databinding.FragmentMedicalHistoryBinding
import com.afares.emergency.databinding.FragmentProfileBinding
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

            val uId = mAuth.currentUser!!.uid.toString()

            val diabetic = binding.diabetic.isChecked
            val heartPatient = binding.heartPatient.isChecked
            val pressurePatient = binding.pressurePatient.isChecked
            val age = binding.ageEt.text.toString()
            val height = binding.heightEt.text.toString()
            val weight = binding.weightEt.text.toString()
            val bloodType = binding.bloodTypeSpinner.selectedItem.toString()
            val gender = binding.GenderSpinner.selectedItem.toString()

            val medicalHistory = MedicalHistory(
                uId, diabetic, heartPatient, pressurePatient, height, weight, age, bloodType, gender
            )
            viewModel.addMedicalHistory(medicalHistory)
        }

        return binding.root
    }


}