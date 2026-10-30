package library.model;

public enum BookSort {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    DATE_NEWEST("Publication Date (Newest first)"),
    DATE_OLDEST("Publication Date (Oldest first)"),
    AUTHOR("Author Name"),
    SECTION("Catalogue Section"),
    AVAILABILITY("Available Copies (High to Low)");

    private final String label;

    BookSort(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
