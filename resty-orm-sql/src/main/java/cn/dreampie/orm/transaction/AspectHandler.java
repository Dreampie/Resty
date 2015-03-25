package cn.dreampie.orm.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class AspectHandler implements InvocationHandler {

  private Object target = null;
  private Class<? extends Aspect>[] aspects = null;
  private int index = 0;


  public AspectHandler(Object target, Class<? extends Aspect>[] aspects) {
    this.target = target;
    this.aspects = aspects;
  }


  public Object getTarget() {
    return target;
  }


  public void setTarget(Object target) {
    this.target = target;
  }

  public Class<? extends Aspect>[] getAspects() {
    return aspects;
  }


  public void setAspects(Class<? extends Aspect>... aspects) {
    this.aspects = aspects;
  }

  /**
   * 委托方法
   *
   * @param proxy  代理对象
   * @param method 代理方法
   * @param args   方法参数
   */
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    Object result = null;
    if (index < aspects.length) {
      result = aspects[index++].newInstance().aspect(this, proxy, method, args);
    } else if (index++ == aspects.length) {
      result = method.invoke(target, args);
    }
    return result;
  }

}