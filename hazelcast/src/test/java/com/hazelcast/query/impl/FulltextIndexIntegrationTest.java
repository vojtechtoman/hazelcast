/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.query.impl;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Serializable;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@Category(QuickTest.class)
public class FulltextIndexIntegrationTest extends HazelcastTestSupport {

    private static final String MAP_NAME = "map";

    @Override
    protected Config getConfig() {
        Config config = super.getConfig();
        config.getMapConfig(MAP_NAME);
        return config;
    }

    public static class Doc implements Serializable {
        private String text;

        public Doc(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    @Test
    public void testSimpleQuery() {
        HazelcastInstance instance = createHazelcastInstance();
        IMap<String, Doc> map = instance.getMap(MAP_NAME);
        map.addFulltextIndex("text");

        map.put("foo", new Doc("Quick brown fox jumps over a lazy dog"));
        map.put("bar", new Doc("I'm going to make him an offer he can't refuse."));

        Predicate predicate = Predicates.fulltext("text", "brown fox");
        Collection<Doc> values = map.values(predicate);
        assertThat(values, hasSize(1));
        assertThat(values.iterator().next().getText(), startsWith("Quick brown"));
    }

}




