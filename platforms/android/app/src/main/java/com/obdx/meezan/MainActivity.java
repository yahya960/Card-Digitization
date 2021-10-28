/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.obdx.meezan;

import android.content.ComponentName;
import android.content.Intent;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.os.Bundle;

import com.obdx.meezan.THSCard.app.AppConstants;

import org.apache.cordova.*;

import static android.nfc.cardemulation.CardEmulation.ACTION_CHANGE_DEFAULT;
import static android.nfc.cardemulation.CardEmulation.EXTRA_CATEGORY;
import static android.nfc.cardemulation.CardEmulation.EXTRA_SERVICE_COMPONENT;

public class MainActivity extends CordovaActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);

        checkTapAndPay();
    }
    public void checkTapAndPay() {
        if (!isEmulator()) {
            Intent activate = new Intent();
            activate.setAction(ACTION_CHANGE_DEFAULT);
            activate.putExtra(EXTRA_SERVICE_COMPONENT, new ComponentName(this,
                    AppConstants.CANONICAL_PAYMENT_SERVICENAME));
            activate.putExtra(EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT);
            startActivity(activate);
        }
    }
    private static boolean isEmulator() {
        String model = Build.MODEL;
//        AppLogger.d(TAG, "model=" + model);
        String product = Build.PRODUCT;
//        AppLogger.d(TAG, "product=" + product);
        boolean isEmulator = false;
        if (product != null) {
            isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
        }
//        AppLogger.d(TAG, "isEmulator=" + isEmulator);
        return isEmulator;
    }
}
