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

package br.com.scagliabaroni.bakingapp.common;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.RemoteViews;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.scagliabaroni.bakingapp.activity.RecipeDetailActivity;
import br.com.scagliabaroni.bakingapp.model.Ingredient;
import br.com.scagliabaroni.bakingapp.provider.RecipesProvider;
import br.com.scagliabaroni.bakingapp.R;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import br.com.scagliabaroni.bakingapp.model.Step;
import br.com.scagliabaroni.bakingapp.database.DatabaseContract;
import br.com.scagliabaroni.bakingapp.service.RecipesLoadIntentService;
import br.com.scagliabaroni.bakingapp.widget.RecipeIngredientsWidgetProvider;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * This class group utilitaries methods to deal with access, network access and database ops.
 */
public class RecipesUtils {
    public final static String RECIPE_PARAM = "recipe_param";
    public final static String STEP_PARAM = "step_param";
    public final static String RECIPE_NAME_PARAM = "recipe_name";

    /**
     * This method retrieve the recipe list from internet server whose data are in JSON format and
     * cast the result based on model created with GSON marks
     *
     * @param urlRecipes The JSON recipes URL where are the recipes itself.
     * @return List of recipes or null if not retrieved.
     */
    public static List<Recipe> retrieveRecipes(String urlRecipes) {
        // Use default way to construct retrofit call
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlRecipes)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // Create retrofit call based on RecipesRetrofitContract
        RecipesRetrofitContract service = retrofit
                .create(RecipesRetrofitContract.class);
        List<Recipe> result = new ArrayList<>();
        try {
            // Execute the call from recipe URL and get its response based on GSON
            Response<List<Recipe>> recipesResponse = service.getRecipes().execute();
            // Get the real list in the body response
            result = recipesResponse.body();
        } catch (IOException e) {
            Timber.d(e);
        }
        return result;
    }

    /**
     * We'd might put this method direct in RecipesLoadIntentService but Google has said that we
     * should encapsulate it in separate class since there is no support for intent services tests:
     * https://developer.android.com/training/testing/integration-testing/service-testing.html#setup
     *
     * @param intent  The incoming intent from {@link RecipesLoadIntentService}
     * @param context The {@link RecipesLoadIntentService} context
     */
    public static void loadRecipes(Intent intent, Context context) {
        // Get the action if any
        final String action = intent.getAction();
        // Default behavior is load data
        boolean continueLoadingData = true;
        int totalRecipesDatabase = 0;
        // Get the recipes count in database
        Cursor recipesCountCursor = context.getContentResolver()
                .query(RecipesProvider
                        .Recipe.CONTENT_URI, new String[]{"count(*)"}, null, null, null);

        // Move to first row and get the total recipes in database
        if (recipesCountCursor.getCount() > 0) {
            // Move to first row and get the total recipes in database
            recipesCountCursor.moveToFirst();
            totalRecipesDatabase = recipesCountCursor.getInt(0);
        }

        // If exists data then delete all to accommodate new data
        // We are doing this because we know that remote recipes data is static, normally we
        // should update or add recipes accordingly
        if (totalRecipesDatabase > 0) {

            // If action is stop when data exists then we have to avoid the deletions.
            if (RecipesLoadIntentService.ACTION_STOP_WHEN_DATA_EXISTS.equals(action)) {
                continueLoadingData = false;
            } else {
                // Since we are using cascade support, we delete just the recipes for everything
                // be deleted
                context.getContentResolver()
                        .delete(RecipesProvider
                                .Recipe.CONTENT_URI, null, null);
            }
        }
        // Close the count query cursor
        recipesCountCursor.close();
        // The total of recipes added in database
        int totalRecipesLoaded = 0;

        // If the action passed agree with, we continue
        if (continueLoadingData) {
            // Get remote recipes
            List<Recipe> remoteRecipes = retrieveRecipes(context.getResources()
                    .getString(R.string.recipes_url));

            // For each recipe add it to database
            for (Recipe remoteRecipe : remoteRecipes) {
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
                Uri insertResult = context.getContentResolver()
                        .insert(RecipesProvider
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
                    context.getContentResolver()
                            .insert(RecipesProvider.Ingredient.CONTENT_URI, values);
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
                    context.getContentResolver()
                            .insert(RecipesProvider.Step.CONTENT_URI, values);
                    // Increment position
                    position++;
                }
                // Increment total recipes loaded
                totalRecipesLoaded++;
            } // end for
        } // end if
        // Until here we've done all inserts then it's time to signal who wants to know the changes
        // by Broadcast
        Intent localIntent = new Intent(RecipesLoadIntentService.ACTION_BROADCAST_RESULT)
                .putExtra(RecipesLoadIntentService.EXTENDED_DATA_TOTAL_RECIPES_LOADED,
                        totalRecipesLoaded);
        // Broadcasts the Intent to receivers in this app.
        // LocalBroadcastManager limits broadcast Intent objects to components in uor app.
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    /**
     * This method is responsible to give the internet connection status.
     *
     * @param context The context that you want to use.
     * @return True if there is a internet connection made, otherwise, false.
     */
    public static boolean hasInternetConnection(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Nullable
    private static Step getStepFromPosition(Context context, int idRecipe, int position) {
        try (Cursor stepCursor = context.getContentResolver()
                .query(RecipesProvider.Step.fromRecipe(String.valueOf(idRecipe)),
                        RecipesProvider.Step.PROJECTION,
                        String.format("%s=?", DatabaseContract.StepEntry.COLUMN_POSITION),
                        new String[]{String.valueOf(position)},
                        null)) {

            // If is a valid cursor
            if (stepCursor != null) {

                // If has a returned value
                if (stepCursor.getCount() > 0) {
                    stepCursor.moveToFirst();
                    return Step.from(stepCursor);
                }
            }
            return null;
        }
    }

    /**
     * This method create a fake step if the position is equals to 0, otherwise, try get the step
     * in database.
     */
    public static Step getOrCreateStep(Context context, int idRecipe, int calculatedPosition) {

        // If is a fake step
        if (calculatedPosition <= 0) {
            Step step = new Step();
            step.setId(0);
            step.setIdRecipe(idRecipe);
            step.setPosition(0);
            return step;
        } else {
            int lastPosition = RecipesUtils.getLastStepPosition(context, idRecipe);

            // If calculated position is grater than last position
            if (calculatedPosition > lastPosition) {
                // Calculated position should be equal to the last position
                calculatedPosition = lastPosition;
            }
            return RecipesUtils.getStepFromPosition(context, idRecipe, calculatedPosition);
        }
    }

    /**
     * This method get the last step position based on searching all steps from a recipe
     */
    public static Integer getLastStepPosition(Context context, int idRecipe) {
        try (Cursor lastStepPositionCursor = context.getContentResolver().query(
                RecipesProvider.Step.CONTENT_URI,
                new String[]{DatabaseContract.StepEntry.COLUMN_MAX_POSITION},
                String.format("%s=?", DatabaseContract.StepEntry.COLUMN_ID_RECIPE),
                new String[]{String.valueOf(idRecipe)},
                null, null)) {

            // If is a valid cursor
            if (lastStepPositionCursor != null) {

                // If has a returned value
                if (lastStepPositionCursor.getCount() > 0) {
                    lastStepPositionCursor.moveToFirst();
                    return lastStepPositionCursor.getInt(lastStepPositionCursor
                            .getColumnIndex(DatabaseContract
                                    .StepEntry.COLUMN_MAX_POSITION));
                }
            }
            return 0;
        }
    }

    /**
     * This method summarize all ingredients from a recipe in a text string
     */
    @NonNull
    public static String generateIngredientsSummary(Context context, Integer idRecipe) {
        StringBuilder builder = new StringBuilder();
        // Get all ingredients from recipe id
        try (Cursor ingredientsCursor = context.getContentResolver().query(
                RecipesProvider.Ingredient
                        .fromRecipe(String.valueOf(idRecipe)),
                RecipesProvider.Ingredient.PROJECTION, null, null, null)) {

            // For each ingredient
            while (ingredientsCursor.moveToNext()) {
                // Cast the ingredient
                Ingredient ingredient = Ingredient.from(ingredientsCursor);
                DecimalFormat df = new DecimalFormat("#.#");
                // Format the quantity
                String quantity = df.format(ingredient.getQuantity());
                // Append the ingredient line
                builder.append(String
                        .format("- %1$s %2$s %3$s\n", new Object[]{
                                quantity,
                                ingredient.getMeasure(),
                                Character.toUpperCase(ingredient.getIngredient().charAt(0)) +
                                        ingredient.getIngredient().substring(1)
                        }));
            }
        }
        // Return the summary list
        return builder.toString();
    }

    /**
     * This method update the ingredients summary widget with a new ingredients summary list
     * based on a recipe
     */
    public static void updateIngredientsSummaryWidget(Context context, Recipe recipe) {
        // Generate ingredients summary from recipe id;
        String ingredientsSummary = String.format("%1$s\n\n%2$s", recipe.getName(),
                RecipesUtils.generateIngredientsSummary(context, recipe.getId()));
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName =
                new ComponentName(context, RecipeIngredientsWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        // Set the widget_recipe_ingredients layout to our widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_recipe_ingredients);
        // Configure the RecipeDetailActivity activity to be opened when widget gets clicked
        Intent recipeDetailIntent = new Intent(context, RecipeDetailActivity.class)
                .putExtra(RecipesUtils.RECIPE_PARAM, recipe);
        PendingIntent recipeDetailPendingIntent =
                PendingIntent.getActivity(context, 0, recipeDetailIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        // Set the pending intent to the WidgetIngredientsSummaryTextView view
        remoteViews.setOnClickPendingIntent(R.id.WidgetIngredientsSummaryTextView,
                recipeDetailPendingIntent);
        // Update the ingredients summary itself
        remoteViews.setTextViewText(R.id.WidgetIngredientsSummaryTextView, ingredientsSummary);
        // Send the update. Could be one id, but we update all widgets with the same information.
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}