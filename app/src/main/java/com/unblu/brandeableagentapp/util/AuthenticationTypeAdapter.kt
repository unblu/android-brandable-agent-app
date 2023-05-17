package com.unblu.brandeableagentapp.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.unblu.brandeableagentapp.model.AuthenticationType

class AuthenticationTypeAdapter : TypeAdapter<AuthenticationType>() {
    override fun write(out: JsonWriter?, value: AuthenticationType?) {
        out?.beginObject()
        out?.name("name")
        out?.value(value?.name)
        out?.endObject()
    }

    override fun read(`in`: JsonReader?): AuthenticationType {
        `in`?.beginObject()
        var authType: AuthenticationType = AuthenticationType.Direct
        while (`in`?.hasNext() == true) {
            when (`in`.nextName()) {
                "name" -> {
                    val name = `in`.nextString()
                    authType = when (name) {
                        AuthenticationType.Direct.name -> AuthenticationType.Direct
                        AuthenticationType.OAuth.name -> AuthenticationType.OAuth
                        AuthenticationType.WebProxy.name -> AuthenticationType.WebProxy
                        else -> throw IllegalArgumentException("Unknown AuthenticationType: $name")
                    }
                }
            }
        }
        `in`?.endObject()
        return authType
    }
}