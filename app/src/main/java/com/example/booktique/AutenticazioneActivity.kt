package com.example.booktique

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController

class AutenticazioneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autenticazione)

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

    }
}