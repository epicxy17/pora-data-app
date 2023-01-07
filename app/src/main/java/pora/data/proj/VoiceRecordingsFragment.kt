package pora.data.proj

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pora.data.proj.databinding.FragmentVoiceRecordingsBinding

class VoiceRecordingsFragment : Fragment() {

    private var _binding: FragmentVoiceRecordingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentVoiceRecordingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)

        return binding.root
    }
}