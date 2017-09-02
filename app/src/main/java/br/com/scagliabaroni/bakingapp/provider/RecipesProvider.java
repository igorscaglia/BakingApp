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

package br.com.scagliabaroni.bakingapp.provider;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.MapColumns;
import net.simonvt.schematic.annotation.TableEndpoint;

import java.util.HashMap;
import java.util.Map;

import br.com.scagliabaroni.bakingapp.database.DatabaseContract;
import br.com.scagliabaroni.bakingapp.database.RecipesDatabase;

/**
 * Represents a schematic content provider infrastructure requirement
 */
@ContentProvider(authority = RecipesProvider.AUTHORITY, database = RecipesDatabase.class,
        packageName = "br.com.scagliabaroni.bakingapp.infrastructure")
public final class RecipesProvider {
    public final static String AUTHORITY = "br.com.scagliabaroni.bakingapp.authority";
    private final static Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();

        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        final static String RECIPES = "recipes";
        final static String INGREDIENTS = "ingredients";
        final static String STEPS = "steps";
        final static String FROM_RECIPE = "fromRecipe";
        final static String WITH_ONE_FAKE_ROW = "withOneFakeRow";
    }

    @TableEndpoint(table = RecipesDatabase.RECIPE)
    public static class Recipe {
        public static String[] PROJECTION = new String[]{
                DatabaseContract.RecipeEntry._ID,
                DatabaseContract.RecipeEntry.COLUMN_NAME,
                DatabaseContract.RecipeEntry.COLUMN_SERVINGS,
                DatabaseContract.RecipeEntry.COLUMN_IMAGE
        };

        @ContentUri(path = Path.RECIPES,
                type = "vnd.android.cursor.dir/recipe",
                defaultSort = DatabaseContract.RecipeEntry._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.RECIPES);

        @InexactContentUri(path = Path.RECIPES + "/#",
                name = "RECIPE_ID",
                type = "vnd.android.cursor.item/recipe",
                whereColumn = DatabaseContract.RecipeEntry._ID,
                pathSegment = 1)
        public static Uri withId(long idRecipe) {
            return buildUri(Path.RECIPES, String.valueOf(idRecipe));
        }
    }

    @TableEndpoint(table = RecipesDatabase.INGREDIENT)
    public static class Ingredient {
        public static String[] PROJECTION = new String[]{
                DatabaseContract.IngredientEntry._ID,
                DatabaseContract.IngredientEntry.COLUMN_ID_RECIPE,
                DatabaseContract.IngredientEntry.COLUMN_NAME,
                DatabaseContract.IngredientEntry.COLUMN_QUANTITY,
                DatabaseContract.IngredientEntry.COLUMN_MEASURE
        };

        @ContentUri(path = Path.INGREDIENTS,
                type = "vnd.android.cursor.dir/ingredient",
                defaultSort = DatabaseContract.IngredientEntry._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.INGREDIENTS);

        @InexactContentUri(path = Path.INGREDIENTS + "/#",
                name = "INGREDIENT_ID",
                type = "vnd.android.cursor.item/ingredient",
                whereColumn = DatabaseContract.IngredientEntry._ID,
                pathSegment = 1)
        public static Uri withId(long idIngredient) {
            return buildUri(Path.INGREDIENTS, String.valueOf(idIngredient));
        }

        @InexactContentUri(
                name = "INGREDIENTS_FROM_RECIPE",
                path = Path.INGREDIENTS + "/" + Path.FROM_RECIPE + "/#",
                type = "vnd.android.cursor.dir/ingredient",
                whereColumn = DatabaseContract.IngredientEntry.COLUMN_ID_RECIPE,
                defaultSort = DatabaseContract.IngredientEntry._ID + " ASC",
                pathSegment = 2)
        public static Uri fromRecipe(String idRecipe) {
            return buildUri(Path.INGREDIENTS, Path.FROM_RECIPE, idRecipe);
        }
    }

    @TableEndpoint(table = RecipesDatabase.STEP)
    public static class Step {
        public static String[] PROJECTION = new String[]{
                DatabaseContract.StepEntry._ID,
                DatabaseContract.StepEntry.COLUMN_ID_RECIPE,
                DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION,
                DatabaseContract.StepEntry.COLUMN_DESCRIPTION,
                DatabaseContract.StepEntry.COLUMN_VIDEO_URL,
                DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL,
                DatabaseContract.StepEntry.COLUMN_POSITION
        };

        private static final String LAST_STEP_POSITION_QUERY_STRING =
                RecipesDatabase.createMaxQueryString(DatabaseContract.StepEntry.COLUMN_POSITION);

        @MapColumns
        public static Map<String, String> mapColumns() {
            Map<String, String> map = new HashMap<>();
            map.put(DatabaseContract.StepEntry.COLUMN_MAX_POSITION,
                    LAST_STEP_POSITION_QUERY_STRING);
            return map;
        }

        @ContentUri(path = Path.STEPS,
                type = "vnd.android.cursor.dir/step",
                defaultSort = DatabaseContract.StepEntry._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.STEPS);

        @InexactContentUri(path = Path.STEPS + "/#",
                name = "STEP_ID",
                type = "vnd.android.cursor.item/step",
                whereColumn = DatabaseContract.StepEntry._ID,
                pathSegment = 1)
        public static Uri withId(long idStep) {
            return buildUri(Path.STEPS, String.valueOf(idStep));
        }

        @InexactContentUri(name = "STEPS_FROM_RECIPE",
                path = Path.STEPS + "/" + Path.FROM_RECIPE + "/#",
                type = "vnd.android.cursor.dir/step",
                whereColumn = DatabaseContract.StepEntry.COLUMN_ID_RECIPE,
                defaultSort = DatabaseContract.StepEntry._ID + " ASC",
                pathSegment = 2)
        public static Uri fromRecipe(String idRecipe) {
            return buildUri(Path.STEPS, Path.FROM_RECIPE, idRecipe);
        }
    }
}