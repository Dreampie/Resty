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

package cn.dreampie.orm;

import java.util.TreeMap;

/**
 * A case insensitive map for <code>java.lang.String</code> keys. The current implementation is based on
 * {@link java.util.TreeMap}, so it does not accept <code>null</code> keys and keeps entries ordered by case
 * insensitive alphabetical order of keys.
 *
 */
public class CaseInsensitiveMap<V> extends TreeMap<String, V> {

    public CaseInsensitiveMap() {
        super(String.CASE_INSENSITIVE_ORDER);
    }
}
