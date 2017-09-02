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

package br.com.scagliabaroni.bakingapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import br.com.scagliabaroni.bakingapp.R;
import br.com.scagliabaroni.bakingapp.common.RecipeDetail;
import br.com.scagliabaroni.bakingapp.common.RecipeDetailClickListener;
import br.com.scagliabaroni.bakingapp.common.RecipeDetailMaster;
import br.com.scagliabaroni.bakingapp.common.RecipeDetailStep;
import br.com.scagliabaroni.bakingapp.fragment.RecipeDetailFragment;
import br.com.scagliabaroni.bakingapp.fragment.RecipeDetailStepFragment;
import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import br.com.scagliabaroni.bakingapp.model.Step;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This activity is responsible to show the details of a recipe chosen at recipe activity.
 */
public class RecipeDetailActivity extends AppCompatActivity implements
        RecipeDetailMaster,
        RecipeDetailClickListener {
    @BindView(R.id.MainToolbar)
    Toolbar mMainToolbar;
    // RecipeDetail instance
    private RecipeDetail mRecipeDetail;
    // RecipeDetailStep instance
    private RecipeDetailStep mRecipeDetailStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        // Enable ButterKnife
        ButterKnife.bind(this);
        // Try find and get a reference for RecipeDetailFragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.RecipeDetailFragment);

        if (fragment != null) {
            this.mRecipeDetail = (RecipeDetailFragment) fragment;
        }
        // Try find and get a reference for RecipeDetailStepFragment
        // If not find then this activity was opened in a phone device
        fragment = getSupportFragmentManager().findFragmentById(R.id.RecipeDetailStepFragment);

        if (fragment != null) {
            this.mRecipeDetailStep = (RecipeDetailStepFragment) fragment;
        }
        // Set the toolbar as an action bar
        setSupportActionBar(this.mMainToolbar);
        // Get intent that start this activity
        Intent intent = this.getIntent();

        // Verify if was passed the recipe param
        if (intent.hasExtra(RecipesUtils.RECIPE_PARAM)) {
            // Set the default click step listener
            this.mRecipeDetail.setRecipeDetailClickListener(this);
            // Get the recipe
            Recipe recipeParam = intent.getParcelableExtra(RecipesUtils.RECIPE_PARAM);
            // Set recipe to the RecipeDetailFragment fragment
            this.mRecipeDetail.show(recipeParam);
            this.getSupportActionBar()
                    .setTitle(String.format("%1$s Details", recipeParam.getName()));
        }
    }

    @Override
    public RecipeDetail getRecipeDetail() {
        return mRecipeDetail;
    }

    @Override
    public RecipeDetailStep getRecipeDetailStep() {
        return mRecipeDetailStep;
    }

    /**
     * Since we are using static fragments the local mRecipeDetailStepFragment
     * will be instantiate automatically, thus indicating us if we are in tablet or phone mode.
     * Obs: Because we are using static fragments we don't need use FragmentManager to load our
     * fragments in order to be viewed by the user.
     */
    @Override
    public boolean isRecipeDetailStepLoaded() {
        return mRecipeDetailStep != null;
    }

    @Override
    public void onStepSelected(Step step) {

        // If RecipeDetailStep is loaded then we are in tablet mode
        if (this.isRecipeDetailStepLoaded()) {
            // Show the selected step on RecipeDetailStep
            this.getRecipeDetailStep().show(this.mRecipeDetail.getRecipe(), step);
        } else {
            // If not is loaded then means that we are in phone mode and we should initiate
            // RecipeDetailStepActivity.
            this.startActivity(new Intent(this, RecipeDetailStepActivity.class)
                    .putExtra(RecipesUtils.STEP_PARAM, step)
                    .putExtra(RecipesUtils.RECIPE_NAME_PARAM,
                            this.getRecipeDetail().getRecipe().getName()));
        }
    }
}