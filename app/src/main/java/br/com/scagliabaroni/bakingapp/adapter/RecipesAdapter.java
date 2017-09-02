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
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.scagliabaroni.bakingapp.R;
import br.com.scagliabaroni.bakingapp.common.RecipeClickListener;
import br.com.scagliabaroni.bakingapp.common.RecipeLongClickListener;
import br.com.scagliabaroni.bakingapp.model.Recipe;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Act as an ordinal adapter for recyclerview recipes
 */
public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder> {
    private Context mContext;
    // Data container
    private Cursor mCursor;
    // Click Listener
    private RecipeClickListener mRecipeClickListener;
    private RecipeLongClickListener mRecipeLongClickListener;

    public RecipeClickListener getRecipeClickListener() {
        return mRecipeClickListener;
    }

    public void setRecipeClickListener(RecipeClickListener recipeClickListener) {
        this.mRecipeClickListener = recipeClickListener;
    }

    public RecipeLongClickListener getRecipeLongClickListener() {
        return mRecipeLongClickListener;
    }

    public void setRecipeLongClickListener(RecipeLongClickListener recipeLongClickListener) {
        this.mRecipeLongClickListener = recipeLongClickListener;
    }

    public RecipesAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout recipe_card_item.xml
        View viewInflated = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_card_item, parent, false);
        return new RecipeViewHolder(viewInflated);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        // Move to the cursor to the new position
        mCursor.moveToPosition(position);
        // Get the recipe from cursor
        Recipe recipe = Recipe.from(this.mCursor);
        // Set recipe item name
        holder.mRecipeNameTextView.setText(recipe.getName());
        // Set summary with plurals
        holder.mRecipeSummaryTextView.setText(this.mContext.getResources()
                .getQuantityString(R.plurals.recipe_card_item_summary,
                        recipe.getServings(),
                        recipe.getServings()));
        // Below we arrange to get the correct image based on localized settings
        String imageName = this.mContext.getString(R.string.no_image_available);
        int localizableResourceId = this.mContext
                .getResources()
                .getIdentifier(imageName, "drawable", this.mContext.getPackageName());
        Drawable correctDrawableImage = this.mContext.getDrawable(localizableResourceId);
        try {
            // Load recipe image with Picasso
            Picasso.with(this.mContext)
                    .load(recipe.getImage())
                    .error(correctDrawableImage)
                    .into(holder.mRecipePreviewImageView);
        } catch (IllegalArgumentException e) {
            Timber.d(e);
            // Load localized 'no available' image
            holder.mRecipePreviewImageView
                    .setImageDrawable(this.mContext.getDrawable(localizableResourceId));
        }
    }

    @Override
    public int getItemCount() {

        // If cursor is null then item count is 0
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * Load a new cursor inside this adapter.
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

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {
        @BindView(R.id.RecipePreviewImageView)
        ImageView mRecipePreviewImageView;
        @BindView(R.id.RecipeNameTextView)
        TextView mRecipeNameTextView;
        @BindView(R.id.RecipeSummaryTextView)
        TextView mRecipeSummaryTextView;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            // Enable ButterKnife
            ButterKnife.bind(this, itemView);
            // Enable view to handle click
            itemView.setOnClickListener(this);
            // Enable view to handle long click
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {

            // If has a listener
            if (mRecipeClickListener != null) {
                // Go to the right position
                mCursor.moveToPosition(this.getAdapterPosition());
                // Get the recipe from cursor
                Recipe recipe = Recipe.from(mCursor);
                // Call listener
                mRecipeClickListener.onRecipeSelected(recipe);
            }
        }

        @Override
        public boolean onLongClick(View v) {

            // If has a listener
            if (mRecipeLongClickListener != null) {
                // Go to the right position
                mCursor.moveToPosition(this.getAdapterPosition());
                // Get the recipe from cursor
                Recipe recipe = Recipe.from(mCursor);
                // Call listener
                mRecipeLongClickListener.onRecipeLongSelected(recipe);
                return true;
            } else {
                return false;
            }
        }
    }
}