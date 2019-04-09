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
import com.hazelcast.query.*;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category(QuickTest.class)
public class FulltextIndexIntegrationTest extends HazelcastTestSupport {

    private static final String MAP_NAME = "map";

    public static class Quote implements Serializable {
        private String author;
        private String text;

        public Quote(String author, String text) {
            this.author = author;
            this.text = text;
        }

        public String getAuthor() {
            return author;
        }

        public String getText() {
            return text;
        }
    }

    @Override
    protected Config getConfig() {
        Config config = super.getConfig();
        config.getMapConfig(MAP_NAME);
        return config;
    }

    private IMap<String, Quote> getQuotes() {
        HazelcastInstance instance = createHazelcastInstance();
        IMap<String, Quote> map = instance.getMap(MAP_NAME);
        map.addFulltextIndex("text");

        map.put("1", new Quote("Donald Knuth", "Beware of bugs in the above code; I have only proved it correct, not tried it."));
        map.put("2", new Quote("Donald Knuth", "I can’t go to a restaurant and order food because I keep looking at the fonts on the menu."));
        map.put("3", new Quote("Edsger W. Dijkstra",
                "If debugging is the process of removing bugs, then programming must be the process of putting them in."));
        return map;
    }

    @Test
    public void testFulltextPredicate() {
        IMap<String, Quote> map = getQuotes();

        PredicateBuilder builder = new PredicateBuilder();
        EntryObject entryObject = builder.getEntryObject();
        Predicate predicate = entryObject.get("text").fulltext("removing bugs").and(entryObject.get("author").equal("Donald Knuth"));
        Collection<Quote> values = map.values(predicate);
        assertThat(values, hasSize(1));
        assertThat(values.iterator().next().getAuthor(), is("Donald Knuth"));
    }

    @Test
    public void testFulltextSql() {
        IMap<String, Quote> map = getQuotes();

        Collection<Quote> values = map.values(new SqlPredicate("text contains 'removing bugs'"));
        assertThat(values, hasSize(2));
        Iterator<Quote> it = values.iterator();
        assertThat(it.next().getAuthor(), is("Edsger W. Dijkstra"));
        assertThat(it.next().getAuthor(), is("Donald Knuth"));
    }

}




