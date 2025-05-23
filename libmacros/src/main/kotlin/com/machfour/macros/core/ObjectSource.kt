package com.machfour.macros.core

/*
 * Describes how an object was created, which describes its state with respect to the database.
 * In particular, three conditions are relevant:
 * ID:  whether the object has an ID that has been (previously) assigned by the database
 * MOD: whether the object has been modified with respect to the database copy.
 * DB:  whether the object has been stored in the database, even if it's a previous copy.
 *      If so, then by definition the object has an ID. If not, then by definition the object is modified.
 */
enum class ObjectSource(private val niceName: String) {
    /*
    * Data loaded from DB. All data is truth.
    * ID: yes
    * MOD: no
    * DB: yes
    */
    DATABASE("database"),

    /*
     * From import of user-friendly spreadsheet, which does not have IDs.
     * Need to use secondary keys to determine whether the object is in the DB, as it may already be.
     * If foreign key data is not present in the data (e.g. IDs) then it is up to the caller to call AndroidDatabase.completeForeignKeys()
     * ID: no
     * MOD: ?
     * DB: ?
     */
    IMPORT("import"),

    /*
     * From restore of complete database dump, which does have IDs
     * As with IMPORT_FK_MISSING, the object may already be in the DB, but this time we can use the ID to check presence.
     * Alternatively, we could assume that the database has been cleared before restores, which removes this issue
     * ID: yes
     * MOD: ? (assume yes if DB = no also assumed)
     * DB: ? (could assume no)
     */
    RESTORE("restore"),

    /*
     * Object has just been created by user input (CLI or GUI), build by builder
     * ID: no
     * MOD: yes
     * DB: no
     */
    USER_NEW("user created"),
    /*
     * Object is a user-edited version of something already in the database, built by builder
     * ID: yes
     * MOD: yes
     * DB: yes
     */
    DB_EDIT("edit"),

    /*
     * Describes when objects are combined, e.g. nutrition data objects.
     * These objects shouldn't be saved in the DB, as obviously they can be computed from what is in there.
     * ID: no
     * MOD: yes
     * DB: no
     */
    COMPUTED("computed"),

    /*
     * Describes when an object exists as hardcoded into the business logic
     * For example, Quantity units, and their corresponding unit servings.
     * These objects are either already into the DB during initialization and should not otherwise be saved.
     * ID: no
     * MOD: no
     * DB: no
     */
    INBUILT("inbuilt"),

    // For testing; application code skips foreign key/ID match assertions for these objects.
    TEST("test"),

    // created from JSON deserialisation with default argument
    JSON("json"),
    ;

    override fun toString(): String {
        return niceName
    }
}