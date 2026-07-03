package com.university.unify.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.university.unify.constants.FirebaseCollections;

public class FirebaseRefs {


    public static final String REALTIME_DB_URL =
            "https://unify-86505-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private FirebaseRefs() {
        // no instance
    }

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseDatabase realtimeDb() {
        return FirebaseDatabase.getInstance(REALTIME_DB_URL);
    }

    public static DatabaseReference chats() {
        return realtimeDb().getReference(FirebaseCollections.CHATS);
    }

    public static DatabaseReference messages() {
        return realtimeDb().getReference(FirebaseCollections.MESSAGES);
    }

    public static DatabaseReference notifications() {
        return realtimeDb().getReference(FirebaseCollections.NOTIFICATIONS);
    }

    public static DatabaseReference presence() {
        return realtimeDb().getReference(FirebaseCollections.PRESENCE);
    }
}