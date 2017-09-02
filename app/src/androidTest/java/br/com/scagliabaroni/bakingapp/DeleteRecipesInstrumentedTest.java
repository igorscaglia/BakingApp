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

package br.com.scagliabaroni.bakingapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.model.Ingredient;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import br.com.scagliabaroni.bakingapp.model.Step;
import br.com.scagliabaroni.bakingapp.database.DatabaseContract;
import br.com.scagliabaroni.bakingapp.database.RecipesDatabase;
import br.com.scagliabaroni.bakingapp.infrastructure.RecipesProvider;

/**
 * Test the content provider in isolated mode. The file and database operations themselves
 * take place in a directory that is local to the device or emulator and has a special prefix.
 * <p>
 * https://developer.android.com/training/testing/integration-testing/content-provider-testing.html#WhatToTest
 */
@RunWith(AndroidJUnit4.class)
public class DeleteRecipesInstrumentedTest extends ProviderTestCase2<RecipesProvider> {

    public DeleteRecipesInstrumentedTest() {
        super(RecipesProvider.class, RecipesProvider.AUTHORITY);
    }

    /**
     * Sets up the environment for the test fixture.
     * <p>
     * The super call creates a new
     * {@link android.test.mock.MockContentResolver}, a new IsolatedContext
     * that isolates the provider's file operations, and a new instance of
     * the provider under test within the isolated environment.
     * </p>
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // Doing this we knew what kind of context we'll use
        // when calling .getContext() from this class.
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
        // Delete any previous database
        this.getContext().deleteDatabase(RecipesDatabase.FILE_NAME);
        // Load basic data in database
        this.prepareDatabaseWithData();
    }

    public void prepareDatabaseWithData() {
        int totalRecipes = 0;
        int totalSteps = 0;
        int totalIngredients = 0;
        // Add recipes to database
        // ---------------------------------------------------------------------------------------
        // Get recipes URL
        String recipesUrl = this.getContext().getResources().getString(R.string.recipes_url);
        // Get list from remote json recipes
        List<Recipe> recipesRetrieved = RecipesUtils.retrieveRecipes(recipesUrl);
        // For each recipe add it to database
        for (Recipe remoteRecipe : recipesRetrieved) {
            // Set the values to add
            ContentValues values = new ContentValues();
            // Prepare values
            values.put(DatabaseContract.RecipeEntry.COLUMN_NAME,
                    remoteRecipe.getName());
            values.put(DatabaseContract.RecipeEntry.COLUMN_SERVINGS,
                    remoteRecipe.getServings());
            values.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE,
                    remoteRecipe.getImage());
            // Add the recipe
            Uri insertResult = this.getContext().getContentResolver()
                    .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                            .Recipe.CONTENT_URI, values);
            // Hold the new recipe id
            long idRecipe = ContentUris.parseId(insertResult);

            // For each ingredient add it to database
            for (Ingredient remoteIngredient : remoteRecipe.getIngredients()) {
                // Clear all previous values
                values.clear();
                // Prepare values
                values.put(DatabaseContract.IngredientEntry.COLUMN_ID_RECIPE,
                        idRecipe);
                values.put(DatabaseContract.IngredientEntry.COLUMN_NAME,
                        remoteIngredient.getIngredient());
                values.put(DatabaseContract.IngredientEntry.COLUMN_MEASURE,
                        remoteIngredient.getMeasure());
                values.put(DatabaseContract.IngredientEntry.COLUMN_QUANTITY,
                        remoteIngredient.getQuantity());
                // Add the ingredient
                this.getContext().getContentResolver()
                        .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider.Ingredient.CONTENT_URI, values);
                totalIngredients++;
            }

            // For each step add it to database
            for (Step remoteStep : remoteRecipe.getSteps()) {
                // Clear all previous values
                values.clear();
                // Prepare values
                values.put(DatabaseContract.StepEntry.COLUMN_ID_RECIPE,
                        idRecipe);
                values.put(DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION,
                        remoteStep.getShortDescription());
                values.put(DatabaseContract.StepEntry.COLUMN_DESCRIPTION,
                        remoteStep.getDescription());
                values.put(DatabaseContract.StepEntry.COLUMN_VIDEO_URL,
                        remoteStep.getVideoURL());
                values.put(DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL,
                        remoteStep.getThumbnailURL());
                // Add the Step
                this.getContext().getContentResolver()
                        .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider.Step.CONTENT_URI, values);

                totalSteps++;
            }

            totalRecipes++;
        } // end for
        // ---------------------------------------------------------------------------------------
        // Begin Testes
        // ---------------------------------------------------------------------------------------
    }

    @Test
    public void confirmPragmaForeignKeyOn() {
        br.com.scagliabaroni.bakingapp.infrastructure.RecipesDatabase db =
                br.com.scagliabaroni.bakingapp.infrastructure.RecipesDatabase.getInstance(this.getContext());

        Cursor cursor = db.getReadableDatabase().rawQuery("PRAGMA foreign_keys;", null);

        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {

                int pragma = cursor.getInt(0);

                // If the command "PRAGMA foreign_keys" returns no data instead of a single row containing "0" or "1", then the version of SQLite you are using does not support foreign keys (either because it is older than 3.6.19 or because it was compiled with SQLITE_OMIT_FOREIGN_KEY or SQLITE_OMIT_TRIGGER defined).
                assertTrue(pragma == 1);
            }
        }
        // PRAGMA foreign_keys;
    }

    @Test
    @LargeTest
    public void verifyRecipeDeletionsOnCascade() {
        int count = 0;
        // Get all items
        Cursor cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Recipe.CONTENT_URI,
                        new String[]{"count(*)"},
                        null,
                        null,
                        null);
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        cursor.close();
        assertEquals("Count rows not equal 4.", count, 4);

        cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Step.CONTENT_URI,
                        new String[]{"count(*)"},
                        null,
                        null,
                        null);
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        cursor.close();
        assertEquals("Count rows not equal 43.", 43, count);

        cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Ingredient.CONTENT_URI,
                        new String[]{"count(*)"},
                        null,
                        null,
                        null);
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);

            }
        }
        cursor.close();
        assertEquals("Count rows not equal 38.", 38, count);

        // --------------------------------------- Delete

        // Delete the recipes
        this.getMockContentResolver()
                .delete(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                        .Recipe.CONTENT_URI, null, null);


        // ------------------------------------- Verifications

        // Verify is was deleted
        cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Recipe.CONTENT_URI,
                        new String[]{"count(*)"},
                        null,
                        null,
                        null);
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        cursor.close();
        assertEquals("Recipes count rows not equal 0.", count, 0);

        // Verify if steps was deleted
        cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Step.CONTENT_URI,
                        new String[]{"count(*)"},
                        null,
                        null,
                        null);
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        cursor.close();
        assertEquals("Steps count rows not equal 0.", count, 0);


        // Verify if steps was deleted
        cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Ingredient.CONTENT_URI,
                        new String[]{"count(*)"},
                        null,
                        null,
                        null);
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }
        cursor.close();
        assertEquals("Ingredients count rows not equal 0.", count, 0);
    }
}