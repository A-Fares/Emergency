package com.afares.emergency.ui.fragments.requestdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afares.emergency.R
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentRequestDetailesBinding
import com.afares.emergency.mailapi.MailBody
import com.afares.emergency.util.Constants.FINISHED
import com.afares.emergency.util.Constants.LOADING
import com.afares.emergency.util.Constants.REQUESTED
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.RequestsViewModel
import com.afares.emergency.viewmodels.UserViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.roundToInt

@AndroidEntryPoint
class RequestDetailsFragment : Fragment() {

    private var _binding: FragmentRequestDetailesBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val requestsViewModel: RequestsViewModel by viewModels()
    private val args: RequestDetailsFragmentArgs by navArgs()

    private var userInfo = ""

    @Inject
    lateinit var mAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestDetailesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        getUserInfo(args.currentItem.uid)
        attachUserInfo()

        binding.stepView.done(true)
        val steps = ArrayList<String>()
        steps.add(REQUESTED)
        steps.add(LOADING)
        steps.add(FINISHED)
        binding.stepView.setSteps(steps)


        attachRequestDetails()
        checkRequestStatus(args.currentItem.status)
        lifecycleScope.launchWhenStarted {
            requestsViewModel.requestStatus.collect { status ->
                when (status) {
                    REQUESTED -> {
                        binding.acceptBtn.visibility = View.VISIBLE
                        binding.stepView.go(0, false)
                    }
                    LOADING -> {
                        binding.finishBtn.visibility = View.VISIBLE
                        binding.acceptBtn.visibility = View.GONE
                        binding.stepView.go(1, false)
                    }
                    "تم الانتهاء" -> {
                        binding.apply {
                            finishBtn.visibility = View.GONE
                            acceptBtn.visibility = View.GONE
                            binding.stepView.go(2, false)
                        }
                    }
                }
            }
        }

        binding.apply {
            acceptBtn.setOnClickListener {
                updateRequestStatus(args.currentItem.id!!, LOADING)
                acceptBtn.visibility = View.GONE
                finishBtn.visibility = View.VISIBLE
                binding.stepView.go(1, true)
            }
        }

        binding.finishBtn.setOnClickListener {
            updateRequestStatus(args.currentItem.id!!, FINISHED)
            findNavController().navigate(R.id.action_requestDetailesFragment_to_requestsFragment)
        }

        if (args.currentItem.type == "اسعاف") {
            binding.medicalInfoContainer.visibility = View.VISIBLE
            attachMedicalInfo()
        }else{
            binding.apply {
                requestDetailsContainer.visibility = View.VISIBLE
                shimmerRequestContainer.stopShimmer()
                shimmerRequestContainer.visibility = View.GONE
            }
        }
        binding.reportBtn.setOnClickListener {
            showReportDialog(args.recipientMail.toString())
        }

        return binding.root
    }


    private fun showReportDialog(recipients: String) {
        val dialog = MaterialDialog(requireContext())
            .noAutoDismiss()
            .customView(R.layout.report_dialog_layout)

        val subject = dialog.findViewById<EditText>(R.id.subject_et)
        val body = dialog.findViewById<EditText>(R.id.body_et)
        dialog.findViewById<Button>(R.id.send_btn).setOnClickListener {
            val saviorInfo = "اسم المساعد: ${args.saviorData.name}\n" +
                    " رقم الهوية: ${args.saviorData.ssn}\n"

            MailBody.subject = subject.text.toString()
            MailBody.body = saviorInfo + userInfo + "التقرير: ${body.text}"
            MailBody.recipients = recipients
            sendReport()
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun sendReport() {
        Thread {
            try {
                val sender = com.afares.emergency.mailapi.MailSender(
                    "emergency.ksa.health@gmail.com",
                    "EMERGENCY.KSA"
                )
                sender.sendMail(
                    MailBody.subject, MailBody.body,
                    MailBody.sender, MailBody.recipients
                )
            } catch (e: Exception) {
                Log.e("SendMail", e.message, e)
            }
        }.start()
    }

    private fun checkRequestStatus(requestStatus: String?) {
        lifecycleScope.launch {
            if (requestStatus != null) {
                requestsViewModel.checkRequestStatus(requestStatus)
            }
        }
    }

    private fun updateRequestStatus(currentRequest: String, status: String) {
        requestsViewModel.updateRequestStatus(currentRequest, status)
    }

    private fun attachMedicalInfo() {
        lifecycleScope.launch {
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
                            getUserAge(medicalData.data?.age!!)

                            genderTextView.text = medicalData.data.gender
                            bmiTextView.text =
                                computeBmi(
                                    medicalData.data.weight!!,
                                    medicalData.data.height!!
                                ).toString()
                            requestDetailsContainer.visibility = View.VISIBLE
                            shimmerRequestContainer.stopShimmer()
                            shimmerRequestContainer.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun getUserAge(birthOfDate: String) {
        val locale = Locale("ar", "SA")
        val sdf = SimpleDateFormat("yyyy-MM-dd", locale)
        val date = sdf.parse(birthOfDate)
        binding.ageTextview.text = calculateAge(date).toString()
    }

    private fun calculateAge(birthdate: Date?): Int {
        val birth = Calendar.getInstance()
        birth.time = birthdate
        val today = Calendar.getInstance()
        var yearDifference = (today[Calendar.YEAR]
                - birth[Calendar.YEAR])
        if (today[Calendar.MONTH] < birth[Calendar.MONTH]) {
            yearDifference--
        } else {
            if (today[Calendar.MONTH] === birth[Calendar.MONTH]
                && today[Calendar.DAY_OF_MONTH] < birth[Calendar.DAY_OF_MONTH]
            ) {
                yearDifference--
            }
        }
        return yearDifference
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
                            Glide.with(requireActivity())
                                .load(it.data?.photoUrl)
                                .placeholder(R.drawable.profile_placeholder)
                                .into(userImageView)
                            phoneTextView.text = it.data?.phone
                            closePhoneTextView.text = it.data?.closePersonPhone
                            getUserMedical(it.data?.ssn)
                        }
                        userInfo =
                            "اسم الحالة: ${it.data?.name}\n" + "رقم الهوية: ${it.data?.ssn}\n" +
                                    "رقم الهاتف: ${it.data?.phone}\n" +
                                    "رقم شخص مقرب: ${it.data?.closePersonPhone}\n"
                    }
                    is NetworkResult.Error -> {
                        toast(requireContext(), it.message.toString())
                    }
                    is NetworkResult.Loading -> {
                        // LOADING
                    }
                    is NetworkResult.Empty -> {
                        // NO THING
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

    private fun getUserMedical(userSnn: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (userSnn != null) {
                requestsViewModel.getMedicalHistory(userSnn)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}