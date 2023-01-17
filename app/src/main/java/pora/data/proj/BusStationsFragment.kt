package pora.data.proj

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import pora.data.proj.databinding.FragmentBusStationsBinding
import pora.data.proj.models.BusStationsResponse
import pora.data.proj.networking.ApiModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BusStationsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var _binding: FragmentBusStationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter1: BusStationsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("shows", Context.MODE_PRIVATE)
        _binding = FragmentBusStationsBinding.inflate(inflater, container, false)
        initViewPointsRecycler()
        return binding.root
    }

    private fun initViewPointsRecycler() {
        ApiModule.retrofit.busStations().enqueue(object : Callback<List<BusStationsResponse>> {
            override fun onResponse(call: Call<List<BusStationsResponse>>, response: Response<List<BusStationsResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    adapter1 = BusStationsAdapter(response.body()!!) { show ->
                        val directions = BusStationsFragmentDirections.actionBusStationsFragmentToPhotosFragment(show.id, show.name,
                            show.location.coordinates[0].toFloat(), show.location.coordinates[1].toFloat()
                        )
                        findNavController().navigate(directions)
                    }
                    with(binding.showsRecycler) {
                        layoutManager = LinearLayoutManager(this@BusStationsFragment.requireContext(), LinearLayoutManager.VERTICAL, false)
                        adapter = adapter1
                        addItemDecoration(DividerItemDecoration(this@BusStationsFragment.requireContext(), DividerItemDecoration.VERTICAL))
                    }
                }
            }
            override fun onFailure(call: Call<List<BusStationsResponse>>, t: Throwable) {
                Log.d("shows fail", "onFailure: " + t + call.toString())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}