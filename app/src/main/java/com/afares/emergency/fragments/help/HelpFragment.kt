package com.afares.emergency.fragments.help

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Geocoder
import android.location.Location
import android.os.Build
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
import com.afares.emergency.data.model.Request
import com.afares.emergency.databinding.FragmentHelpBinding
import com.afares.emergency.util.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.afares.emergency.util.TrackingUtility
import com.afares.emergency.util.showSnackBar
import com.afares.emergency.viewmodels.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_help.*
import kotlinx.android.synthetic.main.fragment_help.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class HelpFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val userViewModel: UserViewModel by viewModels()
    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    //Declaring the needed Variables
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var requestsId: DocumentReference

    private lateinit var requestType: String
    private var hasMedicalHistory by Delegates.notNull<Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        userViewModel.getUserInfo(mAuth.currentUser!!.uid)

        lifecycleScope.launch {
            userViewModel.userData.collect { userData ->
                val userSsn = userData.data?.ssn.toString()
                checkMedicalHistory(userSsn)
            }
        }

        userViewModel.hasMedicalHistory.observe(viewLifecycleOwner, {
            hasMedicalHistory = it
        })


        binding.orderBtn.setOnClickListener {
            if (validateRequest()) {
                requestPermissions()
            } else {
                return@setOnClickListener
            }
        }

        return binding.root
    }

    private fun checkMedicalHistory(userSsn: String) {
        userViewModel.getMedicalHistory(userSsn)
    }

    private fun validateRequest(): Boolean {
        binding.apply {
            if (binding.requestTypeRg.checkedRadioButtonId == -1) {
                showSnackBar(helpFragment, "من فضلك اختر نوع الطلب")
                return false
            } else {
                when (binding.requestTypeRg.checkedRadioButtonId) {
                    R.id.ambulance_btn -> {
                        requestType = "اسعاف"
                        if (!hasMedicalHistory) {
                            confirmMedicalHistory()
                            return false
                        }
                    }
                    R.id.civil_defense_btn -> {
                        requestType = "دفاع مدني"
                    }
                }
            }
            if (TextUtils.isEmpty(requestDescriptionEt.text)) {
                requestDescriptionEt.error = "برجاء وصف المشكلة "
                requestDescriptionEt.requestFocus()
                return false
            }
        }
        return true
    }


    private fun confirmMedicalHistory() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("تم") { _, _ ->
            findNavController().navigate(R.id.action_helpFragment_to_medicalHistoryFragment)
        }
        builder.setTitle("تنبيه ")
        builder.setMessage("برجاء ملئ السجل الطبي اولا ")
        builder.create().show()
    }


    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            getLastLocation()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "نحتاج للسماح باذونات المكان الحالي",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "نحتاج للسماح باذونات المكان الحالي",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location: Location? = task.result
                val coordinates = "${location!!.latitude},${location.longitude}"

                val request = Request(
                    requestsId.id,
                    mAuth.currentUser!!.uid,
                    requestType,
                    binding.requestDescriptionEt.text.toString(),
                    coordinates,
                    getCityName(location.latitude, location.longitude),
                    "تم الطلب",
                    null
                )
                addRequest(request)
                findNavController().navigate(R.id.action_helpFragment_to_historyFragment)
            }
        }
    }

    private fun getCityName(lat: Double, long: Double): String {

        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 3)

        return address[0].adminArea
    }

    private fun addRequest(request: Request) = userViewModel.addRequest(request)

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}