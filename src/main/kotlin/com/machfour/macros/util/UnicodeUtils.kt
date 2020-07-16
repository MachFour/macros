package com.machfour.macros.util

import java.lang.Character.UnicodeBlock
import java.util.*

object UnicodeUtils {
    // https://stackoverflow.com/a/41982074/
    private val cjkBlocks: Collection<UnicodeBlock> = setOf(
            UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            , UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            , UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            , UnicodeBlock.CJK_COMPATIBILITY
            , UnicodeBlock.CJK_COMPATIBILITY_FORMS
            , UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            , UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT
            , UnicodeBlock.CJK_RADICALS_SUPPLEMENT
            , UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            , UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS
            , UnicodeBlock.KANGXI_RADICALS
            , UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS
            , UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C // Android API 19
            , UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D // Android API 19
            , UnicodeBlock.CJK_STROKES // Android API 19
            , UnicodeBlock.ENCLOSED_IDEOGRAPHIC_SUPPLEMENT // Android API 19
    )

    // Japanese / Kana
    private val kanaBlocks: Collection<UnicodeBlock> = setOf(
            UnicodeBlock.HIRAGANA
            , UnicodeBlock.KATAKANA
            , UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
    )

    // Japanese / Fullwidth characters.
    private val halfWidthFullWidthBlock = UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS

    // Korean / Hangul
    // https://gist.github.com/TheFinestArtist/2fd1b4aa1d4824fcbaef
    private val hangulBlocks: Collection<UnicodeBlock> = HashSet(Arrays.asList(
            UnicodeBlock.HANGUL_JAMO
            , UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
            , UnicodeBlock.HANGUL_SYLLABLES
    ))

    @JvmStatic
    fun isFullWidthChar(codepoint: Int): Boolean {
        if (UnicodeBlock.of(codepoint) !== halfWidthFullWidthBlock) {
            return false
        }
        // https://en.wikipedia.org/wiki/Halfwidth_and_Fullwidth_Forms_(Unicode_block) 
        // U+FF61 - U+FF9F (inclusive) and U+FFE8 - U+FFEE/F (inclusive) are halfwidth
        val hWidth1Start = 0xFF61
        val hWidth1End = 0xFF9F
        val hWidth2Start = 0xFFE8
        val hWidth2End = 0xFFEE
        val inGroup1 = codepoint in hWidth1Start..hWidth1End
        val inGroup2 = codepoint in hWidth2Start..hWidth2End

        // every character not in this group is full width
        return !inGroup1 && !inGroup2
    }

    // Returns true if the character with the given codepoint is printed as a
    // double (or full) width character in most monospace fonts.
    @JvmStatic
    fun isDoubleSpaceChar(codepoint: Int): Boolean {
        val block = UnicodeBlock.of(codepoint)
        return (kanaBlocks.contains(block)
                || cjkBlocks.contains(block)
                || hangulBlocks.contains(block)
                || isFullWidthChar(codepoint))
    }

    // length function for nullable string
    private fun length(s: String?): Int {
        return s?.length ?: 0
    }

    // Computes the number of monospace characters taken by string s,
    // taking into account double-space characters from Chinese, Japanese and Korean.
    fun displayLength(s: String): Int {
        val len = length(s)
        return if (len == 0) {
            0 // felt like optimising, might delete later
        } else {
            // Assume characters have width 1 by default, just add 1 for double spaced
            len + countDoubleWidthChars(s)
        }
    }

    @JvmStatic
    fun countDoubleWidthChars(s: String): Int {
        var count = 0
        // if s is null, length(s) is 0, so the loop will be skipped
        for (i in length(s) - 1 downTo 0) {
            if (isDoubleSpaceChar(s.codePointAt(i))) {
                count++
            }
        }
        return count
    }
}