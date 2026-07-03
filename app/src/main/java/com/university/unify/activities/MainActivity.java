package com.university.unify.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.university.unify.SqLite.DatabaseHelper;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private volatile boolean keepSplash = true;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplash);

        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);

        checkLocalSession();
    }

    private void checkLocalSession() {
        if (!db.hasLoggedInUser()) {
            keepSplash = false;
            openLogin();
            return;
        }

        String role = db.getLoggedInRole();
        String isApproved = db.getLoggedInApproval();

        keepSplash = false;

        if (!isUserApproved(isApproved)) {
            openPendingApproval();
            return;
        }

        openScreenByRole(role);
    }

    private boolean isUserApproved(String isApproved) {
        if (isApproved == null) {
            return false;
        }

        String value = isApproved.trim().toLowerCase(Locale.ROOT);

        return value.equals("true")
                || value.equals("1")
                || value.equals("approved")
                || value.equals("yes");
    }

    private void openScreenByRole(String role) {
        String cleanRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);

        Intent intent;

        switch (cleanRole) {
            case "SUPER_ADMIN":
                intent = new Intent(this, AdminMainActivity.class);
                break;

            case "FACULTY_ADMIN":
                intent = new Intent(this, FacultyAdminMainActivity.class);
                break;

            case "INSTRUCTOR":
                intent = new Intent(this, InstructorMainActivity.class);
                break;

            case "STUDENT":
            default:
                intent = new Intent(this, StudentMainActivity.class);
                break;
        }

        openAndClear(intent);
    }

    private void openPendingApproval() {
        openAndClear(new Intent(this, PendingApprovalActivity.class));
    }

    private void openLogin() {
        openAndClear(new Intent(this, LoginActivity.class));
    }

    private void openAndClear(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}