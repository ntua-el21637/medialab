package org.medialab;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private String title;
    private String author;
    private String category;
    private String creationDate;

    private List<DocumentVersion> versions;

    public Document(String title, String author, String category, String creationDate, List<String> initialParagraphs) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.creationDate = creationDate;
        this.versions = new ArrayList<>();

        this.versions.add(new DocumentVersion(1, initialParagraphs));
    }

    public void addVersion(List<String> newParagraphs) {
        int nextVersion= this.versions.size() + 1;
        this.versions.add(new DocumentVersion(nextVersion, newParagraphs));
    }

    public String getTitle() {return title;}
    public String getAuthor() {return author;}
    public String getCategory() {return category;}
    public String getCreationDate() {return creationDate;}
    public List<DocumentVersion> getVersions() {return versions;}

    public DocumentVersion getLatestVersion() {
        return this.versions.get(this.versions.size() - 1);
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
