package com.machfour.macros.core

// Search relevance can take any integer value; these are some preset ones
enum class SearchRelevance(val value: Int) {
    HIDDEN(-20),
    ALL(0),
    INBUILT(20),
    USER(40),
    STARRED(60);

}