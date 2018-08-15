package org.jetbrains.research.groups.ml_methods.error_reporting;

import com.intellij.errorreport.bean.ErrorBean;

import java.util.Arrays;

/**
 * Extends the standard class to provide the hash of the thrown exception stack trace.
 * @author patrick (17.06.17).
 */
class GitHubErrorBean extends ErrorBean {

    private String myExceptionHash;

    GitHubErrorBean(Throwable throwable, String lastAction) {
        super(throwable, lastAction);
        final int hashCode = Arrays.hashCode(throwable.getStackTrace());
        myExceptionHash = String.valueOf(hashCode);
    }

    String getExceptionHash() {
        return myExceptionHash;
    }
}
