package com.afares.emergency.fragments.help

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.afares.emergency.R
import com.afares.emergency.util.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.afares.emergency.util.TrackingUtility
import com.google.android.gms.location.*
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_help.view.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class HelpFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    //Declaring the needed Variables
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_help, container, false)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        view.order_btn.setOnClickListener {
            requestPermissions()

        }

        return view
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
                val latitude = location!!.latitude
                val longitude = location!!.longitude
                val coordinates: String = "$latitude,$longitude"


                /** for handle best route location */
               /* val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = Uri.parse("google.navigation:q=$coordinates")
                startActivity(intent)*/
            }
        }
    }


}