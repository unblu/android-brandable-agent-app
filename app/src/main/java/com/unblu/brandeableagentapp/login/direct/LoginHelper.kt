/*
 * Copyright (c) 2020. Unblu inc., Basel, Switzerland
 * All rights reserved.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.unblu.brandeableagentapp.login.direct

import android.net.Uri
import android.util.Log
import android.webkit.ValueCallback
import androidx.compose.ui.text.toLowerCase
import androidx.core.util.Pair
import com.unblu.brandeableagentapp.login.LoginFailedException
import com.unblu.sdk.core.configuration.UnbluClientConfiguration
import com.unblu.sdk.core.configuration.UnbluCookie
import com.unblu.sdk.core.internal.utils.CookieUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object LoginHelper {
    private const val  UNBLU_REST_PATH: String = "/rest/v3"
    private val TAG = LoginHelper::class.java.simpleName
    fun login(
        configuration: UnbluClientConfiguration,
        loginUsername: String?,
        loginPassword: String?,
        success: ValueCallback<Set<UnbluCookie>?>,
        failure: ValueCallback<String?>
    ) {
        if (loginUsername == null || loginPassword == null) {
            success.onReceiveValue(null)
            return
        }
        val username: String = loginUsername
        val password: String = loginPassword
        val loginConnection = arrayOf<HttpURLConnection?>(null)
        Single.fromCallable {
            val loginUrl = URL(
                Uri.parse(configuration.unbluBaseUrl + configuration.entryPath + UNBLU_REST_PATH + "/authenticator/login")
                    .toString()
            )
            loginConnection[0] = loginUrl.openConnection() as HttpURLConnection
            loginConnection[0]!!.requestMethod = "POST"
            loginConnection[0]!!.setRequestProperty("Content-Type", "application/json")
            loginConnection[0]!!.doOutput = true
            val obj: JSONObject = generateCredentials(username, password)
                ?: return@fromCallable Pair<Any?, LoginFailedException?>(
                    null,
                    LoginFailedException("Failed to encode authentication login credentials")
                )
            val cookies: Set<UnbluCookie> = CookieUtils.enhanceCookiesWithDeviceIdentifier(
                configuration.preferencesStorage,
                configuration.customCookies
            )
            if (cookies.isNotEmpty()) {
                for (cookie in cookies) {
                    val cookieHeader: String = CookieUtils.createCookieString(
                        cookie,
                        CookieUtils.checkIfSecureUrl(loginUrl.toString())
                    )
                    loginConnection[0]!!.setRequestProperty("Cookie", cookieHeader)
                }
            }
            val os = loginConnection[0]!!.outputStream
            val input: ByteArray = obj.toString().toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
            val responseCode = loginConnection[0]!!.responseCode
            val responseBody = loginConnection[0]!!.inputStream.readToString()
            handleLoginResponse(loginConnection[0], responseCode, responseBody)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result: Pair<out Any?, LoginFailedException?> ->
                    if (result.first == null) {
                        Log.e(
                            TAG,
                            "Failed to login with the given credentials!",
                            result.second
                        )
                        failure.onReceiveValue(result.second!!.message)
                    } else {
                        success.onReceiveValue(result.first as Set<UnbluCookie>?)
                    }
                    if (loginConnection[0] != null) {
                        loginConnection[0]!!.disconnect()
                    }
                }
            ) { throwable: Throwable ->
                throwable.message?.let { Log.e(TAG, it, throwable) }
                failure.onReceiveValue("Could not Login: ${throwable.message}")
            }
    }

    private fun generateCredentials(username: String, password: String): JSONObject? {
        return try {
            val obj = JSONObject()
            obj.put("username", username)
            obj.put("password", password)
            obj
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to encode authentication login credentials", e)
            null
        }
    }

    @Throws(IOException::class)
    private fun handleLoginResponse(
        loginConnection: HttpURLConnection?,
        responseCode: Int,
        responseBody: String
    ): Pair<Set<UnbluCookie>?, LoginFailedException?> {
        return if (responseCode == 200) {
            if (!responseBody.toBooleanStrict()) {
                return Pair<Set<UnbluCookie>?, LoginFailedException?>(
                    null,
                    LoginFailedException("Invalid credentials for login")
                )
            }
            val headers = loginConnection!!.headerFields
            val authCookies: Set<UnbluCookie> =
                getAuthenticationCookies(headers)
            if (authCookies.isEmpty()) {
                Pair<Set<UnbluCookie>?, LoginFailedException?>(
                    null,
                    LoginFailedException("Did not receive valid cookies after authentication. Headers: $headers")
                )
            } else Pair<Set<UnbluCookie>?, LoginFailedException?>(
                authCookies,
                null
            )
        } else {
            Pair<Set<UnbluCookie>?, LoginFailedException?>(
                null,
                LoginFailedException("Failed to login. StatusCode: " + loginConnection!!.responseCode + "; StatusText: " + loginConnection.responseMessage + "; Details: " + responseBody)
            )
        }
    }

    private fun getAuthenticationCookies(headers: Map<String, List<String>>): Set<UnbluCookie> {
        val authCookies: MutableMap<String, String> = HashMap()
        for ((key, value) in headers) {
            if (key?.lowercase() == "set-cookie") {
                for (setCookieValue in value) {
                    val responseCookieList = HttpCookie.parse(setCookieValue)
                    for (responseCookie in responseCookieList) {
                        authCookies[responseCookie.name] = responseCookie.value
                    }
                }
            }
        }
        return HashSet<UnbluCookie>(UnbluCookie.from(authCookies))
    }


    fun InputStream.readToString(charset: Charset = Charsets.UTF_8): String {
        return this.bufferedReader(charset).use { it.readText() }
    }
}