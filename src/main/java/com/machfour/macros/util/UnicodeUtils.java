
package com.machfour.macros.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Collection;

import static java.lang.Character.PARAGRAPH_SEPARATOR;
import static java.lang.Character.UnicodeBlock;

public class UnicodeUtils {

    // https://stackoverflow.com/a/41982074/
    public static final Collection<UnicodeBlock> cjkBlocks = new HashSet<>(Arrays.asList(
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
        , UnicodeBlock.CJK_STROKES                        // Android API 19
        , UnicodeBlock.ENCLOSED_IDEOGRAPHIC_SUPPLEMENT    // Android API 19
    ));


    // Japanese / Kana
    public static final Collection<UnicodeBlock> kanaBlocks = new HashSet<>(Arrays.asList(
          UnicodeBlock.HIRAGANA
        , UnicodeBlock.KATAKANA
        , UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
    ));

    // Japanese / Fullwidth characters.
    public static final UnicodeBlock halfWidthFullWidthBlock =
        UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;

    // Korean / Hangul
    // https://gist.github.com/TheFinestArtist/2fd1b4aa1d4824fcbaef
    public static final Collection<UnicodeBlock> hangulBlocks = new HashSet<>(Arrays.asList(
              UnicodeBlock.HANGUL_JAMO
            , UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
            , UnicodeBlock.HANGUL_SYLLABLES
    ));

    public static boolean isFullWidthChar(int codepoint) {
        if (UnicodeBlock.of(codepoint) != halfWidthFullWidthBlock) {
            return false;
        }
        // https://en.wikipedia.org/wiki/Halfwidth_and_Fullwidth_Forms_(Unicode_block) 
        // U+FF61 - U+FF9F (inclusive) and U+FFE8 - U+FFEE/F (inclusive) are halfwidth
        final int hWidth1Start = 0xFF61;
        final int hWidth1End   = 0xFF9F;
        final int hWidth2Start = 0xFFE8;
        final int hWidth2End   = 0xFFEE;
        boolean inGroup1 = hWidth1Start <= codepoint && codepoint <= hWidth1End;
        boolean inGroup2 = hWidth2Start <= codepoint && codepoint <= hWidth2End;

        // every character not in this group is full width
        return !inGroup1 && !inGroup2;
    }

    // Returns true if the character with the given codepoint is printed as a
    // double (or full) width character in most monospace fonts.
    public static boolean isDoubleSpaceChar(int codepoint) {
        UnicodeBlock block = UnicodeBlock.of(codepoint);
        return kanaBlocks.contains(block)
            || cjkBlocks.contains(block)
            || hangulBlocks.contains(block)
            || isFullWidthChar(codepoint);
    }

    // length function for nullable string
    private static int length(String s) {
        return s == null ? 0 : s.length();
    }

    // Computes the number of monospace characters taken by string s,
    // taking into account double-space characters from Chinese, Japanese and Korean.
    public static int displayLength(String s) {
        int len = length(s);
        if (len == 0) {
            // felt like optimising, might delete later
            return 0;
        }
        // Assume characters have width 1 by default,
        // just add 1 for double spaced
        return len + countDoubleWidthChars(s);
    }

    public static int countDoubleWidthChars(String s) {
        int count = 0;
        // if s is null, length(s) is 0, so the loop will be skipped
        for (int i = length(s) - 1; i >= 0; i--) {
            if (isDoubleSpaceChar(s.codePointAt(i))) {
                count++;
            }
        }
        return count;
    }
}
