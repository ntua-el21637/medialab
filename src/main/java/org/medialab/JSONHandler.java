package org.medialab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

public class JSONHandler {
    private static final String FOLDER_NAME = "medialab";
    private static final String FILE_PATH = FOLDER_NAME + "/application_data.json";

    //Start the App
    public static DataManager loadData() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (Files.exists(path)) {
                Reader reader = Files.newBufferedReader(path);
                Gson gson = new Gson();
                DataManager dataManager = gson.fromJson(reader, DataManager.class);
                reader.close();
                if (dataManager != null) return dataManager;
            }
        } catch (IOException e) {
            System.err.println("Error trying to find the JSON files: " + e.getMessage());
        }

        return new DataManager();
    }

    //Close the App
    public static void saveData(DataManager dataManager) {
        try {

            Path dirPath = Paths.get(FOLDER_NAME);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Writer writer = Files.newBufferedWriter(Paths.get(FILE_PATH));

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(dataManager, writer);
            writer.close();

            System.out.println("Saved in file: " + FOLDER_NAME);
        } catch (IOException e) {
            System.err.println("Error during the save of the JSON files: " + e.getMessage());
        }
    }
}