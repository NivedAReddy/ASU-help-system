package simpleDatabase;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Encryption.EncryptionHelper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Controller class for user management functionalities.
 * Handles user registration, login, profile completion, password reset, etc.
 */
public class UserManagementController {

    // Simulated databases
    protected static Map<String, UserAccount> userDB = new HashMap<>();
    protected static Map<String, Invite> inviteDB = new HashMap<>();
    protected static Map<String, TempPass> tempPassDB = new HashMap<>();

    // New List to Store Help Messages
    protected static List<HelpMessage> helpMessages = new ArrayList<>();

    // Role constants
    protected static final String ROLE_ADMINISTRATOR = "Administrator";
    protected static final String ROLE_LEARNER = "Student";
    protected static final String ROLE_TEACHER = "Teacher";

    // Variable to keep track of the currently logged-in administrator's username
    protected String loggedInAdmin;

    // Encryption helper
    protected EncryptionHelper encryptionHelper;

    // Set the encryption helper
    public void setEncryptionHelper(EncryptionHelper helper) {
        this.encryptionHelper = helper;
    }

    // Admin registration scene
    public Scene initAdminRegistration(Stage window) {
        GridPane regGrid = new GridPane();
        regGrid.setPadding(new Insets(10));
        regGrid.setVgap(8);
        regGrid.setHgap(10);

        Label titleLbl = new Label("Admin Registration");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();

        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();

        Label confirmPassLbl = new Label("Confirm Password:");
        PasswordField confirmPassTxt = new PasswordField();

        Button regBtn = new Button("Register Admin");
        Label statusLbl = new Label();

        regGrid.add(titleLbl, 0, 0, 2, 1);
        regGrid.add(userLbl, 0, 1);
        regGrid.add(userTxt, 1, 1);
        regGrid.add(passLbl, 0, 2);
        regGrid.add(passTxt, 1, 2);
        regGrid.add(confirmPassLbl, 0, 3);
        regGrid.add(confirmPassTxt, 1, 3);
        regGrid.add(regBtn, 1, 4);
        regGrid.add(statusLbl, 1, 5);

        Scene regScene = new Scene(regGrid, 400, 300);

        regBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            String pwd = passTxt.getText();
            String confirmPwd = confirmPassTxt.getText();

            if (!validatePassword(pwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }
            if (!pwd.equals(confirmPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            if (userDB.isEmpty()) {
                Set<String> roles = new HashSet<>();
                roles.add(ROLE_ADMINISTRATOR);
                UserAccount adminUser = new UserAccount(uname, pwd.toCharArray(), roles);
                userDB.put(uname, adminUser);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText(null);
                alert.setContentText("Admin account created. Please log in.");
                alert.showAndWait();

                window.setScene(new Scene(buildLoginScreen(window), 400, 350));
            } else {
                statusLbl.setText("An admin already exists. Please log in.");
            }
        });

        window.setTitle("Admin Registration");

        return regScene;
    }

    // Build the login screen for users
    public GridPane buildLoginScreen(Stage window) {
        GridPane loginGrid = new GridPane();
        loginGrid.setPadding(new Insets(10));
        loginGrid.setVgap(8);
        loginGrid.setHgap(10);

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();

        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();

        Button loginBtn = new Button("Login");
        Hyperlink inviteLink = new Hyperlink("Use Invitation Code");

        Label statusLbl = new Label();

        loginGrid.add(userLbl, 0, 0);
        loginGrid.add(userTxt, 1, 0);
        loginGrid.add(passLbl, 0, 1);
        loginGrid.add(passTxt, 1, 1);
        loginGrid.add(loginBtn, 1, 2);
        loginGrid.add(inviteLink, 1, 3);
        loginGrid.add(statusLbl, 1, 4);

        window.setTitle("Login");

        loginBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            String pwd = passTxt.getText();

            if (userDB.containsKey(uname)) {
                UserAccount user = userDB.get(uname);

                if (user.isTempPass()) {
                    TempPass tp = tempPassDB.get(uname);
                    if (tp != null && tp.getPassword().equals(pwd)) {
                        if (LocalDateTime.now().isAfter(tp.getExpiry())) {
                            statusLbl.setText("Temporary password expired.");
                            return;
                        }
                        window.setScene(new Scene(buildPassResetScreen(window, user), 400, 300));
                        return;
                    } else {
                        statusLbl.setText("Invalid temporary password.");
                        return;
                    }
                }

                if (String.valueOf(user.getPassword()).equals(pwd)) {
                    if (user.getFirstName() == null || user.getLastName() == null || user.getEmail() == null) {
                        window.setScene(new Scene(completeUserProfile(window, user), 500, 400));
                    } else {
                        if (user.getRoles().size() > 1) {
                            window.setScene(new Scene(selectUserRole(window, user), 400, 200));
                        } else {
                            String role = user.getRoles().iterator().next();
                            displayHomeScreen(window, user, role);
                        }
                    }
                } else {
                    statusLbl.setText("Incorrect password.");
                }
            } else {
                statusLbl.setText("User not found.");
            }
        });

