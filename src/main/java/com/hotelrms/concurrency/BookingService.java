package com.hotelrms.concurrency;

import com.hotelrms.model.Room;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookingService implements AutoCloseable {

    private final ExecutorService executor;

    public BookingService(int numberOfThreads) {
        if (numberOfThreads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive");
        }
        executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    public boolean bookRoom(Room room) {
        synchronized (room) {
            if (!room.isAvailable()) {
                System.out.println(Thread.currentThread().getName()
                        + " FAILED - room already booked");
                return false;
            }
            room.setAvailable(false);
            System.out.println(Thread.currentThread().getName()
                    + " booking room " + room.getRoomNumber());
            return true;
        }
    }

    public List<Boolean> runConcurrentBookingDemo(Room room) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(3);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch runnableDone = new CountDownLatch(1);
        AtomicBoolean runnableResult = new AtomicBoolean();
        AtomicBoolean threadResult = new AtomicBoolean();

        Thread thread = new Thread(() -> {
            ready.countDown();
            if (awaitStart(start)) {
                threadResult.set(bookRoom(room));
            }
        }, "direct-booking-thread");

        try {
            executor.execute((Runnable) () -> {
                ready.countDown();
                try {
                    if (awaitStart(start)) {
                        runnableResult.set(bookRoom(room));
                    }
                } finally {
                    runnableDone.countDown();
                }
            });
            var callableResult = executor.submit(() -> {
                ready.countDown();
                return awaitStart(start) && bookRoom(room);
            });
            thread.start();

            ready.await();
            start.countDown();
            thread.join();
            runnableDone.await();
            return List.of(runnableResult.get(), callableResult.get(), threadResult.get());
        } catch (InterruptedException exception) {
            start.countDown();
            thread.interrupt();
            joinUninterruptibly(thread);
            throw exception;
        } catch (java.util.concurrent.ExecutionException exception) {
            throw new IllegalStateException("Concurrent booking task failed", exception.getCause());
        } finally {
            shutdown();
        }
    }

    private static boolean awaitStart(CountDownLatch start) {
        try {
            start.await();
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void joinUninterruptibly(Thread thread) {
        boolean interrupted = false;
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException exception) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void shutdown() {
        boolean interrupted = Thread.interrupted();
        executor.shutdown();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!executor.isTerminated() && System.nanoTime() < deadline) {
                try {
                    executor.awaitTermination(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                } catch (InterruptedException exception) {
                    interrupted = true;
                    executor.shutdownNow();
                }
            }

            if (!executor.isTerminated()) {
                executor.shutdownNow();
                deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (!executor.isTerminated() && System.nanoTime() < deadline) {
                    try {
                        executor.awaitTermination(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                    } catch (InterruptedException exception) {
                        interrupted = true;
                    }
                }
            }
            if (!executor.isTerminated()) {
                throw new IllegalStateException("Booking executor did not terminate");
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void close() {
        shutdown();
    }
}
