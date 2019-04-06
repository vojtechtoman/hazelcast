package com.hazelcast.query.impl.fulltext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostingList {

    private String term;
    private List<Posting> postings;
    private Set<Document> uniqueDocuments;

    public PostingList(String term) {
        this.term = term;
        this.postings = new ArrayList<Posting>();
        this.uniqueDocuments = new HashSet<Document>();
    }

    public void addPosting(Term term, Document doc) {
        postings.add(new Posting(term, doc));
        uniqueDocuments.add(doc);
    }

    public String getTerm() {
        return term;
    }

    public List<Posting> getPostings() {
        return postings;
    }

    public Set<Document> getUniqueDocuments() {
        return uniqueDocuments;
    }
}
