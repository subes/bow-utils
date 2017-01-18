package be.bagofwords.text;

public class Match implements Comparable<Match> {

    private int start;
    private int end;
    private String replacement;
    private boolean keepWordMapping;

    public Match(int start, int end, String replacement) {
        this(start, end, replacement, false);
    }

    public Match(int start, int end, String replacement, boolean keepWordMapping) {
        this.start = start;
        this.end = end;
        this.replacement = replacement;
        this.keepWordMapping = keepWordMapping;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getReplacement() {
        return replacement;
    }

    public boolean keepWordMapping() {
        return keepWordMapping;
    }

    @Override
    public int compareTo(Match o) {
        if (getStart() == o.getStart()) {
            if (getEnd() == o.getEnd()) {
                return replacement.compareTo(o.getReplacement());
            } else {
                return Integer.compare(getEnd(), o.getEnd());
            }
        } else {
            return Integer.compare(getStart(), o.getStart());
        }
    }
}
