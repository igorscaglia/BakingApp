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
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
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
public class ContentProviderInstrumentedTest extends ProviderTestCase2<RecipesProvider> {

    public ContentProviderInstrumentedTest() {
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
            }
        } // end for
        // ---------------------------------------------------------------------------------------
        // Begin Testes
        // ---------------------------------------------------------------------------------------
    }

    @Test
    public void addNewEmptyFakeRowInCursor() {
        Cursor cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Step.fromRecipe("2"),
                        br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Step.PROJECTION,
                        null,
                        null,
                        null);
        // Should be 10
        assertEquals(10, cursor.getCount());

        MatrixCursor matrixCursor = new MatrixCursor(new String[]
                {DatabaseContract.StepEntry._ID,
                        DatabaseContract.StepEntry.COLUMN_ID_RECIPE,
                        DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION,
                        DatabaseContract.StepEntry.COLUMN_DESCRIPTION,
                        DatabaseContract.StepEntry.COLUMN_VIDEO_URL,
                        DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL});
        matrixCursor.addRow(new Object[]{0, 2, "", "", "", ""});

        MergeCursor mergeCursor = new MergeCursor(new Cursor[]{matrixCursor, cursor});

        String v = DatabaseUtils.dumpCursorToString(mergeCursor);

        assertEquals(11, mergeCursor.getCount());
    }

       public void add_nutellapie_recipe() {
        // Set the values to add
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeEntry.COLUMN_NAME, "Nutella Pie");
        values.put(DatabaseContract.RecipeEntry.COLUMN_SERVINGS, 8);
        values.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE, "");
        // Add the recipe
        Uri insertResult = this.getMockContentResolver()
                .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                        .Recipe.CONTENT_URI, values);
        // Should be not null
        assertNotNull(insertResult);
        // Get the id
        long id = ContentUris.parseId(insertResult);
        // Should be more then 1
        assertTrue(id > 0);
    }

    public void add_brownies_recipe_with_two_ingredients() {
        // Set the values to add
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.RecipeEntry.COLUMN_NAME, "Brownies");
        values.put(DatabaseContract.RecipeEntry.COLUMN_SERVINGS, 8);
        values.put(DatabaseContract.RecipeEntry.COLUMN_IMAGE, "");
        // Add the recipe
        Uri insertResult = this.getMockContentResolver()
                .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                        .Recipe.CONTENT_URI, values);
        // Should be not null
        assertNotNull(insertResult);
        // Get the id
        long idRecipe = ContentUris.parseId(insertResult);
        // Should be more then 1
        assertTrue(idRecipe > 0);

        // Set ingredient values ------------------- 1
        values.clear();
        values.put(DatabaseContract.IngredientEntry.COLUMN_ID_RECIPE, idRecipe);
        values.put(DatabaseContract.IngredientEntry.COLUMN_NAME, "Bittersweet chocolate (60-70% cacao)");
        values.put(DatabaseContract.IngredientEntry.COLUMN_MEASURE, "G");
        values.put(DatabaseContract.IngredientEntry.COLUMN_QUANTITY, 350);
        // Add the ingredient
        insertResult = this.getMockContentResolver()
                .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                        .Ingredient.CONTENT_URI, values);
        // Should be not null
        assertNotNull(insertResult);
        // Get the id
        long id = ContentUris.parseId(insertResult);
        // Should be more then 1
        assertTrue(id > 0);

        // Set ingredient values ------------------- 2
        values.clear();
        values.put(DatabaseContract.IngredientEntry.COLUMN_ID_RECIPE, idRecipe);
        values.put(DatabaseContract.IngredientEntry.COLUMN_NAME, "unsalted butter");
        values.put(DatabaseContract.IngredientEntry.COLUMN_MEASURE, "G");
        values.put(DatabaseContract.IngredientEntry.COLUMN_QUANTITY, 226);
        // Add the ingredient
        insertResult = this.getMockContentResolver()
                .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                        .Ingredient.CONTENT_URI, values);
        // Should be not null
        assertNotNull(insertResult);
        // Get the id
        id = ContentUris.parseId(insertResult);
        // Should be more then 1
        assertTrue(id > 0);
    }

    public void count_query_properly() {
        // Get count
        Cursor cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Recipe.CONTENT_URI,
                        new String[]{"count(*)"},
                        null, null, null);
        // Should be not null
        assertNotNull(cursor);
    }

    @Test
    public void retrieveStepsByRecipeId() {
        Cursor cursor = this.getMockContentResolver()
                .query(br.com.scagliabaroni.bakingapp.provider.RecipesProvider
                                .Step.fromRecipe("1"),
                        null,
                        null,
                        null,
                        null)
                ;
        // Should be not null
        assertNotNull(cursor);
        // Get total records in database to compare
        int totalRecords = cursor.getCount();
        cursor.close();

        assertTrue(totalRecords > 0);
    }
}