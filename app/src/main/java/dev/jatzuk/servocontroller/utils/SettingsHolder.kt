package dev.jatzuk.servocontroller.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.jatzuk.servocontroller.R
import dev.jatzuk.servocontroller.connection.ConnectionType
import dev.jatzuk.servocontroller.other.SHARED_PREFERENCES_NAMEE
import dev.jatzuk.servocontroller.other.ServoTexture
import javax.inject.Inject

data class SettingsHolder @Inject constructor(
    private val applicationContext: Context
) {

    private val sharedPreferences = applicationContext.getSharedPreferences(
        SHARED_PREFERENCES_NAMEE,
        Context.MODE_PRIVATE
    )

    private val _connectionType = MutableLiveData(loadConnectionTypeFromSharedPreferences())
    val connectionType: LiveData<ConnectionType> get() = _connectionType

    private val _servosCount = MutableLiveData(loadServosCountFromSharedPreferences())
    val servosCount: LiveData<Int> get() = _servosCount

    private val _servosTexture = MutableLiveData(loadServosTextureTypeFromSharedPreferences())
    val servosTexture: LiveData<ServoTexture> get() = _servosTexture

    fun applyChanges(
        connectionType: ConnectionType = this.connectionType.value!!,
        servosCount: Int = this.servosCount.value!!,
        servosTexture: ServoTexture = this.servosTexture.value!!
    ) {
        sharedPreferences.edit().run {
            if (connectionType != _connectionType.value!!) {
                putString(
                    applicationContext.getString(R.string.key_connection_type),
                    connectionType.name
                )
            }
            if (servosCount != _servosCount.value) {
                putInt(applicationContext.getString(R.string.key_servos_count), servosCount)
            }
            if (servosTexture != _servosTexture.value!!) {
                putString(
                    applicationContext.getString(R.string.key_servos_textures),
                    servosTexture.name
                )
            }
            apply()
        }

        _connectionType.value = connectionType
        _servosCount.value = servosCount
        _servosTexture.value = servosTexture
    }

    private fun loadConnectionTypeFromSharedPreferences(): ConnectionType {
        val stringType = sharedPreferences.getString(
            applicationContext.getString(R.string.key_connection_type),
            ConnectionType.BLUETOOTH.name
        ) ?: ConnectionType.BLUETOOTH.name
        return if (stringType.startsWith('B')) ConnectionType.BLUETOOTH else ConnectionType.WIFI
    }

    private fun loadServosCountFromSharedPreferences() =
        sharedPreferences.getInt(applicationContext.getString(R.string.key_servos_count), 1)

    private fun loadServosTextureTypeFromSharedPreferences(): ServoTexture {
        val stringType = sharedPreferences.getString(
            applicationContext.getString(R.string.key_servos_textures),
            ServoTexture.TEXTURE.name
        ) ?: ServoTexture.TEXTURE.name
        return if (stringType.startsWith('T')) ServoTexture.TEXTURE else ServoTexture.SEEKBAR
    }
}
