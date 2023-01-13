package pora.data.proj

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import pora.data.proj.networking.ApiModule
import java.io.IOException
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
    private var fileName: String = ""

    private var recorder: MediaRecorder? = null

    private var player: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)


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
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    //    private fun uploadFile(fileUri: Uri) {
    //        // create RequestBody instance from file
    //        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
    //            bitmap
    //        }
    //        val requestFile: RequestBody = RequestBody.create(
    //            MediaType.parse(getContentResolver().getType(BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
    //                bitmap
    //            })),
    //            file
    //        )
    //
    //        // MultipartBody.Part is used to send also the actual file name
    //        val body: Part = createFormData.createFormData("picture", file.name, requestFile)
    //
    //        // add another part within the multipart request
    //        val descriptionString = "hello, this is description speaking"
    //        val description = RequestBody.create(
    //            MultipartBody.FORM, descriptionString
    //        )
    //
    //        // finally, execute the request
    //        val call: Call<ResponseBody> = service.upload(description, body)
    //        call.enqueue(object : Callback<ResponseBody?> {
    //            override fun onResponse(
    //                call: Call<ResponseBody?>,
    //                response: Response<ResponseBody?>
    //            ) {
    //                Log.v("Upload", "success")
    //            }
    //
    //            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
    //                Log.e("Upload error:", t.message!!)
    //            }
    //        })
    //    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentVoiceRecordingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
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
        ApiModule.retrofit.busStations().enqueue(object : Callback<List<BusStationsResponse>> {
            override fun onResponse(call: Call<List<BusStationsResponse>>, response: Response<List<BusStationsResponse>>) {
                if (response.isSuccessful) {
                    Log.d("shows success", "on success: " + response.body()?.get(1))
                }
            }

            override fun onFailure(call: Call<List<BusStationsResponse>>, t: Throwable) {
                Log.d("shows fail", "onFailure: " + t + call.toString())
            }

        })
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }
}