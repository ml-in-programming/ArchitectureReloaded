package org.jetbrains.research.groups.ml_methods.error_reporting;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Provides functionality to throw a runtime exception when the action is invoked. It is used to test the error reporting
 * functions. Don't forget to register the action in plugin.xml to make it work.
 */
public class TriggerExceptionAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        throw new RuntimeException("I'm an artificial exception to test error reporting!");
    }
}
