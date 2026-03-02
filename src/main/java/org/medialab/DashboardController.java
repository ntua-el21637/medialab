package org.medialab;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;


import java.util.Optional;

public class DashboardController {

    @FXML private Label totalCategoriesLabel;
    @FXML private Label totalDocumentsLabel;
    @FXML private Label followedDocumentsLabel;

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> categoryNameColumn;
    @FXML private TextField newCategoryField;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private ListView<String> categorySelectionList;

    @FXML private TextField userFirstNameField;
    @FXML private TextField userLastNameField;
    @FXML private TextField userUsernameField;
    @FXML private PasswordField userPasswordField;
    @FXML private ComboBox<UserRole> userRoleComboBox;

    @FXML private TabPane mainTabPane;
    @FXML private Tab usersTab;
    @FXML private Tab categoriesTab;

    @FXML private TableView<Document> documentTable;
    @FXML private TableColumn<Document, String> docTitleColumn;
    @FXML private TableColumn<Document, String> docAuthorColumn;
    @FXML private TableColumn<Document, String> docCategoryColumn;
    @FXML private TableColumn<Document, String> docDateColumn;
    @FXML private TableColumn<Document, String> docVersionColumn;

    @FXML private Button createDocumentButton;
    @FXML private Button editDocumentButton;
    @FXML private Button deleteDocumentButton;
    @FXML private Button versionHistoryButton;

    @FXML private TextField searchTitleField;
    @FXML private TextField searchAuthorField;
    @FXML private ComboBox<String> filterCategoryCombo;

    private User currentUser;
    private DataManager dataManager;

    public void initData(User user){
        this.currentUser = user;
        this.dataManager = Main.getDataManager();

        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        refreshCategoryTable();

        initDocumentTable();
        initUserTab();

        updateStatistics();
        checkForUpdates();

        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.addAll(dataManager.getCategories().stream().map(Category::getName).toList());

        filterCategoryCombo.setItems(FXCollections.observableArrayList(categories));
        filterCategoryCombo.setValue("All Categories");

        applyRolePermissions();
        refreshDocumentList();

    }

    private void applyRolePermissions() {
        if (currentUser.getRole() != UserRole.ADMIN) {
            mainTabPane.getTabs().remove(usersTab);
            mainTabPane.getTabs().remove(categoriesTab);
        }
        if (currentUser.getRole() == UserRole.SIMPLE_USER) {
            createDocumentButton.setDisable(true);
            editDocumentButton.setDisable(true);
            deleteDocumentButton.setDisable(true);
            versionHistoryButton.setDisable(true);
        }
    }

    private void initUserTab() {
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        userRoleComboBox.setItems(FXCollections.observableArrayList(UserRole.values()));

        categorySelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        refreshUserList();

        refreshCategorySelectionList();

    }

    private void refreshUserList() {
        userTable.setItems(FXCollections.observableArrayList(dataManager.getUsers()));
    }

    private void refreshCategorySelectionList() {
        List<String> catNames = dataManager.getCategories().stream()
                .map(Category::getName)
                .toList();
        categorySelectionList.setItems(FXCollections.observableArrayList(catNames));
    }

    private void updateStatistics() {
        int cats = dataManager.getCategories().size();
        int docs = dataManager.getAllDocuments().size();
        int followed = currentUser.getWatchedDocuments().size();

        totalCategoriesLabel.setText("Total Categories: " + cats);
        totalDocumentsLabel.setText("Total Documents: " + docs);
        followedDocumentsLabel.setText("Followed Documents: " + followed);
        followedDocumentsLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
    }

    private void checkForUpdates(){
        StringBuilder updates = new StringBuilder();

        List<Document> accessibleDocs = dataManager.getAccessibleDocuments(currentUser);

        for (Document doc : accessibleDocs){
            if (dataManager.hasNewVersion(currentUser, doc)){
                updates.append("- ").append(doc.getTitle()).append("\n");
            }
        }

        if (updates.length() > 0){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("New Version Available");
            alert.setHeaderText("New Version Available for documents you follow:");
            alert.setContentText(updates.toString());
            alert.showAndWait();
        }
    }

    private void refreshCategoryTable(){
        categoryTable.setItems(FXCollections.observableArrayList(dataManager.getCategories()));
        updateStatistics();
    }

    @FXML
    private void handleAddCategory() {
        String name = newCategoryField.getText();
        if (name == null || name.trim().isEmpty()) return;

        String result = dataManager.addCategory(currentUser, name);
        if (result.startsWith("Error")) {
            AlertHelper.showError("Failure", result);
        }
        else {
            newCategoryField.clear();
            refreshCategoryTable();
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dataManager.deleteCategory(currentUser, selected.getName());
            refreshCategoryTable();
        }
    }

