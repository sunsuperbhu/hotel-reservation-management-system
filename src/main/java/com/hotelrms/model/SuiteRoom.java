package com.hotelrms.model;
public class SuiteRoom extends Room {

    public SuiteRoom(int roomNumber) {
        super(roomNumber);
    }

    @Override
    public double calculatePrice() {
        return 6000;
    }

    @Override
    public String getRoomType() {
        return "Suite";
    }
}