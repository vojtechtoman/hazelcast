package com.hazelcast.query.impl.fulltext;

import com.hazelcast.query.impl.QueryableEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public abstract class FulltextUtils {

    private FulltextUtils() {
    }

    public static Document makeDocument(QueryableEntry entry, String text) {
        List<Term> documentTerms = terms(text);
        Document document = new Document(entry, documentTerms);
        return document;
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();

        // TODO use a real analyzer, omit stop words etc.
        text = text.toLowerCase();
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        while (tokenizer.hasMoreTokens()) {
            String t = tokenizer.nextToken();
            t = t.replaceAll("[^a-zA-Z ]", "");
            if (!t.isEmpty()) {
                result.add(t);
            }
        }
        return result;
    }

    private static List<Term> terms(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> terms = tokenize(text);
        List<Term> result = new ArrayList<Term>();
        int pos = 0;
        for (String str : terms) {
            result.add(new Term(str, pos));
            pos++;
        }
        return result;
    }
}