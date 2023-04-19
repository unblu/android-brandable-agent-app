package com.unblu.brandeableagentapp.login.sso

import android.annotation.SuppressLint
import android.net.Uri
import net.openid.appauth.connectivity.ConnectionBuilder
import java.net.HttpURLConnection
import java.net.URL

class NoCacheConnectionBuilder private constructor() : ConnectionBuilder {
    @SuppressLint("NewApi")
    override fun openConnection(uri: Uri): HttpURLConnection {
        val url = URL(uri.toString())
        val conn = url.openConnection()
        if (conn is HttpURLConnection) {
            conn.instanceFollowRedirects = false
            conn.useCaches = false
            conn.setRequestProperty("Pragma", "no-cache")
            conn.setRequestProperty("Cache-Control", "no-store")
        }
        return conn as HttpURLConnection
    }

    companion object {
        val INSTANCE = NoCacheConnectionBuilder()
    }
}