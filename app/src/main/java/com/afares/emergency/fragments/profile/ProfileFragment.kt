package com.afares.emergency.fragments.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afares.emergency.R
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentProfileBinding
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.afares.emergency.viewmodels.UserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
    private var mVerificationInProgress = false

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        userViewModel.getUserInfo(mAuth.currentUser!!.uid)

        fetchUserData()

        binding.signOutBtn.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setPositiveButton("نعم") { _, _ ->
                authViewModel.signOut()
                findNavController().navigate(R.id.action_profileFragment_to_mainActivity)
                activity?.finish()
            }
            builder.setNegativeButton("لا") { _, _ -> }
            builder.setMessage("هل تريد تسجيل خروجك")
            builder.create().show()
        }
        return binding.root
    }

    private fun fetchUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.userData.collect { userData ->
                when (userData) {
                    is NetworkResult.Success -> {
                        binding.apply {
                            userNameTextView.text = userData.data?.name
                            userSsnTextView.text = userData.data?.ssn
                            userPhoneTextView.text = userData.data?.phone
                            userClosePersonPhoneTextView.text = userData.data?.closePersonPhone
                            Glide.with(requireActivity())
                                .load(userData.data?.photoUrl)
                                .placeholder(R.drawable.profile_placeholder)
                                .into(userImageView)
                            profileContainer.visibility = View.VISIBLE
                            shimmerContainer.visibility = View.GONE
                            shimmerContainer.stopShimmer()
                        }
                    }
                    is NetworkResult.Error -> {
                        toast(requireContext(), userData.message.toString())
                    }
                    is NetworkResult.Loading -> {
                        binding.apply {
                            profileContainer.visibility = View.INVISIBLE
                            shimmerContainer.visibility = View.VISIBLE
                        }
                    }
                    is NetworkResult.Empty -> {
                        // No Thing
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}