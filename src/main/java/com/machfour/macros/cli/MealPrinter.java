package com.machfour.macros.cli;

import com.machfour.macros.objects.Meal;
import com.machfour.macros.objects.NutritionData;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

class MealPrinter {
    private final PrintStream out;

    MealPrinter(PrintStream out) {
        this.out = out;
    }

    private void println(String s) {
        out.println(s);
    }
    private void println() {
        out.println();
    }

    private void printPer100g(NutritionData nd, boolean verbose) {
        printNutritionData(nd.rescale(100), verbose);
    }

    private void printNutritionData(NutritionData nd, boolean verbose) {
    }
    private void printEnergyProportions(NutritionData nd, boolean verbose) {
    }


    private void printMeal(@NotNull Meal meal) {
        String columnSep = " | ";
        int nameWidth = 45;
        int servingWidth = 6;
        println(meal.getDescription().getName());
    }

    public void printMeals(List<Meal> meals) {
        boolean print100 = false;
        boolean verbose = false;
        boolean printGrandTotal = true;
        println("============");
        println("Meal totals:");
        println("============");
        println();
        for (Meal m : meals) {
            printMeal(m);
            println();
            if (print100) {
                printPer100g(m.getNutritionTotal(), verbose);
            }
            println("============================");
            println();
        }
        if (printGrandTotal) {
            List<NutritionData> allNutData = new ArrayList<>();
            for (Meal m : meals) {
                allNutData.add(m.getNutritionTotal());
            }
            NutritionData totalNutData = NutritionData.sum(allNutData);
            println("====================");
            println("Total for all meals:");
            println("====================");
            printNutritionData(totalNutData, verbose);
            println();
            printEnergyProportions(totalNutData, verbose);
        }
    }
}
