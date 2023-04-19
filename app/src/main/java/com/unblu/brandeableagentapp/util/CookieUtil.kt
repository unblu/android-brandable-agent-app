package com.unblu.brandeableagentapp.util

import android.webkit.CookieManager
import android.webkit.ValueCallback


object CookieUtil {
    fun clear(onRemoved : (Boolean)-> Unit){
        CookieManager.getInstance().removeAllCookies(onRemoved)
    }
}
