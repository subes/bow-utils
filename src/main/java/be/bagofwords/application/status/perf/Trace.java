package be.bagofwords.application.status.perf;

import be.bagofwords.util.HashUtils;

public class Trace {

    private String threadName;
    private String line;
    private Trace parent;

    public Trace(String threadName, String line, Trace parent) {
        this.threadName = threadName;
        this.line = line;
        this.parent = parent;
    }

    public String getLine() {
        return line;
    }

    public Trace getParent() {
        return parent;
    }

    public boolean equals(Object other) {
        if (other instanceof Trace) {
            Trace otherTrace = (Trace) other;
            return threadName.equals(otherTrace.getThreadName()) && line.equals(otherTrace.getLine()) && (parent == otherTrace.getParent() || (parent != null && parent.equals(otherTrace.getParent())));
        } else {
            return false;
        }
    }

    public int hashCode() {
        int hash = line.hashCode();
        if (parent != null) {
            return hash * HashUtils.addHash + parent.hashCode();
        } else {
            return hash;
        }
    }

    //Serialization

    public Trace() {
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void setParent(Trace parent) {
        this.parent = parent;
    }

    public String getThreadName() {
        return threadName;
    }
}
