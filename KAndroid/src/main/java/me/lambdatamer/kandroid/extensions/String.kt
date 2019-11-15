package me.lambdatamer.kandroid.extensions

fun String?.normalize() = this?.trim()?.takeIf(String::isNotBlank)