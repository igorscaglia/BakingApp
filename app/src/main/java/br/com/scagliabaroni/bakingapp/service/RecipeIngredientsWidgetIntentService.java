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

package br.com.scagliabaroni.bakingapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import br.com.scagliabaroni.bakingapp.R;
import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import timber.log.Timber;

/**
 * Responsible to update the recipe ingredients widget.
 */
public class RecipeIngredientsWidgetIntentService extends IntentService {
    public final static String ACTION_UPDATE_INGREDIENTS_SUMMARY
            = "br.com.scagliabaroni.bakingapp.action.update_ingredients_summary";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RecipeIngredientsWidgetIntentService(String name) {
        super(name);
    }

    public RecipeIngredientsWidgetIntentService() {
        super(RecipeIngredientsWidgetIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_UPDATE_INGREDIENTS_SUMMARY.equals(action)) {

                if (intent.hasExtra(RecipesUtils.RECIPE_PARAM)) {
                    final Recipe recipeParam = intent.getParcelableExtra(RecipesUtils.RECIPE_PARAM);
                    RecipesUtils.updateIngredientsSummaryWidget(this.getBaseContext(), recipeParam);
                    // Show to the user that ingredients summary was updated
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(),
                                    String.format(getBaseContext()
                                                    .getString(R.string.widget_updated),
                                            recipeParam.getName()), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Timber.d("No recipe extra. Can't update widget.");
                }
            }
        }
    }

    /**
     * Initiate the recipe ingredients summary widget update
     */
    public static void startUpdateIngredientsSummary(Context context, Recipe recipe) {
        Bundle extras = new Bundle();
        extras.putParcelable(RecipesUtils.RECIPE_PARAM, recipe);
        Intent intent = new Intent(context, RecipeIngredientsWidgetIntentService.class);
        intent.setAction(ACTION_UPDATE_INGREDIENTS_SUMMARY);
        intent.putExtras(extras);
        context.startService(intent);
    }
}