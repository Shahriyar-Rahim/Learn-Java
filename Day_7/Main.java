import java.util.ArrayList;
import java.util.List;

// Main class housing the entry point
public class Main {

      // 1. Book Class
      static class Book {
            private String title;
            private int id;
            private int quantity;

            public Book(String s, int i, int q) {
                  this.title = s;
                  this.id = i;
                  this.quantity = q;
            }

            // Getters & Setters (Needed in Java instead of 'friend' keyword)
            public int getId() {
                  return id;
            }

            public String getTitle() {
                  return title;
            }

            public int getQuantity() {
                  return quantity;
            }

            public void setQuantity(int quantity) {
                  this.quantity = quantity;
            }

            // Overloaded search methods
            public void search(int x) {
                  if (this.id == x) {
                        display();
                  } else {
                        System.out.println("Opps! Not Found of the id: " + x);
                  }
            }

            public void search(String x) {
                  if (this.title.equals(x)) {
                        display();
                  } else {
                        System.out.println("Opps! Not Found of the title: " + x);
                  }
            }

            // Java does not support operator overloading (+ / -).
            // Replaced with standard methods:
            public void addQuantity(Book b) {
                  if (this.id == b.id) {
                        this.quantity += b.quantity;
                  }
            }

            public void addQuantity(int x) {
                  this.quantity += x;
            }

            public void subtractQuantity(Book b) {
                  if (this.id == b.id && this.quantity >= b.quantity) {
                        this.quantity -= b.quantity;
                  }
            }

            public void subtractQuantity(int x) {
                  if (this.quantity >= x) {
                        this.quantity -= x;
                  }
            }

            // All non-static methods in Java are virtual by default
            public void display() {
                  System.out.println("Book Title : " + title);
                  System.out.println("Book ID : " + id);
                  System.out.println("Available Copies : " + quantity);
            }
      }

      // 2. Ebook Class (Inherits from Book)
      static class Ebook extends Book {
            private double fileSize;

            public Ebook(String s, int i, int q, double f) {
                  super(s, i, q);
                  this.fileSize = f;
            }

            @Override
            public void display() {
                  super.display(); // Calls parent display() method
                  System.out.println("File size (in MB) : " + fileSize);
            }
      }

      // 3. Library Class
      static class Library {
            private List<Book> books = new ArrayList<>();

            public void addBook(Book b) {
                  books.add(b);
            }

            public void displayBooks() {
                  for (Book b : books) {
                        b.display();
                  }
            }

            public void borrowBook(int id) {
                  for (Book b : books) {
                        if (id == b.getId()) {
                              if (b.getQuantity() <= 0) {
                                    throw new RuntimeException("Books not available");
                              }
                              b.subtractQuantity(1);
                              System.out.println("Book successfully borrowed! ");
                              return;
                        }
                  }
                  throw new RuntimeException("Invalid Book ID! ");
            }

            public void returnBook(int id) {
                  for (Book b : books) {
                        if (id == b.getId()) {
                              b.addQuantity(1);
                              System.out.println("Book returned successfully ! ");
                              return;
                        }
                  }
                  throw new RuntimeException("Invalid Book ID! ");
            }
      }

      // 4. Main Method
      public static void main(String[] args) {
            Library lib = new Library();

            Book b1 = new Book("C++", 101, 5);
            Book b2 = new Book("OOP", 102, 3);

            lib.addBook(b1);
            lib.addBook(b2);

            System.out.println("\nAll Books : ");
            lib.displayBooks();

            System.out.println("\nSearch By ID : ");
            b1.search(101);

            System.out.println("\nSearch By TITLE : ");
            b1.search("OOP");
            System.out.println();

            try {
                  lib.borrowBook(101);
                  lib.returnBook(101);
                  lib.borrowBook(999);
                  lib.returnBook(555);
            } catch (Exception e) {
                  System.out.println("\nException : " + e.getMessage());
            }

            System.out.println("\n Ebook Information : ");
            Ebook eb1 = new Ebook("AI Basics", 201, 10, 15.5);
            eb1.display();
      }
}