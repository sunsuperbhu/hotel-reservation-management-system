package com.hotelrms.service;

import com.hotelrms.model.Guest;
import com.hotelrms.model.Reservation;
import com.hotelrms.model.Room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HotelManager {

    private Map<Integer, Room> rooms = new HashMap<>();
    private Map<Integer, Guest> guests = new HashMap<>();
    private List<Reservation> reservations = new ArrayList<>();
    private Set<String> registeredEmails = new HashSet<>();

    public synchronized boolean addRoom(Room room) {
        if (room == null || rooms.containsKey(room.getRoomNumber())) {
            return false;
        }
        rooms.put(room.getRoomNumber(), room);
        return true;
    }

    public synchronized Room findRoom(int roomNumber) {
        return rooms.get(roomNumber);
    }

    public synchronized List<Room> searchAvailableRooms() {
        return rooms.values().stream()
                .filter(Room::isAvailable)
                .sorted(Comparator.comparingInt(Room::getRoomNumber))
                .toList();
    }

    public synchronized List<Room> searchByType(String roomType) {
        if (roomType == null) {
            return List.of();
        }
        return rooms.values().stream()
                .filter(room -> room.getRoomType().equalsIgnoreCase(roomType))
                .sorted(Comparator.comparingInt(Room::getRoomNumber))
                .toList();
    }

    public synchronized List<Room> sortRoomsByPrice() {
        return rooms.values().stream()
                .sorted(Comparator.comparingDouble(Room::calculatePrice)
                        .thenComparingInt(Room::getRoomNumber))
                .toList();
    }

    public synchronized void displayRooms() {
        System.out.println("\n===== ROOMS =====");
        sortRoomsByPrice().forEach(System.out::println);
    }

    public synchronized boolean addGuest(Guest guest) {
        if (guest == null || guests.containsKey(guest.getGuestId())) {
            return false;
        }
        String emailKey = emailKey(guest.getEmail());
        if (registeredEmails.contains(emailKey)) {
            return false;
        }
        guests.put(guest.getGuestId(), guest);
        registeredEmails.add(emailKey);
        return true;
    }

    public synchronized Guest findGuest(int id) {
        return guests.get(id);
    }

    public synchronized void displayGuests() {
        System.out.println("\n===== GUESTS =====");
        guests.values().stream()
                .sorted(Comparator.comparingInt(Guest::getGuestId))
                .forEach(System.out::println);
    }

    public synchronized boolean addReservation(Reservation reservation) {
        if (reservation == null || findReservation(reservation.getReservationId()) != null) {
            return false;
        }
        Guest registeredGuest = guests.get(reservation.getGuest().getGuestId());
        Room registeredRoom = rooms.get(reservation.getRoom().getRoomNumber());
        if (registeredGuest == null || registeredRoom == null
                || reservations.stream().anyMatch(existing ->
                existing.getRoom().getRoomNumber() == registeredRoom.getRoomNumber())
                || !registeredRoom.bookIfAvailable()) {
            return false;
        }

        reservations.add(new Reservation(
                reservation.getReservationId(),
                registeredGuest,
                registeredRoom,
                reservation.getCheckIn(),
                reservation.getCheckOut()));
        return true;
    }

    public synchronized Reservation findReservation(int reservationId) {
        return reservations.stream()
                .filter(reservation -> reservation.getReservationId() == reservationId)
                .findFirst()
                .orElse(null);
    }

    public synchronized List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }

    public synchronized void displayReservations() {
        System.out.println("\n===== RESERVATIONS =====");
        reservations.forEach(System.out::println);
    }

    public synchronized void sortByDate() {
        reservations.sort(Comparator.comparing(Reservation::getCheckIn)
                .thenComparingInt(Reservation::getReservationId));
        System.out.println("\nReservations sorted by check-in date:");
        displayReservations();
    }

    public synchronized void sortByAmount() {
        reservations.sort(Comparator.comparingDouble(Reservation::calculateAmount)
                .thenComparingInt(Reservation::getReservationId));
        System.out.println("\nReservations sorted by amount:");
        displayReservations();
    }

    public synchronized void sortReservationsByDate() {
        sortByDate();
    }

    public synchronized void sortReservationsByAmount() {
        sortByAmount();
    }

    public <T> void printList(List<T> list) {
        list.forEach(System.out::println);
    }

    public synchronized Map<Integer, Room> getRooms() {
        return Collections.unmodifiableMap(new HashMap<>(rooms));
    }

    public synchronized Map<Integer, Guest> getGuests() {
        return Collections.unmodifiableMap(new HashMap<>(guests));
    }

    public synchronized void restoreState(Collection<Guest> restoredGuests,
                                          Collection<Room> restoredRooms,
                                          Collection<Reservation> restoredReservations) {
        Map<Integer, Guest> newGuests = new HashMap<>();
        Set<String> newEmails = new HashSet<>();
        for (Guest guest : restoredGuests) {
            if (guest == null || newGuests.putIfAbsent(guest.getGuestId(), guest) != null
                    || !newEmails.add(emailKey(guest.getEmail()))) {
                throw new IllegalArgumentException("Loaded state contains duplicate or invalid guests");
            }
        }

        Map<Integer, Room> newRooms = new HashMap<>();
        for (Room room : restoredRooms) {
            if (room == null || newRooms.putIfAbsent(room.getRoomNumber(), room) != null) {
                throw new IllegalArgumentException("Loaded state contains duplicate or invalid rooms");
            }
        }

        List<Reservation> newReservations = new ArrayList<>();
        Set<Integer> reservationIds = new HashSet<>();
        Set<Integer> reservedRoomNumbers = new HashSet<>();
        for (Reservation reservation : restoredReservations) {
            if (reservation == null || !reservationIds.add(reservation.getReservationId())) {
                throw new IllegalArgumentException("Loaded state contains duplicate or invalid reservations");
            }
            Guest guest = newGuests.get(reservation.getGuest().getGuestId());
            Room room = newRooms.get(reservation.getRoom().getRoomNumber());
            if (guest == null || room == null || !reservedRoomNumbers.add(room.getRoomNumber())) {
                throw new IllegalArgumentException("Loaded reservation references missing or already-reserved records");
            }
            room.setAvailable(false);
            newReservations.add(new Reservation(
                    reservation.getReservationId(), guest, room,
                    reservation.getCheckIn(), reservation.getCheckOut()));
        }

        guests = newGuests;
        rooms = newRooms;
        reservations = newReservations;
        registeredEmails = newEmails;
    }

    private static String emailKey(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
