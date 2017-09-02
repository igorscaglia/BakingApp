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

package br.com.scagliabaroni.bakingapp.model;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import br.com.scagliabaroni.bakingapp.database.DatabaseContract;
import timber.log.Timber;

/**
 * Represents a recipe
 */
public class Recipe implements Parcelable {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("ingredients")
    @Expose
    private List<Ingredient> ingredients = new ArrayList<Ingredient>();
    @SerializedName("steps")
    @Expose
    private List<Step> steps = new ArrayList<Step>();
    @SerializedName("servings")
    @Expose
    private Integer servings;
    @SerializedName("image")
    @Expose
    private String image;

    public final static Parcelable.Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @SuppressWarnings({
                "unchecked"
        })
        public Recipe createFromParcel(Parcel in) {
            Recipe instance = new Recipe();
            instance.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.name = ((String) in.readValue((String.class.getClassLoader())));
            in.readList(instance.ingredients, (Ingredient.class.getClassLoader()));
            in.readList(instance.steps, (Step.class.getClassLoader()));
            instance.servings = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.image = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public Recipe[] newArray(int size) {
            return (new Recipe[size]);
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(name);
        dest.writeList(ingredients);
        dest.writeList(steps);
        dest.writeValue(servings);
        dest.writeValue(image);
    }

    public int describeContents() {
        return 0;
    }

    /**
     * Just convert a cursor in a Recipe.
     * It's expected that this cursor was loaded with a Recipe data
     *
     * @param cursor The cursor where the recipe data is.
     * @return A recipe loaded or a empty recipe when the columns wasn't match.
     */
    public static Recipe from(Cursor cursor) {
        Recipe result = new Recipe();
        try {
            result.setId(cursor
                    .getInt(cursor.getColumnIndex(DatabaseContract.RecipeEntry._ID)));
            result.setName(cursor
                    .getString(cursor.getColumnIndex(DatabaseContract.RecipeEntry.COLUMN_NAME)));
            result.setServings(cursor
                    .getInt(cursor.getColumnIndex(DatabaseContract.RecipeEntry.COLUMN_SERVINGS)));
            result.setImage(cursor
                    .getString(cursor.getColumnIndex(DatabaseContract.RecipeEntry.COLUMN_IMAGE)));
        } catch (IllegalArgumentException e) {
            Timber.d(e);
        }
        return result;
    }
}