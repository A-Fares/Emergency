package com.afares.emergency.ui.fragments.help

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.data.model.Request
import com.afares.emergency.databinding.FragmentHelpBinding
import com.afares.emergency.util.Constants.CIVIL_DEFENSE
import com.afares.emergency.util.Constants.PARAMEDIC
import com.afares.emergency.util.Constants.REQUESTED
import com.afares.emergency.util.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.afares.emergency.util.TrackingUtility
import com.afares.emergency.util.showSnackBar
import com.afares.emergency.viewmodels.UserViewModel
import com.google.android.gms.location.*
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
            if (it != null) {
                hasMedicalHistory = it
            }
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
                        requestType = PARAMEDIC
                        if (!hasMedicalHistory) {
                            confirmMedicalHistory()
                            return false
                        }
                    }
                    R.id.civil_defense_btn -> {
                        requestType = CIVIL_DEFENSE
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        /**No Thing*/
    }

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

    private fun newLocationData(){
        val locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )
    }
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            val coordinates = "${lastLocation.latitude},${lastLocation.longitude}"
            val request = Request(
                requestsId.id,
                mAuth.currentUser!!.uid,
                requestType,
                binding.requestDescriptionEt.text.toString(),
                coordinates,
                getCityName(lastLocation.latitude, lastLocation.longitude),
                REQUESTED,
                null
            )

            userViewModel.addRequest(request)
            confirmRequestAdded()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location: Location? = task.result
                if(location==null){
                    newLocationData()
                //    showSnackBar(binding.helpFragment,"البرنامج يعمل على اندرويد 10 او اكثر")
                }else{
                    val coordinates = "${location.latitude},${location.longitude}"
                    val request = Request(
                        requestsId.id,
                        mAuth.currentUser!!.uid,
                        requestType,
                        binding.requestDescriptionEt.text.toString(),
                        coordinates,
                        getCityName(location.latitude, location.longitude),
                        REQUESTED,
                        null
                    )

                    userViewModel.addRequest(request)
                    confirmRequestAdded()
                }
            }
        }
    }

    private fun confirmRequestAdded() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("تم") { _, _ ->
            findNavController().navigate(
                R.id.helpFragment,
                arguments,
                NavOptions.Builder()
                    .setPopUpTo(R.id.helpFragment, true)
                    .build()
            )
        }
        builder.setMessage("تم اضافة طلبك الان")
        builder.create().show()
    }

    private fun getCityName(lat: Double, long: Double): String {

        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 3)

        return address[0].adminArea
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}