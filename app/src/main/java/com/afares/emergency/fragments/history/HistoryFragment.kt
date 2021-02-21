package com.afares.emergency.fragments.history

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afares.emergency.R
import com.afares.emergency.adapters.RequestAdapter
import com.afares.emergency.data.Status
import com.afares.emergency.databinding.FragmentHistoryBinding
import com.afares.emergency.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val mAdapter by lazy { RequestAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            viewModel.getUserRequestsHistory()
        }
        setupRecyclerView()
        getHistory()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getHistory() {
        viewModel.readRequestsHistory.observe(viewLifecycleOwner, {
            Log.d("HHH", it.status.toString())
            if (it.status == Status.LOADING || it.status == Status.ERROR) {
                binding.apply {
                    placeholderHistoryRow.startShimmer()
                    placeholderHistoryRow.visibility = View.VISIBLE
                }
            } else if (it.status == Status.SUCCESS) {

                lifecycleScope.launch {
                    viewModel.flow.collectLatest { dataFlow ->
                        mAdapter.submitData(dataFlow)
                    }
                }
                binding.apply {
                    recyclerview.visibility = View.VISIBLE
                    lifecycleScope.launch{ delay(1000L)
                    placeholderHistoryRow.visibility=View.GONE
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