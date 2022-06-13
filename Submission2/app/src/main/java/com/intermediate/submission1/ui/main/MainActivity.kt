package com.intermediate.submission1.ui.main

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.intermediate.submission1.R
import com.intermediate.submission1.data.preferences.SettingPreferences
import com.intermediate.submission1.databinding.ActivityMainBinding


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            SettingPreferences.getInstance(dataStore), this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNavigationView.setupWithNavController(navController)

        viewModel.user.observe(this) { currUser ->
            val graphInflater = navController.navInflater
            val navGraph = graphInflater.inflate(R.navigation.main_navigation)
            val startingPoint =
                if (currUser.token.isNotEmpty()) (R.id.homeFragment) else (R.id.loginFragment)
            navGraph.setStartDestination(startingPoint)
            navController.graph = navGraph
        }

        viewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun setActionBarSubtitle(subtitle: String = "") {
        supportActionBar?.subtitle = subtitle
    }

    fun removeBackButtonActionBar() {
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    fun showBottomNavigationBar(isShow: Boolean = true){
        binding.bottomNavigationView.visibility = if(isShow) View.VISIBLE else View.GONE
    }
}