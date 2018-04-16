package com.machfour.macros.storage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBSample {
    private static final Path INIT_SQL = Paths.get("/home/max/devel/macros/macros-db-create.sql");
    private static final Path TRIG_SQL = Paths.get("/home/max/devel/macros/macros-db-triggers.sql");
    private static final Path DATA_SQL = Paths.get("/home/max/devel/macros/macros-initial-data.sql");
    private static final PrintStream out = System.out;

    private static final String DB_LOCATION = "/home/max/devel/macros-java/sample.db";
    private static final Path DB_PATH = Paths.get(DB_LOCATION);

    private static String createStatements(List<String> sqlFileLines) {
        // steps: remove all comment lines, trim, join, split on semicolon
        List<String> trimmedAndDecommented = new ArrayList<>(sqlFileLines.size());
        for (String line : sqlFileLines) {
            int commentIndex = line.indexOf("--");
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            line = line.trim();
            line = line.replaceAll("\\s+", " ");
            if (line.length() != 0) {
                trimmedAndDecommented.add(line);
            }
        }
        return String.join(" ", trimmedAndDecommented);
    }

    private static boolean runStatements(Connection c, List<String> sqlStatements) {
        try (Statement s = c.createStatement()) {
            for (String sql : sqlStatements) {
                out.println("Executing statement: '" + sql + "'");
                s.executeUpdate(sql);
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }


    private static boolean removeDb() {
        try {
            if (Files.exists(DB_PATH)) {
                Files.delete(DB_PATH);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Could not delete database: " + e.getMessage());
            return false;
        }
    }

    private static boolean initDb() {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH.toAbsolutePath())) {
            List<String> initStatements = new ArrayList<>(3);
            initStatements.add(createStatements(Files.readAllLines(INIT_SQL)));
            initStatements.add(createStatements(Files.readAllLines(TRIG_SQL)));
            initStatements.add(createStatements(Files.readAllLines(DATA_SQL)));
            return runStatements(c, initStatements);
        } catch (IOException | SQLException e) {
            System.err.println(e);
            return false;
        }
    }

    public static void main(String[] args) {
        removeDb();
        boolean success = initDb();
        if (success) {
            out.println("Success!");
        } else {
            out.println("Failed");
        }

        /*

        return;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:/home/max/devel/macros-java/sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, sqlName string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.println("sqlName = " + rs.getString("sqlName"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.err.println(e);
            }
        }
        */
    }
}
