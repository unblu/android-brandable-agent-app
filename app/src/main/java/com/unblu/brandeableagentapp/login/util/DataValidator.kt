package com.unblu.brandeableagentapp.login.util

fun validateUsername(user : String?) : Boolean{
    return  user?.isNotEmpty() ?: false
}

fun validatePassword(password : String?) : Boolean{
    return  password?.isNotEmpty() ?: false
}