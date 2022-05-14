package com.machfour.macros.core

// Search relevance can take any integer value; these are some preset ones
class SearchRelevance private constructor(val value: Int): Comparable<SearchRelevance> {
    companion object {
        val EVERYTHING = SearchRelevance(Int.MIN_VALUE)
        val HIDDEN = SearchRelevance(-200)
        val EXCLUDE_HIDDEN = SearchRelevance(-100)
        val UNSET = SearchRelevance(0)
        val INBUILT = SearchRelevance(200)
        val USER = SearchRelevance(400)
        val STARRED = SearchRelevance(600)

        fun fromValue(value: Int) = SearchRelevance(value)
    }

    override fun compareTo(other: SearchRelevance): Int = value.compareTo(other.value)

}


/*
 Things to care about:
 - user-hidden or not
 - inbuilt or not
 - amount of information present? (servings?)
 */