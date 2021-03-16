package com.afares.emergency.ui.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afares.emergency.adapters.HistoryAdapter
import com.afares.emergency.data.NetworkResult
import com.afares.emergency.databinding.FragmentHistoryBinding
import com.afares.emergency.util.toast
import com.afares.emergency.viewmodels.RequestsViewModel
import com.afares.emergency.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private val requestsViewModel: RequestsViewModel by viewModels()

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val mAdapter by lazy { HistoryAdapter() }
    private var hasRequests by Delegates.notNull<Boolean>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        viewModel.checkRequestHistory()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasRequests.collectLatest { value ->
                hasRequests = value
            }
        }
       requestsViewModel.getRequestsState()
        setupRecyclerView()
        getRequestsStatus()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerviewHistory.adapter = mAdapter
        binding.recyclerviewHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getRequestsStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            requestsViewModel.requestState.collect {
                when (it) {
                    is NetworkResult.Success -> {
                        if (hasRequests) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewModel.historyRequestsFlow.collectLatest { dataFlow ->
                                    mAdapter.submitData(dataFlow)
                                }
                            }
                            binding.recyclerviewHistory.visibility = View.VISIBLE
                            binding.placeholderHistoryRow.visibility = View.GONE
                        } else {
                            binding.noDataTextView.visibility = View.VISIBLE
                            binding.noDataImageView.visibility = View.VISIBLE
                            binding.placeholderHistoryRow.visibility = View.GONE
                        }
                    }
                    is NetworkResult.Error -> {
                        binding.placeholderHistoryRow.visibility = View.GONE
                        toast(requireContext(), it.message.toString())
                    }
                    is NetworkResult.Loading -> {
                        binding.apply {
                            placeholderHistoryRow.startShimmer()
                            placeholderHistoryRow.visibility = View.VISIBLE
                        }
                    }
                    is NetworkResult.Empty -> {
                        // NO Thing
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