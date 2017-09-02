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

package br.com.scagliabaroni.bakingapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.scagliabaroni.bakingapp.common.RecipeDetailClickListener;
import br.com.scagliabaroni.bakingapp.model.Step;
import br.com.scagliabaroni.bakingapp.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This adapter is used by RecipeDetailRecyclerView in order to show the ingredients and the
 * steps of a chosen recipe.
 * We differentiate the first element of the list loading recipe_ingredients_item layout and then
 * recipe_step_item for the remain items. By this way, we are accomplishing the requirements.
 */
public class RecipeDetailAdapter extends
        RecyclerView.Adapter<RecipeDetailAdapter.RecipeDetailViewHolder> {
    private static final int VIEW_INGREDIENTS = 0;
    private static final int VIEW_STEP = 1;
    private Context mContext;
    // Data container
    private Cursor mCursor;
    // Click Listener
    private RecipeDetailClickListener mRecipeDetailClickListener;

    public RecipeDetailClickListener getRecipeDetailClickListener() {
        return mRecipeDetailClickListener;
    }

    // We've created this setter primarily to facilitate testing.
    public void setRecipeDetailClickListener(RecipeDetailClickListener recipeDetailClickListener) {
        this.mRecipeDetailClickListener = recipeDetailClickListener;
    }

    public RecipeDetailAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public RecipeDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewInflated = null;
        RecipeDetailViewHolder viewHolder = null;

        // Based on viewType we instantiate the correct ViewHolder
        switch (viewType) {
            case VIEW_INGREDIENTS: {
                viewInflated = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recipe_ingredients_item, parent, false);
                viewHolder = new RecipeDetailIngredientsViewHolder(viewInflated);
                break;
            }
            case VIEW_STEP: {
                viewInflated = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recipe_step_item, parent, false);
                viewHolder = new RecipeDetailStepViewHolder(viewInflated);
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
        // To be able to receive focus
        viewInflated.setFocusable(true);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecipeDetailViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {

        // If cursor is null then item count is 0
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * Based on position we back the correct layout id that we want that our view holder load up.
     */
    @Override
    public int getItemViewType(int position) {

        // If 0 then we want load recipe_ingredients_item, otherwise, recipe_step_item layout.
        if (position == 0) {
            return VIEW_INGREDIENTS;
        } else {
            return VIEW_STEP;
        }
    }

    /**
     * Load a new cursor inside this adapter.
     *
     * @param newCursor The new dataset that will be exchanged
     */
    public void swapCursor(Cursor newCursor) {

        if (this.mCursor != null) {
            this.mCursor.close();
        }
        this.mCursor = newCursor;

        if (this.mCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    /**
     * We've opted to create this abstract class to facilitate our onBindViewHolder logic.
     * This class implements View.OnClickListener to handle click on the view.
     */
    public abstract class RecipeDetailViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        public RecipeDetailViewHolder(View itemView) {
            super(itemView);
            // Enable view to handle click
            itemView.setOnClickListener(this);
        }

        /**
         * This abstract should be implemented in order to update the views in the ViewHolder
         */
        public abstract void bind(int position);

        @Override
        public void onClick(View v) {

            // If has a listener
            if (mRecipeDetailClickListener != null) {
                // Go to the right position
                mCursor.moveToPosition(this.getAdapterPosition());
                // Get the step from cursor
                Step step = Step.from(mCursor);
                // Call listener
                mRecipeDetailClickListener.onStepSelected(step);
            }
        }
    }

    /**
     * This ViewHolder is used with recipe_ingredients_item.xml layout file.
     */
    public class RecipeDetailIngredientsViewHolder extends RecipeDetailViewHolder {
        @BindView(R.id.IngredientsImageView)
        ImageView mIngredientsImageView;
        @BindView(R.id.IngredientsTitleTextView)
        TextView mIngredientsTextView;

        public RecipeDetailIngredientsViewHolder(View itemView) {
            super(itemView);
            // Enable ButterKnife
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(int position) {
            // We just sync the cursor moving it to the cursor to the new position. All texts are
            // set in xml layout.
            mCursor.moveToPosition(position);
        }
    }

    /**
     * This ViewHolder is used with recipe_step_item.xml layout file.
     */
    public class RecipeDetailStepViewHolder extends RecipeDetailViewHolder {
        @BindView(R.id.StepImageView)
        ImageView mStepImageView;
        @BindView(R.id.StepNumberTextView)
        TextView mStepNumberTextView;
        @BindView(R.id.StepTitleTextView)
        TextView mStepTitleTextView;

        public RecipeDetailStepViewHolder(View itemView) {
            super(itemView);
            // Enable ButterKnife
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(int position) {
            // We just move to the cursor to the new position
            mCursor.moveToPosition(position);
            // Get the step from cursor
            Step step = Step.from(mCursor);
            // Just add one in position is enough to set correct step number
            this.mStepNumberTextView.setText(String.valueOf(position));
            // Set short description step
            this.mStepTitleTextView.setText(step.getShortDescription());
        }
    }
}