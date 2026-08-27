package br.com.tiago.obramaster.platform

enum class Platform { ANDROID, IOS, WEB, DESKTOP }

expect val currentPlatform: Platform
