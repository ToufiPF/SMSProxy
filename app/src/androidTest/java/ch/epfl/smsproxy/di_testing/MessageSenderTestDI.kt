package ch.epfl.smsproxy.di_testing

import ch.epfl.smsproxy.di.MessageSenderDI
import ch.epfl.smsproxy.sms.MessageSender
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton


@Suppress("unused")
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [MessageSenderDI::class])
object MessageSenderTestDI {

    // Note: must be singleton for testing,
    // so that injected instances are the same in the test and tested class
    @Provides
    @Singleton
    fun provideMockMessageSender(): MessageSender = mockk(relaxUnitFun = true)
}
