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

package br.com.scagliabaroni.bakingapp.common;

/**
 * * Responsible to facilitate organization in the master-detail pattern flow
 */
public interface RecipeDetailMaster extends RecipeDetailClickListener {
    /**
     * The RecipeDetail part
     */
    RecipeDetail getRecipeDetail();

    /**
     * The RecipeDetailStep part
     */
    RecipeDetailStep getRecipeDetailStep();

    /**
     * True if RecipeDetailStep was loaded
     */
    boolean isRecipeDetailStepLoaded();
}