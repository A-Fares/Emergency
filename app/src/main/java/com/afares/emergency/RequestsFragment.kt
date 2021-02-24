package com.afares.emergency

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.afares.emergency.adapters.RequestAdapter
import com.afares.emergency.data.Status
import com.afares.emergency.databinding.FragmentRequestsBinding
import com.afares.emergency.util.Constants
import com.afares.emergency.util.Constants.USER_TYPE_KEY
import com.afares.emergency.viewmodels.LoginViewModel
import com.afares.emergency.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RequestsFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!


    private val mAdapter by lazy { RequestAdapter() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)

        val args = arguments
        val userType = args?.getString(USER_TYPE_KEY)
        //  viewModel.setRequestType(userType!!)

        viewModel.getRequestsStatus()


        setupRecyclerView()
        if (userType != null) {
            getRequests(userType)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerviewRequests.adapter = mAdapter
        binding.recyclerviewRequests.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getRequests(userType: String) {
        viewModel.readRequests.observe(viewLifecycleOwner, {
            if (it.status == Status.LOADING || it.status == Status.ERROR) {
                binding.apply {
                    placeholderRequestRow.startShimmer()
                    placeholderRequestRow.visibility = View.VISIBLE
                }
            } else if (it.status == Status.SUCCESS) {
                lifecycleScope.launch {
                     when (userType) {
                         "اسعاف" -> {
                             viewModel.requestsAmbulanceFlow.collectLatest { dataFlow ->
                                 mAdapter.submitData(dataFlow)
                             }
                         }
                         "دفاع مدني" -> {
                             viewModel.requestsFireFighterFlow.collectLatest { dataFlow ->
                                 mAdapter.submitData(dataFlow)
                             }
                         }
                     }

                }
                binding.apply {
                    recyclerviewRequests.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        delay(1000L)
                        placeholderRequestRow.visibility = View.GONE
                    }

                }

            }

        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }

}