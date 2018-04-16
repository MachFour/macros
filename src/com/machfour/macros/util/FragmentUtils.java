/*
 * Copyright 2016, The Android Open Source Project
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

// copied from package com.example.android.architecture.blueprints.todoapp.com.machfour.macros.util;

package com.machfour.macros.util;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.machfour.macros.core.MacrosUtils;

/**
 * This provides methods to help Activities load their UI.
 */
public class FragmentUtils {

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addFragment(@NonNull FragmentManager fm, @NonNull Fragment f, int frameId) {
        addFragment(fm, f, frameId, false);
    }

    private static void addFragment(@NonNull FragmentManager fm, @NonNull Fragment f,
                                    int frameId, boolean now) {
        MacrosUtils.exceptionIfNull(fm, "fragmentManager");
        MacrosUtils.exceptionIfNull(f, "fragment");
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(frameId, f);
        if (now) {
            transaction.commitNow();
        } else {
            transaction.commit();
        }
    }

    public static void addFragmentNow(@NonNull FragmentManager fm, @NonNull Fragment f, int frameId) {
        addFragment(fm, f, frameId, true);
    }

}

