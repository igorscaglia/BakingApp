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

package br.com.scagliabaroni.bakingapp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import br.com.scagliabaroni.bakingapp.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Responsible to show the step detail previously selected on RecipeDetailActivity when in
 * phone mode
 */
public class RecipeDetailStepActivity extends AppCompatActivity {
    @BindView(R.id.MainToolbar)
    Toolbar mMainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail_step);
        // Enable ButterKnife
        ButterKnife.bind(this);
        // Set the toolbar as an action bar
        setSupportActionBar(this.mMainToolbar);
    }
}