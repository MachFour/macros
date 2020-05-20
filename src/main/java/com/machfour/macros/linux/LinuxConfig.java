package com.machfour.macros.linux;

import com.machfour.macros.core.MacrosConfig;
import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;
import com.machfour.macros.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class LinuxConfig implements MacrosConfig {

    private static final String PROJECT_DIR = "/home/max/devel/macros-java";
    private static final String LIB_DIR = FileUtils.joinPath(PROJECT_DIR, "lib");

    public static final String SQLITE_NATIVE_LIB_DIR = LIB_DIR;
    public static final String SQLITE_NATIVE_LIB_NAME = "libsqlitejdbc-3.30.1.so";

    private static final String SQL_DIR = FileUtils.joinPath(PROJECT_DIR, "src/sql");
    private static final String DATA_DIR = "/home/max/devel/macros/macros-data";
    private static final String DB_DIR = PROJECT_DIR;


    private static final String INIT_SQL_NAME = "macros-db-create.sql";
    private static final String TRIG_SQL_NAME = "macros-db-triggers.sql";
    private static final String DATA_SQL_NAME = "macros-initial-data.sql";

    private static final String FOOD_CSV_NAME = "foods.csv";
    private static final String SERVING_CSV_NAME = "servings.csv";
    private static final String RECIPE_CSV_NAME = "recipes.csv";
    private static final String INGREDIENTS_CSV_NAME = "ingredients.csv";

    private static final String DB_NAME = "macros.sqlite";

    private static final String FOOD_CSV_PATH = FileUtils.joinPath(DATA_DIR, FOOD_CSV_NAME);
    private static final String SERVING_CSV_PATH = FileUtils.joinPath(DATA_DIR, SERVING_CSV_NAME);
    private static final String RECIPE_CSV_PATH = FileUtils.joinPath(DATA_DIR, RECIPE_CSV_NAME);
    private static final String INGREDIENTS_CSV_PATH = FileUtils.joinPath(DATA_DIR, INGREDIENTS_CSV_NAME);

    private static final String DEFAULT_CSV_OUTPUT_DIR = FileUtils.joinPath(PROJECT_DIR,"csv-out");

    static final File INIT_SQL = new File(FileUtils.joinPath(SQL_DIR, INIT_SQL_NAME));
    static final File TRIG_SQL = new File(FileUtils.joinPath(SQL_DIR, TRIG_SQL_NAME));
    static final File DATA_SQL = new File(FileUtils.joinPath(SQL_DIR, DATA_SQL_NAME));

    private static String DB_LOCATION = FileUtils.joinPath(DB_DIR, DB_NAME);

    private static final String PROGNAME = "macros";

    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    public PrintStream outStream() {
        return System.out;
    }

    public PrintStream errStream() {
        return System.err;
    }

    public BufferedReader inputReader() {
        return inputReader;
    }

    public String getInitSqlName() {
        return INIT_SQL_NAME;
    }

    public String getTrigSqlName() {
        return TRIG_SQL_NAME;
    }

    public String getDataSqlName() {
        return DATA_SQL_NAME;
    }

    public String getDbName() {
        return DB_NAME;
    }

    public String getFoodCsvPath() {
        return FOOD_CSV_PATH;
    }

    public String getServingCsvPath() {
        return SERVING_CSV_PATH;
    }

    public String getRecipeCsvPath() {
        return RECIPE_CSV_PATH;
    }

    public String getIngredientsCsvPath() {
        return INGREDIENTS_CSV_PATH;
    }

    public String getDefaultCsvOutputDir() {
        return DEFAULT_CSV_OUTPUT_DIR;
    }

    public String getDbLocation() {
        return DB_LOCATION;
    }

    public void setDbLocation(String dbLocation) {
        DB_LOCATION = dbLocation;
    }

    public String getProgramName() {
        return PROGNAME;
    }

    public MacrosDataSource getDataSourceInstance() {
        return getDatabaseInstance();
    }

    public MacrosDatabase getDatabaseInstance() {
        return LinuxDatabase.getInstance(getDbLocation());
    }
}
