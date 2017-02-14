package cn.dreampie.log.provider;


import cn.dreampie.log.Logger;

/**
 * Created by ice on 14-12-19.
 */
public interface LoggerProvider {
    public Logger getLogger(Class clazz);

    public Logger getLogger(String clazzName);

}
