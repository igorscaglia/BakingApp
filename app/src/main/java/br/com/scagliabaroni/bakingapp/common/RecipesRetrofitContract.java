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

import java.util.List;

import br.com.scagliabaroni.bakingapp.model.Recipe;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * This interface is required by retrofit to inform what kind of callings we will do.
 */
public interface RecipesRetrofitContract {

    /**
     * The '.' means that the URL is the same of base URL.
     *
     * @return A response that should be a list of recipes in your body message.
     */
    @GET(".")
    Call<List<Recipe>> getRecipes();
}