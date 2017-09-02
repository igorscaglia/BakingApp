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

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.scagliabaroni.bakingapp.R;
import br.com.scagliabaroni.bakingapp.adapter.RecipeDetailAdapter;
import br.com.scagliabaroni.bakingapp.common.RecipeDetail;
import br.com.scagliabaroni.bakingapp.common.RecipeDetailClickListener;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import br.com.scagliabaroni.bakingapp.database.DatabaseContract;
import br.com.scagliabaroni.bakingapp.provider.RecipesProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * This fragment will show the recipes's ingredients and steps.
 */
public class RecipeDetailFragment extends Fragment implements
        RecipeDetail,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 0;
    @BindView(R.id.RecipeDetailRecyclerView)
    RecyclerView mRecipeDetailRecyclerView;
    // This is the recipe chosen in RecipesActivity
    private Recipe mRecipe;
    private RecipeDetailAdapter mRecipeDetailAdapter;

    @Override
    public RecipeDetailClickListener getRecipeDetailClickListener() {
        return this.mRecipeDetailAdapter.getRecipeDetailClickListener();
    }

    @Override
    public void setRecipeDetailClickListener(RecipeDetailClickListener recipeDetailClickListener) {
        this.mRecipeDetailAdapter.setRecipeDetailClickListener(recipeDetailClickListener);
    }

    /**
     * Mandatory default constructor required for instantiates this fragment.
     */
    public RecipeDetailFragment() {
    }

    /**
     * Inflate the layout fragment fragment_recipe_detail.
     * This method is the onCreate version compared with an activity.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);
        // Enable ButterKnife
        ButterKnife.bind(this, view);
        // Initialize adapter with null cursor
        this.mRecipeDetailAdapter = new RecipeDetailAdapter(this.getContext(), null);
        // Set adapter to recyclerview
        this.mRecipeDetailRecyclerView.setAdapter(this.mRecipeDetailAdapter);
        // Changes shouldn't affect the size of the RecyclerView
        this.mRecipeDetailRecyclerView.setHasFixedSize(true);
        // Create loader for recipes list
        this.getLoaderManager().initLoader(LOADER_ID, null, this);
        // Return inflated view
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Create a new async loader anonymous class for
        // get the recipe steps from database by ContentProvider
        // Note that we won't load the ingredients list since it will not be showed to the user
        // from this fragment.
        return new AsyncTaskLoader<Cursor>(this.getContext()) {
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

                // If no recipe then return null
                if (mRecipe == null) {
                    return null;
                }
                try {
                    Timber.i("All steps from recipe id: %s", mRecipe.getId());
                    // Get all steps from recipe id
                    Cursor stepsCursor = this.getContext().getContentResolver().query(
                            RecipesProvider.Step.fromRecipe(String.valueOf(mRecipe.getId())),
                            RecipesProvider.Step.PROJECTION,
                            null,
                            null,
                            null);
                    // Adding new fake row on the top. By this way the first element will hold
                    // the recipe id. Even though all data items are steps, the first view item
                    // is a ingredient summary (see the layout). The detail step will get the
                    // ingredients list based on this row.
                    MatrixCursor fakeCursor = new MatrixCursor(
                            new String[]{DatabaseContract.StepEntry._ID,
                                    DatabaseContract.StepEntry.COLUMN_ID_RECIPE,
                                    DatabaseContract.StepEntry.COLUMN_SHORT_DESCRIPTION,
                                    DatabaseContract.StepEntry.COLUMN_DESCRIPTION,
                                    DatabaseContract.StepEntry.COLUMN_VIDEO_URL,
                                    DatabaseContract.StepEntry.COLUMN_THUMBNAIL_URL,
                                    DatabaseContract.StepEntry.COLUMN_POSITION});
                    fakeCursor.addRow(new Object[]{0, String.valueOf(mRecipe.getId()),
                            "", "", "", "", 0});
                    // Create a new merge cursor
                    MergeCursor mergeCursor = new MergeCursor(
                            new Cursor[]{fakeCursor, stepsCursor});
                    return mergeCursor;
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
        this.mRecipeDetailAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Just set null for adapter dataset
        this.mRecipeDetailAdapter.swapCursor(null);
    }

    @Override
    public void show(Recipe recipe) {
        this.mRecipe = recipe;
        // Restart loader
        this.getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Recipe getRecipe() {
        return this.mRecipe;
    }
}