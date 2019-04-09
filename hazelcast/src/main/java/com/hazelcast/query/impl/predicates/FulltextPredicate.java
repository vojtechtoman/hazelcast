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

package com.hazelcast.query.impl.predicates;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.BinaryInterface;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.Comparables;
import com.hazelcast.query.impl.Index;
import com.hazelcast.query.impl.QueryContext;
import com.hazelcast.query.impl.QueryableEntry;

import java.io.IOException;
import java.util.Set;

import static com.hazelcast.query.impl.predicates.PredicateUtils.isNull;

/**
 * Fulltext Predicate
 */
@BinaryInterface
public class FulltextPredicate extends AbstractIndexAwarePredicate {

    private static final long serialVersionUID = 1L;

    private String query;

    public FulltextPredicate() {
    }

    public FulltextPredicate(String attribute) {
        super(attribute);
    }

    public FulltextPredicate(String attribute, String query) {
        super(attribute);
        this.query = query;
    }

    @Override
    public Set<QueryableEntry> filter(QueryContext queryContext) {
        Index index = matchIndex(queryContext, QueryContext.IndexMatchHint.PREFER_UNORDERED);
        return index.getFulltextRecords(query);
    }

    protected boolean applyForSingleAttributeValue(Comparable attributeValue) {
        return false;
//        if (attributeValue == null) {
//            return isNull(value);
//        }
//        value = convert(attributeValue, value);
//        attributeValue = (Comparable) convertEnumValue(attributeValue);
//        return Comparables.equal(attributeValue, value);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        super.writeData(out);
        out.writeObject(query);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        super.readData(in);
        query = in.readObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (!(o instanceof EqualPredicate)) {
            return false;
        }

        EqualPredicate that = (EqualPredicate) o;
        if (!that.canEqual(this)) {
            return false;
        }

        return query != null ? query.equals(that.value) : that.value == null;
    }

    @Override
    public boolean canEqual(Object other) {
        return (other instanceof EqualPredicate);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return attributeName + " CONTAINS " + query;
    }

//    @Override
//    public Predicate negate() {
//        return new NotEqualPredicate(attributeName, value);
//    }

    @Override
    public int getId() {
        return PredicateDataSerializerHook.FULLTEXT_PREDICATE;
    }

}
