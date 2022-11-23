package com.ojhdtapp.parabox.extension.ws.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ojhdtapp.parabox.extension.ws.domain.model.ChatMapping
import com.ojhdtapp.parabox.extension.ws.domain.model.ChatMappingDao

@Database(entities = [ChatMapping::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMappingDao() : ChatMappingDao
}