package be.bagofwords.text;

import be.bagofwords.ui.UI;
import be.bagofwords.util.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class MappedText implements Serializable, Text {

    public transient char[] text;
    public String url;
    private int[] mappingToOrig;

    public MappedText(char[] text, int[] mappingToOrig, String url) {
        super();
        this.text = text;
        this.mappingToOrig = mappingToOrig;
        this.url = url;
    }

    public MappedText() {
    }

    public MappedText(char[] text, String url) {
        this.text = text;
        mappingToOrig = new int[text.length];
        for (int i = 0; i < text.length; i++) {
            mappingToOrig[i] = i;
        }
        this.url = url;
    }

    @JsonIgnore
    public char[] getTextArray() {
        return text;
    }

    public void setText(char[] text) {
        this.text = text;
    }

    @Override
    @JsonIgnore
    public String getId() {
        return url;
    }

    public void setId(String id) {
        //messed up json annotations. We should be able to remove this at some point...
    }

    @JsonIgnore
    public String getS() {
        return new String(text);
    }

    public void setText(String text) {
        this.text = text.toCharArray();
    }

    @JsonIgnore
    public String getText() {
        return new String(text);
    }

    //Legacy
    public void setFileName(String fileName) {
        if (StringUtils.isEmpty(url)) {
            url = fileName;
        } else {
            url += "/" + fileName;
        }
    }

    //Legacy
    public void setPath(String path) {
        if (StringUtils.isEmpty(url)) {
            url = path;
        } else {
            url = path + "/" + url;
        }
    }

    public int[] getMappingToOrig() {
        return mappingToOrig;
    }

    public void setMappingToOrig(int[] mappingToOrig) {
        this.mappingToOrig = mappingToOrig;
    }

    public String toString() {
        return url;
    }

    private int getInverseMapping(int position) {
        for (int i = 0; i < mappingToOrig.length; i++)
            if (mappingToOrig[i] == position)
                return i;
        return -1;
    }

    public Pair<Integer, Integer> getMappingToOrig(BowString string) {
        return getMappingToOrig(string.getStart(), string.getEnd());
    }

    public Pair<Integer, Integer> getMappingFromOrig(BowString string) {
        return getMappingFromOrig(string.getStart(), string.getEnd());
    }

    public Pair<Integer, Integer> getMappingToOrig(int startPos, int endPos) {
        endPos = endPos - 1; //make end inclusive
        int mappedStart = startPos < mappingToOrig.length ? mappingToOrig[startPos] : -1;
        int mappedEnd = endPos < mappingToOrig.length ? mappingToOrig[endPos] : -1;
        if (mappedEnd != -1) {
            mappedEnd++; //We want to return the end non-inclusive
        }
        return new Pair<>(mappedStart, mappedEnd);
    }

    public Pair<Integer, Integer> getMappingFromOrig(int startPos, int endPos) {
        endPos = endPos - 1; //make end inclusive
        int origStart = getInverseMapping(startPos);
        int origEnd = getInverseMapping(endPos);
        if (origEnd != -1) {
            origEnd++; //make end non-inclusive again
        }
        return new Pair<>(origStart, origEnd);
    }

    public void printMapping(String orig) {
        for (int i = 0; i < text.length; i++) {
            Pair<Integer, Integer> mapping = getMappingToOrig(i, i + 1);
            if (mapping.getFirst() != -1 && mapping.getSecond() != -1) {
                UI.write(text[i] + " " + orig.substring(mapping.getFirst(), mapping.getSecond()));
            } else {
                UI.write(text[i]);
            }
        }
    }

    public void printInverseMapping(String orig) {
        String curr = new String(text);
        for (int i = 0; i < orig.length(); i++) {
            Pair<Integer, Integer> mapping = getMappingFromOrig(i, i + 1);
            if (mapping.getFirst() == -1 || mapping.getSecond() == -1)
                UI.write(orig.charAt(i));
            else
                UI.write(orig.charAt(i) + " " + curr.substring(mapping.getFirst(), mapping.getSecond()));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MappedText) {
            MappedText otherFile = (MappedText) obj;
            return getUrl().equals(otherFile.getUrl());
        }
        return false;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int length() {
        return getText().length();
    }

    public char charAt(int pos) {
        return getText().charAt(pos);
    }

    public String substring(int start, int end) {
        return getText().substring(start, end);
    }

    public String substring(int start) {
        return getText().substring(start);
    }

    @Override
    public int size() {
        return length();
    }
}
