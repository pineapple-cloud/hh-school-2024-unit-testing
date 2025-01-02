package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.example.PaymentService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private UserService userService;

    @InjectMocks
    private LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        libraryManager.addBook("item1", 10);
        libraryManager.addBook("item2", 5);
        libraryManager.addBook("item3", 1);
        libraryManager.addBook("item1", 10);
    }

    @ParameterizedTest
    @CsvSource({
            "5, True, True, 3.0",
            "6, True, False, 4.5",
            "7, True, False, 5.25",
            "8, False, False, 4"
    })
    void testCalculateDynamicLateFee(
            int overdueDays,
            boolean isBestseller,
            boolean isPremiumMember,
            double expectedFee
    ) {
        double fee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
        assertEquals(expectedFee, fee);
    }

    @Test
    void calculateDynamicLateFeeShouldThrowException() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> libraryManager.calculateDynamicLateFee(-1, true, true)
        );
        assertEquals("Overdue days cannot be negative.", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "item1, 20",
            "item2, 5",
            "item3, 1",
            "item4, 0"
    })
    void testGetAvailableCopies(
            String bookId,
            int expectedCopies
    ) {
        var copies = libraryManager.getAvailableCopies(bookId);
        assertEquals(expectedCopies, copies);
    }

    @Test
    void testBorrowBookUserNotActive(){
        when(userService.isUserActive("user1")).thenReturn(false);        when(userService.isUserActive("user1")).thenReturn(false);
        boolean result = libraryManager.borrowBook("item3", "user1");
        assertFalse(result);
    }

    @Test
    void testBorrowBookNoAvailableCopies(){
        when(userService.isUserActive("user2")).thenReturn(true);
        boolean result = libraryManager.borrowBook("item4", "user2");
        assertFalse(result);
    }

    @Test
    void testBorrowBookSuccessfulBorrow(){
        when(userService.isUserActive("user2")).thenReturn(true);
        boolean result = libraryManager.borrowBook("item1", "user2");
        assertEquals(19, libraryManager.getAvailableCopies("item1"));
        assertTrue(result);
    }

    @Test
    void testReturnBookBookNotBorrowed(){
        boolean result = libraryManager.returnBook("item1", "user1");
        assertEquals(20, libraryManager.getAvailableCopies("item1"));
        assertFalse(result);
    }

    @Test
    void testReturnBookBookNotBorrowedByThisUser(){
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("item1", "user1");
        boolean result = libraryManager.returnBook("item1", "user2");
        assertFalse(result);
    }

    @Test
    public void testReturnBookSuccessfulReturn() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("item1", "user1");
        assertEquals(19, libraryManager.getAvailableCopies("item1"));
        boolean result = libraryManager.returnBook("item1", "user1");
        assertEquals(20, libraryManager.getAvailableCopies("item1"));
        assertTrue(result);
    }
}