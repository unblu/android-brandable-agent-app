package com.unblu.brandeableagentapp.login.direct.util

fun validateUsername(user : String?) : Boolean{
    return  user?.isNotEmpty() ?: false
}

fun validatePassword(password : String?) : Boolean{
    return  password?.isNotEmpty() ?: false
}