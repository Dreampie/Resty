package cn.dreampie.security;

/**
 * Created by wangrenhui on 15/1/6.
 */
public abstract class CacheLoader<K, V> {
  public abstract V load(K var1) throws Exception;
}
