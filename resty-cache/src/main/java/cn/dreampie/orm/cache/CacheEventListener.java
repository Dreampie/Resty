package cn.dreampie.orm.cache;

/**
 * CacheEventListener
 */
public interface CacheEventListener {
    void onFlush(CacheEvent event);
}
