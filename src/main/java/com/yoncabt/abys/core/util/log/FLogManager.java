package com.yoncabt.abys.core.util.log;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.collections.MapUtils;

import org.apache.log4j.Logger;

/**
 * Support for injecting a JDK logger. Uses the class name of the injecting
 * class as the category.
 */
public class FLogManager {

    /**
     * Başlayan her Thread için oluşturulan tekil threadId parametresi burada
     * tutuluyor. Oluşturulan bir RequestFilter yardımıyla bu Map kontrol
     * ediliyor.
     */
    private final static Map<Long, String> requstIdMap = MapUtils.synchronizedMap(new HashMap());

    public static void setRequestId(long threadId, String requestId) {
        requstIdMap.put(threadId, requestId);
    }

    private Class<?> loggerClass;

    private Logger log;

    public static FLogManager getLogger(Class<?> clazz) {
        FLogManager ret = new FLogManager();
        ret.log = Logger.getLogger(
                clazz.getName().replace("$Proxy$_$$_WeldSubclass", "")//proxy ismini kaldırmaya çalışalım
        );
        ret.loggerClass = clazz;
        return ret;
    }

    public void debug(String message) {
        log.debug(message);
    }

    public void debug(Object value) {
        log.debug(String.valueOf(value));
    }

    public void info(String message) {
        log.info(message);
    }

    public void error(String message) {
        log.error(message);
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void fatal(String message) {
        log.fatal(message);
    }

    public void debug(Throwable throwable) {
        log.debug("", throwable);
    }

    public void info(Throwable throwable) {
        log.info("", throwable);
    }

    public void error(Throwable throwable) {
        log.error("", throwable);
    }

    public void warn(Throwable throwable) {
        log.warn("", throwable);
    }

    public void fatal(Throwable throwable) {
        log.fatal("", throwable);
    }

    public void debug(String message, Throwable throwable) {
        log.debug(message, throwable);
    }

    public void info(String message, Throwable throwable) {
        log.info(message, throwable);
    }

    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    public void warn(String message, Throwable throwable) {
        log.warn(message, throwable);
    }

    public void fatal(String message, Throwable throwable) {
        log.fatal(message, throwable);
    }

    public void log(Level level, String format, Object[] args) {
        String msg = new MessageFormat(format).format(args);
        if (level == Level.WARNING) {
            log.warn(msg);
        } else if (level == Level.INFO) {
            log.info(msg);
        } else if (level == Level.SEVERE) {
            log.fatal(msg);
        } else {
            log.debug(msg);
        }
    }
}
