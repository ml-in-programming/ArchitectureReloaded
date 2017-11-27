/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.reporting;

import com.google.gson.Gson;
import com.intellij.openapi.application.PermanentInstallationID;

public class Reporter {
    public static <T> String createReportLine(String recorderId, String actionType, T data) {
        String recorderVersion = "1";
        String userId = PermanentInstallationID.get();
        String sessionId = "random_session_id";
        int bucket = -1;
        String json = Utils.gson.toJson(data);
        long time = System.currentTimeMillis();
        return String.format("%d\t%s\t%s\t%s\t%s\t%d\t%s\t%s",
                time, recorderId, recorderVersion, userId, sessionId, bucket, actionType, json);
    }
}
