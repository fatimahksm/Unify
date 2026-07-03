package com.university.unify.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.university.unify.R;
import com.university.unify.fragments.common.ConversationsFragment;
import com.university.unify.fragments.common.NotificationsFragment;
import com.university.unify.fragments.instructor.InstructorCoursesFragment;
import com.university.unify.fragments.instructor.InstructorHomeFragment;
import com.university.unify.fragments.instructor.InstructorProfileFragment;

public class InstructorMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_main);

        bottomNav = findViewById(R.id.instructorBottomNav);

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.navHome);
            loadFragment(new InstructorHomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navHome) {
                loadFragment(new InstructorHomeFragment());
                return true;

            } else if (id == R.id.navCourses) {
                loadFragment(new InstructorCoursesFragment());
                return true;

            } else if (id == R.id.navChats) {
                loadFragment(new ConversationsFragment());
                return true;

            } else if (id == R.id.navNotifications) {
                loadFragment(new NotificationsFragment());
                return true;

            } else if (id == R.id.navProfile) {
                loadFragment(new InstructorProfileFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.instructorContainer, fragment)
                .commit();
    }
}