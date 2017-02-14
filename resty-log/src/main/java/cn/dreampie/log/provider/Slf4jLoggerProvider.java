package cn.dreampie.log.provider;

import cn.dreampie.log.Colorer;
import cn.dreampie.log.Logger;

/**
 * Created by ice on 15-1-5.
 */
public class Slf4jLoggerProvider implements LoggerProvider {

    public Logger getLogger(Class clazz) {
        return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    public Logger getLogger(String clazzName) {
        return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(clazzName));
    }


    public class Slf4JLogger extends Logger {

        private org.slf4j.Logger logger;

        Slf4JLogger(org.slf4j.Logger logger) {
            this.logger = logger;
        }

        public void debug(String message) {
            logger.debug(Colorer.cyan(message));
        }

        public void debug(String message, Object... args) {
            logger.debug(Colorer.cyan(String.format(message, args)));
        }

        public void debug(String message, Throwable t) {
            logger.debug(Colorer.cyan(message), t);
        }

        public void debug(String message, Throwable t, Object... args) {
            logger.debug(Colorer.cyan(String.format(message, args)), t);
        }

        public void info(String message) {
            logger.info(Colorer.blue(message));
        }

        public void info(String message, Object... args) {
            logger.info(Colorer.blue(String.format(message, args)));
        }

        public void info(String message, Throwable t) {
            logger.info(Colorer.blue(message), t);
        }

        public void info(String message, Throwable t, Object... args) {
            logger.info(Colorer.blue(String.format(message, args)), t);
        }

        public void warn(String message) {
            logger.warn(Colorer.yellow(message));
        }

        public void warn(String message, Object... args) {
            logger.warn(Colorer.yellow(String.format(message, args)));
        }

        public void warn(String message, Throwable t) {
            logger.warn(Colorer.yellow(message), t);
        }

        public void warn(String message, Throwable t, Object... args) {
            logger.warn(Colorer.yellow(String.format(message, args)), t);
        }

        public void error(String message) {
            logger.error(Colorer.red(message));
        }

        public void error(String message, Object... args) {
            logger.error(Colorer.red(String.format(message, args)));
        }

        public void error(String message, Throwable t) {
            logger.error(Colorer.red(message), t);
        }

        public void error(String message, Throwable t, Object... args) {
            logger.error(Colorer.red(String.format(message, args)), t);
        }

        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }


        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }


        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }


        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }
    }
}
