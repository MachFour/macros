package com.machfour.macros.core

// Search relevance can take any integer value; these are some preset ones
enum class SearchRelevance(val value: Int) {
    EVERYTHING(Int.MIN_VALUE),
    HIDDEN(-20),
    MIN_VISIBLE(0),
    INBUILT(20),
    USER(40),
    STARRED(60);

}