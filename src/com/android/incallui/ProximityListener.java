/*
 * Copyright (C) 2013-2014 dimfish
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

package com.android.incallui;

import android.app.StatusBarManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public final class ProximityListener {
    private static final String TAG = "ProximityListener";
    private static final boolean DEBUG = true;
    private static final boolean VDEBUG = false;

    private static final float PROXIMITY_THRESHOLD = 5.0f;

    private long mLastProximityEventTime;
    private boolean mActive;

    private SensorManager mSensorManager;
    private StatusBarManager mStatusBarManager;
    private Sensor mSensor;


    public ProximityListener(Context context) {
        mActive = false;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mStatusBarManager = (StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    public void enable(boolean enable) {
        if (DEBUG) Log.d(TAG, "enable(" + enable + ")");
        synchronized (this) {
            mActive = false;
            if (enable) {
                mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                updateStatusBar();
                mSensorManager.unregisterListener(mSensorListener);
            }
        }
    }

    public boolean isActive() {
        return mActive;
    }

    private void updateStatusBar() {
        if (mActive)
            mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND 
                | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
                | StatusBarManager.DISABLE_NOTIFICATION_TICKER 
                | StatusBarManager.DISABLE_HOME
                | StatusBarManager.DISABLE_RECENT 
                | StatusBarManager.DISABLE_BACK
                | StatusBarManager.DISABLE_SEARCH);
        else
            mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
    }   

    SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                float distance = event.values[0];
                boolean oldStatus = mActive;
                // compare against getMaximumRange to support sensors that only return 0 or 1
                mActive = (distance >= 0.0 && distance < PROXIMITY_THRESHOLD &&
                           distance < mSensor.getMaximumRange());

                if (mActive != oldStatus) updateStatusBar();

                if (VDEBUG) Log.d(TAG, "mProximityListener.onSensorChanged active: " + mActive);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignore
        }
    };
}
