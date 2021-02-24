package com.afares.emergency

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.navigation.ui.setupActionBarWithNavController
import com.afares.emergency.util.Constants.USER_TYPE_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaviorActivity : AppCompatActivity() {

    private val args by navArgs<SaviorActivityArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savior)

        val typeBundle = Bundle()
        typeBundle.putString(USER_TYPE_KEY, args.userType)

        findNavController(R.id.navHostFragment).setGraph(R.navigation.savior_nav, typeBundle)
       // setupActionBarWithNavController(findNavController(R.id.navHostFragment))
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }*/
}