package com.hazelcast.query.impl.fulltext;

import java.util.HashMap;
import java.util.Map;

public class ParsedDocumentMetrics {
    private Document document;
    private Map<String, PostingList> termsToPostings;
    private int numDocs;

    //metrics
    private double magnitude;
    private Map<String, Double> tfidf;

    public ParsedDocumentMetrics(Document document,
                                 Map<String, PostingList> termsToPostings, int numDocs) {
        this.document = document;
        this.termsToPostings = termsToPostings;
        this.numDocs = numDocs;
        this.tfidf = new HashMap<String, Double>();

        double sumOfSquares = 0;
        for (String term : document.getUniqueTerms()) {
            double d = calcTfidf(term);
            tfidf.put(term, d);
            sumOfSquares += d * d;
        }
        magnitude = Math.sqrt(sumOfSquares);
    }

    public double getTfidf(String term) {
        Double retVal = tfidf.get(term);
        if (retVal == null) {
            return 0;
        }
        return retVal;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public Document getDocument() {
        return this.document;
    }

    private double calcTfidf(String term) {
        int termFreq = document.getTermFrequency(term);
        if (termFreq == 0) {
            return 0;
        }

        return getInverseDocumentFrequency(term) * termFreq;
    }

    private double getInverseDocumentFrequency(String term) {
        double numDocsWithTerm = numDocumentsTermIsIn(term);
        return Math.log(numDocs / (1 + numDocsWithTerm));
    }

    private int numDocumentsTermIsIn(String term) {
        PostingList postingList = termsToPostings.get(term);
        if (postingList == null) {
            return 0;
        }
        return postingList.getUniqueDocuments().size();
    }

}