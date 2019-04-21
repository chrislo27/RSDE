package io.github.chrislo27.rhre3.sfxdb


enum class PlayalongInput(val id: String, val deprecatedIDs: List<String> = listOf(), val isTouchScreen: Boolean = false) {

    BUTTON_A("A"),
    BUTTON_B("B"),
    BUTTON_DPAD("+"),
    BUTTON_A_OR_DPAD("A_+"),
    BUTTON_DPAD_UP("+_up"),
    BUTTON_DPAD_DOWN("+_down"),
    BUTTON_DPAD_LEFT("+_left"),
    BUTTON_DPAD_RIGHT("+_right"),

    TOUCH_TAP("touch_tap", isTouchScreen = true),
    TOUCH_FLICK("touch_flick", isTouchScreen = true),
    TOUCH_RELEASE("touch_release", isTouchScreen = true),
    TOUCH_QUICK_TAP("touch_quick_tap", isTouchScreen = true),
    TOUCH_SLIDE("touch_slide", isTouchScreen = true);

    companion object {
        val VALUES: List<PlayalongInput> = values().toList()
        private val ID_MAP: Map<String, PlayalongInput> = VALUES.flatMap { pi -> listOf(pi.id to pi) + pi.deprecatedIDs.map { i -> i to pi } }.toMap()
        private val INDICES_MAP: Map<PlayalongInput, Int> = VALUES.associateWith(VALUES::indexOf)
        private val REVERSE_INDICES_MAP: Map<PlayalongInput, Int> = VALUES.associateWith { VALUES.size - 1 - VALUES.indexOf(it) }

        operator fun get(id: String): PlayalongInput? = ID_MAP[id]

        fun indexOf(playalongInput: PlayalongInput?): Int = if (playalongInput == null) -1 else INDICES_MAP.getOrDefault(playalongInput, -1)
        fun reverseIndexOf(playalongInput: PlayalongInput?): Int = if (playalongInput == null) -1 else REVERSE_INDICES_MAP.getOrDefault(playalongInput, -1)
    }
}

enum class PlayalongMethod(val instantaneous: Boolean, val isRelease: Boolean) {

    PRESS(true, false),
    PRESS_AND_HOLD(false, false),
    LONG_PRESS(false, false),
    RELEASE_AND_HOLD(false, true),
    RELEASE(true, true); // RELEASE is for Quick Tap

    companion object {
        val VALUES = values().toList()
        private val ID_MAP: Map<String, PlayalongMethod> = VALUES.associateBy { it.name }

        operator fun get(id: String): PlayalongMethod? = ID_MAP[id]
    }

}
