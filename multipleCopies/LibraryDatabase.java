package library.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.NavigableSet;
import java.util.TreeMap;
import org.javatuples.Pair;

/**
 * Holds all data for the Library, storing it in a file and reading and
 * updating the file as needed.
 *
 * <p>Singleton class: only one instance exists at a time.
 */
public class LibraryDatabase implements java.io.Serializable {

  // All implementation code is omitted, but comments and declarations
  // are included, to define the interface used by unit tests.



  // getInstance is included, to be sure you have the fixed code that's
  // needed for unit testing.
  /**
   * Get the singleton instance of LibraryDatabase. If the instance does not exist, it will be
   * created by reading from a file.
   */
  public static LibraryDatabase getInstance() {
    return getInstance(false);
  }

  /**
   * In the normal (non-testing) case, return the singleton instance, or create one by reading from
   * a file, or create a new one if the file does not exist.
   *
   * <p>In the testing case, always create a new empty LibraryDatabase (even if one exists).
   *
   * @param testing If true, the database always starts empty.
   */
  public static LibraryDatabase getInstance(boolean testing) {
    if (testing) {
      return new LibraryDatabase();
    }
    if (instance == null) {
      try {
        readFromFile();
      } catch (FileNotFoundException e) {
        instance = new LibraryDatabase();
      } catch (Throwable e) {
        System.err.println("Unexpected exception " + e);
        e.printStackTrace(System.err);
        System.exit(1);
      }
    }
    return instance;
  }

  //...

  /**
   * Add a book to the library, without knowing anything about objects in the library.
   *
   * @param title The book's title
   * @param author The book's author
   * @param callNumber The book's call number
   * @return true if the book was added, false if a book with the same call number already exists
   */
  public boolean addBook(String title, String author, String callNumber) {
  }

  /**
   * Add a copy of a book to the library, without knowing anything about objects in the library.
   *
   * @param callNumber
   * @return true if the book copy was added, false if there is no book with that call number
   */
  public boolean addBookCopy(String callNumber) {
  }

  /**
   * Add a borrower to the library, without knowing anything about objects in the library.
   *
   * @param firstName The borrower's first name
   * @param lastName The borrower's last name
   * @param email The borrower's email
   * @param phone The borrower's phone number
   * @return true if the borrower was added, false if a borrower with the same email already exists
   */
  public boolean addBorrower(String firstName, String lastName, String email, String phone) {
  }

  /**
   * Get callNumbers of all books in the library.
   *
   * @return A sorted set of all callNumbers.
   */
  public NavigableSet<String> getCallNumbers() {
    return books.navigableKeySet();
  }

  /**
   * Get emails of all borrowers in the library.
   *
   * @return A sorted set of all emails.
   */
  public NavigableSet<String> getEmails() {
    return borrowers.navigableKeySet();
  }

  /**
   * Return a report of all books in the library, in CSV (comma-separated values) format.
   *
   * @return A string containing one line per book in the library, in the form "title", "author",
   *     "callNumber" Note that each field is in quotes, and the fields are separated by commas.
   */
  public String getBookCsv() {
  }

  /**
   * Return a report of all borrowers (library users), in CSV (comma-separated values) format.
   *
   * @return A string containing one line per borrower, in the form "first name", "last name",
   *     "email", "phone" Note that each field is in quotes, and the fields are separated by commas.
   */
  public String getBorrowerCsv() {
  }

  /**
   * Checkout a book copy to a borrower using their identifiers. Fails if book, copy, or borrower
   * does not exist, or if the copy is already checked out.
   *
   * @param callNumber The call number of the book to check out
   * @param copyNumber The copy number of the book to check out
   * @param email The email of the borrower checking out the book
   * @return true if checkout was successful, false otherwise
   */
  public boolean checkout(String callNumber, int copyNumber, String email) {
  }

  /**
   * Check if a book is currently checked out (has an active loan).
   *
   * @param callNumber The call number of the book
   * @return true if the book is checked out, false otherwise
   */
  public boolean isCheckedOut(String callNumber, int copyNumber) {
  }

  /**
   * Return a copy of a book that was previously checked out.
   *
   * @param callNumber The call number of the book to return
   * @param copyNumber The copy number of the book to return
   * @return true if the return was successful, false if the book copy wasn't checked out
   */
  public boolean returnCopy(String callNumber, int copyNumber) {
  }

  /**
   * Get the due date for a copy of a book that is currently checked out.
   *
   * @param callNumber The call number of the book
   * @param copyNumber The copy number of the book
   * @return The due date if the book is checked out, null otherwise
   */
  public LocalDate getDueDate(String callNumber, int copyNumber) {
  }

  /**
   * Renew a book copy loan, extending the due date by 28 days. A loan can only be renewed once and
   * only if the book has not been returned.
   *
   * @param callNumber The call number of the book to renew
   * @param copyNumber The copy number of the book to renew
   * @return true if the renewal was successful, false if the book wasn't checked out, was already
   *     returned, or has already been renewed
   */
  public boolean renew(String callNumber, int copyNumber) {
}