    @FXML
    private void handleRenameCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertHelper.showError("Selection Error", "Please select a category to rename");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Rename Category");
        dialog.setHeaderText("Renaming category: " + selected.getName());
        dialog.setContentText("Enter the new category name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newName -> {
            if (newName.trim().isEmpty()) {
                AlertHelper.showError("Input Error", "Category name cannot be empty.");
                return;
            }

            String status = dataManager.renameCategory(currentUser, selected.getName(), newName.trim());

            if (status.startsWith("Error")) {
                AlertHelper.showError("Failure", status);
            } else {
                refreshCategoryTable();
            }

            if (status.startsWith("Error")) {
                AlertHelper.showError("Failure", status);
            } else {
                categoryTable.refresh();
                updateStatistics();
            }
        });



    }

    @FXML
    private void handleAddUser() {
        String firstName = userFirstNameField.getText();
        String lastName = userLastNameField.getText();
        String username = userUsernameField.getText();
        String password = userPasswordField.getText();
        UserRole role = userRoleComboBox.getValue();

        ObservableList<String> selectedItems = categorySelectionList.getSelectionModel().getSelectedItems();
        List<String> selectedCategories = new ArrayList<>(selectedItems);

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
            AlertHelper.showError("Input Error", "Please fill in all the user fields.");
            return;
        }

        if (selectedCategories.isEmpty() && role != UserRole.ADMIN) {
            AlertHelper.showError("Input Error", "Please select at least one accessible category for the user.");
            return;
        }

        String result = dataManager.createUser(
                currentUser,
                firstName,
                lastName,
                role,
                username,
                password,
                selectedCategories
        );

        if (result.startsWith("Error")) {
            AlertHelper.showError("Failure", result);
        }
        else {
            userFirstNameField.clear();
            userLastNameField.clear();
            userUsernameField.clear();
            userPasswordField.clear();
            userRoleComboBox.getSelectionModel().clearSelection();
            categorySelectionList.getSelectionModel().clearSelection();

            refreshUserList();
            updateStatistics();

            AlertHelper.showInfo("Success", result);
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            AlertHelper.showError("Selection Error", "Please select a user to delete.");
            return;
        }

        boolean confirm = AlertHelper.showConfirmation("Confirm Delete", "Are you sure you want to delete user: " +selectedUser.getUsername() + "?");

        if (confirm) {
            String result = dataManager.deleteUser(currentUser, selectedUser.getUsername());

            if (result.startsWith("Error")) {
                AlertHelper.showError("Failure", result);
            }
            else {
                refreshUserList();
                AlertHelper.showInfo("Deleted", result);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------* DOCUMENTS *---------------------------------------------
    //------------------------------------------------------------------------------------------------------
    private void initDocumentTable() {
        docTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        docTitleColumn.setCellFactory(column -> new TableCell<Document, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Document doc = getTableRow().getItem();
                    // Αν ο χρήστης ακολουθεί αυτό το έγγραφο, κάνε το μπλε
                    if (doc != null && currentUser.getWatchedDocuments().containsKey(doc.getTitle())) {
                        setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    } else {
                        setStyle(""); // Επαναφορά στο κανονικό για τα υπόλοιπα
                    }
                }
            }
        });

        docAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        docCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        docDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        docVersionColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getLatestVersion() != null) {
                return new SimpleObjectProperty<>(cellData.getValue().getLatestVersion().getVersion()).asString();
            }
            return new SimpleStringProperty("0");
        });

        documentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleCreateDocument() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Document");
        dialog.setHeaderText("Enter Document Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Document Title");

        List<String> userCategories = currentUser.getAccessibleCategories();
        ComboBox<String> categoryComboBox = new ComboBox<>(FXCollections.observableArrayList(userCategories));
        categoryComboBox.setPromptText("Select Category");

        TextArea contentArea =  new TextArea();
        contentArea.setPromptText("Enter your text here...");
        contentArea.setPrefRowCount(10);
        contentArea.setWrapText(true);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryComboBox, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButtonType) {
            String title = titleField.getText();
            String category = categoryComboBox.getValue();
            String content = contentArea.getText();

            if (title.isEmpty() || category == null || content.isEmpty()) {
                AlertHelper.showError("Input Error", "Please fill in all the fields.");
                return;
            }

            String status = dataManager.createDocument(currentUser, title, category, content);

            if (status.startsWith("Error")) {
                AlertHelper.showError("Failure", status);
            }
            else {
                refreshDocumentList();
                updateStatistics();
                AlertHelper.showInfo("Success", status);
            }
        }
    }

    @FXML
    private void handleViewDocument() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();

        if (selectedDocument == null) {
            AlertHelper.showError("Selection Error", "Please select a document to display.");
            return;
        }

        dataManager.viewDocument(currentUser, selectedDocument);

        String fullContent = String.join("\n", selectedDocument.getLatestVersion().getParagraphs());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("View Mode: " + selectedDocument.getTitle());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TextArea contentArea = new TextArea(fullContent);
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(15);

        dialog.getDialogPane().setContent(contentArea);
        dialog.showAndWait();
    }

    @FXML
    private void handleEditDocument() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            AlertHelper.showError("Selection Error", "Please select a document to edit.");
            return;
        }

        String fullContent = String.join("\n", selectedDocument.getLatestVersion().getParagraphs());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Mode: " + selectedDocument.getTitle());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);

        TextArea contentArea = new TextArea(fullContent);
        contentArea.setEditable(true);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(15);

        dialog.getDialogPane().setContent(contentArea);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            String newText = contentArea.getText();

            String status = dataManager.updateDocument(currentUser, selectedDocument, newText);

            if (status.startsWith("Error")) {
                AlertHelper.showError("Failure", status);
            }
            else {
                refreshDocumentList();
                AlertHelper.showInfo("Success", status);
            }
        }
    }

    @FXML
    private void handleDeleteDocument() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            AlertHelper.showError("Selection Error", "Please select a document to delete.");
            return;
        }

        boolean confirmation = AlertHelper.showConfirmation("Delete Document", "Document: " + selectedDocument.getTitle() + "\nThis action cannot be undone.");

        if (confirmation) {
            String status = dataManager.deleteDocument(currentUser, selectedDocument);
            if (status.startsWith("Error")) {
                AlertHelper.showError("Failure", status);
            }
            else {
                refreshDocumentList();
                updateStatistics();
                AlertHelper.showInfo("Success", status);
            }
        }
    }

    @FXML
    private void handleVersionHistory() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            AlertHelper.showError("Selection Error", "Please select a document to display.");
            return;
        }

        List<DocumentVersion> versions = dataManager.getAvailableVersionsForUser(currentUser, selectedDocument);

        ChoiceDialog<DocumentVersion> dialog = new ChoiceDialog<>(versions.get(versions.size() -1), versions);
        dialog.setTitle("Version History");
        dialog.setHeaderText("Document: " + selectedDocument.getTitle());
        dialog.setContentText("Choose a document version:");

        Optional<DocumentVersion> result = dialog.showAndWait();
        result.ifPresent(version -> {
            showVersionContent(selectedDocument.getTitle(), version);
        });


    }

    private void showVersionContent(String title, DocumentVersion version) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Viewing Version " + version.getVersion());
        dialog.setHeaderText("Document: " + title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        String content = String.join("\n", version.getParagraphs());

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(40);

        dialog.getDialogPane().setContent(textArea);
        dialog.showAndWait();
    }

    @FXML
    private void handleSearch() {
        String titleQuery = searchTitleField.getText();
        String authorQuery = searchAuthorField.getText();
        String categoryQuery = filterCategoryCombo.getValue();

        if ("All Categories".equals(categoryQuery)) {
            categoryQuery = "";
        }

        List<Document> filteredDocs = dataManager.searchDocuments(
                currentUser,
                titleQuery,
                authorQuery,
                categoryQuery
        );

        documentTable.setItems(FXCollections.observableArrayList(filteredDocs));
        documentTable.refresh();

    }

    @FXML
    private void handleClear() {
        searchTitleField.clear();
        searchAuthorField.clear();

        filterCategoryCombo.getSelectionModel().select("All Categories");

        refreshDocumentList();
    }

    public void refreshDocumentList() {
        List<Document> accessibleDocs = dataManager.getAccessibleDocuments(currentUser);

        ObservableList<Document> observableDocs = FXCollections.observableArrayList(accessibleDocs);

        documentTable.setItems(observableDocs);

        documentTable.refresh();
    }

    @FXML
    private void handleFollowDocument() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            AlertHelper.showError("Selection Error", "Please select a document to follow.");
            return;
        }

        if (currentUser.getWatchedDocuments().containsKey(selectedDocument.getTitle())) {
            AlertHelper.showInfo("Already Following", "You are already following this document.");
            return;
        }

        dataManager.followDocument(currentUser, selectedDocument);
        documentTable.refresh();
        updateStatistics();
        AlertHelper.showInfo("Success", "You are now following: " + selectedDocument.getTitle());
    }

    @FXML
    private void handleUnfollowDocument() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            AlertHelper.showError("Selection Error", "Please select a document to unfollow.");
            return;
        }

        if (!currentUser.getWatchedDocuments().containsKey(selectedDocument.getTitle())) {
            AlertHelper.showError("Error", "You are not following this document.");
            return;
        }

        dataManager.unfollowDocument(currentUser, selectedDocument);
        documentTable.refresh();
        updateStatistics();
        AlertHelper.showInfo("Success", "Unfollowed: " + selectedDocument.getTitle());
    }
}

























