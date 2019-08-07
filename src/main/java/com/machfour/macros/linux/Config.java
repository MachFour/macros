package com.machfour.macros.linux;

import java.io.File;

public class Config {
    private Config() {}

    static final File INIT_SQL = new File("/home/max/devel/macros-java/src/sql/macros-db-create.sql");
    static final File TRIG_SQL = new File("/home/max/devel/macros-java/src/sql/macros-db-triggers.sql");
    static final File DATA_SQL = new File("/home/max/devel/macros-java/src/sql/macros-initial-data.sql");

    public static final String DEFAULT_DB_LOCATION = "/home/max/devel/macros-java/macros.sqlite";
    public static final String FOOD_CSV_FILENAME = "/home/max/devel/macros/macros-data/foods.csv";
    public static final String SERVING_CSV_FILENAME = "/home/max/devel/macros/macros-data/servings.csv";
    public static final String CSV_OUTPUT_DIR = "/home/max/devel/macros-java/csv-out";

    public static String DB_LOCATION = DEFAULT_DB_LOCATION;

}
