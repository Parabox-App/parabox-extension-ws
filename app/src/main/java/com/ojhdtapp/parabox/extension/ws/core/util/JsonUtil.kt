package com.ojhdtapp.parabox.extension.ws.core.util

object JsonUtil {
    fun wrapJson(type: String, data: String): String{
        return "{\"type\":\"$type\",\"data\":$data}"
    }
}