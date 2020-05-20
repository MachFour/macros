package com.machfour.macros.core;

import com.machfour.macros.storage.MacrosDataSource;
import com.machfour.macros.storage.MacrosDatabase;

import java.io.BufferedReader;
import java.io.PrintStream;

public interface MacrosConfig {
    // All of these methods return not null

    PrintStream outStream();

    PrintStream errStream();

    BufferedReader inputReader();

    String getInitSqlName();

    String getTrigSqlName();

    String getDataSqlName();

    String getDbName();

    String getFoodCsvPath();

    String getServingCsvPath();

    String getRecipeCsvPath();

    String getIngredientsCsvPath();

    String getDefaultCsvOutputDir();

    String getDbLocation();

    void setDbLocation(String dbLocation);

    String getProgramName();

    // for not-usual operations on the database
    MacrosDatabase getDatabaseInstance();
    MacrosDataSource getDataSourceInstance();
}