        inviteLink.setOnAction(e -> {
            window.setScene(new Scene(enterInviteCode(window), 400, 200));
        });

        return loginGrid;
    }

    // Display home screen for different user roles
    public void displayHomeScreen(Stage window, UserAccount user, String role) {
        VBox homeLayout = new VBox(10);
        homeLayout.setPadding(new Insets(10));

        String displayName = user.getPreferredName() != null ? user.getPreferredName() : user.getFirstName();
        Label welcomeLbl = new Label("Welcome, " + displayName + " (" + role + ")!");
        Button logoutBtn = new Button("Logout");

        homeLayout.getChildren().addAll(welcomeLbl, logoutBtn);

        if (ROLE_ADMINISTRATOR.equals(role)) {
            loggedInAdmin = user.getUsername();

            Button inviteBtn = new Button("Invite User");
            inviteBtn.setOnAction(e -> openInviteUserScreen(window));

            Button resetBtn = new Button("Reset User Account");
            resetBtn.setOnAction(e -> openResetUserScreen(window));

            Button deleteBtn = new Button("Delete User Account");
            deleteBtn.setOnAction(e -> openDeleteUserScreen(window));

            Button listBtn = new Button("List Users");
            listBtn.setOnAction(e -> openListUsersScreen(window));

            Button modifyBtn = new Button("Modify User Roles");
            modifyBtn.setOnAction(e -> openModifyRolesScreen(window));

            ArticleManagementController articleController = new ArticleManagementController();
            articleController.setEncryptionHelper(encryptionHelper);

            Button createArticleBtn = new Button("Create Help Article");
            createArticleBtn.setOnAction(e -> articleController.openCreateArticleScreen(window, user));

            Button manageArticlesBtn = new Button("Manage Help Articles");
            manageArticlesBtn.setOnAction(e -> articleController.openManageArticlesScreen(window, user));

            Button backupBtn = new Button("Backup Articles");
            backupBtn.setOnAction(e -> articleController.openBackupArticlesScreen(window, user));

            Button restoreBtn = new Button("Restore Articles");
            restoreBtn.setOnAction(e -> articleController.openRestoreArticlesScreen(window, user));

            Button manageGroupsBtn = new Button("Manage Article Groups");
            manageGroupsBtn.setOnAction(e -> articleController.openManageGroupsScreen(window, user));

            Button manageUsersBtn = new Button("Manage Users in Groups");
            manageUsersBtn.setOnAction(e -> openManageUsersInGroupsScreen(window));

            Button viewHelpMessagesBtn = new Button("View Help Messages");
            viewHelpMessagesBtn.setOnAction(e -> openViewHelpMessagesScreen(window, user));

            homeLayout.getChildren().addAll(inviteBtn, resetBtn, deleteBtn, listBtn, modifyBtn,
                    createArticleBtn, manageArticlesBtn, backupBtn, restoreBtn, manageGroupsBtn, manageUsersBtn, viewHelpMessagesBtn);

            window.setTitle("Admin Home");
        } else if (ROLE_TEACHER.equals(role)) {
            ArticleManagementController articleController = new ArticleManagementController();
            articleController.setEncryptionHelper(encryptionHelper);

            Button createArticleBtn = new Button("Create Help Article");
            createArticleBtn.setOnAction(e -> articleController.openCreateArticleScreen(window, user));

            Button manageArticlesBtn = new Button("Manage Help Articles");
            manageArticlesBtn.setOnAction(e -> articleController.openManageArticlesScreen(window, user));

            Button backupBtn = new Button("Backup Articles");
            backupBtn.setOnAction(e -> articleController.openBackupArticlesScreen(window, user));

            Button restoreBtn = new Button("Restore Articles");
            restoreBtn.setOnAction(e -> articleController.openRestoreArticlesScreen(window, user));

            Button manageGroupsBtn = new Button("Manage Article Groups");
            manageGroupsBtn.setOnAction(e -> articleController.openManageGroupsScreen(window, user));

            Button viewHelpMessagesBtn = new Button("View Help Messages");
            viewHelpMessagesBtn.setOnAction(e -> openViewHelpMessagesScreen(window, user));

            homeLayout.getChildren().addAll(createArticleBtn, manageArticlesBtn, backupBtn, restoreBtn, manageGroupsBtn, viewHelpMessagesBtn);

            window.setTitle("Instructor Home");
        } else {
            window.setTitle("Student Home");
            ArticleManagementController articleController = new ArticleManagementController();
            articleController.setEncryptionHelper(encryptionHelper);

            Button searchArticlesBtn = new Button("Search Help Articles");
            searchArticlesBtn.setOnAction(e -> articleController.openStudentSearchArticlesScreen(window, user));

            Button helpSystemBtn = new Button("Help System");
            helpSystemBtn.setOnAction(e -> openHelpSystemScreen(window, user));

            Button viewResponsesBtn = new Button("View Responses");
            viewResponsesBtn.setOnAction(e -> openViewResponsesScreen(window, user));

            homeLayout.getChildren().addAll(searchArticlesBtn, helpSystemBtn, viewResponsesBtn);
        }

        logoutBtn.setOnAction(e -> {
            loggedInAdmin = null;
            window.setScene(new Scene(buildLoginScreen(window), 400, 350));
        });

        Scene homeScene = new Scene(homeLayout, 400, 500);
        window.setScene(homeScene);
    }

    // Validate password against a strong password pattern
    protected boolean validatePassword(String pwd) {
        String pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$";
        return Pattern.matches(pattern, pwd);
    }

    // Open screen for submitting help requests
    public void openHelpSystemScreen(Stage window, UserAccount user) {
        GridPane helpGrid = new GridPane();
        helpGrid.setPadding(new Insets(10));
        helpGrid.setVgap(8);
        helpGrid.setHgap(10);

        Label genericLbl = new Label("Generic Help Topic:");
        ComboBox<String> genericSelector = new ComboBox<>();
        genericSelector.getItems().addAll("How to Search", "What are Keywords?", "What are groups?",
                "How do I view articles in special access groups?");
        genericSelector.setValue("How to Search");
        Button genericBtn = new Button("Submit Generic Help Message");

        Label specificLbl = new Label("Specific Help Message:");
        TextArea specificTxt = new TextArea();
        specificTxt.setPromptText("Describe your issue or question here...");
        specificTxt.setWrapText(true);
        Button specificBtn = new Button("Submit Specific Help Message");

        Label feedbackLbl = new Label("");
        Button backBtn = new Button("Back");

        helpGrid.add(genericLbl, 0, 0);
        helpGrid.add(genericSelector, 1, 0);
        helpGrid.add(genericBtn, 1, 1);
        helpGrid.add(specificLbl, 0, 2);
        helpGrid.add(specificTxt, 1, 2);
        helpGrid.add(specificBtn, 1, 3);
        helpGrid.add(backBtn, 0, 4);
        helpGrid.add(feedbackLbl, 1, 4);

        window.setTitle("Help System");

        genericBtn.setOnAction(e -> {
            String topic = genericSelector.getValue();
            HelpMessage message = new HelpMessage(user.getUsername(), topic, null);
            helpMessages.add(message);
            feedbackLbl.setText("Generic help message submitted!");
        });

        specificBtn.setOnAction(e -> {
            String messageText = specificTxt.getText().trim();
            if (messageText.isEmpty()) {
                feedbackLbl.setText("Please enter a specific help message.");
                return;
            }
            HelpMessage message = new HelpMessage(user.getUsername(), null, messageText);
            helpMessages.add(message);
            specificTxt.clear();
            feedbackLbl.setText("Specific help message submitted!");
        });

        backBtn.setOnAction(e -> {
            displayHomeScreen(window, user, ROLE_LEARNER);
        });

        Scene helpScene = new Scene(helpGrid, 600, 500);
        window.setScene(helpScene);
    }

    // Open screen for viewing help messages (for teachers and admins)
    public void openViewHelpMessagesScreen(Stage window, UserAccount user) {
        VBox messagesLayout = new VBox(10);
        messagesLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Help Messages from Students:");
        ListView<String> messagesList = new ListView<>();

        for (HelpMessage message : helpMessages) {
            String displayText = "From: " + message.getSenderUsername();
            if (message.getTopic() != null) {
                displayText += " | Topic: " + message.getTopic();
            }
            if (message.getMessage() != null) {
                displayText += " | Message: " + message.getMessage();
            }
            messagesList.getItems().add(displayText);
        }

        Button respondBtn = new Button("Respond to Selected Message");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        messagesLayout.getChildren().addAll(titleLbl, messagesList, respondBtn, backBtn, statusLbl);

        respondBtn.setOnAction(e -> {
            String selectedMessage = messagesList.getSelectionModel().getSelectedItem();
            if (selectedMessage == null) {
                statusLbl.setText("Select a message to respond.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Respond to Help Message");
            dialog.setHeaderText("Enter your response:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(response -> {
                for (HelpMessage message : helpMessages) {
                    String displayText = "From: " + message.getSenderUsername();
                    if (message.getTopic() != null) {
                        displayText += " | Topic: " + message.getTopic();
                    }
                    if (message.getMessage() != null) {
                        displayText += " | Message: " + message.getMessage();
                    }
                    if (displayText.equals(selectedMessage)) {
                        message.setResponse(response);
                        statusLbl.setText("Response sent.");
                        break;
                    }
                }
            });
        });

        backBtn.setOnAction(e -> {
            displayHomeScreen(window, user, user.getRoles().contains(ROLE_ADMINISTRATOR) ? ROLE_ADMINISTRATOR : ROLE_TEACHER);
        });

        window.setTitle("View Help Messages");
        Scene messagesScene = new Scene(messagesLayout, 600, 500);
        window.setScene(messagesScene);
    }

    // Open screen for students to view responses
    public void openViewResponsesScreen(Stage window, UserAccount user) {
        VBox responsesLayout = new VBox(10);
        responsesLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Your Help Messages and Responses:");
        ListView<String> responsesList = new ListView<>();

        for (HelpMessage message : helpMessages) {
            if (message.getSenderUsername().equals(user.getUsername())) {
                String displayText = "";
                if (message.getTopic() != null) {
                    displayText += "Topic: " + message.getTopic() + " | ";
                }
                if (message.getMessage() != null) {
                    displayText += "Message: " + message.getMessage() + " | ";
                }
                if (message.getResponse() != null) {
                    displayText += "Response: " + message.getResponse();
                } else {
                    displayText += "Response: (No response yet)";
                }
                responsesList.getItems().add(displayText);
            }
        }

        Button backBtn = new Button("Back");
        responsesLayout.getChildren().addAll(titleLbl, responsesList, backBtn);

        backBtn.setOnAction(e -> {
            displayHomeScreen(window, user, ROLE_LEARNER);
        });

        window.setTitle("View Responses");
        Scene responsesScene = new Scene(responsesLayout, 600, 500);
        window.setScene(responsesScene);
    }

    // Open screen for inviting a new user (admin function)
    public void openInviteUserScreen(Stage window) {
        GridPane inviteGrid = new GridPane();
        inviteGrid.setPadding(new Insets(10));
        inviteGrid.setVgap(8);
        inviteGrid.setHgap(10);

        Label emailLbl = new Label("Email:");
        TextField emailTxt = new TextField();

        Label rolesLbl = new Label("Roles:");
        CheckBox adminChk = new CheckBox("Admin");
        CheckBox learnerChk = new CheckBox("Student");
        CheckBox teacherChk = new CheckBox("Teacher");
        VBox rolesBox = new VBox(5, adminChk, learnerChk, teacherChk);

        Label levelLbl = new Label("Topic Level:");
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert", "All");
        levelCombo.setValue("All");

        Button sendInviteBtn = new Button("Send Invite");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        inviteGrid.add(emailLbl, 0, 0);
        inviteGrid.add(emailTxt, 1, 0);
        inviteGrid.add(rolesLbl, 0, 1);
        inviteGrid.add(rolesBox, 1, 1);
        inviteGrid.add(levelLbl, 0, 2);
        inviteGrid.add(levelCombo, 1, 2);
        inviteGrid.add(sendInviteBtn, 1, 3);
        inviteGrid.add(backBtn, 0, 3);
        inviteGrid.add(statusLbl, 1, 4);

        window.setTitle("Invite User");

        sendInviteBtn.setOnAction(e -> {
            String email = emailTxt.getText();
            String topicLevel = levelCombo.getValue();
            String code = generateInviteCode();

            Set<String> roles = new HashSet<>();
            if (adminChk.isSelected()) roles.add(ROLE_ADMINISTRATOR);
            if (learnerChk.isSelected()) roles.add(ROLE_LEARNER);
            if (teacherChk.isSelected()) roles.add(ROLE_TEACHER);

            if (roles.isEmpty()) {
                statusLbl.setText("Select at least one role.");
                return;
            }

            Invite invite = new Invite(code, roles, email, topicLevel);
            inviteDB.put(code, invite);
            statusLbl.setText("Invitation sent to " + email + " with code: " + code);
        });

        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene inviteScene = new Scene(inviteGrid, 500, 400);
        window.setScene(inviteScene);
    }

    // Generate a 6-character code for invitations
    protected String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString();
    }

    // Reset user account screen for setting temporary passwords
    public void openResetUserScreen(Stage window) {
        GridPane resetGrid = new GridPane();
        resetGrid.setPadding(new Insets(10));
        resetGrid.setVgap(8);
        resetGrid.setHgap(10);

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label dateLbl = new Label("Expiry Date (yyyy-MM-dd):");
        TextField dateTxt = new TextField();
        Label timeLbl = new Label("Expiry Time (24-hour HH:mm):");
        TextField timeTxt = new TextField();

        Button resetBtn = new Button("Reset Account");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        resetGrid.add(userLbl, 0, 0);
        resetGrid.add(userTxt, 1, 0);
        resetGrid.add(dateLbl, 0, 1);
        resetGrid.add(dateTxt, 1, 1);
        resetGrid.add(timeLbl, 0, 2);
        resetGrid.add(timeTxt, 1, 2);
        resetGrid.add(resetBtn, 1, 3);
        resetGrid.add(backBtn, 0, 3);
        resetGrid.add(statusLbl, 1, 4);

        window.setTitle("Reset User Account");

        resetBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            String dateStr = dateTxt.getText();
            String timeStr = timeTxt.getText();

            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
            if (dateStr.isEmpty() || timeStr.isEmpty()) {
                statusLbl.setText("Enter date and time.");
                return;
            }

            try {
                DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime expiryDT = LocalDateTime.parse(dateStr + "T" + timeStr, dtFormatter);

                String tempPass = generateTempPassword();
                TempPass tempPassword = new TempPass(tempPass, expiryDT);
                tempPassDB.put(uname, tempPassword);

                UserAccount user = userDB.get(uname);
                user.setTempPass(true);

                statusLbl.setText("Temporary password set for " + uname + ": " + tempPass);

            } catch (Exception ex) {
                statusLbl.setText("Invalid date/time format.");
            }
        });

        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene resetScene = new Scene(resetGrid, 500, 300);
        window.setScene(resetScene);
    }

    // Generate Temporary Password
    protected String generateTempPassword() {
        Random rnd = new Random();
        int tempPass = 100000 + rnd.nextInt(900000);
        return String.valueOf(tempPass);
    }

    // Delete User Screen
    public void openDeleteUserScreen(Stage window) {
        GridPane delGrid = new GridPane();
        delGrid.setPadding(new Insets(10));
        delGrid.setVgap(8);
        delGrid.setHgap(10);

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Button delBtn = new Button("Delete User");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        delGrid.add(userLbl, 0, 0);
        delGrid.add(userTxt, 1, 0);
        delGrid.add(delBtn, 1, 1);
        delGrid.add(backBtn, 0, 1);
        delGrid.add(statusLbl, 1, 2);

        window.setTitle("Delete User Account");

        delBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }

            UserAccount userToDelete = userDB.get(uname);
            if (userToDelete.getRoles().contains(ROLE_ADMINISTRATOR)) {
                long adminCount = userDB.values().stream()
                        .filter(u -> u.getRoles().contains(ROLE_ADMINISTRATOR))
                        .count();
                if (adminCount <= 1) {
                    statusLbl.setText("Cannot delete the last administrator.");
                    return;
                }
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User");
            alert.setHeaderText("Delete user: " + uname + "?");
            alert.setContentText("This cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                userDB.remove(uname);
                statusLbl.setText("User " + uname + " deleted.");
            } else {
                statusLbl.setText("Deletion canceled.");
            }
        });

        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene delScene = new Scene(delGrid, 500, 300);
        window.setScene(delScene);
    }

    // List Users Screen
    public void openListUsersScreen(Stage window) {
        VBox listLayout = new VBox(10);
        listLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("User Accounts:");
        listLayout.getChildren().add(titleLbl);

        for (UserAccount user : userDB.values()) {
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " "
                    + (user.getMiddleName() != null ? user.getMiddleName() + " " : "")
                    + (user.getLastName() != null ? user.getLastName() : "");
            Label userLbl = new Label("Username: " + user.getUsername() +
                    ", Name: " + fullName.trim() +
                    ", Roles: " + user.getRoles());
            listLayout.getChildren().add(userLbl);
        }

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));
        listLayout.getChildren().add(backBtn);

        window.setTitle("List Users");

        Scene listScene = new Scene(listLayout, 500, 400);
        window.setScene(listScene);
    }

    // Modify User Roles Screen
    public void openModifyRolesScreen(Stage window) {
        GridPane modGrid = new GridPane();
        modGrid.setPadding(new Insets(10));
        modGrid.setVgap(8);
        modGrid.setHgap(10);

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label rolesLbl = new Label("Roles:");
        CheckBox adminChk = new CheckBox("Admin");
        CheckBox learnerChk = new CheckBox("Student");
        CheckBox teacherChk = new CheckBox("Teacher");
        VBox rolesBox = new VBox(5, adminChk, learnerChk, teacherChk);
        Button updateBtn = new Button("Update Roles");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        modGrid.add(userLbl, 0, 0);
        modGrid.add(userTxt, 1, 0);
        modGrid.add(rolesLbl, 0, 1);
        modGrid.add(rolesBox, 1, 1);
        modGrid.add(updateBtn, 1, 2);
        modGrid.add(backBtn, 0, 2);
        modGrid.add(statusLbl, 1, 3);

        window.setTitle("Modify User Roles");

        updateBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            if (!userDB.containsKey(uname)) {
                statusLbl.setText("User not found.");
                return;
            }
            Set<String> roles = new HashSet<>();
            if (adminChk.isSelected()) {
                roles.add(ROLE_ADMINISTRATOR);
            }
            if (learnerChk.isSelected()) {
                roles.add(ROLE_LEARNER);
            }
            if (teacherChk.isSelected()) {
                roles.add(ROLE_TEACHER);
            }
            if (roles.isEmpty()) {
                statusLbl.setText("Select at least one role.");
                return;
            }

            UserAccount userToModify = userDB.get(uname);
            if (userToModify.getRoles().contains(ROLE_ADMINISTRATOR) && !roles.contains(ROLE_ADMINISTRATOR)) {
                long adminCount = userDB.values().stream()
                        .filter(u -> u.getRoles().contains(ROLE_ADMINISTRATOR))
                        .count();
                if (adminCount <= 1) {
                    statusLbl.setText("Cannot remove the last administrator.");
                    return;
                }
            }

            userToModify.setRoles(roles);
            statusLbl.setText("Roles updated for " + uname);
        });

        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        Scene modScene = new Scene(modGrid, 500, 400);
        window.setScene(modScene);
    }

    // Enter Invitation Code Screen
    public GridPane enterInviteCode(Stage window) {
        GridPane codeGrid = new GridPane();
        codeGrid.setPadding(new Insets(10));
        codeGrid.setVgap(8);
        codeGrid.setHgap(10);

        Label codeLbl = new Label("Invitation Code:");
        TextField codeTxt = new TextField();
        Button contBtn = new Button("Continue");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        codeGrid.add(codeLbl, 0, 0);
        codeGrid.add(codeTxt, 1, 0);
        codeGrid.add(contBtn, 1, 1);
        codeGrid.add(backBtn, 0, 1);
        codeGrid.add(statusLbl, 1, 2);

        window.setTitle("Enter Invitation Code");

        contBtn.setOnAction(e -> {
            String code = codeTxt.getText();
            if (inviteDB.containsKey(code)) {
                Invite invite = inviteDB.get(code);
                window.setScene(new Scene(registerNewUser(window, invite), 400, 250));
            } else {
                statusLbl.setText("Invalid code.");
            }
        });

        backBtn.setOnAction(e -> {
            window.setScene(new Scene(buildLoginScreen(window), 400, 350));
        });

        return codeGrid;
    }

    // Register New User Screen
    public GridPane registerNewUser(Stage window, Invite invite) {
        GridPane regGrid = new GridPane();
        regGrid.setPadding(new Insets(10));
        regGrid.setVgap(8);
        regGrid.setHgap(10);

        Label userLbl = new Label("Username:");
        TextField userTxt = new TextField();
        Label passLbl = new Label("Password:");
        PasswordField passTxt = new PasswordField();
        Label confirmPassLbl = new Label("Confirm Password:");
        PasswordField confirmPassTxt = new PasswordField();
        Button regBtn = new Button("Create Account");
        Label statusLbl = new Label();

        regGrid.add(userLbl, 0, 0);
        regGrid.add(userTxt, 1, 0);
        regGrid.add(passLbl, 0, 1);
        regGrid.add(passTxt, 1, 1);
        regGrid.add(confirmPassLbl, 0, 2);
        regGrid.add(confirmPassTxt, 1, 2);
        regGrid.add(regBtn, 1, 3);
        regGrid.add(statusLbl, 1, 4);

        window.setTitle("User Registration");

        regBtn.setOnAction(e -> {
            String uname = userTxt.getText();
            String pwd = passTxt.getText();
            String confirmPwd = confirmPassTxt.getText();

            if (!validatePassword(pwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }
            if (!pwd.equals(confirmPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            if (userDB.containsKey(uname)) {
                statusLbl.setText("Username exists.");
                return;
            }

            UserAccount newUser = new UserAccount(uname, pwd.toCharArray(), invite.getRoles());
            newUser.setEmail(invite.getEmail());
            newUser.setTopicLevel(invite.getTopicLevel());
            userDB.put(uname, newUser);

            inviteDB.remove(invite.getCode());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Account created. Please log in.");
            alert.showAndWait();

            window.setScene(new Scene(buildLoginScreen(window), 400, 350));
        });

        return regGrid;
    }

    // Select User Role Screen
    public GridPane selectUserRole(Stage window, UserAccount user) {
        GridPane roleGrid = new GridPane();
        roleGrid.setPadding(new Insets(10));
        roleGrid.setVgap(8);
        roleGrid.setHgap(10);

        Label selectLbl = new Label("Choose a role:");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(user.getRoles());
        roleCombo.setValue(roleCombo.getItems().get(0));
        Button contBtn = new Button("Continue");

        roleGrid.add(selectLbl, 0, 0);
        roleGrid.add(roleCombo, 0, 1);
        roleGrid.add(contBtn, 0, 2);

        window.setTitle("Select Role");

        contBtn.setOnAction(e -> {
            String selectedRole = roleCombo.getValue();
            if (selectedRole != null) {
                displayHomeScreen(window, user, selectedRole);
            }
        });

        return roleGrid;
    }

    // Password Reset Screen
    public GridPane buildPassResetScreen(Stage window, UserAccount user) {
        GridPane resetGrid = new GridPane();
        resetGrid.setPadding(new Insets(10));
        resetGrid.setVgap(8);
        resetGrid.setHgap(10);

        Label newPassLbl = new Label("New Password:");
        PasswordField newPassTxt = new PasswordField();
        Label confirmNewPassLbl = new Label("Confirm New Password:");
        PasswordField confirmNewPassTxt = new PasswordField();
        Button resetBtn = new Button("Reset Password");
        Label statusLbl = new Label();

        resetGrid.add(newPassLbl, 0, 0);
        resetGrid.add(newPassTxt, 1, 0);
        resetGrid.add(confirmNewPassLbl, 0, 1);
        resetGrid.add(confirmNewPassTxt, 1, 1);
        resetGrid.add(resetBtn, 1, 2);
        resetGrid.add(statusLbl, 1, 3);

        window.setTitle("Reset Password");

        resetBtn.setOnAction(e -> {
            String newPwd = newPassTxt.getText();
            String confirmNewPwd = confirmNewPassTxt.getText();

            if (!validatePassword(newPwd)) {
                statusLbl.setText("Password must be 8+ chars, with uppercase, digit, special char.");
                return;
            }

            if (!newPwd.equals(confirmNewPwd)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }

            user.setPassword(newPwd.toCharArray());
            user.setTempPass(false);
            tempPassDB.remove(user.getUsername());

            statusLbl.setText("Password reset. Please log in.");
            window.setScene(new Scene(buildLoginScreen(window), 400, 350));
        });

        return resetGrid;
    }

    // Complete User Profile Screen
    public GridPane completeUserProfile(Stage window, UserAccount user) {
        GridPane profileGrid = new GridPane();
        profileGrid.setPadding(new Insets(10));
        profileGrid.setVgap(8);
        profileGrid.setHgap(10);

        Label firstLbl = new Label("First Name:");
        TextField firstTxt = new TextField();
        Label middleLbl = new Label("Middle Name:");
        TextField middleTxt = new TextField();
        Label lastLbl = new Label("Last Name:");
        TextField lastTxt = new TextField();
        Label prefLbl = new Label("Preferred Name:");
        TextField prefTxt = new TextField();
        Label emailLbl = new Label("Email:");
        TextField emailTxt = new TextField(user.getEmail() != null ? user.getEmail() : "");
        Button finishBtn = new Button("Finish Setup");
        Label statusLbl = new Label();

        profileGrid.add(firstLbl, 0, 0);
        profileGrid.add(firstTxt, 1, 0);
        profileGrid.add(middleLbl, 0, 1);
        profileGrid.add(middleTxt, 1, 1);
        profileGrid.add(lastLbl, 0, 2);
        profileGrid.add(lastTxt, 1, 2);
        profileGrid.add(prefLbl, 0, 3);
        profileGrid.add(prefTxt, 1, 3);
        profileGrid.add(emailLbl, 0, 4);
        profileGrid.add(emailTxt, 1, 4);
        profileGrid.add(finishBtn, 1, 5);
        profileGrid.add(statusLbl, 1, 6);

        window.setTitle("Complete Profile");

        finishBtn.setOnAction(e -> {
            String firstName = firstTxt.getText();
            String middleName = middleTxt.getText();
            String lastName = lastTxt.getText();
            String preferredName = prefTxt.getText();
            String email = emailTxt.getText();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                statusLbl.setText("Fill in required fields.");
                return;
            }

            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setLastName(lastName);
            user.setPreferredName(preferredName.isEmpty() ? null : preferredName);
            user.setEmail(email);

            statusLbl.setText("Profile complete. Redirecting...");
            if (user.getRoles().size() > 1) {
                window.setScene(new Scene(selectUserRole(window, user), 400, 200));
            } else {
                String role = user.getRoles().iterator().next();
                displayHomeScreen(window, user, role);
            }
        });

        return profileGrid;
    }

    // Method for Admin to Manage Users in Groups
    public void openManageUsersInGroupsScreen(Stage window) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label instructionLbl = new Label("Select a group to manage users:");

        ComboBox<String> groupCombo = new ComboBox<>();
        groupCombo.getItems().addAll(ArticleManagementController.groupDB.keySet());

        Button selectBtn = new Button("Select Group");
        Button backBtn = new Button("Back");

        layout.getChildren().addAll(instructionLbl, groupCombo, selectBtn, backBtn);

        selectBtn.setOnAction(e -> {
            String groupName = groupCombo.getValue();
            if (groupName != null) {
                ArticleGroup group = ArticleManagementController.groupDB.get(groupName);
                openGroupUserManagementScreen(window, group);
            }
        });

        backBtn.setOnAction(e -> displayHomeScreen(window, userDB.get(loggedInAdmin), ROLE_ADMINISTRATOR));

        window.setTitle("Manage Users in Groups");
        Scene scene = new Scene(layout, 400, 300);
        window.setScene(scene);
    }

    // Method to Manage Users within a Group
    public void openGroupUserManagementScreen(Stage window, ArticleGroup group) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label groupLbl = new Label("Managing Users in Group: " + group.getGroupName());

        Label adminsLbl = new Label("Admins:");
        ListView<String> adminsList = new ListView<>();
        adminsList.getItems().addAll(group.getInstructorAdmins());

        Button addAdminBtn = new Button("Add Admin");
        Button removeAdminBtn = new Button("Remove Selected Admin");

        Label instructorsLbl = new Label("Instructors:");
        ListView<String> instructorsList = new ListView<>();
        instructorsList.getItems().addAll(group.getInstructorViewers());

        Button addInstructorBtn = new Button("Add Instructor");
        Button removeInstructorBtn = new Button("Remove Selected Instructor");

        layout.getChildren().addAll(groupLbl, adminsLbl, adminsList, addAdminBtn, removeAdminBtn,
                instructorsLbl, instructorsList, addInstructorBtn, removeInstructorBtn);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> openManageUsersInGroupsScreen(window));
        layout.getChildren().add(backBtn);

        addAdminBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Admin");
            dialog.setHeaderText("Enter the username of the instructor to add as admin:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(username -> {
                if (userDB.containsKey(username)) {
                    group.addInstructorAdmin(username);
                    adminsList.getItems().add(username);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("User not found.");
                    alert.showAndWait();
                }
            });
        });

        removeAdminBtn.setOnAction(e -> {
            String selectedUser = adminsList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                group.removeInstructorAdmin(selectedUser);
                adminsList.getItems().remove(selectedUser);
            }
        });

        addInstructorBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Instructor");
            dialog.setHeaderText("Enter the username of the instructor to add:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(username -> {
                if (userDB.containsKey(username)) {
                    group.addInstructorViewer(username);
                    instructorsList.getItems().add(username);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("User not found.");
                    alert.showAndWait();
                }
            });
        });

        removeInstructorBtn.setOnAction(e -> {
            String selectedUser = instructorsList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                group.removeInstructorViewer(selectedUser);
                instructorsList.getItems().remove(selectedUser);
            }
        });

        window.setTitle("Manage Users in Group: " + group.getGroupName());
        Scene scene = new Scene(layout, 500, 600);
        window.setScene(scene);
    }
}
