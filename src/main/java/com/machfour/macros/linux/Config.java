package com.machfour.macros.linux;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private Config() {}

    static final Path INIT_SQL = Paths.get("/home/max/devel/macros/macros-db-create.sql");
    static final Path TRIG_SQL = Paths.get("/home/max/devel/macros/macros-db-triggers.sql");
    static final Path DATA_SQL = Paths.get("/home/max/devel/macros/macros-initial-data.sql");

    public static final String DB_LOCATION = "/home/max/devel/macros-java/macros.sqlite";
    public static final String FOOD_CSV_FILENAME = "/home/max/devel/macros/macros-data/foods.csv";
    public static final String SERVING_CSV_FILENAME = "/home/max/devel/macros/macros-data/servings.csv";
    public static final String CSV_OUTPUT_DIR = "/home/max/devel/macros-java/csv-out";
}
