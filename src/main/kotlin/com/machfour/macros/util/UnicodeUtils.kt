package com.machfour.macros.util

import java.lang.Character.UnicodeBlock

// https://stackoverflow.com/a/41982074/
private val cjkBlocks = setOf(
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
private val hangulBlocks: Collection<UnicodeBlock> = setOf(
    UnicodeBlock.HANGUL_JAMO
    , UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
    , UnicodeBlock.HANGUL_SYLLABLES
)

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
fun Int.isDoublespaceCodePoint(): Boolean {
    val block = UnicodeBlock.of(this)
    return (kanaBlocks.contains(block)
            || cjkBlocks.contains(block)
            || hangulBlocks.contains(block)
            || isFullWidthChar(this))
}

fun String.countDoubleWidthChars() = indices.count { codePointAt(it).isDoublespaceCodePoint() }

// Computes the number of monospace characters taken by string s,
// taking into account double-space characters from Chinese, Japanese and Korean.
// Assumes characters have width 1 by default, so just add 1 for each double spaced character

fun String.displayLength() = if (isEmpty()) 0 else length + countDoubleWidthChars()


// Fullwidth characters take up two monospace character widths in a terminal. This function reduces
// the padding space applied by the string format function so that the result appears in the terminal
// with the correct width. Essentially, it reduces 'width' by the number of fullwidth characters in s
fun formatUnicodeString(s: String, width: Int, leftAlign: Boolean) : String {
    val align = if (leftAlign) "-" else ""

    // displayed length of text appears to be length() + numDoubleWidthChars characters long.
    // Equivalently, we can reduce width by this amount, to get the printing right
    val formatWidth = maxOf(width - s.countDoubleWidthChars(), 0)


    // prevent long strings from overrunning the width:
    // replace "This is a really long string"
    // with    "This is a really lo.."
    val truncatedString = if (s.length > formatWidth) {
        val newWidth = maxOf(formatWidth - 2, 0)
        // TODO this may reduce by too much, with double width chars
        s.substring(0, newWidth - 2) + ".."
    } else {
        s
    }
    //String widthStr = String.valueOf(width);
    return "%${align}${formatWidth}s".format(truncatedString)
}