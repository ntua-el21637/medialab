package org.medialab;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = Main.getDataManager().login(username, password);

        if (user != null) {
            System.out.println("Login successful");
            navigateToMainDashboard(user);
        }
        else {
            AlertHelper.showError("Login Fialed", "Invalid username or password");
        }
    }

    private void navigateToMainDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/medialab/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.initData(user);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("MediaLab Documents");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Could not load dashboard.");
        }
    }
}
