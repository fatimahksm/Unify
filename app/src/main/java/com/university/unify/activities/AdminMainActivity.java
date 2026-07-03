package com.university.unify.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.university.unify.R;
import com.university.unify.fragments.admin.AdminDashboardFragment;
import com.university.unify.fragments.admin.AdminProfileFragment;
import com.university.unify.fragments.admin.FacultiesFragment;
import com.university.unify.fragments.admin.FacultyAdminsFragment;

public class AdminMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);

        fragmentContainer = findViewById(R.id.adminFragmentContainer);
        bottomNavigationView = findViewById(R.id.adminBottomNavigationView);

        applyWindowInsets();

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_dashboard);
            loadFragment(new AdminDashboardFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.admin_nav_dashboard) {
                loadFragment(new AdminDashboardFragment());
                return true;
            } else if (itemId == R.id.admin_nav_faculties) {
                loadFragment(new FacultiesFragment());
                return true;
            } else if (itemId == R.id.admin_nav_faculty_admins) {
                loadFragment(new FacultyAdminsFragment());
                return true;
            } else if (itemId == R.id.admin_nav_profile) {
                loadFragment(new AdminProfileFragment());
                return true;
            }

            return false;
        });
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAdminMain), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            fragmentContainer.setPadding(
                    fragmentContainer.getPaddingLeft(),
                    insets.top,
                    fragmentContainer.getPaddingRight(),
                    insets.bottom + 140
            );

            bottomNavigationView.setPadding(
                    bottomNavigationView.getPaddingLeft(),
                    bottomNavigationView.getPaddingTop(),
                    bottomNavigationView.getPaddingRight(),
                    insets.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment)
                .commit();
    }
}