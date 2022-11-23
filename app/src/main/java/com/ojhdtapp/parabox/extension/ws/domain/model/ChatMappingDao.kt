package com.ojhdtapp.parabox.extension.ws.domain.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatMappingDao {
    @Insert
    fun insertChatMapping(chatMapping: ChatMapping): Long

    @Query("SELECT * FROM chatmapping WHERE slaveOriginUid = :slaveOriginUid LIMIT 1")
    fun getChatMappingBySlaveOriginUid(slaveOriginUid: String): ChatMapping?

    @Query("SELECT * FROM chatmapping WHERE id = :id LIMIT 1")
    fun getChatMappingById(id: Long): ChatMapping?
}