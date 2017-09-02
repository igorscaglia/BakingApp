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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import br.com.scagliabaroni.bakingapp.adapter.RecipesAdapter;
import br.com.scagliabaroni.bakingapp.common.RecipeClickListener;
import br.com.scagliabaroni.bakingapp.common.RecipeLongClickListener;
import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.provider.RecipesProvider;
import br.com.scagliabaroni.bakingapp.service.RecipeIngredientsWidgetIntentService;
import br.com.scagliabaroni.bakingapp.service.RecipesLoadIntentService;
import br.com.scagliabaroni.bakingapp.R;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Responsible primarily to show all recipes in database
 */
public class RecipesActivity extends AppCompatActivity implements
        RecipeClickListener,
        RecipeLongClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 0;
    private static final String PARCELABLE_RECYCLERVIEW_LAYOUT_STATE = "recyclerview_layout_state";
    private RecipesLoadBroadcastReceiver mRecipesLoadBroadcastReceiver;
    private RecipesAdapter mRecipesAdapter;
    @BindView(R.id.MainToolbar)
    Toolbar mMainToolbar;
    @BindView(R.id.MainProgressBar)
    ProgressBar mMainProgressBar;
    @BindView(R.id.RecipesRecyclerView)
    RecyclerView mRecipesRecyclerView;

    public RecipeClickListener getRecipeClickListener() {
        return this.mRecipesAdapter.getRecipeClickListener();
    }

    public void setRecipeClickListener(RecipeClickListener recipeClickListener) {
        this.mRecipesAdapter.setRecipeClickListener(recipeClickListener);
    }

    public RecipeLongClickListener getRecipeLongClickListener() {
        return this.mRecipesAdapter.getRecipeLongClickListener();
    }

    public void setRecipeLongClickListener(RecipeLongClickListener recipeLongClickListener) {
        this.mRecipesAdapter.setRecipeLongClickListener(recipeLongClickListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        // Enable ButterKnife
        ButterKnife.bind(this);
        // Set the toolbar as an action bar
        setSupportActionBar(this.mMainToolbar);
        // Initialize adapter with null cursor
        this.mRecipesAdapter = new RecipesAdapter(this, null);
        // Set the default click recipe listener
        this.mRecipesAdapter.setRecipeClickListener(this);
        // Set the default long click recipe listener
        this.mRecipesAdapter.setRecipeLongClickListener(this);
        // Set adapter to recyclerview
        this.mRecipesRecyclerView.setAdapter(this.mRecipesAdapter);
        // Changes shouldn't affect the size of the RecyclerView
        this.mRecipesRecyclerView.setHasFixedSize(true);
        // Create loader for recipes list
        this.getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        // Create the BroadcastReceiver
        this.mRecipesLoadBroadcastReceiver = new RecipesLoadBroadcastReceiver();
        // Starts visual working indicator
        this.mMainProgressBar.setVisibility(View.VISIBLE);
        // Starts the RecipesLoadIntentService, get online recipes data and record it in database.
        RecipesLoadIntentService.startRecipesLoad(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the BroadcastReceiver
        IntentFilter receiverFilter =
                new IntentFilter(RecipesLoadIntentService.ACTION_BROADCAST_RESULT);
        // Register the RecipesLoadBroadcastReceiver
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(this.mRecipesLoadBroadcastReceiver, receiverFilter);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * {@link #onResume}.
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Disable Broadcast when UI is no longer visible to the user
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(this.mRecipesLoadBroadcastReceiver);
    }

    /**
     * Called when activity begins to stop.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the recipes recyclerview instance state
        savedInstanceState.putParcelable(PARCELABLE_RECYCLERVIEW_LAYOUT_STATE,
                this.mRecipesRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Called after start.
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there is any instance state to be restore
        if (savedInstanceState != null) {
            // Restore recyclerview instance state
            this.mRecipesRecyclerView.getLayoutManager().onRestoreInstanceState(
                    savedInstanceState.getParcelable(PARCELABLE_RECYCLERVIEW_LAYOUT_STATE));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Create a new async loader anonymous class for get the recipes from database
        // by contentprovider. It could be a CursorLoader either.
        return new AsyncTaskLoader<Cursor>(this) {
            // This will hold all data and act as cache
            Cursor mCursor = null;

            @Override
            protected void onStartLoading() {
                // If is not null then there is data already loaded in mCursor
                if (mCursor != null) {
                    // Delivers loaded data immediately
                    deliverResult(mCursor);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    // Get and return all recipes from database
                    return getContentResolver()
                            .query(RecipesProvider.Recipe.CONTENT_URI, null, null, null, null);
                } catch (Exception e) {
                    Timber.d(e);
                }
                return null;
            }

            @Override
            public void deliverResult(Cursor data) {
                // Set the cache and deliver it
                mCursor = data;
                super.deliverResult(mCursor);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Set new dataset for adapter
        this.mRecipesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Just set null for adapter dataset
        this.mRecipesAdapter.swapCursor(null);
    }

    @Override
    public void onRecipeSelected(Recipe recipe) {
        // Just call RecipeDetailActivity passing the clicked recipe
        this.startActivity(new Intent(this, RecipeDetailActivity.class)
                .putExtra(RecipesUtils.RECIPE_PARAM, recipe));
    }

    @Override
    public void onRecipeLongSelected(Recipe recipe) {
        // Update the widget with the ingredients summary text
        RecipeIngredientsWidgetIntentService
                .startUpdateIngredientsSummary(this.getBaseContext(), recipe);
    }

    /**
     * This {@link BroadcastReceiver} execute UI update request if needed after
     * a database has been loaded with new recipes
     */
    public class RecipesLoadBroadcastReceiver extends BroadcastReceiver {

        @Override
        // Reminder that onReceive is executed on main thread, so we have to be quickly
        public void onReceive(Context context, Intent intent) {
            // Hold the total of recipes loaded
            final int totalRecipesLoaded = intent
                    .getIntExtra(RecipesLoadIntentService.EXTENDED_DATA_TOTAL_RECIPES_LOADED, 0);

            // If greater than 0 restart loader to update UI
            if (totalRecipesLoaded > 0) {
                // Refresh de UI by restarting loader
                getSupportLoaderManager().restartLoader(LOADER_ID, null, RecipesActivity.this);
            }
            // Stops visual working indicator
            mMainProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}