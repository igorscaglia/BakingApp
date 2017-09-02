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

package br.com.scagliabaroni.bakingapp.fragment;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.Formatter;
import java.util.Locale;

import br.com.scagliabaroni.bakingapp.R;
import br.com.scagliabaroni.bakingapp.common.IngredientsSummaryAsyncTaskLoader;
import br.com.scagliabaroni.bakingapp.common.RecipeStepNavigationDirection;
import br.com.scagliabaroni.bakingapp.common.RecipeDetailStep;
import br.com.scagliabaroni.bakingapp.common.RecipeDetailStepTargetContentIntent;
import br.com.scagliabaroni.bakingapp.common.RecipeDetailTargetContentIntent;
import br.com.scagliabaroni.bakingapp.common.RecipesExoPlayerManager;
import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.common.StepAsyncTaskLoader;
import br.com.scagliabaroni.bakingapp.common.TargetContentIntent;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import br.com.scagliabaroni.bakingapp.model.Step;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import timber.log.Timber;

/**
 * Responsible to show de step detail, video player and navigation between steps
 */
public class RecipeDetailStepFragment extends Fragment
        implements RecipeDetailStep {
    private static final int INGREDIENTS_SUMMARY_LOADER_ID = 1;
    private static final int STEP_LOADER_ID = 2;
    private static final String STEP_STATE = "step_state";
    private static final String RECIPE_NAME_STATE = "recipe_name_state";
    private String mTitle;
    private Step mStep;
    private String mRecipeName;
    private LoaderManager.LoaderCallbacks<String> mIngredientsSummaryLoaderCallbacks;
    private LoaderManager.LoaderCallbacks<Step> mStepLoaderCallbacks;
    private RecipeStepNavigationDirection mRecipeStepNavigationDirection;
    private RecipesExoPlayerManager mRecipesExoPlayerManager;
    private TargetContentIntent mTargetContentIntent;
    @BindView(R.id.RecipeDetailStepSimpleExoPlayerView)
    SimpleExoPlayerView mSimpleExoPlayerView;
    @BindView(R.id.RecipeDetailStepDescriptionTextView)
    @Nullable
    TextView mRecipeDetailStepDescriptionTextView;
    @BindView(R.id.PreviousButton)
    @Nullable
    Button mPreviousButton;
    @BindView(R.id.NextButton)
    @Nullable
    Button mNextButton;

    @Override
    public Step getStep() {
        return this.mStep;
    }

    private void setStep(Step step) {
        this.mStep = step;
    }

    private void setRecipeName(String recipeName) {
        this.mRecipeName = recipeName;
    }

    /**
     * Mandatory default constructor required for instantiates this fragment.
     */
    public RecipeDetailStepFragment() {
        // Initiate the ingredients summary loader callback
        this.mIngredientsSummaryLoaderCallbacks = new LoaderManager.LoaderCallbacks<String>() {

            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                // Return new instance of loader
                return new IngredientsSummaryAsyncTaskLoader(
                        RecipeDetailStepFragment.this.getContext(),
                        RecipeDetailStepFragment.this.mStep.getIdRecipe());
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                // Just update the description with a ingredients summary text
                RecipeDetailStepFragment.this.updateDescriptionText(data);
            }

            @Override
            public void onLoaderReset(Loader<String> loader) {
            }
        };
        // Initiate the step loader callback
        this.mStepLoaderCallbacks = new LoaderManager.LoaderCallbacks<Step>() {

            @Override
            public Loader<Step> onCreateLoader(int id, Bundle args) {
                // Return new instance of loader
                return new StepAsyncTaskLoader(
                        RecipeDetailStepFragment.this.getContext(),
                        RecipeDetailStepFragment.this.mStep.getIdRecipe(),
                        RecipeDetailStepFragment.this.mStep.getPosition(),
                        RecipeDetailStepFragment.this.mRecipeStepNavigationDirection);
            }

            @Override
            public void onLoadFinished(Loader<Step> loader, Step data) {
                RecipeDetailStepFragment.this.setStep(data);
                RecipeDetailStepFragment.this.updateUI();
            }

            @Override
            public void onLoaderReset(Loader<Step> loader) {
            }
        };
    }

    /**
     * Inflate the layout fragment fragment_recipe_detail.
     * This method is the onCreate version compared with an activity.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail_step, container, false);
        // Enable ButterKnife
        ButterKnife.bind(this, view);
        // Default load the app icon as the background image.
        this.mSimpleExoPlayerView.setDefaultArtwork(BitmapFactory
                .decodeResource(getResources(), R.drawable.ic_launcher));
        // Initiate ExoPlayer manager
        this.mRecipesExoPlayerManager =
                new RecipesExoPlayerManager(this.getContext(), this.mSimpleExoPlayerView);

        // If has extras
        if (this.getActivity().getIntent().hasExtra(RecipesUtils.STEP_PARAM) &&
                this.getActivity().getIntent().hasExtra(RecipesUtils.RECIPE_NAME_PARAM)) {
            // Get and set the step
            this.mStep = this.getActivity().getIntent()
                    .getParcelableExtra(RecipesUtils.STEP_PARAM);
            // Get and set the recipe name
            this.mRecipeName = this.getActivity().getIntent()
                    .getStringExtra(RecipesUtils.RECIPE_NAME_PARAM);
            // Set TargetContentIntent as RecipeDetailStepTargetContentIntent
            this.mTargetContentIntent =
                    new RecipeDetailStepTargetContentIntent(this.mStep, this.mRecipeName);
            this.updateUI();
        }
        return view;
    }

    /**
     * Called when fragment begins to stop.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the step
        savedInstanceState.putParcelable(STEP_STATE, this.mStep);
        // Save the recipe name
        savedInstanceState.putString(RECIPE_NAME_STATE, this.mRecipeName);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Called after start. That is the same of Activity's onRestoreInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // If there is any instance state to be restore
        if (savedInstanceState != null) {
            // Restore step instance state
            this.mStep = savedInstanceState.getParcelable(STEP_STATE);
            // Restore recipe name instance state
            this.mRecipeName = savedInstanceState.getString(RECIPE_NAME_STATE);
            this.updateUI();
        }
    }

    private void updateDescriptionText(String text) {

        if (this.mRecipeDetailStepDescriptionTextView != null) {
            this.mRecipeDetailStepDescriptionTextView.setText(text);
        }
    }

    private void updateExoPlayerVisibility(int visibility) {

        if (this.mSimpleExoPlayerView != null) {
            this.mSimpleExoPlayerView.setVisibility(visibility);
        }
    }

    /**
     * This method update description text of a step considering its position.
     */
    private void updateUI() {

        // If is ingredient summary
        if (this.mStep.getPosition() == 0) {
            this.updateExoPlayerVisibility(View.GONE);
            // Set Title
            this.mTitle = String.format("%1$s %2$s", this.mRecipeName,
                    getContext().getResources().getString(R.string.ingredients));
            // Execute ingredients summary loader
            this.executeIngredientsSummaryLoader();
        } else { // Otherwise update with the step description
            this.updateExoPlayerVisibility(View.VISIBLE);
            Formatter formatter = new Formatter(Locale.getDefault());
            // Set Title
            this.mTitle = formatter.format(this.getResources()
                            .getString(R.string.recipe_detail_step_title), this.mRecipeName,
                    this.mStep.getPosition()).toString();
            String createdStepSummary = String.format("%1$s\n\n%2$s",
                    mStep.getShortDescription(), this.mStep.getDescription());
            this.updateDescriptionText(createdStepSummary);
        }
        this.startExoPlayer();
    }

    private void startExoPlayer() {
        Uri uri = Uri.EMPTY;
        try {
            uri = Uri.parse(this.mStep.getVideoURL());
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        // Start player
        this.mRecipesExoPlayerManager.initializePlayer(uri, this.mTitle,
                this.mStep.getShortDescription(),
                this.mTargetContentIntent);
    }

    private void updateTitle() {
        ActionBar actionBar = ((AppCompatActivity) this.getActivity()).getSupportActionBar();

        // Only update title in toolbar if it was loaded and if a title has been configured
        if (actionBar != null && this.mTitle != null) {
            actionBar.setTitle(this.mTitle);
        }
    }

    /**
     * Executed after restore instance state
     */
    @Override
    public void onStart() {
        super.onStart();
        // We call here updateTitle because here we have sure that a toolbar has been load in the
        // activity that hold this fragment
        this.updateTitle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mRecipesExoPlayerManager.destroy();
    }

    /**
     * This method is called only in tablet mode
     */
    @Override
    public void show(Recipe recipe, Step step) {
        // Cache the step
        this.mStep = step;

        // If is a ingredients step
        if (this.mStep.getPosition() == 0) {
            this.updateExoPlayerVisibility(View.GONE);
            this.executeIngredientsSummaryLoader();
        } else {
            // Otherwise update with the step description
            this.updateExoPlayerVisibility(View.VISIBLE);
            String createdStepSummary = String.format("%1$s\n\n%2$s",
                    mStep.getShortDescription(), mStep.getDescription());
            this.updateDescriptionText(createdStepSummary);
        }
        // Set TargetContentIntent as RecipeDetailTargetContentIntent
        this.mTargetContentIntent = new RecipeDetailTargetContentIntent(recipe);
        this.startExoPlayer();
    }

    @Override
    @OnClick(R.id.PreviousButton)
    @Optional
    public void previous() {
        // We've selected the previous step position
        this.mRecipeStepNavigationDirection = RecipeStepNavigationDirection.PREVIOUS;
        // Execute step loader
        this.executeStepLoader();
    }

    @Override
    @OnClick(R.id.NextButton)
    @Optional
    public void next() {
        // We've selected the next step position
        this.mRecipeStepNavigationDirection = RecipeStepNavigationDirection.NEXT;
        // Execute step loader
        this.executeStepLoader();
    }

    private void executeIngredientsSummaryLoader() {
        Object ingredientsSummaryLoader = getLoaderManager()
                .getLoader(INGREDIENTS_SUMMARY_LOADER_ID);

        // Verify if ingredients loader has been loaded.
        if (ingredientsSummaryLoader == null) {
            // Initiate the ingredients loader
            this.getLoaderManager().initLoader(INGREDIENTS_SUMMARY_LOADER_ID, null,
                    this.mIngredientsSummaryLoaderCallbacks);
        } else {
            // The loader has been loaded, so we restart the loader
            this.getLoaderManager().restartLoader(INGREDIENTS_SUMMARY_LOADER_ID, null,
                    this.mIngredientsSummaryLoaderCallbacks);
        }
    }

    private void executeStepLoader() {
        Object stepLoader = getLoaderManager()
                .getLoader(STEP_LOADER_ID);

        // Verify if step loader has been loaded.
        if (stepLoader == null) {
            // Initiate the step loader
            this.getLoaderManager().initLoader(STEP_LOADER_ID, null,
                    this.mStepLoaderCallbacks);
        } else {
            // The loader has been loaded, so we restart the loader
            this.getLoaderManager().restartLoader(STEP_LOADER_ID, null,
                    this.mStepLoaderCallbacks);
        }
    }
}