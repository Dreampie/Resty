package cn.dreampie.cache;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Event object. Sent to {@link CacheProvider} to let it know
 * of cache purge events.
 */
public class CacheEvent {

  public static final CacheEvent ALL = new CacheEvent(null);
  private String source, group;
  private CacheEventType type;

  /**
   * Creates a new event type of {@link CacheEvent.CacheEventType#GROUP}.
   * Usually an application creates an instance of this event to clear a group of caches for a table.
   *
   * @param group  name of group (usually name of table), cannot be null.
   * @param source string representation of source of event, whatever that means for the application. This event will
   *               be broadcast to listeners, and they might use this piece of information. Can be null.
   */
  public CacheEvent(String group, String source) {
    checkNotNull(group, "group could not be null");

    this.type = CacheEventType.GROUP;
    this.source = source;
    this.group = group;
  }


  /**
   * Creates a new event type of {@link CacheEvent.CacheEventType#ALL}
   *
   * @param source string representation of source of event, whatever that means for the application. This event will
   *               be broadcast to listeners, and they might use this piece of information. Can be null.
   */
  public CacheEvent(String source) {
    this.type = CacheEventType.ALL;
    this.source = source;
  }

  public String getSource() {
    return source;
  }

  public CacheEventType getType() {
    return type;
  }

  public String getGroup() {
    return group;
  }

  public String toString() {
    return "CacheEvent{" +
        "source='" + source + '\'' +
        ", group='" + group + '\'' +
        ", type=" + type +
        '}';
  }


  public enum CacheEventType {
    /**
     * This type means that all caches need to be cleared.
     */
    ALL,

    /**
     * This type means that only a cache for a specific group (table) needs to be cleared.
     */
    GROUP
  }
}
