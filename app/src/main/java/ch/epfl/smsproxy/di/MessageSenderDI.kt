package ch.epfl.smsproxy.di

import ch.epfl.smsproxy.sms.MessageSender
import ch.epfl.smsproxy.sms.MessageSenderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageSenderDI {

    @Binds
    abstract fun bindMessageSender(sender: MessageSenderImpl): MessageSender

//    fun provideMessageSender(@ApplicationContext context: Context): MessageSender =
//        MessageSenderImpl(context)
}
