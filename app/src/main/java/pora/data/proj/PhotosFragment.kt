package pora.data.proj

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import pora.data.proj.databinding.FragmentPhotosBinding
import pora.data.proj.networking.ApiModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val CAMERA_REQUEST_CODE = 101
private const val REQUEST_IMAGE_CAPTURE = 1

class PhotosFragment : Fragment() {

    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentPhotoPath: String
    private val args by navArgs<PhotosFragmentArgs>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("data", Context.MODE_PRIVATE)
        setupPermissions()
        binding.pictureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.name.text = args.name
        binding.location.text = "x: " + args.lat.toString() + " || y: " + args.long.toString()

        return binding.root
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this.requireContext(),
            android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this.requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this.requireContext(), "You need camera permissiion to be able to use this app!", Toast.LENGTH_SHORT).show()
                } else {
                }
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e("as", "fuuuckkk")
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this.requireContext(),
                "com.example.android.fileprovider",
                it
            )
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(captureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic()
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = this.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = binding.takenPicture.width
        val targetH: Int = binding.takenPicture.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            binding.takenPicture.setImageBitmap(bitmap)
        }
        uploadFile()
    }

    private fun uploadFile() {
        val file = File(currentPhotoPath)
        val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val body: MultipartBody.Part = createFormData("image", file.name, requestFile)


        val name: RequestBody = file.name.toRequestBody()
        val id: RequestBody = args.id.toRequestBody()
        val lat: RequestBody = args.long.toString().toRequestBody()
        val long: RequestBody = args.lat.toString().toRequestBody()

        val timeZone: TimeZone = TimeZone.getTimeZone("CET")
        val calendar: Calendar = Calendar.getInstance(timeZone)
        val dateTime: RequestBody = calendar.time.toString().toRequestBody()


        ApiModule.retrofit.uploadImage(name, id, lat, long, dateTime, body).enqueue(object : Callback<ResponseBody> {
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
}
