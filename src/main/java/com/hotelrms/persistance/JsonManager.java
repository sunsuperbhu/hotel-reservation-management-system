package com.hotelrms.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hotelrms.model.DeluxeRoom;
import com.hotelrms.model.Guest;
import com.hotelrms.model.Reservation;
import com.hotelrms.model.Room;
import com.hotelrms.model.StandardRoom;
import com.hotelrms.model.SuiteRoom;
import com.hotelrms.service.HotelManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonManager {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonManager() {
    }

    public static void saveState(HotelManager hotel, String fileName) throws IOException {
        Path path = Path.of(fileName);
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        MAPPER.writeValue(path.toFile(), createSnapshot(hotel));
        System.out.println("Hotel state saved to " + path);
    }

    public static void loadState(String fileName, HotelManager hotel) throws IOException {
        Path path = Path.of(fileName);
        if (!Files.exists(path)) {
            throw new IOException("State file does not exist: " + path);
        }

        StateFile state = MAPPER.readValue(path.toFile(), StateFile.class);
        if (state == null || state.guests == null || state.rooms == null || state.reservations == null) {
            throw new IOException("State file is missing guests, rooms, or reservations");
        }

        try {
            List<Guest> guests = new ArrayList<>();
            Map<Integer, Guest> guestsById = new HashMap<>();
            for (GuestData data : state.guests) {
                if (data == null || data.name == null || data.phone == null || data.email == null) {
                    throw new IOException("State file contains an incomplete guest");
                }
                Guest guest = new Guest(data.guestId, data.name, data.phone, data.email);
                if (guestsById.putIfAbsent(guest.getGuestId(), guest) != null) {
                    throw new IOException("State file contains duplicate guest ID " + guest.getGuestId());
                }
                guests.add(guest);
            }

            List<Room> rooms = new ArrayList<>();
            Map<Integer, Room> roomsByNumber = new HashMap<>();
            for (RoomData data : state.rooms) {
                if (data == null) {
                    throw new IOException("State file contains an empty room record");
                }
                Room room = createRoom(data);
                if (roomsByNumber.putIfAbsent(room.getRoomNumber(), room) != null) {
                    throw new IOException("State file contains duplicate room number " + room.getRoomNumber());
                }
                room.setAvailable(data.available);
                rooms.add(room);
            }

            List<Reservation> reservations = new ArrayList<>();
            for (ReservationData data : state.reservations) {
                if (data == null || data.checkIn == null || data.checkOut == null) {
                    throw new IOException("State file contains an incomplete reservation");
                }
                Guest guest = guestsById.get(data.guestId);
                Room room = roomsByNumber.get(data.roomNumber);
                if (guest == null || room == null) {
                    throw new IOException("Reservation " + data.reservationId
                            + " references a missing guest or room");
                }
                reservations.add(new Reservation(
                        data.reservationId, guest, room,
                        parseDate(data.checkIn, data.reservationId),
                        parseDate(data.checkOut, data.reservationId)));
            }

            hotel.restoreState(guests, rooms, reservations);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid hotel state in " + path + ": "
                    + exception.getMessage(), exception);
        }

        System.out.println("Hotel state loaded from " + path);
    }

    private static StateFile createSnapshot(HotelManager hotel) {
        StateFile state = new StateFile();
        for (Guest guest : hotel.getGuests().values()) {
            GuestData data = new GuestData();
            data.guestId = guest.getGuestId();
            data.name = guest.getName();
            data.phone = guest.getPhone();
            data.email = guest.getEmail();
            state.guests.add(data);
        }

        for (Room room : hotel.getRooms().values()) {
            RoomData data = new RoomData();
            data.roomNumber = room.getRoomNumber();
            data.roomType = room.getRoomType();
            data.available = room.isAvailable();
            state.rooms.add(data);
        }

        for (Reservation reservation : hotel.getReservations()) {
            ReservationData data = new ReservationData();
            data.reservationId = reservation.getReservationId();
            data.guestId = reservation.getGuest().getGuestId();
            data.roomNumber = reservation.getRoom().getRoomNumber();
            data.checkIn = reservation.getCheckIn().toString();
            data.checkOut = reservation.getCheckOut().toString();
            state.reservations.add(data);
        }
        return state;
    }

    private static Room createRoom(RoomData data) throws IOException {
        if (data.roomType == null) {
            throw new IOException("State file contains a room without a type");
        }
        return switch (data.roomType) {
            case "Standard" -> new StandardRoom(data.roomNumber);
            case "Deluxe" -> new DeluxeRoom(data.roomNumber);
            case "Suite" -> new SuiteRoom(data.roomNumber);
            default -> throw new IOException("Unknown room type: " + data.roomType);
        };
    }

    private static LocalDate parseDate(String value, int reservationId) throws IOException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeException exception) {
            throw new IOException("Invalid date for reservation " + reservationId + ": " + value, exception);
        }
    }

    public static class StateFile {
        public List<GuestData> guests = new ArrayList<>();
        public List<RoomData> rooms = new ArrayList<>();
        public List<ReservationData> reservations = new ArrayList<>();
    }

    public static class GuestData {
        public int guestId;
        public String name;
        public String phone;
        public String email;
    }

    public static class RoomData {
        public int roomNumber;
        public String roomType;
        public boolean available;
    }

    public static class ReservationData {
        public int reservationId;
        public int guestId;
        public int roomNumber;
        public String checkIn;
        public String checkOut;
    }
}
