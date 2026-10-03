package com.hotelrms.model;

import java.util.Objects;

public class Billing {

    private int billId;
    private Reservation reservation;
    private double taxRate;

    public Billing(int billId, Reservation reservation) {
        if (billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be positive");
        }
        this.billId = billId;
        this.reservation = Objects.requireNonNull(reservation, "reservation");
        this.taxRate = 0.18;
    }

    public double calculateSubtotal() {
        return reservation.calculateAmount();
    }

    public double calculateTax() {
        return calculateSubtotal() * taxRate;
    }

    public double calculateTotal() {
        return calculateSubtotal() + calculateTax();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass()
                && billId == ((Billing) obj).billId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId);
    }

    @Override
    public String toString() {
        return "Billing{" +
                "billId=" + billId +
                ", reservationId=" + reservation.getReservationId() +
                ", total=" + calculateTotal() +
                '}';
    }

    public void printBill() {

        System.out.println("\n========== BILL ==========");
        System.out.println("Bill ID      : " + billId);
        System.out.println("Guest        : "
                + reservation.getGuest().getName());
        System.out.println("Room         : "
                + reservation.getRoom().getRoomNumber());
        System.out.println("Room Type    : "
                + reservation.getRoom().getRoomType());
        System.out.println("Subtotal     : ₹"
                + calculateSubtotal());
        System.out.println("Tax          : ₹"
                + calculateTax());
        System.out.println("Total        : ₹"
                + calculateTotal());
        System.out.println("===========================\n");
    }
}