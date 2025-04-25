package library.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDate;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LibraryDatabaseTest {
  private TreeSet<String> callNumbers;
  private TreeSet<String> emails;
  private LibraryDatabase db;

  @BeforeEach
  public void setUp() {
    db = LibraryDatabase.getInstance(true);
    db.addBook("Title1", "Author1", "CallNumber1");
    db.addBook("Title2", "Author2", "CallNumber2");
    db.addBook("Title3", "Author3", "CallNumber3");

    // Create copies of books (two copies for CallNumber1)
    db.addBookCopy("CallNumber1"); // Copy 1
    db.addBookCopy("CallNumber1"); // Copy 2
    db.addBookCopy("CallNumber2"); // Copy 1
    db.addBookCopy("CallNumber3"); // Copy 1

    callNumbers = new TreeSet<String>();
    callNumbers.add("CallNumber1");
    callNumbers.add("CallNumber2");
    callNumbers.add("CallNumber3");
    db.addBorrower("FirstName1", "LastName1", "Email1", "Phone1");
    db.addBorrower("FirstName2", "LastName2", "Email2", "Phone2");
    emails = new TreeSet<String>();
    emails.add("Email1");
    emails.add("Email2");
  }

  @Test
  public void testAddBook() {
    assertTrue(callNumbers.tailSet("").equals(db.getCallNumbers()));
  }

  @Test
  public void testAddBorrower() {
    assertTrue(emails.tailSet("").equals(db.getEmails()));
  }

  @Test
  public void testAddBookCopy() {
    // Test adding another copy of an existing book
    assertTrue(db.addBookCopy("CallNumber1"), "Should successfully add a copy of an existing book");

    // Test adding a copy of a non-existent book
    assertFalse(
        db.addBookCopy("NonExistentBook"), "Should fail when adding a copy of a non-existent book");
  }

  @Test
  public void testGetBookCsv() {
    String expectedCsv =
        "\"Title1\",\"Author1\",\"CallNumber1\",2\n"
            + "\"Title2\",\"Author2\",\"CallNumber2\",1\n"
            + "\"Title3\",\"Author3\",\"CallNumber3\",1\n";
    assertEquals(expectedCsv, db.getBookCsv());
  }

  @Test
  public void testGetBorrowerCsv() {
    String expectedCsv =
        "\"FirstName1\",\"LastName1\",\"Email1\",\"Phone1\"\n"
            + "\"FirstName2\",\"LastName2\",\"Email2\",\"Phone2\"\n";
    assertEquals(expectedCsv, db.getBorrowerCsv());
  }

  // It's possible to write good tests for writeToFile() and readFromFile(), but it would
  // complicate your LibraryDatabase code. The problem is that de-serialization (in readFromFile())
  // creates a new instance of LibraryDatabase, which is a singleton.  But changing that would
  // require making the class more complicated to write.  So for now, simply trust that I have
  // provided working code for them, and be sure to test all the code you write.

  // Nevertheless, we can do a little testing of writeToFile().
  @Test
  public void testWriteToFile() {
    try {
      db.writeToFile("testLibraryOutput.db");
    } catch (Exception e) {
      fail("Exception thrown while writing to file: " + e.getMessage());
    }
    // Check if the file was created

    File file = new File("testLibraryOutput.db");
    assertTrue(file.exists(), "Output file should exist after writing.  ");
    // Leave the test file, in case it's useful for debugging
    // Checking the contents is complicated, because de-serializing creates a new object, but the
    // class is a singleton.  For now, we won't complicate the class to work around that.
  }

  @Test
  public void testCheckoutSuccess() {
    // Test successful checkout
    boolean result = db.checkout("CallNumber1", 1, "Email1");
    assertTrue(result, "Checkout should succeed with valid book and borrower");

    // Verify the book is checked out
    assertTrue(db.isCheckedOut("CallNumber1", 1), "Book should be marked as checked out");

    // Verify due date is set (should be around 28 days from now)
    LocalDate dueDate = db.getDueDate("CallNumber1", 1);
    assertNotNull(dueDate, "Due date should be set for checked out book");
    assertTrue(
        dueDate.isAfter(LocalDate.now().plusDays(27)),
        "Due date should be at least 28 days in future");
    assertTrue(
        dueDate.isBefore(LocalDate.now().plusDays(29)),
        "Due date should be at most 28 days in future");
  }

  @Test
  public void testCheckoutInvalidBook() {
    // Try to check out non-existent book
    boolean result = db.checkout("NonExistentBook", 1, "Email1");
    assertFalse(result, "Checkout should fail with invalid book");
  }

  @Test
  public void testCheckoutInvalidBorrower() {
    // Try to check out to non-existent borrower
    boolean result = db.checkout("CallNumber1", 1, "NonExistentEmail");
    assertFalse(result, "Checkout should fail with invalid borrower");
  }

  @Test
  public void testCheckoutInvalidCopyNumber() {
    // Try to check out non-existent copy number
    boolean result = db.checkout("CallNumber1", 5, "Email1");
    assertFalse(result, "Checkout should fail with invalid copy number");
  }

  @Test
  public void testCheckoutAlreadyCheckedOut() {
    // First checkout should succeed
    boolean firstResult = db.checkout("CallNumber1", 1, "Email1");
    assertTrue(firstResult, "First checkout should succeed");

    // Second checkout of same book should fail
    boolean secondResult = db.checkout("CallNumber1", 1, "Email2");
    assertFalse(secondResult, "Checkout should fail when book is already checked out");
  }

  @Test
  public void testMultipleCopiesCheckout() {
    // Check out the first copy of CallNumber1
    assertTrue(db.checkout("CallNumber1", 1, "Email1"), "First copy checkout should succeed");

    // Check out the second copy of CallNumber1
    assertTrue(db.checkout("CallNumber1", 2, "Email2"), "Second copy checkout should succeed");

    // Verify both copies are checked out
    assertTrue(db.isCheckedOut("CallNumber1", 1), "First copy should be checked out");
    assertTrue(db.isCheckedOut("CallNumber1", 2), "Second copy should be checked out");
  }

  @Test
  public void testReturnBook() {
    // First checkout the book
    boolean checkoutResult = db.checkout("CallNumber1", 1, "Email1");
    assertTrue(checkoutResult, "Checkout should succeed");

    // Return the book
    boolean returnResult = db.returnCopy("CallNumber1", 1);
    assertTrue(returnResult, "Return should succeed for checked out book");

    // Verify book is no longer checked out
    assertFalse(
        db.isCheckedOut("CallNumber1", 1), "Book should no longer be checked out after return");

    // Verify due date is no longer available
    assertNull(db.getDueDate("CallNumber1", 1), "Due date should be null for returned book");
  }

  @Test
  public void testReturnBookNotCheckedOut() {
    // Try to return a book that is not checked out
    boolean result = db.returnCopy("CallNumber1", 1);
    assertFalse(result, "Return should fail for book that is not checked out");
  }

  @Test
  public void testMultipleCheckoutsAndReturns() {
    // Check out multiple books
    assertTrue(db.checkout("CallNumber1", 1, "Email1"), "First checkout should succeed");
    assertTrue(db.checkout("CallNumber2", 1, "Email1"), "Second checkout should succeed");

    // Verify both are checked out
    assertTrue(db.isCheckedOut("CallNumber1", 1), "First book should be checked out");
    assertTrue(db.isCheckedOut("CallNumber2", 1), "Second book should be checked out");

    // Return first book
    assertTrue(db.returnCopy("CallNumber1", 1), "Return of first book should succeed");

    // Verify one is returned, one is still checked out
    assertFalse(db.isCheckedOut("CallNumber1", 1), "First book should no longer be checked out");
    assertTrue(db.isCheckedOut("CallNumber2", 1), "Second book should still be checked out");

    // Should be able to check out the first book again
    assertTrue(
        db.checkout("CallNumber1", 1, "Email2"), "Re-checkout of returned book should succeed");
    assertTrue(db.isCheckedOut("CallNumber1", 1), "First book should be checked out again");
  }

  @Test
  public void testMultipleCopyReturns() {
    // Check out both copies of CallNumber1
    assertTrue(db.checkout("CallNumber1", 1, "Email1"), "First copy checkout should succeed");
    assertTrue(db.checkout("CallNumber1", 2, "Email2"), "Second copy checkout should succeed");

    // Return the first copy
    assertTrue(db.returnCopy("CallNumber1", 1), "Return of first copy should succeed");

    // Verify first copy is returned but second copy is still checked out
    assertFalse(db.isCheckedOut("CallNumber1", 1), "First copy should no longer be checked out");
    assertTrue(db.isCheckedOut("CallNumber1", 2), "Second copy should still be checked out");
  }

  @Test
  public void testGetDueDateForNonCheckedOutBook() {
    // Due date should be null for a book that is not checked out
    assertNull(
        db.getDueDate("CallNumber1", 1),
        "Due date should be null for book that is not checked out");
  }

  @Test
  public void testReturnSuccess() {
    // First checkout the book
    boolean checkedOut = db.checkout("CallNumber1", 1, "Email1");
    assertTrue(checkedOut, "Checkout should succeed for available book");
    assertTrue(db.isCheckedOut("CallNumber1", 1), "Book should be checked out");

    // Return the book
    boolean returned = db.returnCopy("CallNumber1", 1);
    assertTrue(returned, "Return should succeed for checked out book");

    // Verify the book is no longer checked out
    assertFalse(db.isCheckedOut("CallNumber1", 1), "Book should no longer be checked out");
  }

  @Test
  public void testReturnNotCheckedOut() {
    // Try to return a book that's not checked out
    boolean returned = db.returnCopy("CallNumber1", 1);
    assertFalse(returned, "Return should fail for book that's not checked out");
  }

  @Test
  public void testRenewLoan() {
    // Checkout a book
    db.checkout("CallNumber1", 1, "Email1");

    // Record the original due date
    var originalDueDate = db.getDueDate("CallNumber1", 1);

    // Renew the loan
    assertTrue(db.renew("CallNumber1", 1), "Should be able to renew loan once");

    // Due date should be extended by 28 days
    assertEquals(
        originalDueDate.plusDays(28),
        db.getDueDate("CallNumber1", 1),
        "Due date should be extended by 28 days");

    // Should not be able to renew again
    assertFalse(db.renew("CallNumber1", 1), "Should not be able to renew loan twice");
  }

  @Test
  public void testRenewMultipleCopies() {
    // Check out both copies of CallNumber1
    assertTrue(db.checkout("CallNumber1", 1, "Email1"), "First copy checkout should succeed");
    assertTrue(db.checkout("CallNumber1", 2, "Email2"), "Second copy checkout should succeed");

    // Record original due dates
    LocalDate firstCopyDueDate = db.getDueDate("CallNumber1", 1);
    LocalDate secondCopyDueDate = db.getDueDate("CallNumber1", 2);

    // Renew the first copy
    assertTrue(db.renew("CallNumber1", 1), "Should be able to renew first copy");

    // Verify first copy due date extended, second copy unchanged
    assertEquals(
        firstCopyDueDate.plusDays(28),
        db.getDueDate("CallNumber1", 1),
        "First copy due date should be extended by 28 days");
    assertEquals(
        secondCopyDueDate,
        db.getDueDate("CallNumber1", 2),
        "Second copy due date should remain unchanged");
  }

  @Test
  public void testGetCopyInfoAvailable() {
    // Test getting info for a copy that is available (not checked out)
    String copyInfo = db.getCopyInfo("CallNumber1", 1);
    assertNotNull(copyInfo, "Copy info should not be null");
    assertTrue(copyInfo.contains("CallNumber1"), "Copy info should contain call number");
    assertTrue(copyInfo.contains("Title1"), "Copy info should contain title");
    assertTrue(copyInfo.contains("Author1"), "Copy info should contain author");
    assertTrue(copyInfo.contains("Available"), "Copy info should show as available");
    String expected = "\"CallNumber1\", 1, \"Title1\", \"Author1\", Available";
    assertEquals(copyInfo, expected);
  }

  @Test
  public void testGetCopyInfoCheckedOut() {
    // Check out a book
    assertTrue(db.checkout("CallNumber1", 1, "Email1"), "Checkout should succeed");

    // Test getting info for a checked out copy
    String copyInfo = db.getCopyInfo("CallNumber1", 1);
    assertNotNull(copyInfo, "Copy info should not be null");
    assertTrue(copyInfo.contains("CallNumber1"), "Copy info should contain call number");
    assertTrue(copyInfo.contains("Title1"), "Copy info should contain title");
    assertTrue(copyInfo.contains("Author1"), "Copy info should contain author");
    assertTrue(copyInfo.contains("Email1"), "Copy info should contain borrower email");
    assertTrue(
        copyInfo.contains(LocalDate.now().plusDays(28).toString()),
        "Copy info should contain due date");
    assertTrue(copyInfo.contains("false"), "Copy info should show not renewed");

    String expected =
        "\"CallNumber1\", 1, \"Title1\", \"Author1\", \"Email1\", "
            + LocalDate.now().plusDays(28).toString()
            + ", false";
    assertEquals(copyInfo, expected);

    // Renew the loan
    assertTrue(db.renew("CallNumber1", 1), "Renewal should succeed");

    // Check that renewed status is updated
    copyInfo = db.getCopyInfo("CallNumber1", 1);
    assertTrue(copyInfo.contains("true"), "Copy info should show as renewed");
  }

  @Test
  public void testGetCopyInfoNonExistent() {
    // Test with non-existent call number
    assertNull(
        db.getCopyInfo("NonExistentBook", 1), "Copy info should be null for non-existent book");

    // Test with valid call number but non-existent copy number
    assertNull(
        db.getCopyInfo("CallNumber1", 10), "Copy info should be null for non-existent copy number");
  }

  @Test
  public void testGetBorrowerInfoWithLoans() {
    // Check out books to a borrower
    assertTrue(db.checkout("CallNumber1", 1, "Email1"), "First checkout should succeed");
    assertTrue(db.checkout("CallNumber2", 1, "Email1"), "Second checkout should succeed");

    // Get borrower info
    String borrowerInfo = db.getBorrowerInfo("Email1");
    assertNotNull(borrowerInfo, "Borrower info should not be null");
    assertTrue(borrowerInfo.contains("\"FirstName1\""), "Borrower info should contain first name");
    assertTrue(borrowerInfo.contains("\"LastName1\""), "Borrower info should contain last name");
    assertTrue(borrowerInfo.contains("\"Email1\""), "Borrower info should contain email");
    assertTrue(borrowerInfo.contains("\"Phone1\""), "Borrower info should contain phone");

    // Check loan information
    assertTrue(borrowerInfo.contains("* \"CallNumber1\""), "Borrower info should show first loan");
    assertTrue(borrowerInfo.contains("* \"CallNumber2\""), "Borrower info should show second loan");
    assertTrue(borrowerInfo.contains("Title1"), "Borrower info should show first book title");
    assertTrue(borrowerInfo.contains("Title2"), "Borrower info should show second book title");

    // Count occurrences of loan entries (lines starting with *)
    String[] lines = borrowerInfo.split("\n");
    int loanCount = 0;
    for (String line : lines) {
      if (line.startsWith("*")) {
        loanCount++;
      }
    }
    assertEquals(2, loanCount, "Borrower should have 2 loans listed");

    String expected =
        "\"FirstName1\", \"LastName1\", \"Email1\", \"Phone1\"\n"
            + "* \"CallNumber1\", 1, \"Title1\", \"Author1\", "
            + LocalDate.now().plusDays(28).toString()
            + ", false\n"
            + "* \"CallNumber2\", 1, \"Title2\", \"Author2\", "
            + LocalDate.now().plusDays(28).toString()
            + ", false\n";
    assertEquals(borrowerInfo, expected);
  }

  @Test
  public void testGetBorrowerInfoNoLoans() {
    // Test borrower with no loans
    String borrowerInfo = db.getBorrowerInfo("Email2");
    assertNotNull(borrowerInfo, "Borrower info should not be null");
    assertTrue(borrowerInfo.contains("\"FirstName2\""), "Borrower info should contain first name");
    assertTrue(borrowerInfo.contains("\"LastName2\""), "Borrower info should contain last name");
    assertTrue(borrowerInfo.contains("\"Email2\""), "Borrower info should contain email");
    assertTrue(borrowerInfo.contains("\"Phone2\""), "Borrower info should contain phone");

    // Should not contain any loan entries
    assertFalse(borrowerInfo.contains("*"), "Borrower info should not contain loan entries");
    String expected = "\"FirstName2\", \"LastName2\", \"Email2\", \"Phone2\"\n";
    assertEquals(borrowerInfo, expected, "Borrower info should match expected format");
  }

  @Test
  public void testGetBorrowerInfoNonExistent() {
    // Test with non-existent borrower email
    assertNull(
        db.getBorrowerInfo("NonExistentEmail"),
        "Borrower info should be null for non-existent borrower");
  }
}
