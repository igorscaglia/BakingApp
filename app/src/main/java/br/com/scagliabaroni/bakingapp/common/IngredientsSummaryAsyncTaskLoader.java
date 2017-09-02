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

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Responsible for manage the creation of ingredient summary text
 */
public class IngredientsSummaryAsyncTaskLoader extends AsyncTaskLoader<String> {
    // Starter recipe id
    private int mStarterIdRecipe = 0;
    // Cache the id
    private int mIdRecipe = 0;
    // Cache the ingredients summary
    private String mIngredientsSummary = "";

    public IngredientsSummaryAsyncTaskLoader(Context context, int idRecipe) {
        super(context);
        this.mStarterIdRecipe = idRecipe;
    }

    @Override
    protected void onStartLoading() {

        // If equals then return cached version
        if (this.mIdRecipe == this.mStarterIdRecipe && !this.mIngredientsSummary.equals("")) {
            // Delivers any previously loaded data immediately
            deliverResult(this.mIngredientsSummary);
        } else {
            // Update the id recipe before load
            this.mIdRecipe = this.mStarterIdRecipe;
            // Force a new load
            this.forceLoad();
        }
    }

    @Override
    public String loadInBackground() {
        // Summarize as a text all ingredients from recipe
        return RecipesUtils.generateIngredientsSummary(this.getContext(), this.mIdRecipe);
    }

    @Override
    public void deliverResult(String data) {
        this.mIngredientsSummary = data;
        super.deliverResult(data);
    }
}