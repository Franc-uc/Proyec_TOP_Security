package com.example.proyec_top

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.proyec_top.repository.AppDatabase
import com.example.proyec_top.repository.TopSecurityRepository
import com.example.proyec_top.ui.MainAppScreen
import com.example.proyec_top.viewmodel.TopSecurityViewModel
import com.example.proyec_top.viewmodel.TopSecurityViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.obtenerInstancia(applicationContext)
        val repository = TopSecurityRepository(database)
        val viewModel = ViewModelProvider(
            this,
            TopSecurityViewModelFactory(repository)
        )[TopSecurityViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen(viewModel)
                }
            }
        }
    }
}
