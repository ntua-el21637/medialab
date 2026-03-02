package org.medialab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {

    static DataManager dataManager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        dataManager = JSONHandler.loadData();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/medialab/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("MediaLab Documents - Login");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (dataManager != null) {
            JSONHandler.saveData(dataManager);
        }
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static void main(String[] args) {
        launch(args);
    }

}