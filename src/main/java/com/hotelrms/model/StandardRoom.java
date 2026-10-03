package com.hotelrms.model;
public class StandardRoom extends Room {

    public StandardRoom(int roomNumber) {
        super(roomNumber);
    }

    @Override
    public double calculatePrice() {
        return 2000;
    }

    @Override
    public String getRoomType() {
        return "Standard";
    }
}