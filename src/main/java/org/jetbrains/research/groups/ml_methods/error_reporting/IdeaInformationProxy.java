package org.jetbrains.research.groups.ml_methods.error_reporting;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.util.SystemProperties;

import java.util.LinkedHashMap;

/**
 * Collects information about the running IDEA and the error
 */
class IdeaInformationProxy {
    static LinkedHashMap<String, String> getKeyValuePairs(GitHubErrorBean error,
                                                          Application application,
                                                          ApplicationInfoEx appInfo,
                                                          ApplicationNamesInfo namesInfo) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>(21);

        params.put("error.description", error.getDescription());

        params.put("Plugin Name", error.getPluginName());
        params.put("Plugin Version", error.getPluginVersion());

        params.put("OS Name", SystemProperties.getOsName());
        params.put("Java version", SystemProperties.getJavaVersion());
        params.put("Java vm vendor", SystemProperties.getJavaVmVendor());

        params.put("App Name", namesInfo.getProductName());
        params.put("App Full Name", namesInfo.getFullProductName());
        params.put("App Version name", appInfo.getVersionName());
        params.put("Is EAP", Boolean.toString(appInfo.isEAP()));
        params.put("App Build", appInfo.getBuild().asString());
        params.put("App Version", appInfo.getFullVersion());

        params.put("Last Action", error.getLastAction());

        params.put("error.message", error.getMessage());
        params.put("error.stacktrace", error.getStackTrace());
        params.put("error.hash", error.getExceptionHash());

        for (Attachment attachment : error.getAttachments()) {
            params.put("attachment.name", attachment.getName());
            params.put("attachment.value", attachment.getEncodedBytes());
        }
        return params;
    }
}