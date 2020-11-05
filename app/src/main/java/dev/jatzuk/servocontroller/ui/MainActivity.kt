package dev.jatzuk.servocontroller.ui

import android.os.Bundle
import android.util.TypedValue
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
import dev.jatzuk.servocontroller.mvp.BaseView
import dev.jatzuk.servocontroller.utils.CommonAdsInitializer
import dev.jatzuk.servocontroller.utils.EnableConnectionHardwareContract
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), HomeFragment.NavigationMenuAvailabilitySwitcher {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    lateinit var enableHardwareContractLauncher: ActivityResultLauncher<Int>
        private set

    @Inject
    lateinit var adsInitializer: CommonAdsInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
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
                val fragment =
                    supportFragmentManager.fragments.last().childFragmentManager.fragments.last()
                if (!result) {
                    (fragment as BaseView<*>).showToast(
                        getString(R.string.enable_connection_module_info)
                    )
                }
            }

        val tv = TypedValue()
        toastOffset = if (theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
            val height = TypedValue.complexToDimensionPixelOffset(tv.data, resources.displayMetrics)
            height + height / 2
        } else {
            0
        }
    }

    private fun initializeAds() {
        adsInitializer.initializeAds(this)
        binding.drawerContent.adBanner.adView.loadAd(adsInitializer.provideAdRequest())
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun updateNavigationMenuItemAvailability(isVisible: Boolean, index: Int) {
        binding.navView.menu.getItem(index).isEnabled = isVisible
    }

    companion object {

        var toastOffset = 0
            private set
    }
}
