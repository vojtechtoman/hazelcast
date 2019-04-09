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

import com.hazelcast.nio.serialization.Data;
import com.hazelcast.query.impl.fulltext.Document;
import com.hazelcast.query.impl.fulltext.FulltextUtils;
import com.hazelcast.query.impl.fulltext.InvertedIndex;
import com.hazelcast.query.impl.fulltext.SearchResult;

import java.util.*;

import static com.hazelcast.query.impl.AbstractIndex.NULL;

public class FulltextIndexStore extends BaseIndexStore {

    private InvertedIndex invertedIndex = new InvertedIndex();
    private final IndexFunctor<Comparable, QueryableEntry> addFunctor;
    private final IndexFunctor<Comparable, Data> removeFunctor;

    public FulltextIndexStore(IndexCopyBehavior copyOn) {
        super(copyOn);
        if (copyOn == IndexCopyBehavior.COPY_ON_WRITE) {
            addFunctor = new CopyOnWriteAddFunctor();
            removeFunctor = new CopyOnWriteRemoveFunctor();
        } else {
            addFunctor = new AddFunctor();
            removeFunctor = new RemoveFunctor();
        }
    }

    @Override
    Object insertInternal(Comparable value, QueryableEntry record) {
        return addFunctor.invoke(value, record);
    }

    @Override
    Object removeInternal(Comparable value, Data recordKey) {
        return removeFunctor.invoke(value, recordKey);
    }

    @Override
    public Comparable canonicalizeQueryArgumentScalar(Comparable value) {
        // Using a storage representation for arguments here to save on
        // conversions later.
        return canonicalizeScalarForStorage(value);
    }

    @Override
    public Comparable canonicalizeScalarForStorage(Comparable value) {
        // Assuming on-heap overhead of 12 bytes for the object header and
        // allocation granularity by modulo 8, there is no point in trying to
        // represent a value in less than 4 bytes.

        if (!(value instanceof Number)) {
            return value;
        }

        Class clazz = value.getClass();
        Number number = (Number) value;

        if (clazz == Double.class) {
            double doubleValue = number.doubleValue();

            long longValue = number.longValue();
            if (Numbers.equalDoubles(doubleValue, (double) longValue)) {
                return canonicalizeLongRepresentable(longValue);
            }

            float floatValue = number.floatValue();
            if (doubleValue == (double) floatValue) {
                return floatValue;
            }
        } else if (clazz == Float.class) {
            float floatValue = number.floatValue();

            long longValue = number.longValue();
            if (Numbers.equalFloats(floatValue, (float) longValue)) {
                return canonicalizeLongRepresentable(longValue);
            }
        } else if (Numbers.isLongRepresentable(clazz)) {
            return canonicalizeLongRepresentable(number.longValue());
        }

        return value;
    }

    @Override
    public void clear() {
        takeWriteLock();
        try {
            invertedIndex = new InvertedIndex();
        } finally {
            releaseWriteLock();
        }
    }

    @Override
    public Set<QueryableEntry> getRecords(String fulltextQuery) {
        takeReadLock();
        try {
            //MultiResultSet results = createMultiResultSet();
            return asQueryableEntrySet(invertedIndex.search(fulltextQuery));
            //copyToMultiResultSet(results, records);
            //return results;
        } finally {
            releaseReadLock();
        }
    }

    private static AbstractSet<QueryableEntry> asQueryableEntrySet(final Collection<SearchResult> set) {
        return new AbstractSet<QueryableEntry>() {

            @Override
            public Iterator<QueryableEntry> iterator() {
                return new Iterator<QueryableEntry>() {
                    private Iterator<SearchResult> it = set.iterator();
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public QueryableEntry next() {
                        return it.next().getDocument().getEntry();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return set.size();
            }
        };
    }

    @Override
    public Set<QueryableEntry> getRecords(Comparable value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<QueryableEntry> getRecords(Set<Comparable> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<QueryableEntry> getRecords(Comparison comparison, Comparable value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<QueryableEntry> getRecords(Comparable from, boolean fromInclusive, Comparable to, boolean toInclusive) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds entry to the given index map without copying it.
     * Needs to be invoked in a thread-safe way.
     *
     * @see IndexCopyBehavior
     */
    private class AddFunctor implements IndexFunctor<Comparable, QueryableEntry> {

        @Override
        public Object invoke(Comparable value, QueryableEntry entry) {
            if (value != NULL) {
                Document doc = FulltextUtils.makeDocument(entry, value.toString());
                invertedIndex.addDocument(doc);
            }
            return null;
        }

    }

    /**
     * Adds entry to the given index map copying it to secure exclusive access.
     * Needs to be invoked in a thread-safe way.
     *
     * @see IndexCopyBehavior
     */
    private class CopyOnWriteAddFunctor implements IndexFunctor<Comparable, QueryableEntry> {

        @Override
        public Object invoke(Comparable value, QueryableEntry entry) {
            return null; // TODO
        }

    }

    /**
     * Removes entry from the given index map without copying it.
     * Needs to be invoked in a thread-safe way.
     *
     * @see IndexCopyBehavior
     */
    private class RemoveFunctor implements IndexFunctor<Comparable, Data> {

        @Override
        public Object invoke(Comparable value, Data indexKey) {
            return null; // TODO
        }

    }

    /**
     * Removes entry from the given index map copying it to secure exclusive access.
     * Needs to be invoked in a thread-safe way.
     *
     * @see IndexCopyBehavior
     */
    private class CopyOnWriteRemoveFunctor implements IndexFunctor<Comparable, Data> {

        @Override
        public Object invoke(Comparable value, Data indexKey) {
            return null; // TODO
        }

    }

//    private Comparable canonicalize(Comparable value) {
//        if (value instanceof CompositeValue) {
//            Comparable[] components = ((CompositeValue) value).getComponents();
//            for (int i = 0; i < components.length; ++i) {
//                components[i] = canonicalizeScalarForStorage(components[i]);
//            }
//            return value;
//        } else {
//            return canonicalizeScalarForStorage(value);
//        }
//    }

    private static Comparable canonicalizeLongRepresentable(long value) {
        if (value == (long) (int) value) {
            return (int) value;
        } else {
            return value;
        }
    }

}
