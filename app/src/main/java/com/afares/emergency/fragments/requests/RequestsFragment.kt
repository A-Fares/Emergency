package com.afares.emergency.fragments.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afares.emergency.adapters.RequestAdapter
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentRequestsBinding
import com.afares.emergency.util.Constants.USER_TYPE_KEY
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.RequestsViewModel
import com.afares.emergency.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

@AndroidEntryPoint
class RequestsFragment : Fragment() {


    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    private val requestsViewModel: RequestsViewModel by viewModels()

    private val mAdapter by lazy { RequestAdapter() }
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        userType = args?.getString(USER_TYPE_KEY)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)

        requestsViewModel.getRequestsStatus()
        setupRecyclerView()
        getRequestsStatus()
        return binding.root
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

}