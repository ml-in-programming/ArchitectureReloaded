package org.jetbrains.research.groups.ml_methods.logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Logging {
    public static Logger getLogger(Class<?> aClass) {
        final Logger logger = Logger.getLogger(aClass);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender(new PatternLayout("%p [%c.%M] - %m%n")));
        return logger;
    }
}
