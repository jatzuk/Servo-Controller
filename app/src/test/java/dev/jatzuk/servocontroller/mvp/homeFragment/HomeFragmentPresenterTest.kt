package dev.jatzuk.servocontroller.mvp.homeFragment

import com.google.common.truth.Truth.assertThat
import dev.jatzuk.servocontroller.connection.BluetoothConnection
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class HomeFragmentPresenterTest {

    @MockK
    private lateinit var presenter: HomeFragmentPresenter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun presenter_isNotNull() {
        assertThat(presenter).isNotNull()
    }

    @Test
    fun connectionType_isSupported() {
        every {
            presenter.connection
        } returns BluetoothConnection()

        every {
            presenter.isConnectionTypeSupported()
        } returns true

        assertThat(presenter.isConnectionTypeSupported()).isTrue()
    }
}
