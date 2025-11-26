package com.ifpr.androidapptemplate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ifpr.androidapptemplate.databinding.ActivityMainBinding
import com.ifpr.androidapptemplate.utils.NotificationHelper
import com.google.android.material.appbar.MaterialToolbar // Certifique-se de que esta importação está correta!

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // **CORREÇÃO AQUI:** Chama a função usando o objeto NotificationHelper
        NotificationHelper.createNotificationChannel(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. OBTEM A REFERÊNCIA DA TOOLBAR QUE ADICIONAMOS NO XML
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        // 2. CONFIGURA A TOOLBAR COMO A ACTIONBAR DA ACTIVITY (RESOLVE O ERRO FATAL)
        setSupportActionBar(toolbar)

        val navView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_notifications,
                R.id.navigation_profile,
                R.id.nav_tarefas
            )
        )

        // Esta chamada agora funcionará porque setSupportActionBar foi chamado antes.
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}