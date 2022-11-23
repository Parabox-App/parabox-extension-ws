package com.ojhdtapp.parabox.extension.ws.domain.service

import android.content.pm.PackageManager
import android.os.Message
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    companion object {
        var connectionType = 0
    }

    private var wsClient: WebSocketClient? = null

    fun onConvertCodeJson(json: String) {
        val jsonObj = JSONObject(json)
        when (jsonObj.getInt("code")) {
            4000 -> {
                updateServiceState(
                    ParaboxKey.STATE_RUNNING,
                    "WebSocket 已连接"
                )
            }
            1000 -> {
                updateServiceState(
                    ParaboxKey.STATE_ERROR,
                    "密钥验证失败，连接已关闭"
                )
//                            throw Exception("密钥验证失败，连接已关闭")
            }
            1001 -> {
                updateServiceState(
                    ParaboxKey.STATE_ERROR,
                    "密钥验证超时"
                )
//                            throw Exception("密钥验证超时")
            }
            else -> {
                updateServiceState(
                    ParaboxKey.STATE_ERROR,
                    "接收到未知错误"
                )
            }
        }
    }

    fun onConvertMessageJson(json: String) {
        val dto = gson.fromJson(json, ReceiveMessageDto::class.java)
        dto?.also {
            lifecycleScope.launch(Dispatchers.IO) {
                receiveMessage(
                    dto.copy(
                        pluginConnection = dto.pluginConnection.copy(
                            connectionType = ConnService.connectionType
                        )
                    )
                ) {
                    Log.d("parabox", "Message data payload: $it")
                }
            }
        }
    }

    fun onConvertStatusJson(json: String) {

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
        return false
    }

    override fun onRefreshMessage() {

    }

    override suspend fun onSendMessage(dto: SendMessageDto): Boolean {
        Log.d("parabox", "Sending message: $dto")
        if (wsClient == null) {
            return false
        } else {
            return dto.contents.map {
                when (it) {
                    is PlainText -> {
                        JsonUtil.wrapJson(
                            type = "message",
                            data = gson.toJson(
                                dto.copy(
                                    contents = listOf(it)
                                ), SendMessageDto::class.java
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
            }.all { it }
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
                updateServiceState(ParaboxKey.STATE_ERROR, "请先设置服务器地址")
                return@launch
            }
            if (wsToken == null) {
                updateServiceState(ParaboxKey.STATE_ERROR, "请先设置连接密钥")
                return@launch
            }
            updateServiceState(
                ParaboxKey.STATE_LOADING,
                "尝试启动 WebSocket 服务"
            )
            wsClient = object : WebSocketClient(URI.create("ws://$wsUrl/")) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    updateServiceState(
                        ParaboxKey.STATE_LOADING,
                        "WebSocket 连接有效，尝试密钥认证"
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
                                    "WebSocket 服务返回了未知类型的数据"
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
                    updateServiceState(
                        ParaboxKey.STATE_ERROR,
                        "WebSocket 连接已断开（${code}）"
                    )
                }

                override fun onError(ex: Exception?) {
                    ex?.printStackTrace()
                    updateServiceState(
                        ParaboxKey.STATE_ERROR,
                        ex?.message ?: "WebSocket 连接发生错误"
                    )
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