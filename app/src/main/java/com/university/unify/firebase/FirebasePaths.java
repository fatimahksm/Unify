package com.university.unify.firebase;

import com.google.firebase.database.DatabaseReference;


public class FirebasePaths {

    private FirebasePaths() {
        // no instance
    }


    public static DatabaseReference presenceUser(String uid) {
        return FirebaseRefs.presence().child(uid);
    }
}