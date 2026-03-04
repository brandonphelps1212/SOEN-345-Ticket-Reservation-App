package com.soen345.ticketreservation.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Connects to Firebase Realtime Database and provides read/write helpers.
 *
 * SETUP:
 *   1. Place firebase-key.json in src/main/resources/
 *   2. Set FIREBASE_DATABASE_URL in your .env file
 *      e.g. https://your-project-default-rtdb.firebaseio.com
 *
 * Author: Yan
 */
public class FirebaseService {

    private static FirebaseDatabase database;

    // ─────────────────────────────────────────────────────────
    //  INITIALIZE  — call once before any DB operations
    // ─────────────────────────────────────────────────────────
    public static void initialize() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            // Already initialized — safe to call multiple times
            database = FirebaseDatabase.getInstance();
            return;
        }

        FileInputStream serviceAccount =
            new FileInputStream("src/main/resources/firebase-key.json");

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl(System.getenv("FIREBASE_DATABASE_URL"))
            .build();

        FirebaseApp.initializeApp(options);
        database = FirebaseDatabase.getInstance();
        System.out.println("[Firebase] Connected successfully.");
    }

    // ─────────────────────────────────────────────────────────
    //  WRITE  —  writeData("reservations/r-001", dataMap)
    // ─────────────────────────────────────────────────────────
    public static void writeData(String path, Object data) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        database.getReference(path).setValue(data, (error, ref) -> {
            if (error != null)
                System.out.println("[Firebase] Write failed: " + error.getMessage());
            else
                System.out.println("[Firebase] Write OK: " + path);
            latch.countDown();
        });

        latch.await();
    }

    // ─────────────────────────────────────────────────────────
    //  READ  —  Object data = readData("events/e-001")
    // ─────────────────────────────────────────────────────────
    public static Object readData(String path) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Object> result = new AtomicReference<>();

        database.getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                result.set(snapshot.getValue());
                latch.countDown();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("[Firebase] Read failed: " + error.getMessage());
                latch.countDown();
            }
        });

        latch.await();
        return result.get();
    }

    // ─────────────────────────────────────────────────────────
    //  DELETE  —  deleteData("reservations/r-001")
    // ─────────────────────────────────────────────────────────
    public static void deleteData(String path) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        database.getReference(path).removeValue((error, ref) -> latch.countDown());
        latch.await();
    }

    // ─────────────────────────────────────────────────────────
    //  RAW REFERENCE  — for advanced queries
    // ─────────────────────────────────────────────────────────
    public static DatabaseReference getRef(String path) {
        return database.getReference(path);
    }
}
