package org.medialab;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DataManager {
    private List<User> users;
    private List<Document> documents;
    private List<Category> categories;

    public DataManager() {
        this.users = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.categories = new ArrayList<>();

        User defaultAdmin = new User("Media", "Lab", UserRole.ADMIN, "medialab", "medialab_2025", new ArrayList<>());
        this.users.add(defaultAdmin);
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Document> getAllDocuments() {
        return documents;
    }


    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------* DOCUMENTS *---------------------------------------------
    //------------------------------------------------------------------------------------------------------

    //Manage Access to Documents
    public List<Document> getAccessibleDocuments(User user) {
        if (user.getRole() == UserRole.ADMIN) {
            return documents;
        }

        return documents.stream()
                .filter(document -> user.getAccessibleCategories().contains(document.getCategory()))
                .collect(Collectors.toList());

    }

    //Open-View Document
    public DocumentVersion viewDocument(User user, Document document) {
        if (user.getWatchedDocuments().containsKey(document.getTitle())) {
            user.getWatchedDocuments().put(document.getTitle(), document.getLatestVersion().getVersion());
        }
        return document.getLatestVersion();
    }

    //Tracking of watched Documents
    public boolean hasNewVersion(User user, Document document) {
        Integer lastViewedVersion = user.getWatchedDocuments().get(document.getTitle());
        if (lastViewedVersion == null) return false;

        return document.getLatestVersion().getVersion() > lastViewedVersion;
    }

    //Add watched Documents
    public void followDocument(User user, Document document) {
        if (!user.getWatchedDocuments().containsKey(document.getTitle())) {
            user.getWatchedDocuments().put(document.getTitle(), document.getLatestVersion().getVersion());
        }
    }

    //Remove watched Documents
    public void unfollowDocument(User user, Document document) {
        user.getWatchedDocuments().remove(document.getTitle());
    }

    //Create Document
    public String createDocument(User creator, String title, String category, String content) {
        if (creator.getRole() == UserRole.SIMPLE_USER) {
            return "Error: You are not allowed to create a document";
        }

        for (Document document : documents) {
            if (document.getTitle().equalsIgnoreCase(title)) {
                return "Error: A document with title " + title + " already exists";
            }
        }

        List<String> paragraphs = Arrays.asList(content.split("\n", -1));

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Document newDocument = new Document(title, creator.getUsername(), category, date, paragraphs);
        documents.add(newDocument);

        return "Document created successfully";
    }

    //Update Document
    public String updateDocument(User editor, Document document, String newContent) {
        boolean isAdmin = editor.getRole() == UserRole.ADMIN;
        boolean isAuthor = editor.getRole() == UserRole.AUTHOR;
        boolean hasAccess = editor.getAccessibleCategories().contains(document.getCategory());

        if (isAdmin || (isAuthor && hasAccess)) {
            List<String> newParagraphs = Arrays.asList(newContent.split("\n", -1));

            document.addVersion(newParagraphs);

            editor.getWatchedDocuments().put(document.getTitle(), document.getLatestVersion().getVersion());

            return "Document updated successfully to version " + document.getLatestVersion().getVersion();

        }


        return "Error: You are not allowed to edit a document in this category";
    }

    //Delete Document
    public String deleteDocument(User actor, Document document) {
        boolean isAdmin = actor.getRole() == UserRole.ADMIN;
        boolean isAuthor = actor.getRole() == UserRole.AUTHOR;
        boolean hasAccess = actor.getAccessibleCategories().contains(document.getCategory());

        if (isAdmin || (isAuthor && hasAccess)) {
            documents.remove(document);

            for (User user : users) {
                user.getWatchedDocuments().remove(document.getTitle());
            }
            return "Document " + document.getTitle() + " deleted successfully";
        }

        return "Error: You are not allowed to delete a document in this category";

    }

    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------* CATEGORIES *--------------------------------------------
    //------------------------------------------------------------------------------------------------------

    //Add Category
    public String addCategory(User admin, String categoryName) {
        if (admin.getRole() != UserRole.ADMIN) {
            return "Error: You are not allowed to add a category";
        }

        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(categoryName)) {
                return "Error: A category with name " + categoryName + " already exists";
            }
        }

        categories.add(new Category(categoryName));

        for (User user : users) {
            if (user.getRole() == UserRole.ADMIN) {
                if (!user.getAccessibleCategories().contains(categoryName)) {
                    user.getAccessibleCategories().add(categoryName);
                }
            }
        }

        return "Category " + categoryName + " added successfully";
    }

    //Assign Categories to user
    public void assignCategory(User admin, User targetUser, String categoryName) {
        if (admin.getRole() == UserRole.ADMIN) {
            if (!targetUser.getAccessibleCategories().contains(categoryName)) {
                targetUser.getAccessibleCategories().add(categoryName);
            }
        }
    }

    //Rename Category
    public String renameCategory(User user, String oldName, String newName) {
        if (user.getRole() != UserRole.ADMIN) {
            return "Error: Only admins can rename categories.";
        }

        for (Category c : getCategories()) {
            if (c.getName().equalsIgnoreCase(newName)) {
                return "Error: Category '" + newName + "' already exists.";
            }
        }

        for (Category c : getCategories()) {
            if (c.getName().equals(oldName)) {
                c.setName(newName);

                for (Document doc : getAllDocuments()) {
                    if (doc.getCategory().equals(oldName)) {
                        doc.setCategory(newName);
                    }
                }

                for (User u : getUsers()) {
                    if (u.getAccessibleCategories().contains(oldName)) {
                        u.getAccessibleCategories().remove(oldName);
                        u.getAccessibleCategories().add(newName);
                    }
                }

                return "Success";
            }
        }
        return "Error: Category not found.";
    }

    //Delete Category
    public void deleteCategory(User admin, String categoryName) {
        if (admin.getRole() != UserRole.ADMIN) {
            return;
        }

        List<Document> documentsToDelete = documents.stream()
                .filter(d -> d.getCategory().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());

        for (Document document : documentsToDelete) {
            for (User user : users) {
                user.getWatchedDocuments().remove(document.getTitle());
            }
        }

        documents.removeAll(documentsToDelete);

        categories.removeIf(category -> category.getName().equalsIgnoreCase(categoryName));

        for (User user : users) {
            user.getAccessibleCategories().remove(categoryName);
        }
    }


    //------------------------------------------------------------------------------------------------------
    //--------------------------------------------* USERS *-------------------------------------------------
    //------------------------------------------------------------------------------------------------------
    //Login
    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    //Add User
    public String createUser(User admin, String firstName, String lastName, UserRole role, String username, String password, List<String> accessibleCategories) {
        if (admin.getRole() != UserRole.ADMIN) {
            return "Error: You are not allowed to create a user";
        }

        if (role != UserRole.ADMIN && (accessibleCategories == null || accessibleCategories.isEmpty())) {
            return "Error: User must have at least one accessible category";
        }

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return "Error: A user with name " + username + " already exists";
            }
        }

        List<String> finalCategories = accessibleCategories;
        if (role == UserRole.ADMIN) {
            finalCategories = this.categories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
        }
        User newUser = new User(firstName, lastName, role, username, password, finalCategories);
        users.add(newUser);

        return "User created successfully";
    }

    //Delete User
    public String deleteUser(User admin, String usernameToDelete) {
        if (admin.getRole() != UserRole.ADMIN) {
            return "Error: You are not allowed to delete a user";
        }

        if (admin.getUsername().equalsIgnoreCase(usernameToDelete)) {
            return ("Error: You can not delete your own account");
        }

        boolean removed = users.removeIf(user -> user.getUsername().equalsIgnoreCase(usernameToDelete));

        if (removed) {
            return "User deleted successfully";
        }
        else {
            return "Error: User not found";
        }
    }

    //Search Documents
    public List<Document> searchDocuments(User user, String titleQuery, String authorQuery, String categoryQuery) {
        List<Document> accessibleDocuments = getAccessibleDocuments(user);

        return accessibleDocuments.stream()
                .filter(doc -> {
                    boolean matchesTitle = (titleQuery == null || titleQuery.isEmpty() || doc.getTitle().toLowerCase().contains(titleQuery.toLowerCase()));
                    boolean matchesAuthor = (authorQuery == null || authorQuery.isEmpty() || doc.getAuthor().toLowerCase().contains(authorQuery.toLowerCase()));
                    boolean matchesCategory = (categoryQuery == null || categoryQuery.isEmpty() || doc.getCategory().toLowerCase().contains(categoryQuery.toLowerCase()));

                    return matchesTitle && matchesAuthor && matchesCategory;
                })
                .collect(Collectors.toList());
    }

    //Get previous versions
    public List<DocumentVersion> getAvailableVersionsForUser(User user, Document document) {
        List<DocumentVersion> allVersions = document.getVersions();

        if (user.getRole() == UserRole.ADMIN) {
            return allVersions;
        }

        if (user.getRole() == UserRole.AUTHOR) {
            int total = allVersions.size();
            int fromIndex = Math.max(0, total - 3);
            return allVersions.subList(fromIndex, total);
        }

        return List.of(document.getLatestVersion());
    }

}
