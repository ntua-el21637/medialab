module FileManager {
    requires com.google.gson;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens org.medialab to javafx.fxml, com.google.gson;

    exports org.medialab;
}