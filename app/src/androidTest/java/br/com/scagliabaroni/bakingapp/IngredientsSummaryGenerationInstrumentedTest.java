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
import android.content.Context;
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

@RunWith(AndroidJUnit4.class)
public class IngredientsSummaryGenerationInstrumentedTest extends ProviderTestCase2<RecipesProvider> {

    public IngredientsSummaryGenerationInstrumentedTest() {
        super(RecipesProvider.class, RecipesProvider.AUTHORITY);
    }

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
    public void verifyIngredientsSummaryGeneration() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        // Generate for first recipe
        String generatedSummary = RecipesUtils.generateIngredientsSummary(appContext, 1);

        // Not null
        assertNotNull(generatedSummary);
        // Not empty
        assertTrue(!generatedSummary.equals(""));
    }
}