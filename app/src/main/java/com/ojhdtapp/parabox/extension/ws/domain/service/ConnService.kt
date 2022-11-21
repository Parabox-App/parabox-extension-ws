package com.ojhdtapp.parabox.extension.ws.domain.service

import android.app.Notification
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Icon
import android.os.IBinder
import android.os.Message
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.ojhdtapp.parabox.extension.ws.R
import com.ojhdtapp.paraboxdevelopmentkit.connector.ParaboxKey
import com.ojhdtapp.paraboxdevelopmentkit.connector.ParaboxMetadata
import com.ojhdtapp.paraboxdevelopmentkit.connector.ParaboxService
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.PluginConnection
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.Profile
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.ReceiveMessageDto
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.SendMessageDto
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.SendTargetType
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.PlainText
import com.ojhdtapp.paraboxdevelopmentkit.messagedto.message_content.getContentString
import com.ojhdtapp.parabox.extension.ws.core.util.DataStoreKeys
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil
import com.ojhdtapp.parabox.extension.ws.core.util.FileUtil.getCircledBitmap
import com.ojhdtapp.parabox.extension.ws.core.util.NotificationUtil
import com.ojhdtapp.parabox.extension.ws.core.util.dataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.inject.Inject

@AndroidEntryPoint
class ConnService : ParaboxService() {

    @Inject
    lateinit var gson: Gson

    companion object {
        var connectionType = 0
    }

    private var wsClient: WebSocketClient? = null

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
        if (wsClient == null) {
            return false
        } else {
            wsClient?.send(gson.toJson(dto, SendMessageDto::class.java))
            return true
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
            if (wsUrl == null) {
                updateServiceState(ParaboxKey.STATE_ERROR, "请先设置服务器地址")
                return@launch
            }
            updateServiceState(
                ParaboxKey.STATE_LOADING,
                "尝试启动 Websocket 服务"
            )
            wsClient = object : WebSocketClient(URI.create(wsUrl)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    updateServiceState(
                        ParaboxKey.STATE_RUNNING,
                        "Websocket 连接正常"
                    )
                }

                override fun onMessage(message: String?) {
                    TODO("Not yet implemented")
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    updateServiceState(
                        ParaboxKey.STATE_STOP,
                        "Websocket 连接已断开"
                    )
                }

                override fun onError(ex: Exception?) {
                    ex?.printStackTrace()
                    updateServiceState(
                        ParaboxKey.STATE_ERROR,
                        "Websocket 连接发生错误"
                    )
                }
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