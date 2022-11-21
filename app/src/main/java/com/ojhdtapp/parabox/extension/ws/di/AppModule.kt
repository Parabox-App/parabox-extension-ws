package com.ojhdtapp.parabox.extension.ws.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ojhdtapp.parabox.extension.ws.remote.ReceiveMessageDtoJsonDeserializer
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.ReceiveMessageDto
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
//    @Provides
//    @Singleton
//    fun provideDatabase(app: Application): AppDatabase =
//        Room.databaseBuilder(
//            app, AppDatabase::class.java, "main_db"
//        ).build()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(
            ReceiveMessageDto::class.java,
            ReceiveMessageDtoJsonDeserializer()
        )
        .create()
}