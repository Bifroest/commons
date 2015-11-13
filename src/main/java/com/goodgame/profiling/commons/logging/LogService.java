package com.goodgame.profiling.commons.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * A service class which offers log capability with SL4J. At the moment, as
 * backend LOG4Jv2 is used.
 */
public final class LogService {

    private final static Logger logger;
    private final static XLogger xlogger;

    static {
        logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        xlogger = XLoggerFactory.getXLogger(org.slf4j.ext.XLogger.ROOT_LOGGER_NAME);
    }

    private LogService() {
    }

    /**
     * Gets a class specific logger
     *
     * @param c the class
     * @return a Logger instance
     */
    public static Logger getLogger(Class c) {
        return LoggerFactory.getLogger(c);
    }

    /**
     * Gets the root logger
     *
     * @return a Logger instance
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Gets a class specific extended logger
     *
     * @param c the class
     * @return a Logger instance
     */
    public static XLogger getXLogger(Class c) {
        return XLoggerFactory.getXLogger(c);
    }

    /**
     * Gets the extended root logger
     *
     * @return a Logger instance
     */
    public static XLogger getXLogger() {
        return xlogger;
    }
}
