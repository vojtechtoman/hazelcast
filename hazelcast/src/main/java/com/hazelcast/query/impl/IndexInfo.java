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

import com.hazelcast.map.impl.MapDataSerializerHook;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;

public class IndexInfo implements IdentifiedDataSerializable, Comparable<IndexInfo> {

    private String name;
    private boolean ordered;
    private boolean fulltext;

    public IndexInfo() {
    }

    public IndexInfo(String name, boolean ordered, boolean fulltext) {
        this.name = name;
        this.ordered = ordered;
        this.fulltext = fulltext;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeBoolean(ordered);
        out.writeBoolean(fulltext);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        name = in.readUTF();
        ordered = in.readBoolean();
        fulltext = in.readBoolean();
    }

    public String getName() {
        return name;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public boolean isFulltext() {
        return fulltext;
    }

    @Override
    public int getFactoryId() {
        return MapDataSerializerHook.F_ID;
    }

    @Override
    public int getId() {
        return MapDataSerializerHook.INDEX_INFO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IndexInfo indexInfo = (IndexInfo) o;
        if (ordered != indexInfo.ordered) {
            return false;
        }
        if (fulltext != indexInfo.fulltext) {
            return false;
        }
        return name != null ? name.equals(indexInfo.name) : indexInfo.name == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (ordered ? 1 : 0);
        result = 31 * result + (fulltext ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(IndexInfo other) {
        int attributeNameCompareResult = name.compareTo(other.name);
        if (attributeNameCompareResult == 0) {
            int orderedCompareResult = Boolean.valueOf(ordered).compareTo(other.ordered);
            return orderedCompareResult == 0 ? Boolean.valueOf(fulltext).compareTo(other.fulltext) : orderedCompareResult;
        }
        return attributeNameCompareResult;
    }
}
