package com.university.unify.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.TextView;
import com.university.unify.fragments.faculty_admin.FacultyAdminMajorsFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.university.unify.R;
import com.university.unify.fragments.common.ConversationsFragment;
import com.university.unify.fragments.common.NotificationsFragment;
import com.university.unify.fragments.faculty_admin.FacultyAdminCoursesFragment;
import com.university.unify.fragments.faculty_admin.FacultyAdminInstructorsFragment;
import com.university.unify.fragments.faculty_admin.FacultyAdminPendingStudentsFragment;
import com.university.unify.fragments.faculty_admin.FacultyAdminProfileFragment;
import com.university.unify.fragments.faculty_admin.FacultyAdminTeachingFragment;

public class FacultyAdminMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton buttonOpenDrawer;
    private TextView textToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_admin_main);

        initViews();
        setupListeners();

        if (savedInstanceState == null) {
            openPendingStudents();
            navigationView.setCheckedItem(R.id.faculty_admin_nav_pending_students);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.facultyAdminDrawerLayout);
        navigationView = findViewById(R.id.facultyAdminNavigationView);
        buttonOpenDrawer = findViewById(R.id.buttonOpenDrawer);
        textToolbarTitle = findViewById(R.id.textFacultyAdminToolbarTitle);
    }

    private void setupListeners() {
        buttonOpenDrawer.setOnClickListener(view ->
                drawerLayout.openDrawer(Gravity.LEFT)
        );

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.faculty_admin_nav_pending_students) {
                openPendingStudents();

            } else if (itemId == R.id.faculty_admin_nav_majors) {
                openMajors();

            } else if (itemId == R.id.faculty_admin_nav_teaching) {
                openTeaching();

            } else if (itemId == R.id.faculty_admin_nav_courses) {
                openCourses();

            } else if (itemId == R.id.faculty_admin_nav_instructors) {
                openInstructors();

            } else if (itemId == R.id.faculty_admin_nav_chats) {
                openChats();

            } else if (itemId == R.id.faculty_admin_nav_notifications) {
                openNotifications();

            } else if (itemId == R.id.faculty_admin_nav_profile) {
                openProfile();
            }

            drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        });
    }

    private void openPendingStudents() {
        textToolbarTitle.setText(getString(R.string.pending_users));
        loadFragment(new FacultyAdminPendingStudentsFragment());
    }

    private void openMajors() {
        textToolbarTitle.setText(getString(R.string.majors));
        loadFragment(new FacultyAdminMajorsFragment());
    }

    private void openTeaching() {
        textToolbarTitle.setText(getString(R.string.teaching));
        loadFragment(new FacultyAdminTeachingFragment());
    }

    private void openCourses() {
        textToolbarTitle.setText(getString(R.string.courses));
        loadFragment(new FacultyAdminCoursesFragment());
    }

    private void openInstructors() {
        textToolbarTitle.setText(getString(R.string.instructors));
        loadFragment(new FacultyAdminInstructorsFragment());
    }

    private void openChats() {
        textToolbarTitle.setText(getString(R.string.chats));
        loadFragment(new ConversationsFragment());
    }

    private void openNotifications() {
        textToolbarTitle.setText(getString(R.string.notifications));
        loadFragment(new NotificationsFragment());
    }

    private void openProfile() {
        textToolbarTitle.setText(getString(R.string.profile));
        loadFragment(new FacultyAdminProfileFragment());
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.facultyAdminFragmentContainer, fragment)
                .commit();
    }
}