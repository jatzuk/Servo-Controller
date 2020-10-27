package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.databinding.ActivityMainBinding
import dev.jatzuk.servocontroller.utils.EnableConnectionHardwareContract

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    lateinit var enableHardwareContractLauncher: ActivityResultLauncher<Int>
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeAds()

        val radius = resources.getDimension(R.dimen.side_nav_bar_corner_radius)
        val navViewBackground = (binding.navView.background as MaterialShapeDrawable)
        navViewBackground.shapeAppearanceModel = navViewBackground.shapeAppearanceModel.toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, radius)
            .setBottomRightCorner(CornerFamily.ROUNDED, radius)
            .build()

        setSupportActionBar(binding.drawerContent.toolbar)

        navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        enableHardwareContractLauncher =
            registerForActivityResult(EnableConnectionHardwareContract()) { result ->
                if (result) {
                    when (val fragment = supportFragmentManager.fragments[0]) {
                        is HomeFragment -> fragment.presenter.onRequestEnableHardwareReceived()
                        is DevicesFragment -> fragment.presenter.onRequestEnableHardwareReceived()
                    }
                } else Toast.makeText(
                    this,
                    getString(R.string.enable_connection_module_info),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun initializeAds() {
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
