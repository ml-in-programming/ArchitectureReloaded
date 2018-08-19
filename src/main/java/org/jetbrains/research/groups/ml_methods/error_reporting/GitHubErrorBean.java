package org.jetbrains.research.groups.ml_methods.error_reporting;

import com.intellij.errorreport.bean.ErrorBean;

import java.util.Arrays;

/**
 * Extends the standard class to provide the hash of the thrown exception stack trace.
 */
class GitHubErrorBean extends ErrorBean {

    private String myExceptionHash;

    GitHubErrorBean(Throwable throwable, String lastAction) {
        super(throwable, lastAction);
        final long hashCode = Integer.toUnsignedLong(Arrays.hashCode(throwable.getStackTrace()));
        myExceptionHash = Long.toHexString(hashCode);
    }

    String getExceptionHash() {
        return myExceptionHash;
    }
}
