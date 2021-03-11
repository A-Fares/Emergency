package com.afares.emergency.fragments.requestdetails

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
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.RequestsViewModel
import com.afares.emergency.viewmodels.UserViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt

@AndroidEntryPoint
class RequestDetailsFragment : Fragment() {

    private var _binding: FragmentRequestDetailesBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val requestsViewModel: RequestsViewModel by viewModels()
    private val args: RequestDetailsFragmentArgs by navArgs()

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
        steps.add("تم الطلب")
        steps.add("تم الاستلام")
        steps.add("تم الانتهاء")
        binding.stepView.setSteps(steps)


        attachRequestDetails()
        checkRequestStatus(args.currentItem.status)
        lifecycleScope.launchWhenStarted {
            requestsViewModel.requestStatus.collect { status ->
                when (status) {
                    "تم الطلب" -> {
                        binding.acceptBtn.visibility = View.VISIBLE
                        binding.stepView.go(0, false)
                    }
                    "تم الاستلام" -> {
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
                updateRequestStatus(args.currentItem.id!!, "تم الاستلام")
                acceptBtn.visibility = View.GONE
                finishBtn.visibility = View.VISIBLE
                binding.stepView.go(1, true)
            }
        }

        binding.finishBtn.setOnClickListener {
            updateRequestStatus(args.currentItem.id!!, "تم الانتهاء")
            findNavController().navigate(R.id.action_requestDetailesFragment_to_requestsFragment)
        }

        if (args.currentItem.type == "اسعاف") {
            binding.medicalInfoContainer.visibility = View.VISIBLE
            attachMedicalInfo()
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
            MailBody.body = saviorInfo + "التقرير: ${body.text}"
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
                            ageTextview.text = medicalData.data?.age
                            genderTextView.text = medicalData.data?.gender
                            bmiTextView.text =
                                computeBmi(
                                    medicalData.data?.weight!!,
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
                            phoneTextView.text = it.data?.phone
                            closePhoneTextView.text = it.data?.closePersonPhone
                            getUserMedical(it.data?.ssn)
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