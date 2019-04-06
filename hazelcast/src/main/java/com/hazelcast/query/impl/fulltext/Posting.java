package com.hazelcast.query.impl.fulltext;

public class Posting {

    private Term term;
    private Document parsedDocument;

    public Posting(Term term, Document document) {
        this.term = term;
        this.parsedDocument = document;
    }

    public Term getTerm() {
        return term;
    }

    public Document getParsedDocument() {
        return parsedDocument;
    }
}