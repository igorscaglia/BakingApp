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

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import br.com.scagliabaroni.bakingapp.database.DatabaseContract;
import timber.log.Timber;

/**
 * Represents a ingredient of a recipe
 */
public class Ingredient implements Parcelable {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("idRecipe")
    @Expose
    private Integer idRecipe;
    @SerializedName("quantity")
    @Expose
    private Double quantity;
    @SerializedName("measure")
    @Expose
    private String measure;
    @SerializedName("ingredient")
    @Expose
    private String ingredient;

    public final static Parcelable.Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
        @SuppressWarnings({
                "unchecked"
        })
        public Ingredient createFromParcel(Parcel in) {
            Ingredient instance = new Ingredient();
            instance.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.idRecipe = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.quantity = ((Double) in.readValue((Double.class.getClassLoader())));
            instance.measure = ((String) in.readValue((String.class.getClassLoader())));
            instance.ingredient = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public Ingredient[] newArray(int size) {
            return (new Ingredient[size]);
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdRecipe() {
        return idRecipe;
    }

    public void setIdRecipe(Integer idRecipe) {
        this.idRecipe = idRecipe;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(idRecipe);
        dest.writeValue(quantity);
        dest.writeValue(measure);
        dest.writeValue(ingredient);
    }

    public int describeContents() {
        return 0;
    }

    public static Ingredient from(Cursor cursor) {
        Ingredient result = new Ingredient();
        try {
            result.setId(cursor.getInt(cursor.getColumnIndex(DatabaseContract
                    .IngredientEntry._ID)));
            result.setIdRecipe(cursor.getInt(cursor.getColumnIndex(DatabaseContract
                    .IngredientEntry.COLUMN_ID_RECIPE)));
            result.setIngredient(cursor.getString(cursor.getColumnIndex(DatabaseContract
                    .IngredientEntry.COLUMN_NAME)));
            result.setMeasure(cursor.getString(cursor.getColumnIndex(DatabaseContract
                    .IngredientEntry.COLUMN_MEASURE)));
            result.setQuantity(cursor.getDouble(cursor.getColumnIndex(DatabaseContract
                    .IngredientEntry.COLUMN_QUANTITY)));
        } catch (IllegalArgumentException e) {
            Timber.d(e);
        }
        return result;
    }
}