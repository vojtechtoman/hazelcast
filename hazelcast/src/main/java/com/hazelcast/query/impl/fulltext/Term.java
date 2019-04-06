package com.hazelcast.query.impl.fulltext;

public class Term {

    private final String text;
    private final int position;

    public Term(String text, int position) {
        this.text = text;
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Term other = (Term) o;
        return position == other.position && text.equals(other.text);
    }

    @Override
    public int hashCode() {
        return 31 * text.hashCode() + position;
    }
}
