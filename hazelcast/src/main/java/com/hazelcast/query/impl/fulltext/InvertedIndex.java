package com.hazelcast.query.impl.fulltext;


import com.hazelcast.nio.serialization.Data;
import com.hazelcast.query.impl.QueryableEntry;

import java.util.*;

public class InvertedIndex {

    private final Map<String, PostingList> termToPostings = new HashMap<String, PostingList>();

    public void addDocument(Document doc) {
        for (Term term : doc.getTerms()) {
            final String text = term.getText();
            if (!termToPostings.containsKey(text)) {
                termToPostings.put(text, new PostingList(text));
            }
            termToPostings.get(text).addPosting(term, doc);
        }
    }

    // TODO remove doc

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

    public Map<Data, QueryableEntry> search(String query/*, int maxResults*/) {
        Document searchDocument = FulltextUtils.makeDocument(null, query);
        Collection<Document> documentsToScan = getRelevantDocuments(searchDocument);

        if (searchDocument.isEmpty() || documentsToScan.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Data, QueryableEntry> results = new HashMap<Data, QueryableEntry>();
        for (Document doc : documentsToScan) {
            results.put(doc.getEntry().getKeyData(), doc.getEntry());
        }
        return results;
    }

}
