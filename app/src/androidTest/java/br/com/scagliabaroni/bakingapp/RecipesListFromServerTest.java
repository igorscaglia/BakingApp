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

package br.com.scagliabaroni.bakingapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.model.Recipe;

@RunWith(AndroidJUnit4.class)
public class RecipesListFromServerTest {

    @Test
    public void verifyInternetAccess() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        boolean hasConnection = RecipesUtils.hasInternetConnection(appContext);
        // If system has internet connection
        assertTrue(hasConnection);
    }

    @Test
    public void verifyRetrievedRecipesListNotNullOrEmpty() throws IOException {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        // Get recipes URL
        String recipesUrl = appContext.getResources().getString(R.string.recipes_url);
        // Get list from remote json recipes
        List<Recipe> recipesRetrieved = RecipesUtils.retrieveRecipes(recipesUrl);
        // Only if is not null
        assertNotNull(recipesRetrieved);
        // Only if list is more than zero
        assertTrue(recipesRetrieved.size() > 0);
    }
}