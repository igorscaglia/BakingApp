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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.LocalBroadcastManager;
import android.test.ProviderTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.com.scagliabaroni.bakingapp.service.RecipesLoadIntentService;
import br.com.scagliabaroni.bakingapp.common.RecipesUtils;
import br.com.scagliabaroni.bakingapp.infrastructure.RecipesProvider;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecipesLoadBroadcastReceiverTest
        extends ProviderTestCase2<RecipesProvider> {

    public RecipesLoadBroadcastReceiverTest() {
        super(RecipesProvider.class, RecipesProvider.AUTHORITY);
    }

    @Before
    public void setUp() throws Exception {
        // Doing this we knew what kind of context we'll use
        // when calling .getContext() from this class.
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Test
    public void broadcastReceiverTriggered() {

        // The configured filter for receiver
        IntentFilter receiverFilter =
                new IntentFilter(RecipesLoadIntentService.ACTION_BROADCAST_RESULT);

        // Configure receiver to assert the onreceive result
        LocalBroadcastManager.getInstance(this.mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // has the total recipes loaded value
                final int totalRecipesLoaded = intent
                        .getIntExtra(RecipesLoadIntentService
                                .EXTENDED_DATA_TOTAL_RECIPES_LOADED, 0);

                // Should be always 4 recipes loaded
                assertEquals(4, totalRecipesLoaded);
            }
        }, receiverFilter);

        // Execute the method which intent service calls
        Intent intent = new Intent(this.mContext, RecipesLoadIntentService.class);
        RecipesUtils.loadRecipes(intent, this.mContext);
    }
}