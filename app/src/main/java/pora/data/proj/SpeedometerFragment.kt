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
import androidx.fragment.app.Fragment
import pora.data.proj.databinding.FragmentSpeedometerBinding
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.min
import kotlin.random.Random

val TAG: String = "SpeedometerFragment"

class SpeedometerFragment : Fragment(), SensorEventListener
{

    private var _binding: FragmentSpeedometerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    private var simulating = false
    private var isSlow = false
    private var isFast = false

    private var minutes = 5
    private var startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    private var endTime = startTime + minutes * 60

    private var samples = mutableListOf<FloatArray>()

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
            endTime = startTime + minutes * 60
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
                samples.add(event.values)

                if (samples.size == 500)
                {
                    Log.i(TAG, "SENDING DATA")
                    samples.clear()

                    startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                    endTime = startTime + minutes * 60
                }
            }

            if (!simulating)
            {
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