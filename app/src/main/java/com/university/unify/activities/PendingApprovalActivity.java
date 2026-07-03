package com.university.unify.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.university.unify.R;
import com.university.unify.auth.AuthHelper;

public class PendingApprovalActivity extends AppCompatActivity {

    private MaterialButton buttonBackToLogin;
    private final AuthHelper authHelper = new AuthHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approval);

        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);

        // Just to be safe: user should not remain signed in while pending approval
        authHelper.logout();

        buttonBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(PendingApprovalActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PendingApprovalActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}