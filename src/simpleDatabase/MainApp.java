package simpleDatabase;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import Encryption.EncryptionHelper;

/**
 * Main application class that launches the JavaFX application.
 * Contains the main method and initial setup.
 */
public class MainApp extends Application {
    // Encryption helper instance
    protected EncryptionHelper encryptionHelper;

    public static void main(String[] args) {
        // Add BouncyCastle Provider
        Security.addProvider(new BouncyCastleProvider());
        launch(args);
    }

    @Override
    public void start(Stage mainWindow) {
        try {
            encryptionHelper = new EncryptionHelper();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Initialize the User Management Controller
        UserManagementController userController = new UserManagementController();
        userController.setEncryptionHelper(encryptionHelper);
        
        /*
         * 
         * 
         * 
         */
        // Initialize the Article Management Controller and set the encryption helper
        ArticleManagementController articleController = new ArticleManagementController();
        articleController.setEncryptionHelper(encryptionHelper);

        // Set up any necessary initial data for groups (if needed)
        initializeData();
        /*
         * 
         * 
         * 
         */
        
        // If no users exist, show admin registration scene
        if (UserManagementController.userDB.isEmpty()) {
            mainWindow.setScene(userController.initAdminRegistration(mainWindow));
        } else {
            // Show login screen
            mainWindow.setScene(new Scene(userController.buildLoginScreen(mainWindow), 400, 350));
        }
        mainWindow.show();
    }

    // Method to initialize any necessary data (e.g., default groups)
    private void initializeData() {
        // Example: Create a default general group if needed
        // ArticleGroup defaultGroup = new ArticleGroup("Default", false);
        // ArticleManagementController.groupDB.put("Default", defaultGroup);
    }
}
