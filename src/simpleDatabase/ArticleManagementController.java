package simpleDatabase;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Encryption.EncryptionHelper;
import java.util.*;
import java.util.Base64;
import java.io.*;

public class ArticleManagementController {

    // Simulated article and group databases
    protected static Map<String, HelpArticle> articleDB = new HashMap<>();
    protected static Map<String, ArticleGroup> groupDB = new HashMap<>();

    // Static counter for article IDs instead of UUID
    private static long articleIdCounter = 1;

    // Encryption helper
    protected EncryptionHelper encryptionHelper;

    // Set the encryption helper
    public void setEncryptionHelper(EncryptionHelper helper) {
        this.encryptionHelper = helper;
    }

    // Open screen for creating a new help article
    public void openCreateArticleScreen(Stage window, UserAccount user) {
        // Check if user is allowed to create articles
        if (!user.getRoles().contains("Teacher") && !user.getRoles().contains("Administrator")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You do not have permission to create articles.");
            alert.showAndWait();
            return;
        }

        GridPane createGrid = new GridPane();
        createGrid.setPadding(new Insets(10));
        createGrid.setVgap(8);
        createGrid.setHgap(10);

        Label titleLbl = new Label("Title:");
        TextField titleTxt = new TextField();

        Label authorLbl = new Label("Author(s):");
        TextField authorTxt = new TextField();

        Label abstractLbl = new Label("Abstract:");
        TextArea abstractTxt = new TextArea();
        abstractTxt.setWrapText(true);

        Label levelLbl = new Label("Content Level:");
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert");
        levelCombo.setValue("Beginner");

        Label groupLbl = new Label("Group:");
        ComboBox<String> groupCombo = new ComboBox<>();
        groupCombo.getItems().addAll(groupDB.keySet());
        if (groupDB.isEmpty()) {
            // If no group exists, user must create one first.
            // For safety, just set a default text:
            groupCombo.setPromptText("No groups available");
        } else {
            groupCombo.setValue(groupDB.keySet().iterator().next());
        }

        Label bodyLbl = new Label("Body:");
        TextArea bodyTxt = new TextArea();
        bodyTxt.setWrapText(true);

        Button createBtn = new Button("Create Article");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        createGrid.add(titleLbl, 0, 0);
        createGrid.add(titleTxt, 1, 0);
        createGrid.add(authorLbl, 0, 1);
        createGrid.add(authorTxt, 1, 1);
        createGrid.add(abstractLbl, 0, 2);
        createGrid.add(abstractTxt, 1, 2);
        createGrid.add(levelLbl, 0, 3);
        createGrid.add(levelCombo, 1, 3);
        createGrid.add(groupLbl, 0, 4);
        createGrid.add(groupCombo, 1, 4);
        createGrid.add(bodyLbl, 0, 5);
        createGrid.add(bodyTxt, 1, 5);
        createGrid.add(createBtn, 1, 6);
        createGrid.add(backBtn, 0, 6);
        createGrid.add(statusLbl, 1, 7);

        window.setTitle("Create Help Article");

        createBtn.setOnAction(e -> {
            String title = titleTxt.getText();
            String authors = authorTxt.getText();
            String abstractText = abstractTxt.getText();
            String level = levelCombo.getValue();
            String groupName = groupCombo.getValue();
            String body = bodyTxt.getText();

            if (title.isEmpty() || authors.isEmpty() || abstractText.isEmpty() || body.isEmpty() || groupName == null || groupName.isEmpty()) {
                statusLbl.setText("Fill in all fields and select a group.");
                return;
            }

            // Generate a simple incremental ID
            String id = String.valueOf(articleIdCounter++);
            HelpArticle article = new HelpArticle(id, title, authors, abstractText, level, body);

            // Add article to group
            if (groupDB.containsKey(groupName)) {
                ArticleGroup group = groupDB.get(groupName);
                // Check if user has rights to add articles to this group
                if (group.getInstructorAdmins().contains(user.getUsername()) || user.getRoles().contains("Administrator")) {
                    group.addArticle(id);
                    article.setGroupName(groupName);
                    // Encrypt the body if it's a special access group
                    if (group.isSpecialAccess()) {
                        try {
                            byte[] encryptedBytes = encryptionHelper.encrypt(body.getBytes());
                            String encryptedBody = Base64.getEncoder().encodeToString(encryptedBytes);
                            article.setBody(encryptedBody);
                            article.setEncrypted(true);
                        } catch (Exception ex) {
                            statusLbl.setText("Encryption failed.");
                            return;
                        }
                    }
                    articleDB.put(id, article);
                    statusLbl.setText("Article created with ID: " + id);
                } else {
                    statusLbl.setText("You do not have admin rights to this group.");
                }
            } else {
                statusLbl.setText("Group not found.");
                return;
            }
        });

        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, user.getRoles().iterator().next());
        });

        Scene createScene = new Scene(createGrid, 600, 700);
        window.setScene(createScene);
    }

    // Open screen for managing help articles
    public void openManageArticlesScreen(Stage window, UserAccount user) {
        VBox manageLayout = new VBox(10);
        manageLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Manage Help Articles");

        ListView<String> articleList = new ListView<>();
        for (HelpArticle article : articleDB.values()) {
            articleList.getItems().add(article.getId() + " - " + article.getTitle());
        }

        Button deleteBtn = new Button("Delete Selected Article");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        manageLayout.getChildren().addAll(titleLbl, articleList, deleteBtn, backBtn, statusLbl);

        // Admins and Teachers can delete articles; they must have admin rights in that group
        deleteBtn.setOnAction(e -> {
            String selected = articleList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLbl.setText("Select an article to delete.");
                return;
            }
            String articleId = selected.split(" - ")[0];
            HelpArticle article = articleDB.get(articleId);
            if (article != null) {
                ArticleGroup group = groupDB.get(article.getGroupName());
                if (group != null && (group.getInstructorAdmins().contains(user.getUsername()) || user.getRoles().contains("Administrator"))) {
                    articleDB.remove(articleId);
                    group.removeArticle(articleId);
                    articleList.getItems().remove(selected);
                    statusLbl.setText("Article deleted.");
                } else {
                    statusLbl.setText("You do not have admin rights to delete articles in this group.");
                }
            }
        });

        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, user.getRoles().iterator().next());
        });

        window.setTitle("Manage Help Articles");
        Scene manageScene = new Scene(manageLayout, 500, 600);
        window.setScene(manageScene);
    }

    // Actual backup articles screen now performs a real backup
    public void openBackupArticlesScreen(Stage window, UserAccount user) {
        backupToFile(window, user);
    }

    // Actual restore articles screen now performs a real restore
    public void openRestoreArticlesScreen(Stage window, UserAccount user) {
        restoreFromFile(window, user);
    }

    // Backup functionality
    private void backupToFile(Stage window, UserAccount user) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup File");
        File file = fileChooser.showSaveDialog(window);
        if (file == null) return;

        BackupData data = new BackupData();
        data.articleMap = new HashMap<>(articleDB);
        data.groupMap = new HashMap<>(groupDB);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
            showAlert("Backup Successful", "Articles and groups have been backed up successfully.");
        } catch (IOException ex) {
            showAlert("Backup Failed", ex.getMessage());
        }

        displayHomeScreen(window, user);
    }

    // Restore functionality
    private void restoreFromFile(Stage window, UserAccount user) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Backup File");
        File file = fileChooser.showOpenDialog(window);
        if (file == null) {
            displayHomeScreen(window, user);
            return;
        }

        Alert choiceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        choiceAlert.setTitle("Restore Mode");
        choiceAlert.setHeaderText("Do you want to remove all existing articles and groups before restoring?");
        choiceAlert.setContentText("Choose an option:");
        ButtonType removeAllBtn = new ButtonType("Remove All First");
        ButtonType mergeBtn = new ButtonType("Merge");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        choiceAlert.getButtonTypes().setAll(removeAllBtn, mergeBtn, cancelBtn);

        Optional<ButtonType> result = choiceAlert.showAndWait();
        if (result.isEmpty() || result.get() == cancelBtn) {
            // Cancel restore
            displayHomeScreen(window, user);
            return;
        }

        boolean removeAll = (result.get() == removeAllBtn);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            BackupData data = (BackupData) ois.readObject();
            if (removeAll) {
                articleDB.clear();
                groupDB.clear();
            }

            // Merge articles
            for (Map.Entry<String, HelpArticle> entry : data.articleMap.entrySet()) {
                String newId = entry.getKey();
                if (!articleDB.containsKey(newId)) {
                    articleDB.put(newId, entry.getValue());
                }
            }

            // Merge groups
            for (Map.Entry<String, ArticleGroup> entry : data.groupMap.entrySet()) {
                String grpName = entry.getKey();
                if (!groupDB.containsKey(grpName)) {
                    groupDB.put(grpName, entry.getValue());
                } else {
                    // Merge group's details
                    ArticleGroup existing = groupDB.get(grpName);
                    existing.getArticleIds().addAll(entry.getValue().getArticleIds());
                    existing.getInstructorAdmins().addAll(entry.getValue().getInstructorAdmins());
                    existing.getInstructorViewers().addAll(entry.getValue().getInstructorViewers());
                    existing.getStudentViewers().addAll(entry.getValue().getStudentViewers());
                    existing.setSpecialAccess(entry.getValue().isSpecialAccess());
                }
            }

            showAlert("Restore Successful", "Articles and groups have been restored successfully.");
        } catch (IOException | ClassNotFoundException ex) {
            showAlert("Restore Failed", ex.getMessage());
        }

        displayHomeScreen(window, user);
    }

    // Open screen for managing article groups
    public void openManageGroupsScreen(Stage window, UserAccount user) {
        VBox groupLayout = new VBox(10);
        groupLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Manage Article Groups");

        ListView<String> groupList = new ListView<>();
        groupList.getItems().addAll(groupDB.keySet());

        Button createGroupBtn = new Button("Create New Group");
        Button deleteGroupBtn = new Button("Delete Selected Group");
        Button backBtn = new Button("Back");
        Label statusLbl = new Label();

        groupLayout.getChildren().addAll(titleLbl, groupList, createGroupBtn, deleteGroupBtn, backBtn, statusLbl);

        createGroupBtn.setOnAction(e -> {
            createGroup(window, user);
        });

        deleteGroupBtn.setOnAction(e -> {
            String selectedGroup = groupList.getSelectionModel().getSelectedItem();
            if (selectedGroup == null) {
                statusLbl.setText("Select a group to delete.");
                return;
            }
            ArticleGroup group = groupDB.get(selectedGroup);
            if (group != null && (group.getInstructorAdmins().contains(user.getUsername()) || user.getRoles().contains("Administrator"))) {
                groupDB.remove(selectedGroup);
                groupList.getItems().remove(selectedGroup);
                statusLbl.setText("Group deleted.");
            } else {
                statusLbl.setText("You do not have admin rights to delete this group.");
            }
        });

        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, user.getRoles().iterator().next());
        });

        window.setTitle("Manage Article Groups");
        Scene groupScene = new Scene(groupLayout, 500, 600);
        window.setScene(groupScene);
    }

    // Method to create a new group
    private void createGroup(Stage window, UserAccount user) {
        GridPane groupGrid = new GridPane();
        groupGrid.setPadding(new Insets(10));
        groupGrid.setVgap(8);
        groupGrid.setHgap(10);

        Label nameLbl = new Label("Group Name:");
        TextField nameTxt = new TextField();

        Label typeLbl = new Label("Group Type:");
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton generalBtn = new RadioButton("General");
        RadioButton specialBtn = new RadioButton("Special Access");
        generalBtn.setToggleGroup(typeGroup);
        specialBtn.setToggleGroup(typeGroup);
        generalBtn.setSelected(true);
        HBox typeBox = new HBox(10, generalBtn, specialBtn);

        Button createBtn = new Button("Create Group");
        Button cancelBtn = new Button("Cancel");
        Label statusLbl = new Label();

        groupGrid.add(nameLbl, 0, 0);
        groupGrid.add(nameTxt, 1, 0);
        groupGrid.add(typeLbl, 0, 1);
        groupGrid.add(typeBox, 1, 1);
        groupGrid.add(createBtn, 1, 2);
        groupGrid.add(cancelBtn, 0, 2);
        groupGrid.add(statusLbl, 1, 3);

        Stage groupStage = new Stage();
        groupStage.setTitle("Create New Group");

        createBtn.setOnAction(e -> {
            String groupName = nameTxt.getText().trim();
            if (groupName.isEmpty()) {
                statusLbl.setText("Enter a group name.");
                return;
            }
            if (groupDB.containsKey(groupName)) {
                statusLbl.setText("Group already exists.");
                return;
            }

            boolean isSpecial = specialBtn.isSelected();
            ArticleGroup newGroup = new ArticleGroup(groupName, isSpecial);
            newGroup.addInstructorAdmin(user.getUsername());
            if (isSpecial) {
                newGroup.addInstructorViewer(user.getUsername());
            }
            groupDB.put(groupName, newGroup);
            statusLbl.setText("Group created.");

            groupStage.close();
            // Refresh the group list
            openManageGroupsScreen(window, user);
        });

        cancelBtn.setOnAction(e -> {
            groupStage.close();
            openManageGroupsScreen(window, user);
        });

        Scene scene = new Scene(groupGrid, 400, 200);
        groupStage.setScene(scene);
        groupStage.show();
    }

    // Open screen for students to search articles
    public void openStudentSearchArticlesScreen(Stage window, UserAccount user) {
        VBox searchLayout = new VBox(10);
        searchLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Search Help Articles");

        Label levelLbl = new Label("Content Level:");
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert", "All");
        levelCombo.setValue(user.getTopicLevel() != null ? user.getTopicLevel() : "All");

        Label groupLbl = new Label("Article Group:");
        ComboBox<String> groupCombo = new ComboBox<>();
        groupCombo.getItems().add("All");
        groupCombo.getItems().addAll(groupDB.keySet());
        groupCombo.setValue("All");

        Label searchLbl = new Label("Search Query:");
        TextField searchTxt = new TextField();

        Button searchBtn = new Button("Search");
        ListView<String> resultList = new ListView<>();
        Label statusLbl = new Label();

        Button viewArticleBtn = new Button("View Selected Article");
        Button backBtn = new Button("Back");

        searchLayout.getChildren().addAll(titleLbl, levelLbl, levelCombo, groupLbl, groupCombo,
                searchLbl, searchTxt, searchBtn, resultList, viewArticleBtn, backBtn, statusLbl);

        searchBtn.setOnAction(e -> {
            String level = levelCombo.getValue();
            String groupName = groupCombo.getValue();
            String query = searchTxt.getText().trim().toLowerCase();

            List<HelpArticle> results = new ArrayList<>();

            for (HelpArticle article : articleDB.values()) {
                boolean matchesLevel = level.equals("All") || article.getLevel().equalsIgnoreCase(level);
                boolean matchesGroup = groupName.equals("All") || article.getGroupName().equals(groupName);
                boolean matchesQuery = article.getTitle().toLowerCase().contains(query) ||
                        article.getAuthors().toLowerCase().contains(query) ||
                        article.getAbstractText().toLowerCase().contains(query);

                if (matchesLevel && matchesGroup && matchesQuery) {
                    // Check if the article is in a special access group
                    ArticleGroup group = groupDB.get(article.getGroupName());
                    if (group != null && group.isSpecialAccess()) {
                        // Check if the student has access to this group
                        if (group.getStudentViewers().contains(user.getUsername())) {
                            results.add(article);
                        }
                    } else {
                        results.add(article);
                    }
                }
            }

            resultList.getItems().clear();
            int seqNum = 1;
            for (HelpArticle article : results) {
                String item = seqNum++ + ". Title: " + article.getTitle() +
                        ", Authors: " + article.getAuthors() +
                        ", Abstract: " + article.getAbstractText();
                resultList.getItems().add(item);
            }

            // Display the number of articles that match each level
            Map<String, Integer> levelCounts = new HashMap<>();
            for (String lvl : Arrays.asList("Beginner", "Intermediate", "Advanced", "Expert")) {
                levelCounts.put(lvl, 0);
            }
            for (HelpArticle article : results) {
                String artLevel = article.getLevel();
                levelCounts.put(artLevel, levelCounts.getOrDefault(artLevel, 0) + 1);
            }

            StringBuilder levelInfo = new StringBuilder();
            for (String lvl : levelCounts.keySet()) {
                levelInfo.append(lvl).append(": ").append(levelCounts.get(lvl)).append(" articles; ");
            }

            statusLbl.setText("Group: " + groupName + "\n" + levelInfo.toString());
        });

        viewArticleBtn.setOnAction(e -> {
            String selected = resultList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLbl.setText("Select an article to view.");
                return;
            }

            int seqNum = Integer.parseInt(selected.split("\\.")[0]) - 1;
            List<HelpArticle> results = new ArrayList<>();

            String level = levelCombo.getValue();
            String groupName = groupCombo.getValue();
            String query = searchTxt.getText().trim().toLowerCase();

            for (HelpArticle article : articleDB.values()) {
                boolean matchesLevel = level.equals("All") || article.getLevel().equalsIgnoreCase(level);
                boolean matchesGroup = groupName.equals("All") || article.getGroupName().equals(groupName);

                boolean matchesQuery = article.getTitle().toLowerCase().contains(query) ||
                        article.getAuthors().toLowerCase().contains(query) ||
                        article.getAbstractText().toLowerCase().contains(query);

                if (matchesLevel && matchesGroup && matchesQuery) {
                    // Check if the article is in a special access group
                    ArticleGroup group = groupDB.get(article.getGroupName());
                    if (group != null && group.isSpecialAccess()) {
                        // Check if the student has access
                        if (group.getStudentViewers().contains(user.getUsername())) {
                            results.add(article);
                        }
                    } else {
                        results.add(article);
                    }
                }
            }

            if (seqNum >= 0 && seqNum < results.size()) {
                HelpArticle article = results.get(seqNum);
                displayArticleDetail(window, article, user);
            } else {
                statusLbl.setText("Invalid selection.");
            }
        });

        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, UserManagementController.ROLE_LEARNER);
        });

        window.setTitle("Search Help Articles");
        Scene searchScene = new Scene(searchLayout, 600, 700);
        window.setScene(searchScene);
    }

    // Display article detail for students and instructors
    public void displayArticleDetail(Stage window, HelpArticle article, UserAccount user) {
        VBox detailLayout = new VBox(10);
        detailLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Title: " + article.getTitle());
        Label authorLbl = new Label("Authors: " + article.getAuthors());
        Label abstractLbl = new Label("Abstract: " + article.getAbstractText());
        Label levelLbl = new Label("Level: " + article.getLevel());
        Label bodyLbl = new Label("Body:");
        TextArea bodyTxt = new TextArea();
        bodyTxt.setWrapText(true);
        bodyTxt.setEditable(false);

        // Check if article is encrypted
        if (article.isEncrypted()) {
            ArticleGroup group = groupDB.get(article.getGroupName());
            if (group != null && group.isSpecialAccess()) {
                // Check if user has access to decrypt
                if (group.getInstructorViewers().contains(user.getUsername()) ||
                        group.getStudentViewers().contains(user.getUsername())) {
                    try {
                        byte[] encryptedBytes = Base64.getDecoder().decode(article.getBody());
                        byte[] decryptedBytes = encryptionHelper.decrypt(encryptedBytes);
                        String decryptedBody = new String(decryptedBytes);
                        bodyTxt.setText(decryptedBody);
                    } catch (Exception ex) {
                        bodyTxt.setText("Decryption failed.");
                    }
                } else {
                    bodyTxt.setText("You do not have access to view this article.");
                }
            }
        } else {
            bodyTxt.setText(article.getBody());
        }

        Button backBtn = new Button("Back to Search");
        Button newSearchBtn = new Button("New Search");
        Button homeBtn = new Button("Home");

        detailLayout.getChildren().addAll(titleLbl, authorLbl, abstractLbl, levelLbl, bodyLbl, bodyTxt,
                backBtn, newSearchBtn, homeBtn);

        backBtn.setOnAction(e -> {
            if (user.getRoles().contains("Teacher")) {
                openInstructorSearchArticlesScreen(window, user);
            } else {
                openStudentSearchArticlesScreen(window, user);
            }
        });

        newSearchBtn.setOnAction(e -> {
            if (user.getRoles().contains("Teacher")) {
                openInstructorSearchArticlesScreen(window, user);
            } else {
                openStudentSearchArticlesScreen(window, user);
            }
        });

        homeBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, user.getRoles().iterator().next());
        });

        window.setTitle("Article Detail");
        Scene detailScene = new Scene(detailLayout, 600, 700);
        window.setScene(detailScene);
    }

    // Open screen for instructors to search articles
    public void openInstructorSearchArticlesScreen(Stage window, UserAccount user) {
        VBox searchLayout = new VBox(10);
        searchLayout.setPadding(new Insets(10));

        Label titleLbl = new Label("Search Help Articles");

        Label levelLbl = new Label("Content Level:");
        ComboBox<String> levelCombo = new ComboBox<>();
        levelCombo.getItems().addAll("Beginner", "Intermediate", "Advanced", "Expert", "All");
        levelCombo.setValue(user.getTopicLevel() != null ? user.getTopicLevel() : "All");

        Label groupLbl = new Label("Article Group:");
        ComboBox<String> groupCombo = new ComboBox<>();
        groupCombo.getItems().add("All");
        groupCombo.getItems().addAll(groupDB.keySet());
        groupCombo.setValue("All");

        Label searchLbl = new Label("Search Query:");
        TextField searchTxt = new TextField();

        Button searchBtn = new Button("Search");
        ListView<String> resultList = new ListView<>();
        Label statusLbl = new Label();

        Button viewArticleBtn = new Button("View Selected Article");
        Button backBtn = new Button("Back");

        searchLayout.getChildren().addAll(titleLbl, levelLbl, levelCombo, groupLbl, groupCombo,
                searchLbl, searchTxt, searchBtn, resultList, viewArticleBtn, backBtn, statusLbl);

        searchBtn.setOnAction(e -> {
            String level = levelCombo.getValue();
            String groupName = groupCombo.getValue();
            String query = searchTxt.getText().trim().toLowerCase();

            List<HelpArticle> results = new ArrayList<>();

            for (HelpArticle article : articleDB.values()) {
                boolean matchesLevel = level.equals("All") || article.getLevel().equalsIgnoreCase(level);
                boolean matchesGroup = groupName.equals("All") || article.getGroupName().equals(groupName);
                boolean matchesQuery = article.getTitle().toLowerCase().contains(query) ||
                        article.getAuthors().toLowerCase().contains(query) ||
                        article.getAbstractText().toLowerCase().contains(query);

                if (matchesLevel && matchesGroup && matchesQuery) {
                    // Check if the article is in a special access group
                    ArticleGroup group = groupDB.get(article.getGroupName());
                    if (group != null && group.isSpecialAccess()) {
                        // Check if the instructor has access
                        if (group.getInstructorViewers().contains(user.getUsername()) ||
                                group.getInstructorAdmins().contains(user.getUsername())) {
                            results.add(article);
                        }
                    } else {
                        results.add(article);
                    }
                }
            }

            resultList.getItems().clear();
            int seqNum = 1;
            for (HelpArticle article : results) {
                String item = seqNum++ + ". Title: " + article.getTitle() +
                        ", Authors: " + article.getAuthors() +
                        ", Abstract: " + article.getAbstractText();
                resultList.getItems().add(item);
            }

            // Display the number of articles that match each level
            Map<String, Integer> levelCounts = new HashMap<>();
            for (String lvl : Arrays.asList("Beginner", "Intermediate", "Advanced", "Expert")) {
                levelCounts.put(lvl, 0);
            }
            for (HelpArticle article : results) {
                String artLevel = article.getLevel();
                levelCounts.put(artLevel, levelCounts.getOrDefault(artLevel, 0) + 1);
            }

            StringBuilder levelInfo = new StringBuilder();
            for (String lvl : levelCounts.keySet()) {
                levelInfo.append(lvl).append(": ").append(levelCounts.get(lvl)).append(" articles; ");
            }

            statusLbl.setText("Group: " + groupName + "\n" + levelInfo.toString());
        });

        viewArticleBtn.setOnAction(e -> {
            String selected = resultList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLbl.setText("Select an article to view.");
                return;
            }

            int seqNum = Integer.parseInt(selected.split("\\.")[0]) - 1;
            List<HelpArticle> results = new ArrayList<>();

            String level = levelCombo.getValue();
            String groupName = groupCombo.getValue();
            String query = searchTxt.getText().trim().toLowerCase();

            for (HelpArticle article : articleDB.values()) {
                boolean matchesLevel = level.equals("All") || article.getLevel().equalsIgnoreCase(level);
                boolean matchesGroup = groupName.equals("All") || article.getGroupName().equals(groupName);
                boolean matchesQuery = article.getTitle().toLowerCase().contains(query) ||
                        article.getAuthors().toLowerCase().contains(query) ||
                        article.getAbstractText().toLowerCase().contains(query);

                if (matchesLevel && matchesGroup && matchesQuery) {
                    // Check if the article is in a special access group
                    ArticleGroup group = groupDB.get(article.getGroupName());
                    if (group != null && group.isSpecialAccess()) {
                        // Check if instructor has access
                        if (group.getInstructorViewers().contains(user.getUsername()) ||
                                group.getInstructorAdmins().contains(user.getUsername())) {
                            results.add(article);
                        }
                    } else {
                        results.add(article);
                    }
                }
            }

            if (seqNum >= 0 && seqNum < results.size()) {
                HelpArticle article = results.get(seqNum);
                displayArticleDetail(window, article, user);
            } else {
                statusLbl.setText("Invalid selection.");
            }
        });

        backBtn.setOnAction(e -> {
            UserManagementController userController = new UserManagementController();
            userController.setEncryptionHelper(encryptionHelper);
            userController.displayHomeScreen(window, user, UserManagementController.ROLE_TEACHER);
        });

        window.setTitle("Search Help Articles");
        Scene searchScene = new Scene(searchLayout, 600, 700);
        window.setScene(searchScene);
    }

    // Helper method to return to home screen
    private void displayHomeScreen(Stage window, UserAccount user) {
        UserManagementController userController = new UserManagementController();
        userController.setEncryptionHelper(encryptionHelper);
        userController.displayHomeScreen(window, user, user.getRoles().iterator().next());
    }

    // Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
