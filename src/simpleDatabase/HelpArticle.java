package simpleDatabase;

import java.io.Serializable;

public class HelpArticle implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String authors;
    private String abstractText;
    private String level;
    private String body;
    private boolean encrypted;
    private String groupName;

    public HelpArticle(String id, String title, String authors, String abstractText, String level, String body) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.abstractText = abstractText;
        this.level = level;
        this.body = body;
        this.groupName = "General";
        this.encrypted = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthors() {
        return authors;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String getLevel() {
        return level;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
