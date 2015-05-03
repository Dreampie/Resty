package cn.dreampie.orm.aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by wangrenhui on 15/1/2.
 */
public interface Aspect {

  public Object aspect(InvocationHandler ih, Object proxy, Method method, Object[] args) throws Throwable;

}
