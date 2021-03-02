package com.afares.emergency.fragments.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afares.emergency.R
import com.afares.emergency.adapters.RequestAdapter
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentRequestsBinding
import com.afares.emergency.util.toast
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

    private val requestsViewModel: RequestsViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val mAdapter by lazy { RequestAdapter() }
    private lateinit var userType: String

    @Inject
    lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        userViewModel.getUserInfo(mAuth.currentUser!!.uid)
        fetchUserData()

        requestsViewModel.getRequestsStatus()
        setupRecyclerView()
        return binding.root
    }

    private fun fetchUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
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
        viewLifecycleOwner.lifecycleScope.launch {
            requestsViewModel.requestState.collect {
                when (it) {
                    is NetworkResult.Success -> {
                        viewLifecycleOwner.lifecycleScope.launch {
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
                            recyclerviewRequests.visibility = View.VISIBLE
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


    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
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

}