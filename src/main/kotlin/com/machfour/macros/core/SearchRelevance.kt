package com.machfour.macros.core

// Search relevance can take any integer value; these are some preset ones
enum class SearchRelevance(val value: Int) {
    EVERYTHING(Int.MIN_VALUE),
    HIDDEN(-20),
    EXCLUDE_HIDDEN(0),
    INBUILT(20),
    USER(40),
    STARRED(60);

}


/*
 Things to care about:
 - user-hidden or not
 - inbuilt or not
 - amount of information present? (servings?)
 */