package com.hazelcast.query.impl.fulltext;


import java.util.*;

public class InvertedIndex {

    private int numDocs;
    private final Map<String, PostingList> termToPostings = new HashMap<String, PostingList>();

    private static final Comparator<SearchResult> COMPARATOR = new Comparator<SearchResult>() {
        @Override
        public int compare(SearchResult r1, SearchResult r2) {
            return -Double.compare(r1.getScore(), r2.getScore());
        }
    };

    public void addDocument(Document doc) {
        for (Term term : doc.getTerms()) {
            final String text = term.getText();
            if (!termToPostings.containsKey(text)) {
                termToPostings.put(text, new PostingList(text));
            }
            termToPostings.get(text).addPosting(term, doc);
        }
        numDocs++;
    }

    // TODO remove doc etc.

    private Set<Document> getRelevantDocuments(Document searchDoc) {
        Set<Document> result = new HashSet<Document>();
        for (String term : searchDoc.getUniqueTerms()) {
            PostingList postingList = termToPostings.get(term);
            if (postingList != null) {
                result.addAll(postingList.getUniqueDocuments());
            }
        }
        return result;
    }

    public Collection<SearchResult> search(String query/*, int maxResults */) {
        Document searchDoc = FulltextUtils.makeDocument(null, query);
        Collection<Document> documentsToScan = getRelevantDocuments(searchDoc);

        if (searchDoc.isEmpty() || documentsToScan.isEmpty()) {
            return Collections.emptyList();
        }

        final PriorityQueue<SearchResult> result = new PriorityQueue<SearchResult>(5, COMPARATOR);
        for (Document doc : documentsToScan) {
            double score = score(searchDoc, doc);
            result.add(new SearchResult(doc, score));
        }

        return result;
    }

    private double score(Document searchDoc, Document doc) {
        Set<String> terms = searchDoc.getUniqueTerms();
        double d = 0.0d;
        double m1 = 0.0d;
        double m2 = 0.0d;
        for (String term : terms) {
            double tfidf1 = getTfidf(searchDoc, term);
            double tfidf2 = getTfidf(doc, term);
            d += tfidf1*tfidf2;
            m1 += tfidf1 * tfidf1;
            m2 += tfidf2 * tfidf2;
        }

        m1 = Math.sqrt(m1);
        m2 = Math.sqrt(m2);
        return d/(m1*m2);
    }

    private double getTfidf(Document doc, String term) {
        int termFreq = doc.getTermFrequency(term);
        if (termFreq == 0) {
            return 0;
        }
        return getIdf(term) * termFreq;
    }

    private double getIdf(String term) {
        double numDocsWithTerm = numDocumentsTermIsIn(term);
        return Math.log(numDocs / numDocsWithTerm);
    }

    private int numDocumentsTermIsIn(String term) {
        PostingList postingList = termToPostings.get(term);
        if (postingList == null) {
            return 0;
        }
        return postingList.getUniqueDocuments().size();
    }

}
