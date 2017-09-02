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

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.com.scagliabaroni.bakingapp.activity.RecipesActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RecipesRecyclerViewTest {

    /**
     * The ActivityTestRule is a rule provided by Android used for functional testing of a single
     * activity. The activity that will be launched before each test that's annotated with @Test
     * and before methods annotated with @Before.
     * <p>
     * The activity will be terminated after the test and methods annotated with @After are
     * complete. This rule allows you to directly access the activity during the test.
     */
    @Rule
    public ActivityTestRule<RecipesActivity> mActivityTestRule =
            new ActivityTestRule<>(RecipesActivity.class);

    @Test
    public void identifyFirstItemText() {
        // First, scroll to the first position
        onView(ViewMatchers.withId(R.id.RecipesRecyclerView))
                .perform(RecyclerViewActions.scrollToPosition(0));
        // Match the text as Nutella Pie
        onView(withText("Nutella Pie"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void identifyLastItemText() {
        // First, scroll to the last position
        onView(ViewMatchers.withId(R.id.RecipesRecyclerView))
                .perform(RecyclerViewActions.scrollToPosition(3));
        // Match the text as Cheesecake
        onView(withText("Cheesecake"))
                .check(matches(isDisplayed()));
    }
}