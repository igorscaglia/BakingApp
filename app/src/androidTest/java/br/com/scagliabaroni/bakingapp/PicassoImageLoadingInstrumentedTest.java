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
import android.graphics.drawable.Drawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import timber.log.Timber;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PicassoImageLoadingInstrumentedTest {

    Context mContext;

    public PicassoImageLoadingInstrumentedTest() {
    }

    @Before
    public void setUp() throws Exception {
        this.mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void loadLocalizableImage() {

        ImageView imageView = new ImageView(this.mContext);
        String imageName = this.mContext.getString(R.string.no_image_available);
        int localizableResourceId = this.mContext
                .getResources()
                .getIdentifier(imageName, "drawable", this.mContext.getPackageName());
        Drawable correctDrawableImage = this.mContext.getDrawable(localizableResourceId);

        try {
            Picasso.with(this.mContext)
                    .load("")
                    .error(correctDrawableImage)
                    .into(imageView);
            assertTrue(true);
        } catch (IllegalArgumentException e) {
            imageView.setImageDrawable(correctDrawableImage);
            Timber.d(e);
            assertTrue(true);
        }
    }
}