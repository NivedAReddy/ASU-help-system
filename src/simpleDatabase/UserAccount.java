package simpleDatabase;

import java.util.Set;

public class UserAccount {
    private String username;
    private char[] password;
    private Set<String> roles;
    private boolean tempPass;
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredName;
    private String email;
    private String topicLevel; // Added field

    public UserAccount(String username, char[] password, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.tempPass = false;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isTempPass() {
        return tempPass;
    }

    public void setTempPass(boolean tempPass) {
        this.tempPass = tempPass;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
       this.middleName = middleName;
    }

    public String getLastName() {
       return lastName;
    }

    public void setLastName(String lastName) {
       this.lastName = lastName;
    }

    public String getPreferredName() {
       return preferredName;
    }

    public void setPreferredName(String preferredName) {
       this.preferredName = preferredName;
    }

    public String getEmail() {
       return email;
    }

    public void setEmail(String email) {
       this.email = email;
    }

    public String getTopicLevel() {
       return topicLevel;
    }

    public void setTopicLevel(String topicLevel) {
       this.topicLevel = topicLevel;
    }
}
