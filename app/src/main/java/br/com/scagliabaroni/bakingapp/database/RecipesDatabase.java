/*
 * Copyright (c) 2017 Igor Scaglia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.scagliabaroni.bakingapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.ExecOnCreate;
import net.simonvt.schematic.annotation.OnConfigure;
import net.simonvt.schematic.annotation.OnCreate;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;

/**
 * Represents a schematic database infrastructure requirement
 */
@Database(version = RecipesDatabase.VERSION, fileName = RecipesDatabase.FILE_NAME,
        packageName = "br.com.scagliabaroni.bakingapp.infrastructure")
public class RecipesDatabase {
    public static final int VERSION = 1;
    public static final String FILE_NAME = "recipes.db";
    private static final String ALTER_TABLE_STATEMENT = "ALTER TABLE ";
    private static final String ADD_COLUMN_STATEMENT = " ADD COLUMN ";
    private static final String INTEGER_STATEMENT = " INTEGER ";
    private static final String REFERENCES_STATEMENT = "REFERENCES ";
    private static final String ON_DELETE_CASCADE_STATEMENT = " ON DELETE CASCADE";
    @Table(DatabaseContract.RecipeEntry.class)
    public static final String RECIPE = "recipe";
    @Table(DatabaseContract.IngredientEntry.class)
    public static final String INGREDIENT = "ingredient";
    @Table(DatabaseContract.StepEntry.class)
    public static final String STEP = "step";

    @ExecOnCreate
    public static String sCreateColumnIdRecipeOnStep =
            ALTER_TABLE_STATEMENT + STEP + ADD_COLUMN_STATEMENT +
                    DatabaseContract.StepEntry.COLUMN_ID_RECIPE +
                    INTEGER_STATEMENT + REFERENCES_STATEMENT + RECIPE +
                    "(" + DatabaseContract.RecipeEntry._ID + ")" +
                    ON_DELETE_CASCADE_STATEMENT + ";";

    // We are adding the id_recipe column by ExecOnCreate to enable delete cascade.
    @ExecOnCreate
    public static String sCreateColumnIdRecipeOnIngredient =
            ALTER_TABLE_STATEMENT + INGREDIENT + ADD_COLUMN_STATEMENT +
                    DatabaseContract.IngredientEntry.COLUMN_ID_RECIPE +
                    INTEGER_STATEMENT + REFERENCES_STATEMENT + RECIPE +
                    "(" + DatabaseContract.RecipeEntry._ID + ")" +
                    ON_DELETE_CASCADE_STATEMENT + ";";

    public static String createMaxQueryString(String columnName) {
        return String.format("MAX(%1$s)", columnName);
    }

    @OnCreate
    public static void onCreate(Context context, SQLiteDatabase db) {
    }

    @OnUpgrade
    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion,
                                 int newVersion) {
    }

    @OnConfigure
    public static void onConfigure(SQLiteDatabase db) {
        // Add Foreign Key support
        db.setForeignKeyConstraintsEnabled(true);
    }
}