package io.github.chrislo27.rhre3.sfxdb


enum class SoundFileExtensions(val fileExt: String) {
    OGG("ogg"), WAV("wav"), MP3("mp3");

    companion object {
        val VALUES = SoundFileExtensions.values().toList()
    }
}