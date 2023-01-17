package pora.data.proj

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pora.data.proj.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
//    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)


        binding.photosButton.setOnClickListener {
            val directions = HomeFragmentDirections.actionHomeFragmentToBusStationsFragment()
            findNavController().navigate(directions)
        }

        binding.voiceRecordingsButton.setOnClickListener {
            val directions = HomeFragmentDirections.actionHomeFragmentToVoiceRecordingsFragment()
            findNavController().navigate(directions)
        }

        binding.speedometerButton.setOnClickListener {
            val directions = HomeFragmentDirections.actionHomeFragmentToSpeedometerFragment()
            findNavController().navigate(directions)
        }
        return binding.root
    }
}