package org.medialab;

import java.util.List;



public class DocumentVersion {
    private int version;
    private List<String> paragraphs;

    public DocumentVersion(int version, List<String> paragraphs) {
        this.version = version;
        this.paragraphs = paragraphs;
    }

    public int getVersion() { return version; }
    public List<String> getParagraphs() { return paragraphs; }

    @Override
    public String toString() {
        return String.valueOf(version);
    }
}
