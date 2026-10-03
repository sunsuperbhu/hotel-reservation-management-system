package com.hotelrms.model;

	import java.util.Objects;

	public abstract class Room {

	    private final int roomNumber;
	    private boolean available;

	    protected Room(int roomNumber) {
	        if (roomNumber <= 0) {
	            throw new IllegalArgumentException("Room number must be positive");
	        }
	        this.roomNumber = roomNumber;
	        this.available = true;
	    }
	    public int getRoomNumber() {
	        return roomNumber;
	    }

	    public synchronized boolean isAvailable() {
	        return available;
	    }

	    public synchronized void setAvailable(boolean available) {
	        this.available = available;
	    }

	    public synchronized boolean bookIfAvailable() {
	        if (!available) {
	            return false;
	        }
	        available = false;
	        return true;
	    }

	    public abstract double calculatePrice();

	    public abstract String getRoomType();

	    @Override
	    public boolean equals(Object obj) {
	        return this == obj || obj != null && getClass() == obj.getClass()
	                && roomNumber == ((Room) obj).roomNumber;
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(getClass(), roomNumber);
	    }

	    @Override
	    public String toString() {
	        return "Room " + roomNumber +
	                " | Type: " + getRoomType() +
	                " | Price: " + calculatePrice() +
	                " | Available: " + isAvailable();
	    }
	}
