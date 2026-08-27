package br.com.tiago.obramaster.platform

enum class Platform {
    ANDROID, IOS, WEB, DESKTOP;

    companion object {
        val ALL: Set<Platform> = entries.toSet()
    }
}

expect val currentPlatform: Platform
