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
 * Represents a step of a recipe
 */
public class Step implements Parcelable {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("idRecipe")
    @Expose
    private Integer idRecipe;
    @SerializedName("shortDescription")
    @Expose
    private String shortDescription;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("videoURL")
    @Expose
    private String videoURL;
    @SerializedName("thumbnailURL")
    @Expose
    private String thumbnailURL;
    private Integer position;

    public final static Parcelable.Creator<Step> CREATOR = new Creator<Step>() {
        @SuppressWarnings({
                "unchecked"
        })
        public Step createFromParcel(Parcel in) {
            Step instance = new Step();
            instance.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.idRecipe = ((Integer) in.readValue((Integer.class.getClassLoader())));
            instance.shortDescription = ((String) in.readValue((String.class.getClassLoader())));
            instance.description = ((String) in.readValue((String.class.getClassLoader())));
            instance.videoURL = ((String) in.readValue((String.class.getClassLoader())));
            instance.thumbnailURL = ((String) in.readValue((String.class.getClassLoader())));
            instance.position = ((Integer) in.readValue((Integer.class.getClassLoader())));
            return instance;
        }

        public Step[] newArray(int size) {
            return (new Step[size]);
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(idRecipe);
        dest.writeValue(shortDescription);
        dest.writeValue(description);
        dest.writeValue(videoURL);
        dest.writeValue(thumbnailURL);
        dest.writeValue(position);
    }

    public int describeContents() {
        return 0;
    }

    /**
     * Just convert a cursor in a Step.
     * It's expected that this cursor was loaded with a Step data
     *
     * @param cursor The cursor where the recipe data is.
     * @return A recipe loaded or a empty recipe when the columns wasn't match.
     */
    public static Step from(Cursor cursor) {
        Step result = new Step();
        try {
            result.setId(cursor.getInt(cursor.getColumnIndex(DatabaseContract
                    .StepEntry._ID)));
            result.setIdRecipe(cursor.getInt(cursor.getColumnIndex(DatabaseContract
                    .StepEntry.COLUMN_ID_RECIPE)));
            result.setShortDescription(cursor.getString(cursor.getColumnIndex(DatabaseContract
                    .StepEntry.COLUMN_SHORT_DESCRIPTION)));
            result.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseContract
                    .StepEntry.COLUMN_DESCRIPTION)));
            result.setThumbnailURL(cursor.getString(cursor.getColumnIndex(DatabaseContract
                    .StepEntry.COLUMN_THUMBNAIL_URL)));
            result.setVideoURL(cursor.getString(cursor.getColumnIndex(DatabaseContract
                    .StepEntry.COLUMN_VIDEO_URL)));
            result.setPosition(cursor.getInt(cursor.getColumnIndex(DatabaseContract
                    .StepEntry.COLUMN_POSITION)));
        } catch (IllegalArgumentException e) {
            Timber.d(e);
        }
        return result;
    }
}