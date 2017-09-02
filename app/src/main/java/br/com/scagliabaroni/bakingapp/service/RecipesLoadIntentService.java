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

package br.com.scagliabaroni.bakingapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import br.com.scagliabaroni.bakingapp.common.RecipesUtils;

/**
 * This service is responsible for load recipes from internet to the local recipes database.
 * Is an {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class RecipesLoadIntentService extends IntentService {
    /**
     * This custom action force data loading stop when data exists in database already.
     * We create this action to add some extra functionally, since it is pretty easy
     */
    public static final String ACTION_STOP_WHEN_DATA_EXISTS =
            "br.com.scagliabaroni.bakingapp.action.stop_when_data_exists";
    // Defines a custom Intent action for broadcast receivers
    public static final String ACTION_BROADCAST_RESULT =
            "br.com.scagliabaroni.bakingapp.action.broadcast_result";
    // Defines the extra key for total recipes loaded
    public static final String EXTENDED_DATA_TOTAL_RECIPES_LOADED =
            "br.com.scagliabaroni.bakingapp.data.total_recipes_loaded";

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RecipesLoadIntentService(String name) {
        super(name);
    }

    public RecipesLoadIntentService() {
        super(RecipesLoadIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RecipesUtils.loadRecipes(intent, this.getBaseContext());
    }

    /**
     * Starts this service to perform data load action. If the service is already performing a
     * task this action will be queued automatic by the IntentService engine.
     *
     * @see IntentService
     */
    public static void startRecipesLoad(Context context) {
        Intent intent = new Intent(context, RecipesLoadIntentService.class);
        context.startService(intent);
    }

    /**
     * Starts this service to perform data load action, but stop loading if there is any data
     * in database already.
     * If the service is already performing a task this action will be queued automatic
     * by the IntentService engine.
     *
     * @see IntentService
     */
    public static void startRecipesLoadStopWhenDataExists(Context context) {
        Intent intent = new Intent(context, RecipesLoadIntentService.class);
        intent.setAction(ACTION_STOP_WHEN_DATA_EXISTS);
        context.startService(intent);
    }
}