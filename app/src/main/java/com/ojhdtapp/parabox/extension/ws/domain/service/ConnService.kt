package com.ojhdtapp.parabox.extension.ws.domain.service

import android.content.pm.PackageManager
import android.os.Message
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.ojhdtapp.parabox.extension.ws.R
import com.ojhdtapp.paraboxdevelopmentkit.connector.ParaboxKey
import com.ojhdtapp.paraboxdevelopmentkit.connector.ParaboxMetadata
import com.ojhdtapp.paraboxdevelopmentkit.connector.ParaboxService
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.ReceiveMessageDto
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.SendMessageDto
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.PlainText
import com.ojhdtapp.parabox.extension.ws.core.util.DataStoreKeys
import com.ojhdtapp.parabox.extension.ws.core.util.JsonUtil
import com.ojhdtapp.parabox.extension.ws.core.util.NotificationUtil
import com.ojhdtapp.parabox.extension.ws.core.util.dataStore
import com.ojhdtapp.parabox.extension.ws.data.AppDatabase
import com.ojhdtapp.parabox.extension.ws.remote.dto.EFBReceiveMessageDto
import com.ojhdtapp.parabox.extension.ws.remote.dto.EFBSendMessageDto
import com.ojhdtapp.parabox.extension.ws.remote.message_content.toEFBMessageContent
import com.ojhdtapp.paraboxdevelopmentkit.connector.ParaboxResult
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Audio
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.File
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.Image
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class ConnService : ParaboxService() {

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appDatabase: AppDatabase

    companion object {
        var connectionType = 0
        var reconnectTimes = 0
        var reconnectJob: Job? = null
    }

    private var wsClient: WebSocketClient? = null

    fun onConvertCodeJson(json: String) {
        val jsonObj = JSONObject(json)
        when (jsonObj.getInt("code")) {
            4000 -> {
                reconnectTimes = 0
                updateServiceState(
                    ParaboxKey.STATE_RUNNING,
                    getString(R.string.ws_connected)
                )
            }
            1000 -> {
                reconnectTimes = 0
                updateServiceState(
                    ParaboxKey.STATE_ERROR,
                    getString(R.string.ws_error_wrong_token)
                )
//                            throw Exception("密钥验证失败，连接已关闭")
            }
            1001 -> {
                updateServiceState(
                    ParaboxKey.STATE_ERROR,
                    getString(R.string.ws_error_timeout)
                )
//                            throw Exception("密钥验证超时")
                tryReconnecting()
            }
            else -> {
                updateServiceState(
                    ParaboxKey.STATE_ERROR,
                    getString(R.string.ws_error_unknown)
                )
                tryReconnecting()
            }
        }
    }

    fun onConvertMessageJson(json: String) {
        val dto = gson.fromJson(json, EFBReceiveMessageDto::class.java)
        dto?.also {
            lifecycleScope.launch(Dispatchers.IO) {
                val chatMappingId = appDatabase.chatMappingDao()
                    .getChatMappingBySlaveOriginUid(dto.slaveOriginUid)?.id
                    ?: appDatabase.chatMappingDao().insertChatMapping(dto.getChatMapping())
                receiveMessage(
                    dto.toReceiveMessageDto(baseContext, chatMappingId)
                ) {
                    Log.d("parabox", "Message data payload: $it")
                    if (it is ParaboxResult.Success) {
                        try {
                            JsonUtil.wrapJson(
                                type = "response",
                                data = "\"${dto.slaveMsgId}\""
                            ).let {
                                wsClient?.run {
                                    send(it)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun onConvertStatusJson(json: String) {

    }

    fun tryReconnecting() {
        reconnectJob?.cancel()
        reconnectJob = lifecycleScope.launch {
            val isAutoReconnectEnabled =
                dataStore.data.first()[DataStoreKeys.AUTO_RECONNECT] ?: true
            if (isAutoReconnectEnabled) {
                if (reconnectTimes < 3) {
                    delay(4000)
                    reconnectTimes++;
                    updateServiceState(
                        ParaboxKey.STATE_LOADING,
                        getString(R.string.reconnecting)
                    )
                    onStartParabox()
                }
            }
        }
    }

    override fun customHandleMessage(msg: Message, metadata: ParaboxMetadata) {
        when (msg.what) {

        }
    }

    override fun onMainAppLaunch() {
        // Auto Login
        if (getServiceState() == ParaboxKey.STATE_STOP) {
            lifecycleScope.launch {
                val isAutoLoginEnabled =
                    dataStore.data.first()[DataStoreKeys.AUTO_LOGIN] ?: false
                if (isAutoLoginEnabled) {
                    onStartParabox()
                }
            }
        }
    }

    override suspend fun onRecallMessage(messageId: Long): Boolean {
        return JsonUtil.wrapJson(
            type = "recall",
            data = "$messageId"
        ).let {
            try {
                wsClient?.run {
                    send(it)
                    true
                } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override fun onRefreshMessage() {
        JsonUtil.wrapJson(
            type = "refresh",
            data = "\"${System.currentTimeMillis()}\""
        ).let {
            try {
                wsClient?.run {
                    send(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun onSendMessage(dto: SendMessageDto): Boolean {
        Log.d("parabox", "Sending message: $dto")
        if (wsClient == null) {
            return false
        } else {
            return withContext(Dispatchers.IO) {
                try {
                    dto.contents.map {
                        val chatMapping =
                            appDatabase.chatMappingDao().getChatMappingById(dto.pluginConnection.id)
                        if (chatMapping == null) false
                        else {
                            val efbDto = EFBSendMessageDto(
                                content = it.toEFBMessageContent(baseContext),
                                timestamp = dto.timestamp,
                                slaveOriginUid = chatMapping.slaveOriginUid,
                                slaveMsgId = chatMapping.slaveMsgId,
                                messageId = dto.messageId!!
                            )
                            when (it) {
                                is PlainText, is Image, is Audio, is File -> {
                                    JsonUtil.wrapJson(
                                        type = "message",
                                        data = gson.toJson(
                                            efbDto, EFBSendMessageDto::class.java
                                        )
                                    ).let {
                                        wsClient?.run {
                                            send(it)
                                            true
                                        } ?: false
                                    }
                                }
                                else -> {
                                    false
                                }
                            }
                        }
                    }.all { it }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
    }

    override fun onStartParabox() {
        lifecycleScope.launch {
            // Foreground Service
            val isForegroundServiceEnabled =
                dataStore.data.first()[DataStoreKeys.FOREGROUND_SERVICE] ?: false
            if (isForegroundServiceEnabled) {
                NotificationUtil.startForegroundService(this@ConnService)
            }
            val wsUrl = dataStore.data.first()[DataStoreKeys.WS_URL]
            val wsToken = dataStore.data.first()[DataStoreKeys.WS_TOKEN]
            if (wsUrl == null) {
                updateServiceState(ParaboxKey.STATE_ERROR, getString(R.string.error_empty_host))
                return@launch
            }
            if (wsToken == null) {
                updateServiceState(ParaboxKey.STATE_ERROR, getString(R.string.error_empty_token))
                return@launch
            }
            updateServiceState(
                ParaboxKey.STATE_LOADING,
                getString(R.string.ws_launching)
            )
            wsClient = object : WebSocketClient(URI.create("ws://$wsUrl/")) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    updateServiceState(
                        ParaboxKey.STATE_LOADING,
                        getString(R.string.ws_authenticating)
                    )
                    send(wsToken)
                }

                override fun onMessage(message: String?) {
                    message?.let {
                        Log.d("parabox", it)
                        val jsonObj = JSONObject(it)
                        when (jsonObj.getString("type")) {
                            "code" -> onConvertCodeJson(jsonObj.getString("data"))
                            "message" -> onConvertMessageJson(jsonObj.getString("data"))
                            "status" -> onConvertStatusJson(jsonObj.getString("data"))
                            "else" -> {
                                updateServiceState(
                                    ParaboxKey.STATE_ERROR,
                                    getString(R.string.ws_error_unknown_type)
                                )
                            }
                        }
                    }
                }

                override fun onMessage(bytes: ByteBuffer?) {
                    Log.d("parabox", "receive bytearray")
                    super.onMessage(bytes)
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    when (code) {
                        1000 -> {

                        }
                        else -> {
                            updateServiceState(
                                ParaboxKey.STATE_ERROR,
                                getString(R.string.ws_disconnected, code)
                            )
                            tryReconnecting()
                        }
                    }
                }

                override fun onError(ex: Exception?) {
                    ex?.printStackTrace()
                    updateServiceState(
                        ParaboxKey.STATE_ERROR,
                        ex?.message ?: getString(R.string.ws_error)
                    )
                    tryReconnecting()
                }
            }.also {
                Log.d("parabox", it.uri.toString())
                it.connect()
            }

        }
    }

    override fun onStateUpdate(state: Int, message: String?) {

    }

    override fun onStopParabox() {
        NotificationUtil.stopForegroundService(this)
        updateServiceState(ParaboxKey.STATE_STOP)
        wsClient?.close()
    }

    override fun onCreate() {
        connectionType = packageManager.getApplicationInfo(
            this@ConnService.packageName,
            PackageManager.GET_META_DATA
        ).metaData.getInt("connection_type")
        super.onCreate()
    }

}