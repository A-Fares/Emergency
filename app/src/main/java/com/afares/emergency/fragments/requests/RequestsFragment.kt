package com.afares.emergency.fragments.requests

import android.os.Bundle
import android.view.*
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
import com.afares.emergency.adapters.RequestAdapter
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentRequestsBinding
import com.afares.emergency.util.hideKeyboard
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.AuthViewModel
import com.afares.emergency.viewmodels.RequestsViewModel
import com.afares.emergency.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RequestsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val requestsViewModel: RequestsViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val mAdapter by lazy { RequestAdapter() }
    private lateinit var userType: String
    private lateinit var searchView: SearchView

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.lifecycleOwner = this

        requestsViewModel.getA()

        searchView = binding.searchView
        initSearchView()

        setHasOptionsMenu(true)
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                // these ids should match the item ids from my_fragment_menu.xml file
                R.id.medicalInfo_search -> {

                    authViewModel.signOut()
                    findNavController().navigate(R.id.action_requestsFragment_to_mainActivity2)
                    activity?.finish()

                    // by returning 'true' we're saying that the event
                    // is handled and it shouldn't be propagated further
                    true
                }
                else -> false
            }
        }
        userViewModel.getUserInfo(mAuth.currentUser!!.uid)
        fetchUserData()



        requestsViewModel.getRequestsStatus()
        setupRecyclerView()
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
                            if (hasMedicalHistory) {
                                val action =
                                    RequestsFragmentDirections.actionRequestsFragmentToMedicalInfoFragment(
                                        true,
                                        query
                                    )
                                findNavController().navigate(action)
                            } else {
                                val action =
                                    RequestsFragmentDirections.actionRequestsFragmentToMedicalInfoFragment(
                                        false,
                                        null
                                    )
                                findNavController().navigate(action)
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
                        userType = userData.data?.type!!
                        getRequestsStatus()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerviewRequests.adapter = mAdapter
        binding.recyclerviewRequests.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getRequestsStatus() {
        lifecycleScope.launch {
            requestsViewModel.requestState.collect {
                when (it) {
                    is NetworkResult.Success -> {
                        lifecycleScope.launch {
                            when (userType) {
                                "اسعاف" -> {
                                    requestsViewModel.requestsAmbulanceFlow.collectLatest { dataFlow ->
                                        mAdapter.submitData(dataFlow)
                                    }
                                }
                                "دفاع مدني" -> {
                                    requestsViewModel.requestsFireFighterFlow.collectLatest { dataFlow ->
                                        mAdapter.submitData(dataFlow)
                                    }
                                }
                            }
                        }
                        binding.apply {
                            profileSaviorContainer.visibility = View.VISIBLE
                            placeholderRequestRow.visibility = View.GONE
                        }
                    }
                    is NetworkResult.Error -> {
                        binding.placeholderRequestRow.visibility = View.GONE
                        toast(requireContext(), it.message.toString())
                    }
                    is NetworkResult.Loading -> {
                        binding.apply {
                            placeholderRequestRow.startShimmer()
                            placeholderRequestRow.visibility = View.VISIBLE
                        }
                    }
                    is NetworkResult.Empty -> {
                        // No Thing
                    }
                }
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}