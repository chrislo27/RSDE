package io.github.chrislo27.rhre3.sfxdb


enum class SubtitleTypes(val type: String) {

    SUBTITLE("subtitle"), SONG_TITLE("songTitle"), SONG_ARTIST("songArtist");

    companion object {
        val VALUES = values().toList()
    }

}