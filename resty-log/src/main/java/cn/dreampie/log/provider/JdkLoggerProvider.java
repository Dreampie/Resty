package cn.dreampie.log.provider;


import cn.dreampie.log.Colorer;
import cn.dreampie.log.Logger;

import java.util.logging.Level;

/**
 * Created by ice on 15-1-5.
 */
public class JdkLoggerProvider implements LoggerProvider {

    public Logger getLogger(Class clazz) {
        return new JdkLogger(java.util.logging.Logger.getLogger(clazz.getName()));
    }

    public Logger getLogger(String clazzName) {
        return new JdkLogger(java.util.logging.Logger.getLogger(clazzName));
    }


    public class JdkLogger extends Logger {

        private java.util.logging.Logger logger;
        private String clazzName;

        JdkLogger(java.util.logging.Logger logger) {
            this.logger = logger;
        }


        public void debug(String message) {
            logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.cyan(message));
        }

        public void debug(String message, Throwable t) {
            logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.cyan(message), t);
        }

        public void debug(String message, Object... args) {
            logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.cyan(String.format(message, args)));
        }

        public void debug(String message, Throwable t, Object... args) {
            logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.cyan(String.format(message, args)), t);
        }


        public void info(String message) {
            logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.blue(message));
        }

        public void info(String message, Throwable t) {
            logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.blue(message), t);
        }

        public void info(String message, Object... args) {
            logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.blue(String.format(message, args)));
        }

        public void info(String message, Throwable t, Object... args) {
            logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.blue(String.format(message, args)), t);
        }

        public void warn(String message) {
            logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), Colorer.yellow(message));
        }

        public void warn(String message, Throwable t) {
            logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(),  Colorer.yellow(message), t);
        }

        public void warn(String message, Object... args) {
            logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(),  Colorer.yellow(String.format(message, args)));
        }

        public void warn(String message, Throwable t, Object... args) {
            logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(),  Colorer.yellow(String.format(message, args)), t);
        }

        public void error(String message) {
            logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(),  Colorer.red(message));
        }

        public void error(String message, Throwable t) {
            logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(),  Colorer.red(message), t);
        }

        public void error(String message, Object... args) {
            logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(),  Colorer.red(String.format(message, args)));
        }

        public void error(String message, Throwable t, Object... args) {
            logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(),  Colorer.red(String.format(message, args)), t);
        }

        public boolean isDebugEnabled() {
            return logger.isLoggable(Level.FINE);
        }


        public boolean isInfoEnabled() {
            return logger.isLoggable(Level.INFO);
        }


        public boolean isWarnEnabled() {
            return logger.isLoggable(Level.WARNING);
        }


        public boolean isErrorEnabled() {
            return logger.isLoggable(Level.SEVERE);
        }

    }

}
