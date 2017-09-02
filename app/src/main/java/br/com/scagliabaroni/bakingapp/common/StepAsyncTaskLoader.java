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

import br.com.scagliabaroni.bakingapp.model.Step;

/**
 * Responsible for get the next step of a recipe
 */
public class StepAsyncTaskLoader extends AsyncTaskLoader<Step> {
    private Integer mIdRecipe;
    private Integer mActualPosition;
    private Integer mCalculatedPosition;
    private RecipeStepNavigationDirection mRecipeStepNavigationDirection;
    private Step mStep;

    public StepAsyncTaskLoader(Context context, Integer idRecipe, Integer idActualPosition,
                               RecipeStepNavigationDirection recipeStepNavigationDirection) {
        super(context);
        this.mIdRecipe = idRecipe;
        this.mActualPosition = idActualPosition;
        this.mRecipeStepNavigationDirection = recipeStepNavigationDirection;
    }

    @Override
    public Step loadInBackground() {
        this.mStep = RecipesUtils
                .getOrCreateStep(this.getContext(), this.mIdRecipe, this.mCalculatedPosition);
        return this.mStep;
    }

    @Override
    protected void onStartLoading() {

        // Calculate de position
        switch (this.mRecipeStepNavigationDirection) {
            case PREVIOUS:
                this.mCalculatedPosition = this.mActualPosition - 1;
                break;
            case NEXT:
                this.mCalculatedPosition = this.mActualPosition + 1;
                break;
        }

        if (this.mStep != null) {

            // If not null and step position equals calculated position then return cached version
            if (this.mStep.getPosition().equals(this.mCalculatedPosition)) {
                this.deliverResult(this.mStep);
            }
        }
        // Force a new load
        this.forceLoad();
    }
}