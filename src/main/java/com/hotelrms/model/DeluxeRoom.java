package com.hotelrms.model;
public class DeluxeRoom extends Room {

    public DeluxeRoom(int roomNumber) {
        super(roomNumber);
    }

    @Override
    public double calculatePrice() {
        return 3500;
    }

    @Override
    public String getRoomType() {
        return "Deluxe";
    }
}