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

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.config.Logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class StatsSender {
    private static final Logger LOG = Logging.getLogger(StatsSender.class);
    private static final String infoUrl = "https://www.jetbrains.com/config/features-service-status.json";

    @Nullable
    private static StatsServerInfo requestServiceUrl() {
        try {
            String json = Request.Get(infoUrl).execute().returnContent().asString();
            StatsServerInfo info = Utils.gson.fromJson(json, StatsServerInfo.class);
            if (info.isServiceAlive()) {
                return info;
            }
        } catch (IOException e) {
            LOG.debug(e);
        }
        return null;
    }

    @NotNull
    private static Request createRequest(StatsServerInfo info, String text, boolean compress) throws IOException {
        if (!compress) {
            return Request.Post(info.url).bodyString(text, ContentType.TEXT_HTML);
        }
        Request r = Request.Post(info.urlForZipBase64Content).bodyByteArray(compress(text));
        r.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        return r;
    }

    @NotNull
    private static byte[] compress(String text) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPOutputStream base64Output = new GZIPOutputStream(new Base64OutputStream(os));
        base64Output.write(text.getBytes());
        base64Output.close();
        return os.toByteArray();
    }

    public static boolean send(String text, boolean compress) {
        StatsServerInfo info = requestServiceUrl();
        if (info == null) {
            return false;
        }
        try {
            Response response = createRequest(info, text, compress).execute();
            int code = response.handleResponse(x -> x.getStatusLine().getStatusCode());
            if (code >= 200 && code < 300) {
                return true;
            }
        } catch (IOException e) {
            LOG.debug(e);
        }
        return false;
    }
}

class StatsServerInfo {
    public String status;
    public String url;
    public String urlForZipBase64Content;

    public boolean isServiceAlive() {
        return "ok".equals(status);
    }
}
