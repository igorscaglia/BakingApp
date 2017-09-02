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

import android.os.Bundle;

import br.com.scagliabaroni.bakingapp.activity.RecipeDetailActivity;
import br.com.scagliabaroni.bakingapp.model.Recipe;

/**
 * Represents a RecipeDetail target activity when user click on ExoPlayer notification.
 */
public class RecipeDetailTargetContentIntent implements TargetContentIntent {
    private Class mActivityClass;
    private Bundle mExtras;

    public RecipeDetailTargetContentIntent(Recipe recipe) {
        this.mExtras = new Bundle();
        // Pass the step and recipe name as parameters
        this.mExtras.putParcelable(RecipesUtils.RECIPE_PARAM, recipe);
        this.mActivityClass = RecipeDetailActivity.class;
    }

    @Override
    public Class getActivityClass() {
        return this.mActivityClass;
    }

    @Override
    public Bundle getExtras() {
        return this.mExtras;
    }
}