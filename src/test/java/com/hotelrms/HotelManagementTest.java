package com.hotelrms;

import com.hotelrms.concurrency.BookingService;
import com.hotelrms.model.Billing;
import com.hotelrms.model.DeluxeRoom;
import com.hotelrms.model.Guest;
import com.hotelrms.model.Reservation;
import com.hotelrms.model.Room;
import com.hotelrms.model.StandardRoom;
import com.hotelrms.model.SuiteRoom;
import com.hotelrms.persistance.JsonManager;
import com.hotelrms.service.HotelManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelManagementTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void modelsProvideEncapsulatedValuesAndValueIdentity() {
        Guest first = new Guest(1, "Asha", "123", "asha@example.test");
        Guest sameId = new Guest(1, "Different", "999", "different@example.test");
        Room suite = new SuiteRoom(301);
        Reservation reservation = new Reservation(
                10, first, suite, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 4));
        Billing billing = new Billing(20, reservation);

        assertEquals(first, sameId);
        assertEquals(first.hashCode(), sameId.hashCode());
        assertTrue(first.toString().contains("Asha"));
        assertEquals(3, reservation.getNumberOfDays());
        assertEquals(18_000, reservation.calculateAmount());
        Reservation sameReservationId = new Reservation(
                10, sameId, new SuiteRoom(301),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 2));
        assertEquals(reservation, sameReservationId);
        assertEquals(reservation.hashCode(), sameReservationId.hashCode());
        assertTrue(reservation.toString().contains("Check-in=2026-01-01"));
        assertEquals(21_240, billing.calculateTotal());
        assertEquals(billing, new Billing(20, sameReservationId));
        assertEquals(billing.hashCode(), new Billing(20, sameReservationId).hashCode());
        assertTrue(billing.toString().contains("reservationId=10"));
        assertFalse(new SuiteRoom(301).equals(new DeluxeRoom(301)));
        assertTrue(suite.toString().contains("Suite"));
    }

    @Test
    void managerRejectsDuplicateBookingsAndSearchesAndSortsWithComparators() {
        HotelManager hotel = new HotelManager();
        Guest guest = new Guest(1, "Asha", "123", "asha@example.test");
        Room deluxe = new DeluxeRoom(201);
        Room standard = new StandardRoom(101);
        Room suite = new SuiteRoom(301);
        assertTrue(hotel.addGuest(guest));
        assertFalse(hotel.addGuest(new Guest(1, "Other", "555", "other@example.test")));
        assertFalse(hotel.addGuest(new Guest(2, "Other", "555", "ASHA@example.test")));
        assertTrue(hotel.addRoom(deluxe));
        assertTrue(hotel.addRoom(standard));
        assertTrue(hotel.addRoom(suite));
        assertFalse(hotel.addRoom(new StandardRoom(101)));

        assertEquals(List.of(standard), hotel.searchByType("standard"));
        assertEquals(List.of(standard, deluxe, suite), hotel.sortRoomsByPrice());
        assertTrue(hotel.addReservation(reservation(9, guest, deluxe, 3)));
        assertFalse(hotel.addReservation(reservation(9, guest, suite, 1)));
        assertFalse(hotel.addReservation(reservation(10, guest, deluxe, 1)));
        assertEquals(List.of(standard, suite), hotel.searchAvailableRooms());

        assertTrue(hotel.addReservation(reservation(11, guest, standard, 1)));
        hotel.sortByAmount();
        assertEquals(List.of(11, 9), hotel.getReservations().stream()
                .map(Reservation::getReservationId).toList());
        hotel.sortByDate();
        assertTrue(hotel.getReservations().get(0).getCheckIn()
                .isBefore(hotel.getReservations().get(1).getCheckIn()));
    }

    @Test
    void jacksonRoundTripRestoresCompleteStateAndRelationshipIdentity() throws IOException {
        HotelManager original = new HotelManager();
        Guest guest = new Guest(42, "Mina", "555-0100", "mina@example.test");
        Room standard = new StandardRoom(105);
        Room suite = new SuiteRoom(305);
        assertTrue(original.addGuest(guest));
        assertTrue(original.addRoom(standard));
        assertTrue(original.addRoom(suite));
        assertTrue(original.addReservation(new Reservation(
                700, guest, standard,
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 13))));

        Path file = temporaryDirectory.resolve("hotel-state.json");
        JsonManager.saveState(original, file.toString());
        String json = Files.readString(file);
        assertTrue(json.contains("\"roomType\" : \"Standard\""));
        assertTrue(json.contains("\"reservations\""));

        HotelManager restored = new HotelManager();
        assertTrue(restored.addGuest(new Guest(99, "Old", "000", "old@example.test")));
        assertTrue(restored.addRoom(new DeluxeRoom(999)));
        JsonManager.loadState(file.toString(), restored);

        assertEquals(1, restored.getGuests().size());
        assertEquals("Mina", restored.findGuest(42).getName());
        assertEquals(2, restored.getRooms().size());
        assertInstanceOf(StandardRoom.class, restored.findRoom(105));
        assertInstanceOf(SuiteRoom.class, restored.findRoom(305));
        assertFalse(restored.findRoom(105).isAvailable());
        assertTrue(restored.findRoom(305).isAvailable());
        Reservation loaded = restored.findReservation(700);
        assertEquals(LocalDate.of(2026, 7, 10), loaded.getCheckIn());
        assertEquals(LocalDate.of(2026, 7, 13), loaded.getCheckOut());
        assertSame(restored.findGuest(42), loaded.getGuest());
        assertSame(restored.findRoom(105), loaded.getRoom());
        assertEquals(3, loaded.getNumberOfDays());
    }

    @Test
    void concurrentBookingAllowsExactlyOneWinner() throws InterruptedException {
        Room room = new DeluxeRoom(999);
        try (BookingService service = new BookingService(2)) {
            List<Boolean> outcomes = service.runConcurrentBookingDemo(room);
            assertEquals(3, outcomes.size());
            assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
            assertFalse(room.isAvailable());
        }
    }

    private static Reservation reservation(int id, Guest guest, Room room, int days) {
        LocalDate checkIn = LocalDate.of(2026, 1, id);
        return new Reservation(id, guest, room, checkIn, checkIn.plusDays(days));
    }
}
