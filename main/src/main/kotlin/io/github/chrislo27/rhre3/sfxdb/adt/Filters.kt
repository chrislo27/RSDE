@file:Suppress("EqualsOrHashCode")

package io.github.chrislo27.rhre3.sfxdb.adt


class CueFileExtFilter {
    override fun equals(other: Any?): Boolean {
        if (other is String) {
            return other.equals("ogg", ignoreCase = true) || other.isBlank()
        }

        return super.equals(other)
    }
}

class UseTimeStretchingFilter {
    override fun equals(other: Any?): Boolean {
        if (other is Boolean) {
            return other
        }

        return super.equals(other)
    }
}

class SubtextFilter {
    override fun equals(other: Any?): Boolean {
        if (other is String?) {
            return other.isNullOrEmpty()
        }

        return super.equals(other)
    }
}

class BeatFilter {
    override fun equals(other: Any?): Boolean {
        if (other is Float) {
            return other == Float.MIN_VALUE
        }

        return super.equals(other)
    }
}

class VolumeFilter {
    override fun equals(other: Any?): Boolean {
        if (other is Int) {
            return other == 100
        }

        return super.equals(other)
    }
}
