package com.example.anonycall.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.anonycall.R
import com.example.anonycall.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            selectedCallButton.setOnClickListener {
                goToSelectedCallFragment()
            }
        }
    }

    private fun goToSelectedCallFragment() {
        findNavController().navigate(R.id.action_welcomeFragment_to_selectedCallFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}