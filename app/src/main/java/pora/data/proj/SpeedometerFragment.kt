package pora.data.proj

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import pora.data.proj.databinding.FragmentSpeedometerBinding
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import kotlin.math.min
import kotlin.random.Random
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import pora.data.proj.models.SpeedometerRequest
import pora.data.proj.networking.ApiModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val TAG: String = "SpeedometerFragment"

class SpeedometerFragment : Fragment(), SensorEventListener
{

    private var _binding: FragmentSpeedometerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    private var simulating = false
    private var isSlow = false
    private var isFast = false

    private val minutesToSeconds = 60
    private var minutes = 5
    private var uuid = UUID.randomUUID().toString()
    private var startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    private var endTime = startTime + minutes * minutesToSeconds

    private var samples = mutableListOf<List<Float>>()

    private lateinit var sensorManager: SensorManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        super.onCreate(savedInstanceState)

        _binding = FragmentSpeedometerBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)

        setUpSensor()

        binding.simulateSwitch.setOnClickListener {
            simulating = binding.simulateSwitch.isChecked
            isSlow = !binding.speedSwitch.isChecked
            isFast = binding.speedSwitch.isChecked
        }

        binding.speedSwitch.setOnClickListener {
            isSlow = !binding.speedSwitch.isChecked
            isFast = binding.speedSwitch.isChecked

            if (binding.speedSwitch.isChecked)
                binding.speedSwitch.text = "No Traffic"
            else
                binding.speedSwitch.text = "Traffic"
        }

        binding.timeSlider.addOnChangeListener { _, _, _ ->
            binding.timerText.text = "Send every ${binding.timeSlider.value.toInt()} minutes"

            minutes = binding.timeSlider.value.toInt()
            endTime = startTime + minutes * minutesToSeconds
        }

        return binding.root
    }

    private fun setUpSensor()
    {
        sensorManager = activity!!.getSystemService(SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this, it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?)
    {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER)
        {
            if (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) >= endTime)
            {
                samples.add(listOf(event.values[0], event.values[1], event.values[2]))

                if (samples.size == 500)
                {
                    val timeZone: TimeZone = TimeZone.getTimeZone("CET")
                    val calendar: Calendar = Calendar.getInstance(timeZone)
                    val dateTime = calendar.time.toString()

                    val request = SpeedometerRequest(samples, uuid, dateTime)
                    ApiModule.retrofit.uploadSpeedometerData(request).enqueue(object : Callback<ResponseBody> {
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
                            val text = "upload failed!"
                            val duration = Toast.LENGTH_SHORT
                            val toast = Toast.makeText(requireContext(), text, duration)
                            toast.show()
                        }

                    })
                    Log.i(TAG, "SENDING DATA")
                    samples.clear()

                    startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                    endTime = startTime + minutes * minutesToSeconds
                }
            }

            if (!simulating)
            {
                if (event.values[0] > 40 || event.values[1] > 40 || event.values[2] > 100)
                    Toast.makeText(context, "Extreme event happened, call help!", Toast.LENGTH_LONG).show()


                binding.xText.text =
                    event.values[0].toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
                binding.yText.text =
                    event.values[1].toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
                binding.zText.text =
                    event.values[2].toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
            }
            else if (isSlow)
            {
                val randomX = Random.nextInt(1, 3) * Random.nextFloat() - 1
                val randomY = Random.nextInt(1, 3) * Random.nextFloat() - 1
                val randomZ = 10 + Random.nextInt(1, 3) * Random.nextFloat()

                binding.xText.text =
                    randomX.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
                binding.yText.text =
                    randomY.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
                binding.zText.text =
                    randomZ.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
            }
            else if (isFast)
            {
                val randomX = Random.nextInt(3, 11) * Random.nextFloat() - 5
                val randomY = Random.nextInt(3, 11) * Random.nextFloat() - 5
                val randomZ = 10 + Random.nextInt(1, 5) * Random.nextFloat()

                binding.xText.text =
                    randomX.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
                binding.yText.text =
                    randomY.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
                binding.zText.text =
                    randomZ.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
    {
        return
    }

    override fun onDestroyView()
    {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}