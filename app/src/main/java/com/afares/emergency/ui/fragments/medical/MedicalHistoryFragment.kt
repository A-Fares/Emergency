package com.afares.emergency.ui.fragments.medical

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.data.model.MedicalHistory
import com.afares.emergency.databinding.FragmentMedicalHistoryBinding
import com.afares.emergency.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_medical_history.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MedicalHistoryFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private var _binding: FragmentMedicalHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var userSsn: String

    @Inject
    lateinit var mAuth: FirebaseAuth
    lateinit var myCalendar: Calendar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicalHistoryBinding.inflate(inflater, container, false)

        userViewModel.getUserInfo(mAuth.currentUser!!.uid)

        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.userData.collect { userData ->
                userSsn = userData.data?.ssn.toString()
            }
        }

        initDatePicker()

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
                    userViewModel.addMedicalHistory(userSsn, medicalHistory)
                    findNavController().navigate(R.id.action_medicalHistoryFragment_to_helpFragment)
                } else {
                    return@setOnClickListener
                }
            }
        }

        return binding.root
    }

    private fun initDatePicker() {
        myCalendar = Calendar.getInstance()
        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLabel()
            }
        binding.ageEt.setOnClickListener {
            val style: Int = AlertDialog.THEME_HOLO_LIGHT
            val datePickerDialog = DatePickerDialog(
                requireContext(), style, date, myCalendar
                    .get(Calendar.YEAR) - 20, myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun updateLabel() {
        val locale = Locale("ar", "SA")
        val sdf = SimpleDateFormat("yyyy-MM-dd", locale)
        binding.ageEt.setText(sdf.format(myCalendar.time))
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