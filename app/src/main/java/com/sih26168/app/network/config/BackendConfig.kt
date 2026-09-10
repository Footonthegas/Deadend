package com.sih26168.app.network.config

import com.sih26168.app.BuildConfig
import java.net.URI

object BackendConfig {
    val restBaseUrl: String = BuildConfig.BACKEND_BASE_URL

    // TODO: confirm with backend team — Socket.IO namespace path
    const val socketNamespace: String = "/nav"

    fun socketUrl(): String {
        val cleanBase = restBaseUrl.removeSuffix("/")
        val uri = URI(cleanBase)
        val portPart = if (uri.port == -1) "" else ":${uri.port}"
        return "${uri.scheme}://$uri.host$portPart$socketNamespace"
    }
}
