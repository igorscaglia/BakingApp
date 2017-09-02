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
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.database.DatabaseContract;
import br.com.scagliabaroni.bakingapp.database.RecipesDatabase;
import br.com.scagliabaroni.bakingapp.infrastructure.RecipesProvider;
import br.com.scagliabaroni.bakingapp.model.Ingredient;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import br.com.scagliabaroni.bakingapp.model.Step;

/**
 * Test the content provider in isolated mode. The file and database operations themselves
 * take place in a directory that is local to the device or emulator and has a special prefix.
 * <p>
 * https://developer.android.com/training/testing/integration-testing/content-provider-testing.html#WhatToTest
 */
@RunWith(AndroidJUnit4.class)
public class LastStepPositionInstrumentedTest extends ProviderTestCase2<RecipesProvider> {

    public LastStepPositionInstrumentedTest() {
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
            // To put a sequence in steps to facilitate the navigation between them
            int position = 1;

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
                values.put(DatabaseContract.StepEntry.COLUMN_POSITION,
                        position);
                // Add the Step
                this.getContext().getContentResolver()
                        .insert(br.com.scagliabaroni.bakingapp.provider.RecipesProvider.Step.CONTENT_URI, values);

                // Increment position
                position++;

                totalSteps++;
            }

            totalRecipes++;
        } // end for
        // ---------------------------------------------------------------------------------------
        // Begin Testes
        // ---------------------------------------------------------------------------------------
    }

    @Test
    public void verifyLastStepPosition() {

        Integer lastStepPosition = RecipesUtils.getLastStepPosition(this.getContext(), 1);

        assertTrue(lastStepPosition == 7);
    }

    @Test
    public void getLastPosition() {

        // Even though the param is 100, the last step is 7
        Step lastStep = RecipesUtils.getOrCreateStep(this.getContext(), 1, 100);

        assertTrue(lastStep.getPosition() == 7);
    }

    @Test
    public void getFirstPosition() {

        // A new fake step should be created
        Step lastStep = RecipesUtils.getOrCreateStep(this.getContext(), 1, 0);

        assertTrue(lastStep.getPosition() == 0);
    }
}