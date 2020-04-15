package com.machfour.macros.linux;

import com.machfour.macros.util.FileUtils;

import java.io.File;

public class Config {

    private Config() {}

    private static final String SQL_DIR = "/home/max/devel/macros-java/src/sql";
    private static final String DATA_DIR = "/home/max/devel/macros/macros-data";
    private static final String DB_DIR = "/home/max/devel/macros-java";


    public static final String INIT_SQL_NAME = "macros-db-create.sql";
    public static final String TRIG_SQL_NAME = "macros-db-triggers.sql";
    public static final String DATA_SQL_NAME = "macros-initial-data.sql";

    private static final String FOOD_CSV_NAME = "foods.csv";
    private static final String SERVING_CSV_NAME = "servings.csv";
    private static final String RECIPE_CSV_NAME = "recipes.csv";
    private static final String INGREDIENTS_CSV_NAME = "ingredients.csv";

    public static final String DB_NAME = "macros.sqlite";

    static final File INIT_SQL = new File(FileUtils.joinPath(SQL_DIR, INIT_SQL_NAME));
    static final File TRIG_SQL = new File(FileUtils.joinPath(SQL_DIR, TRIG_SQL_NAME));
    static final File DATA_SQL = new File(FileUtils.joinPath(SQL_DIR, DATA_SQL_NAME));

    public static final String FOOD_CSV_PATH = FileUtils.joinPath(DATA_DIR, FOOD_CSV_NAME);
    public static final String SERVING_CSV_PATH = FileUtils.joinPath(DATA_DIR, SERVING_CSV_NAME);
    public static final String RECIPE_CSV_PATH = FileUtils.joinPath(DATA_DIR, RECIPE_CSV_NAME);
    public static final String INGREDIENTS_CSV_PATH = FileUtils.joinPath(DATA_DIR, INGREDIENTS_CSV_NAME);

    public static final String CSV_OUTPUT_DIR = "/home/max/devel/macros-java/csv-out";

    public static String DB_LOCATION = FileUtils.joinPath(DB_DIR, DB_NAME);

    public static final String PROGNAME = "macros";

}
