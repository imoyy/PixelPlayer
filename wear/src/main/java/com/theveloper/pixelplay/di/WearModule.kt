package com.theveloper.pixelplay.di

import android.app.Application
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WearModule {

    @Provides
    @Singleton
    fun provideDataClient(application: Application): DataClient =
        Wearable.getDataClient(application)

    @Provides
    @Singleton
    fun provideMessageClient(application: Application): MessageClient =
        Wearable.getMessageClient(application)

    @Provides
    @Singleton
    fun provideNodeClient(application: Application): NodeClient =
        Wearable.getNodeClient(application)
}
