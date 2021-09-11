package com.machfour.macros.util

/*
 * Function to do string joining of arbitrary iterable objects, using a custom string function
 * Similar functionality exists as java.util.StringJoiner but not all of this functionality is
 * available in Android.
 */
fun <E> stringJoin(
    iterable: Iterable<E>,
    sep: String = "",
    itemPrefix: String = "",
    itemSuffix: String = "",
    copies: Int = 1,
    stringFunc: (E) -> String = { it.toString() },
) = stringJoin(
    iterator = iterable.iterator(),
    sep = sep,
    itemPrefix = itemPrefix,
    itemSuffix = itemSuffix,
    copies = copies,
    stringFunc = stringFunc
)

fun <E> stringJoin(
    iterator: Iterator<E>,
    sep: String = "",
    itemPrefix: String = "",
    itemSuffix: String = "",
    copies: Int = 1,
    stringFunc: (E) -> String = { it.toString() },
): String {
    if (!iterator.hasNext()) {
        return ""
    }
    val joined = StringBuilder()
    // there will be one last separator string at the end but we'll remove it
    while (iterator.hasNext()) {
        val next = stringFunc(iterator.next())
        for (i in 1..copies) {
            joined.append(itemPrefix)
            joined.append(next)
            joined.append(itemSuffix)
            joined.append(sep)
        }
    }
    // remove the last sep
    if (sep.isNotEmpty()) {
        joined.delete(joined.length - sep.length, joined.length - 1)
    }
    return joined.toString()
}