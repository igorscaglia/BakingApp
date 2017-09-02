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

package br.com.scagliabaroni.bakingapp.database;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Represents a database contract infrastructure requirement
 */
public class DatabaseContract {

    public static final class RecipeEntry {
        @DataType(DataType.Type.INTEGER)
        @PrimaryKey(onConflict = ConflictResolutionType.FAIL)
        @AutoIncrement
        public static final String _ID = "_id";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_NAME = "name";
        @DataType(DataType.Type.INTEGER)
        public static final String COLUMN_SERVINGS = "servings";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_IMAGE = "image";
    }

    public static final class IngredientEntry {
        @DataType(DataType.Type.INTEGER)
        @PrimaryKey(onConflict = ConflictResolutionType.FAIL)
        @AutoIncrement
        public static final String _ID = "_id";
        public static final String COLUMN_ID_RECIPE = "id_recipe";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_NAME = "name";
        @DataType(DataType.Type.REAL)
        public static final String COLUMN_QUANTITY = "quantity";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_MEASURE = "measure";
    }

    public static final class StepEntry {
        @DataType(DataType.Type.INTEGER)
        @PrimaryKey(onConflict = ConflictResolutionType.FAIL)
        @AutoIncrement
        public static final String _ID = "_id";
        public static final String COLUMN_ID_RECIPE = "id_recipe";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_SHORT_DESCRIPTION = "short_description";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_DESCRIPTION = "description";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_VIDEO_URL = "video_url";
        @DataType(DataType.Type.TEXT)
        public static final String COLUMN_THUMBNAIL_URL = "thumbnail_url";
        @DataType(DataType.Type.INTEGER)
        public static final String COLUMN_POSITION = "position";
        public static final String COLUMN_MAX_POSITION = "max_position";
    }
}