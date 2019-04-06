package com.hazelcast.query.impl.fulltext;

import com.hazelcast.query.impl.QueryableEntry;
import com.hazelcast.util.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Document {

    private QueryableEntry entry;
    private List<Term> terms;
    private Map<String, Integer> termFreq;

    public Document(QueryableEntry entry, List<Term> terms) {
        this.entry = entry;
        this.terms = terms;

        this.termFreq = new HashMap<String, Integer>();
        for (Term t : terms) {
            String text = t.getText();
            Integer cnt = termFreq.get(text);
            if (cnt == null) {
                cnt = Integer.valueOf(0);
            }
            this.termFreq.put(text, cnt + 1);
        }
    }

    public int getTermFrequency(String term) {
        Integer freq = termFreq.get(term);
        return freq == null ? 0 : freq.intValue();
    }

    public boolean isEmpty() {
        return terms == null || terms.isEmpty();
    }

    public List<Term> getTerms() {
        return terms;
    }

    public Set<String> getUniqueTerms() {
        return termFreq.keySet();
    }

    public QueryableEntry getEntry() {
        return entry;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Document)) {
            return false;
        }
        Document other = (Document)o;
        return entry.equals(other.entry);
    }

    @Override
    public int hashCode() {
        return entry.hashCode();
    }
}