package com.hotelrms.model;
import java.time.LocalDate;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Reservation implements Comparable<Reservation> {

    private int reservationId;
    private Guest guest;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public Reservation(
            int reservationId,
            Guest guest,
            Room room,
            LocalDate checkIn,
            LocalDate checkOut) {

        if (reservationId <= 0) {
            throw new IllegalArgumentException("Reservation ID must be positive");
        }
        this.reservationId = reservationId;
        this.guest = Objects.requireNonNull(guest, "guest");
        this.room = Objects.requireNonNull(room, "room");
        this.checkIn = Objects.requireNonNull(checkIn, "checkIn");
        this.checkOut = Objects.requireNonNull(checkOut, "checkOut");
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
    }

    public int getReservationId() {
        return reservationId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public long getNumberOfDays() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double calculateAmount() {

        return room.calculatePrice() * getNumberOfDays();
    }

    @Override
    public int compareTo(Reservation other) {
        int byDate = this.checkIn.compareTo(other.checkIn);
        return byDate != 0 ? byDate : Integer.compare(reservationId, other.reservationId);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass()
                && reservationId == ((Reservation) obj).reservationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    @Override
    public String toString() {

        return "Reservation{" +
                "ID=" + reservationId +
                ", Guest=" + guest.getName() +
                ", Room=" + room.getRoomNumber() +
                ", Check-in=" + checkIn +
                ", Check-out=" + checkOut +
                ", Amount=" + calculateAmount() +
                '}';
    }
}