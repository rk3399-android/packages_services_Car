/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.car;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.IVolumeController;
import android.view.KeyEvent;

import com.android.car.hal.AudioHalService;
import com.android.car.vehiclenetwork.VehicleNetworkConsts.VehicleAudioContextFlag;

/**
 * Handles car volume controls.
 *
 * It delegates to a {@link com.android.car.CarVolumeService.CarVolumeController} to do proper
 * volume controls based on different car properties.
 *
 * @hide
 */
public class CarVolumeService {
    private static final String TAG = "CarVolumeService";

    // TODO: need to have a policy to define the default context
    public static int DEFAULT_CAR_AUDIO_CONTEXT =
            VehicleAudioContextFlag.VEHICLE_AUDIO_CONTEXT_MUSIC_FLAG;
    private final Context mContext;
    private final AudioHalService mAudioHal;
    private final CarAudioService mAudioService;
    private final CarInputService mInputService;

    private CarVolumeController mCarVolumeController;

    public static int androidStreamToCarUsage(int logicalAndroidStream) {
        return CarAudioAttributesUtil.getCarUsageFromAudioAttributes(
                new AudioAttributes.Builder()
                        .setLegacyStreamType(logicalAndroidStream).build());
    }

    public CarVolumeService(Context context, CarAudioService audioService, AudioHalService audioHal,
                            CarInputService inputService) {
        mContext = context;
        mAudioHal = audioHal;
        mAudioService = audioService;
        mInputService = inputService;
    }

    public void init() {
        mCarVolumeController = CarVolumeControllerFactory.createCarVolumeController(mContext,
                mAudioService, mAudioHal, mInputService);

        mCarVolumeController.init();
        mInputService.setVolumeKeyListener(mCarVolumeController);
    }

    public void setStreamVolume(int streamType, int index, int flags) {
        mCarVolumeController.setStreamVolume(streamType, index, flags);
    }

    public void setVolumeController(IVolumeController controller) {
        mCarVolumeController.setVolumeController(controller);
    }

    public int getStreamMaxVolume(int stream) {
        return mCarVolumeController.getStreamMaxVolume(stream);
    }

    public int getStreamMinVolume(int stream) {
        return mCarVolumeController.getStreamMinVolume(stream);
    }

    public int getStreamVolume(int stream) {
        return mCarVolumeController.getStreamVolume(stream);
    }

    /**
     * Abstraction layer for volume controls, so that we don't have if-else check for audio
     * properties everywhere.
     */
    public static abstract class CarVolumeController implements CarInputService.KeyEventListener {
        abstract void init();
        abstract public void setStreamVolume(int stream, int index, int flags);
        abstract public void setVolumeController(IVolumeController controller);
        abstract public int getStreamMaxVolume(int stream);
        abstract public int getStreamMinVolume(int stream);
        abstract public int getStreamVolume(int stream);
    }
}
