package pora.data.proj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pora.data.proj.networking.ApiModule
import pora.data.proj.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        ApiModule.initRetrofit(this)
        setContentView(binding.root)
    }
}