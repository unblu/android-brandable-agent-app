package com.unblu.brandeableagentapp.login.proxy

import android.util.Log
import android.webkit.*
import com.unblu.brandeableagentapp.data.AppConfiguration
import com.unblu.sdk.core.configuration.UnbluCookie
import java.net.HttpCookie

class ProxyWebViewClient(private val onCookieReceived: (Set<UnbluCookie>?) -> Unit) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        var doOverride = false
        request?.apply {
            Log.i("ProxyWebViewClient", "shouldOverrideUrlLoading : $url")
            val urlString = url.toString()
            if (urlString.contains(AppConfiguration.webAuthProxyServerAddress + AppConfiguration.entryPath + "/desk")) {
                doOverride = true
                view?.stopLoading()

                // Get cookies and pass them to the webViewInterface's onComplete method
                val cookieManager = CookieManager.getInstance()
                val cookiesString = cookieManager.getCookie(AppConfiguration.webAuthProxyServerAddress)
                Log.i("ProxyWebViewClient", "cookiesString : $cookiesString")
                val cookies = parseCookies(cookiesString)
                onCookieReceived(UnbluCookie.from(cookies))
            }
        }
        return doOverride // Allow the WebView to handle the navigation
    }

    private fun parseCookies(cookiesString: String): List<HttpCookie> {
        val cookies = mutableListOf<HttpCookie>()
        val keyValuePairs = cookiesString.split(";")

        keyValuePairs.forEach { keyValue ->
            val keyValueSplit = keyValue.split("=", limit = 2)

            if (keyValueSplit.size == 2) {
                val cookieName = keyValueSplit[0].trim()
                val cookieValue = keyValueSplit[1].trim().trim { it == '"' }
                Log.w("ProxyWebViewClient", "keyValueSplit : $cookieName=$cookieValue")
                // Create a new Cookie object and add it to the list
                val cookie = HttpCookie(cookieName, cookieValue)
                cookies.add(cookie)
            }
        }
        return cookies
    }
}