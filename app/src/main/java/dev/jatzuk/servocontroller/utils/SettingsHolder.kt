package dev.jatzuk.servocontroller.utils

import android.content.Context
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAME
import dev.jatzuk.servocontroller.other.ServoTexture
import javax.inject.Inject

data class SettingsHolder @Inject constructor(
    private val applicationContext: Context
) {

    private val sharedPreferences = applicationContext.getSharedPreferences(
        SHARED_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private var _connectionType: ConnectionType = loadConnectionTypeFromSharedPreferences()
    val connectionType get() = _connectionType

    private var _servosCount: Int = loadServosCountFromSharedPreferences()
    val servosCount get() = _servosCount

    private var _servosTexture: ServoTexture = loadServosTextureTypeFromSharedPreferences()
    val texture get() = _servosTexture

    private var _shouldKeepScreenOn: Boolean = loadShouldKeepScreenOnValueFromSharedPreferences()
    val shouldKeepScreenOn get() = _shouldKeepScreenOn

    fun applyChanges(
        connectionType: ConnectionType = this.connectionType,
        servosCount: Int = this.servosCount,
        servosTexture: ServoTexture = this.texture,
        shouldKeepScreenOn: Boolean = this.shouldKeepScreenOn
    ) {
        sharedPreferences.edit().run {
            if (connectionType != _connectionType) {
                putString(
                    applicationContext.getString(R.string.key_connection_type),
                    connectionType.name
                )
            }
            if (servosCount != _servosCount) {
                putInt(applicationContext.getString(R.string.key_servos_count), servosCount)
            }
            if (servosTexture != _servosTexture) {
                putString(
                    applicationContext.getString(R.string.key_servos_textures),
                    servosTexture.name
                )
            }
            if (shouldKeepScreenOn != _shouldKeepScreenOn) {
                putBoolean(
                    applicationContext.getString(R.string.key_should_keep_screen_on),
                    shouldKeepScreenOn
                )
            }
            apply()
        }

        _connectionType = connectionType
        _servosCount = servosCount
        _servosTexture = servosTexture
        _shouldKeepScreenOn = shouldKeepScreenOn
    }

    private fun loadConnectionTypeFromSharedPreferences(): ConnectionType {
        val stringType = sharedPreferences.getString(
            applicationContext.getString(R.string.key_connection_type),
            ConnectionType.BLUETOOTH.name
        ) ?: ConnectionType.BLUETOOTH.name
        val type = if (stringType.startsWith('B')) ConnectionType.BLUETOOTH else ConnectionType.WIFI
        _connectionType = type
        return type
    }

    private fun loadServosCountFromSharedPreferences() =
        sharedPreferences.getInt(applicationContext.getString(R.string.key_servos_count), 1)
            .also { _servosCount = it }

    private fun loadServosTextureTypeFromSharedPreferences(): ServoTexture {
        val stringType = sharedPreferences.getString(
            applicationContext.getString(R.string.key_servos_textures),
            ServoTexture.TEXTURE.name
        ) ?: ServoTexture.TEXTURE.name
        val type = if (stringType.startsWith('T')) ServoTexture.TEXTURE else ServoTexture.SEEKBAR
        _servosTexture = type
        return type
    }

    private fun loadShouldKeepScreenOnValueFromSharedPreferences() =
        sharedPreferences.getBoolean(
            applicationContext.getString(R.string.key_should_keep_screen_on),
            false
        ).also { _shouldKeepScreenOn = it }
}
