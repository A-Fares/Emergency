package com.afares.emergency.ui.fragments.requests

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afares.emergency.R
import com.afares.emergency.adapters.OnRequestClickListener
import com.afares.emergency.adapters.RequestAdapter
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.data.model.User
import com.afares.emergency.databinding.FragmentRequestsBinding
import com.afares.emergency.util.Constants.CIVIL_DEFENSE
import com.afares.emergency.util.Constants.PARAMEDIC
import com.afares.emergency.util.hideKeyboard
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.afares.emergency.viewmodels.RequestsViewModel
import com.afares.emergency.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RequestsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val requestsViewModel: RequestsViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()


    private lateinit var mAdapter: RequestAdapter
    private lateinit var userType: String
    private lateinit var searchView: SearchView

    private lateinit var recipientMail: String
    private lateinit var saviorData: User


    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.lifecycleOwner = this

        mAdapter = RequestAdapter(OnRequestClickListener { request ->
            requestsViewModel.onRequestClicked(request)
        })

        searchView = binding.searchView
        initSearchView()

        setHasOptionsMenu(true)
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)

        userViewModel.getUserInfo(mAuth.currentUser!!.uid)
        fetchUserData()

        requestsViewModel.recipientMail.observe(viewLifecycleOwner, {
            recipientMail = it
        })

        setupRecyclerView()

        lifecycleScope.launchWhenStarted {
            requestsViewModel.navigateToRequestInfo.observe(viewLifecycleOwner, { currentRequest ->
                currentRequest?.let {
                    val action =
                        RequestsFragmentDirections.actionRequestsFragmentToRequestDetailesFragment(
                            currentRequest, recipientMail, saviorData
                        )
                    findNavController().navigate(action)
                    requestsViewModel.onRequestNavigated()
                }
            })
        }

        signOut()
        return binding.root
    }

    private fun initSearchView() {
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotEmpty() && query.length == 9) {
                    checkMedicalHistory(query)
                    userViewModel.hasMedicalHistory.observe(
                        viewLifecycleOwner,
                        { hasMedicalHistory ->
                            if (hasMedicalHistory == true) {
                                val action =
                                    RequestsFragmentDirections.actionRequestsFragmentToMedicalInfoFragment(
                                        true,
                                        query
                                    )
                                findNavController().navigate(action)
                                userViewModel.onMedicalHistoryNavigated()
                            } else if (hasMedicalHistory == false) {
                                val action =
                                    RequestsFragmentDirections.actionRequestsFragmentToMedicalInfoFragment(
                                        false,
                                        null
                                    )
                                findNavController().navigate(action)
                                userViewModel.onMedicalHistoryNavigated()
                            }
                        })
                } else {
                    toast(requireContext(), "برجاء ادخال بيانات صحيحة")
                }
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    private fun checkMedicalHistory(userSsn: String) {
        userViewModel.getMedicalHistory(userSsn)
    }

    private fun fetchUserData() {
        lifecycleScope.launch {
            userViewModel.userData.collect { userData ->
                when (userData) {
                    is NetworkResult.Success -> {
                        saviorData = userData.data!!
                        val cityId = userData.data.cityId!!
                        userType = userData.data.type!!
                        if (userType == PARAMEDIC) {
                            requestsViewModel.getHospitalData(cityId)
                        } else if (userType == CIVIL_DEFENSE) {
                            requestsViewModel.getCivilDefenseData(cityId)
                        }
                        getRequestsState()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerviewRequests.adapter = mAdapter
        binding.recyclerviewRequests.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getRequestsState() {
        lifecycleScope.launch {
            when (userType) {
                "اسعاف" -> {
                    requestsViewModel.requestAmbulance.observe(viewLifecycleOwner,
                        { dataFlow ->
                            lifecycleScope.launch {
                                if (dataFlow != null) {
                                    mAdapter.submitData(dataFlow)
                                }
                            }
                        })
                }
                "دفاع مدني" -> {
                    requestsViewModel.requestFireFighter.observe(viewLifecycleOwner,
                        { dataFlow ->
                            lifecycleScope.launch {
                                mAdapter.submitData(dataFlow)
                            }
                        })
                }
            }
        }
        binding.apply {
            profileSaviorContainer.visibility = View.VISIBLE
            placeholderRequestRow.visibility = View.GONE
        }
    }

    override fun onRefresh() {
        findNavController().navigate(
            R.id.requestsFragment,
            arguments,
            NavOptions.Builder()
                .setPopUpTo(R.id.requestsFragment, true)
                .build()
        )
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun signOut() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                // these ids should match the item ids from my_fragment_menu.xml file
                R.id.medicalInfo_search -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setPositiveButton("نعم") { _, _ ->
                        authViewModel.signOut()
                        findNavController().navigate(R.id.action_requestsFragment_to_mainActivity2)
                        activity?.finish()
                    }
                    builder.setNegativeButton("لا") { _, _ -> }
                    builder.setMessage("هل تريد تسجيل خروجك")
                    builder.create().show()
                    // by returning 'true' we're saying that the event
                    // is handled and it shouldn't be propagated further
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}