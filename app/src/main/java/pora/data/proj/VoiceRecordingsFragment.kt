package pora.data.proj

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import pora.data.proj.networking.ApiModule
import java.io.IOException
import java.util.Calendar
import java.util.TimeZone
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import pora.data.proj.databinding.FragmentVoiceRecordingsBinding
import pora.data.proj.models.BusStationsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class VoiceRecordingsFragment : Fragment() {

    private var _binding: FragmentVoiceRecordingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private var fileName: String = "somefile"

    private var recorder: MediaRecorder? = null


    private var player: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private var currentLocation: Location = Location("dummyprovider")




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                Log.e("path", fileName)
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
            start()
        }
    }

    private fun stopRecording() {
        binding.uploadButton.isEnabled = true
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentVoiceRecordingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        currentLocation.latitude = 0.0;
        currentLocation.longitude = 0.0;
        val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )

        activityResultLauncher.launch(appPerms)
        getLocation()
        // Record to the external cache directory for visibility
        fileName = "${requireContext().externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        requireContext().externalCacheDir?.absolutePath?.let { Log.e("path", it) }
        ActivityCompat.requestPermissions(this.requireActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        var mStartPlaying = true

        binding.playButton.setOnClickListener {
            onPlay(mStartPlaying)
            binding.playButton.text = when (mStartPlaying) {
                true -> "Stop playing"
                false -> "Start playing"
            }
            mStartPlaying = !mStartPlaying
        }

        var mStartRecording = true

        binding.recordButton.setOnClickListener {
            onRecord(mStartRecording)
            binding.recordButton.text = when (mStartRecording) {
                true -> "Stop recording"
                false -> "Start recording"
            }
            mStartRecording = !mStartRecording
        }
        binding.uploadButton.setOnClickListener {
            uploadFile()
        }
        return binding.root
    }

    private fun uploadFile() {
        val file = File(fileName)
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part = MultipartBody.Part.createFormData("voice_recording", file.name, requestFile)

        val name: RequestBody = file.name.toRequestBody()

        val timeZone: TimeZone = TimeZone.getTimeZone("CET")
        val calendar: Calendar = Calendar.getInstance(timeZone)
        val dateTime: RequestBody = calendar.time.toString().toRequestBody()
        val latitude: RequestBody = currentLocation.latitude.toString().toRequestBody()
        val longitude: RequestBody = currentLocation.longitude.toString().toRequestBody()

        ApiModule.retrofit.uploadVoiceRecording(name, latitude, longitude, dateTime,body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("shows success", "on success: " + response.body())
                    val text = "upload successful!"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(requireContext(), text, duration)
                    toast.show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("shows fail", "onFailure: " + t + call.toString())
            }

        })
    }

    private fun getLocation()
    {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        )
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation.latitude = location.latitude
                        currentLocation.longitude = location.longitude
                    }

                }

    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    init {
        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) allAreGranted = allAreGranted && b
        }
    }
}