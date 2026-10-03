package com.hotelrms.model;

import java.util.Objects;

public class Guest {
    private final int guestId;
    private String name;
    private String phone;
    private final String email;

    public Guest(int guestId, String name, String phone, String email) {
        if (guestId <= 0) {
            throw new IllegalArgumentException("Guest ID must be positive");
        }
        this.guestId = guestId;
        this.name = Objects.requireNonNull(name, "name");
        this.phone = Objects.requireNonNull(phone, "phone");
        this.email = Objects.requireNonNull(email, "email");
    }

    public int getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = Objects.requireNonNull(phone, "phone");
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass()
                && guestId == ((Guest) obj).guestId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestId);
    }

    @Override
    public String toString() {
        return "Guest{" +
                "ID=" + guestId +
                ", Name='" + name + '\'' +
                ", Phone='" + phone + '\'' +
                ", Email='" + email + '\'' +
                '}';
    }
}
