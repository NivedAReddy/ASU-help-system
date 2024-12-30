package simpleDatabase;

import java.util.Set;

public class Invite {
    private String code;
    private Set<String> roles;
    private String email;
    private String topicLevel;

    public Invite(String code, Set<String> roles, String email, String topicLevel) {
        this.code = code;
        this.roles = roles;
        this.email = email;
        this.topicLevel = topicLevel;
    }

    public String getCode() {
        return code;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getEmail() {
        return email;
    }

    public String getTopicLevel() {
        return topicLevel;
    }
}
