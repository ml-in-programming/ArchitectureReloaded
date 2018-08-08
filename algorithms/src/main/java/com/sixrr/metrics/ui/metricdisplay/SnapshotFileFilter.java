/*
 * Copyright 2005, Sixth and Red River Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.metrics.ui.metricdisplay;

import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NonNls;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class SnapshotFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        @NonNls final String fileName = f.getName();
        return fileName.endsWith(".met");
    }

    @Override
    public String getDescription() {
        return MetricsReloadedBundle.message("snapshot.files.description");
    }
}
