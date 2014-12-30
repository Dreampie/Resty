/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/

package cn.dreampie.orm.cache;

/**
 * Event object. Sent to {@link cn.dreampie.orm.cache.CacheManager} to let it know
 * of cache purge events. 
 *
 */
public class CacheEvent {

    public static final CacheEvent ALL = new CacheEvent(null);


    public enum  CacheEventType{
        /**
         * This type means that all caches need to be cleared. 
         */
        ALL,

        /**
         * This type means that only a cache for a specific group (table) needs to be cleared.
         */
        GROUP
    }
    private String source, group;
    private CacheEventType type;


    /**
     * Creates a new event type of {@link cn.dreampie.orm.cache.CacheEvent.CacheEventType#GROUP}.
     * Usually an application creates an instance of this event to clear a group of caches for a table.
     *
     *
     * @param group name of group (usually name of table), cannot be null.
     * @param source string representation of source of event, whatever that means for the application. This event will
     * be broadcast to listeners, and they might use this piece of information. Can be null. 
     */
    public CacheEvent( String group, String source){
        if(group == null)
            throw new IllegalArgumentException("group canot be null");
        
        this.type = CacheEventType.GROUP;
        this.source = source;
        this.group = group;
    }


    /**
     * Creates a new event type of {@link cn.dreampie.orm.cache.CacheEvent.CacheEventType#ALL}
     *
     * @param source string representation of source of event, whatever that means for the application. This event will
     * be broadcast to listeners, and they might use this piece of information. Can be null. 
     */
    public CacheEvent(String source){
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

    @Override
    public String toString() {
        return "CacheEvent{" +
                "source='" + source + '\'' +
                ", group='" + group + '\'' +
                ", type=" + type +
                '}';
    }
}
