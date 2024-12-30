package simpleDatabase;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ArticleGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupName;
    private Set<String> articleIds;
    private Set<String> instructorAdmins;
    private Set<String> instructorViewers;
    private Set<String> studentViewers;
    private boolean specialAccess;

    public ArticleGroup(String groupName, boolean specialAccess) {
        this.groupName = groupName;
        this.specialAccess = specialAccess;
        this.articleIds = new HashSet<>();
        this.instructorAdmins = new HashSet<>();
        this.instructorViewers = new HashSet<>();
        this.studentViewers = new HashSet<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public void addArticle(String articleId) {
        articleIds.add(articleId);
    }

    public void removeArticle(String articleId) {
        articleIds.remove(articleId);
    }

    public Set<String> getArticleIds() {
        return articleIds;
    }

    public void addInstructorAdmin(String username) {
        instructorAdmins.add(username);
    }

    public void removeInstructorAdmin(String username) {
        instructorAdmins.remove(username);
    }

    public Set<String> getInstructorAdmins() {
        return instructorAdmins;
    }

    public void addInstructorViewer(String username) {
        instructorViewers.add(username);
    }

    public void removeInstructorViewer(String username) {
        instructorViewers.remove(username);
    }

    public Set<String> getInstructorViewers() {
        return instructorViewers;
    }

    public void addStudentViewer(String username) {
        studentViewers.add(username);
    }

    public void removeStudentViewer(String username) {
        studentViewers.remove(username);
    }

    public Set<String> getStudentViewers() {
        return studentViewers;
    }

    public boolean isSpecialAccess() {
        return specialAccess;
    }

    public void setSpecialAccess(boolean specialAccess) {
        this.specialAccess = specialAccess;
    }
}
