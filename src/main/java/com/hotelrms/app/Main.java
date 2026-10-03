package com.hotelrms.app;
import com.hotelrms.model.*;
import com.hotelrms.service.HotelManager;
import com.hotelrms.persistance.JsonManager;
import com.hotelrms.concurrency.BookingService;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final HotelManager hotel =
            new HotelManager();

    public static void main(String[] args) {

        // Add some rooms when the application starts
        initializeRooms();

        while (true) {

            displayMenu();

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    addGuest();
                    break;

                case 2:
                    displayGuests();
                    break;

                case 3:
                    displayRooms();
                    break;

                case 4:
                    makeReservation();
                    break;

                case 5:
                    displayReservations();
                    break;

                case 6:
                    searchRooms();
                    break;

                case 7:
                    sortReservations();
                    break;

                case 8:
                    generateBill();
                    break;

                case 9:
                    saveData();
                    break;

                case 10:
                    loadData();
                    break;

                case 11:
                    concurrencyDemo();
                    break;

                case 12:
                    addMoreRoom();
                    break;

                case 13:
                    sortRooms();
                    break;

                case 0:
                    System.out.println();
                    System.out.println(
                            "Thank you for using Hotel Reservation Management System!"
                    );
                    scanner.close();
                    return;

                default:
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
            }
        }
    }

    // =========================================================
    // MENU
    // =========================================================

    private static void displayMenu() {

        System.out.println();
        System.out.println(
                "=========================================================="
        );
        System.out.println(
                "        HOTEL RESERVATION MANAGEMENT SYSTEM"
        );
        System.out.println(
                "=========================================================="
        );

        System.out.println();
        System.out.println(
                "---------------------- MENU ----------------------"
        );

        System.out.println("1.  Add Guest");
        System.out.println("2.  Display Guests");
        System.out.println("3.  Display Rooms");
        System.out.println("4.  Make Reservation");
        System.out.println("5.  Display Reservations");
        System.out.println("6.  Search Rooms");
        System.out.println("7.  Sort Reservations");
        System.out.println("8.  Generate Bill");
        System.out.println("9.  Save Data");
        System.out.println("10. Load Data");
        System.out.println("11. Concurrent Booking Demo");
        System.out.println("12. Add Room");
        System.out.println("13. Sort Rooms by Price");
        System.out.println("0.  Exit");

        System.out.println(
                "--------------------------------------------------"
        );
    }

    // =========================================================
    // INITIAL ROOMS
    // =========================================================

    private static void initializeRooms() {
        if (!hotel.getRooms().isEmpty()) {
            return;
        }

        hotel.addRoom(
                new StandardRoom(101)
        );

        hotel.addRoom(
                new StandardRoom(102)
        );

        hotel.addRoom(
                new StandardRoom(103)
        );

        hotel.addRoom(
                new DeluxeRoom(201)
        );

        hotel.addRoom(
                new DeluxeRoom(202)
        );

        hotel.addRoom(
                new DeluxeRoom(203)
        );

        hotel.addRoom(
                new SuiteRoom(301)
        );

        hotel.addRoom(
                new SuiteRoom(302)
        );
    }

    // =========================================================
    // ADD GUEST
    // =========================================================

    private static void addGuest() {

        System.out.println();
        System.out.println("========== ADD GUEST ==========");

        System.out.print("Enter Guest ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        if (id <= 0) {
            System.out.println("Guest ID must be positive.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        Guest guest =
                new Guest(
                        id,
                        name,
                        phone,
                        email
                );

        if (hotel.addGuest(guest)) {

            System.out.println(
                    "Guest added successfully!"
            );

        } else {

            System.out.println(
                    "Guest could not be added."
            );
            System.out.println(
                    "Guest ID or email may already be registered."
            );
        }
    }

    // =========================================================
    // DISPLAY GUESTS
    // =========================================================

    private static void displayGuests() {

        System.out.println();
        System.out.println("========== GUESTS ==========");

        hotel.displayGuests();
    }

    // =========================================================
    // DISPLAY ROOMS
    // =========================================================

    private static void displayRooms() {

        System.out.println();
        System.out.println("========== ROOMS ==========");

        hotel.displayRooms();
    }

    // =========================================================
    // MAKE RESERVATION
    // =========================================================

    private static void makeReservation() {

        System.out.println();
        System.out.println(
                "========== MAKE RESERVATION =========="
        );

        if (hotel.getGuests().isEmpty()) {

            System.out.println(
                    "No guests found."
            );

            System.out.println(
                    "Please add a guest first."
            );

            return;
        }

        System.out.print("Enter Reservation ID: ");
        int reservationId = scanner.nextInt();
        if (reservationId <= 0) {
            System.out.println("Reservation ID must be positive.");
            return;
        }

        System.out.print("Enter Guest ID: ");
        int guestId = scanner.nextInt();

        System.out.print("Enter Room Number: ");
        int roomNumber = scanner.nextInt();

        Guest guest =
                hotel.findGuest(guestId);

        Room room =
                hotel.findRoom(roomNumber);

        if (guest == null) {

            System.out.println(
                    "Guest not found."
            );

            return;
        }

        if (room == null) {

            System.out.println(
                    "Room not found."
            );

            return;
        }

        if (!room.isAvailable()) {

            System.out.println(
                    "Room is already booked."
            );

            return;
        }

        System.out.print(
                "Enter number of days: "
        );

        int days = scanner.nextInt();

        if (days <= 0) {

            System.out.println(
                    "Number of days must be greater than 0."
            );

            return;
        }

        LocalDate checkIn =
                LocalDate.now();

        LocalDate checkOut =
                checkIn.plusDays(days);

        Reservation reservation =
                new Reservation(
                        reservationId,
                        guest,
                        room,
                        checkIn,
                        checkOut
                );

        boolean success =
                hotel.addReservation(
                        reservation
                );

        if (success) {

            System.out.println();
            System.out.println(
                    "Reservation created successfully!"
            );

            System.out.println(
                    reservation
            );

        } else {

            System.out.println(
                    "Reservation failed."
            );
        }
    }

    // =========================================================
    // DISPLAY RESERVATIONS
    // =========================================================

    private static void displayReservations() {

        System.out.println();
        System.out.println(
                "========== RESERVATIONS =========="
        );

        hotel.displayReservations();
    }

    // =========================================================
    // SEARCH ROOMS
    // =========================================================

    private static void searchRooms() {

        System.out.println();
        System.out.println(
                "========== SEARCH ROOMS =========="
        );

        System.out.println(
                "1. Search Standard"
        );

        System.out.println(
                "2. Search Deluxe"
        );

        System.out.println(
                "3. Search Suite"
        );

        System.out.print(
                "Enter choice: "
        );

        int choice = scanner.nextInt();

        String type;

        switch (choice) {

            case 1:
                type = "Standard";
                break;

            case 2:
                type = "Deluxe";
                break;

            case 3:
                type = "Suite";
                break;

            default:
                System.out.println(
                        "Invalid choice."
                );
                return;
        }

        List<Room> rooms =
                hotel.searchByType(type);

        System.out.println();
        System.out.println(
                "Search Results:"
        );

        if (rooms.isEmpty()) {

            System.out.println(
                    "No rooms found."
            );

            return;
        }

        for (Room room : rooms) {

            System.out.println(room);
        }
    }

    private static void sortRooms() {
        System.out.println("\n========== ROOMS SORTED BY PRICE ==========");
        hotel.printList(hotel.sortRoomsByPrice());
    }

    // =========================================================
    // SORT RESERVATIONS
    // =========================================================

    private static void sortReservations() {

        System.out.println();
        System.out.println(
                "========== SORT RESERVATIONS =========="
        );

        System.out.println(
                "1. Sort by Check-in Date"
        );

        System.out.println(
                "2. Sort by Amount"
        );

        System.out.print(
                "Enter choice: "
        );

        int choice = scanner.nextInt();

        switch (choice) {

            case 1:

                hotel.sortByDate();

                break;

            case 2:

                hotel.sortByAmount();

                break;

            default:

                System.out.println(
                        "Invalid choice."
                );
        }
    }

    // =========================================================
    // BILL
    // =========================================================

    private static void generateBill() {

        System.out.println();
        System.out.println(
                "========== GENERATE BILL =========="
        );

        if (hotel.getReservations().isEmpty()) {

            System.out.println(
                    "No reservations available."
            );

            return;
        }

        System.out.print(
                "Enter Reservation ID: "
        );

        int id = scanner.nextInt();

        Reservation reservation =
                hotel.findReservation(id);

        if (reservation == null) {

            System.out.println(
                    "Reservation not found."
            );

            return;
        }

        Billing billing =
                new Billing(
                        1001,
                        reservation
                );

        billing.printBill();
    }

    // =========================================================
    // SAVE DATA
    // =========================================================

    private static void saveData() {

        System.out.println();
        System.out.println(
                "========== SAVE DATA =========="
        );

        try {
            JsonManager.saveState(hotel, "hotel-state.json");
        } catch (IOException exception) {
            System.out.println("Error saving hotel state: " + exception.getMessage());
        }
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private static void loadData() {

        System.out.println();
        System.out.println(
                "========== LOAD DATA =========="
        );

        try {
            JsonManager.loadState("hotel-state.json", hotel);
            System.out.println("Application state restored.");
        } catch (IOException exception) {
            System.out.println("Error loading hotel state: " + exception.getMessage());
        }
    }

    // =========================================================
    // CONCURRENCY
    // =========================================================

    private static void concurrencyDemo() {

        System.out.println();
        System.out.println(
                "========== CONCURRENT BOOKING =========="
        );

        Room room =
                new DeluxeRoom(999);

        System.out.println(
                "Three users are trying to book"
        );

        System.out.println(
                "the same room simultaneously..."
        );

        System.out.println();

        try (BookingService service = new BookingService(2)) {
            List<Boolean> results = service.runConcurrentBookingDemo(room);
            long winners = results.stream().filter(Boolean::booleanValue).count();
            System.out.println("Booking outcomes (Runnable, Callable, Thread): " + results);
            System.out.println("Successful bookings: " + winners);
            System.out.println("Final room availability: " + room.isAvailable());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.out.println("Concurrent booking demo was interrupted.");
        }
    }

    // =========================================================
    // ADD ROOM
    // =========================================================

    private static void addMoreRoom() {

        System.out.println();
        System.out.println(
                "========== ADD ROOM =========="
        );

        System.out.print(
                "Enter Room Number: "
        );

        int roomNumber =
                scanner.nextInt();

        System.out.println();

        System.out.println(
                "1. Standard Room"
        );

        System.out.println(
                "2. Deluxe Room"
        );

        System.out.println(
                "3. Suite Room"
        );

        System.out.print(
                "Enter Room Type: "
        );

        int type =
                scanner.nextInt();

        Room room;

        switch (type) {

            case 1:

                room =
                        new StandardRoom(
                                roomNumber
                        );

                break;

            case 2:

                room =
                        new DeluxeRoom(
                                roomNumber
                        );

                break;

            case 3:

                room =
                        new SuiteRoom(
                                roomNumber
                        );

                break;

            default:

                System.out.println(
                        "Invalid room type."
                );

                return;
        }

        if (hotel.addRoom(room)) {
            System.out.println("Room added successfully!");
            System.out.println(room);
        } else {
            System.out.println("Room could not be added; that room number may already exist.");
        }
    }
}