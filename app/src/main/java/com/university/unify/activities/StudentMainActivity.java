package com.university.unify.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.university.unify.R;
import com.university.unify.fragments.common.ConversationsFragment;
import com.university.unify.fragments.common.NotificationsFragment;
import com.university.unify.fragments.student.StudentAnnouncementsFragment;
import com.university.unify.fragments.student.StudentCoursesFragment;
import com.university.unify.fragments.student.StudentProfileFragment;
import com.university.unify.fragments.student.StudentSocialFragment;
import com.university.unify.utils.StudentAssistantFabHelper;

public class StudentMainActivity extends AppCompatActivity {

    private LinearLayout navCourses;
    private LinearLayout navSocial;
    private LinearLayout navAnnouncements;
    private LinearLayout navChats;
    private LinearLayout navNotifications;
    private LinearLayout navProfile;

    private View indicatorCourses;
    private View indicatorSocial;
    private View indicatorAnnouncements;
    private View indicatorChats;
    private View indicatorNotifications;
    private View indicatorProfile;

    private ImageView iconCourses;
    private ImageView iconSocial;
    private ImageView iconAnnouncements;
    private ImageView iconChats;
    private ImageView iconNotifications;
    private ImageView iconProfile;

    private TextView textCourses;
    private TextView textSocial;
    private TextView textAnnouncements;
    private TextView textChats;
    private TextView textNotifications;
    private TextView textProfile;

    private LinearLayout navCalendar;
    private View indicatorCalendar;
    private ImageView iconCalendar;
    private TextView textCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main);

        StudentAssistantFabHelper.attach(this);

        initViews();
        setupListeners();

        if (savedInstanceState == null) {
            openCourses();
        }
    }

    private void initViews() {
        navCourses = findViewById(R.id.navCourses);
        navSocial = findViewById(R.id.navSocial);
        navAnnouncements = findViewById(R.id.navAnnouncements);
        navChats = findViewById(R.id.navChats);
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);



        indicatorCourses = findViewById(R.id.indicatorCourses);
        indicatorSocial = findViewById(R.id.indicatorSocial);
        indicatorAnnouncements = findViewById(R.id.indicatorAnnouncements);
        indicatorChats = findViewById(R.id.indicatorChats);
        indicatorNotifications = findViewById(R.id.indicatorNotifications);
        indicatorProfile = findViewById(R.id.indicatorProfile);

        iconCourses = findViewById(R.id.iconCourses);
        iconSocial = findViewById(R.id.iconSocial);
        iconAnnouncements = findViewById(R.id.iconAnnouncements);
        iconChats = findViewById(R.id.iconChats);
        iconNotifications = findViewById(R.id.iconNotifications);
        iconProfile = findViewById(R.id.iconProfile);

        textCourses = findViewById(R.id.textCourses);
        textSocial = findViewById(R.id.textSocial);
        textAnnouncements = findViewById(R.id.textAnnouncements);
        textChats = findViewById(R.id.textChats);
        textNotifications = findViewById(R.id.textNotifications);
        textProfile = findViewById(R.id.textProfile);
    }

    private void setupListeners() {
        navCourses.setOnClickListener(v -> openCourses());

        navSocial.setOnClickListener(v -> {
            selectTab("SOCIAL");
            replaceFragment(new StudentSocialFragment());
        });

        navAnnouncements.setOnClickListener(v -> {
            selectTab("ANNOUNCEMENTS");
            replaceFragment(new StudentAnnouncementsFragment());
        });

        navChats.setOnClickListener(v -> {
            selectTab("CHATS");
            replaceFragment(new ConversationsFragment());
        });

        navNotifications.setOnClickListener(v -> {
            selectTab("NOTIFICATIONS");
            replaceFragment(new NotificationsFragment());
        });

        navProfile.setOnClickListener(v -> {
            selectTab("PROFILE");
            replaceFragment(new StudentProfileFragment());
        });
    }

    private void openCourses() {
        selectTab("COURSES");
        replaceFragment(new StudentCoursesFragment());
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.studentFragmentContainer, fragment)
                .commit();
    }

    private void selectTab(String tab) {
        resetAllTabs();

        if (tab.equals("COURSES")) {
            activateTab(indicatorCourses, iconCourses, textCourses);
            return;
        }

        if (tab.equals("SOCIAL")) {
            activateTab(indicatorSocial, iconSocial, textSocial);
            return;
        }

        if (tab.equals("ANNOUNCEMENTS")) {
            activateTab(indicatorAnnouncements, iconAnnouncements, textAnnouncements);
            return;
        }

        if (tab.equals("CHATS")) {
            activateTab(indicatorChats, iconChats, textChats);
            return;
        }

        if (tab.equals("NOTIFICATIONS")) {
            activateTab(indicatorNotifications, iconNotifications, textNotifications);
            return;
        }

        if (tab.equals("PROFILE")) {
            activateTab(indicatorProfile, iconProfile, textProfile);
        }
    }

    private void resetAllTabs() {
        resetTab(indicatorCourses, iconCourses, textCourses);
        resetTab(indicatorSocial, iconSocial, textSocial);
        resetTab(indicatorAnnouncements, iconAnnouncements, textAnnouncements);
        resetTab(indicatorChats, iconChats, textChats);
        resetTab(indicatorNotifications, iconNotifications, textNotifications);
        resetTab(indicatorProfile, iconProfile, textProfile);
    }

    private void activateTab(View indicator, ImageView icon, TextView text) {
        indicator.setBackgroundColor(ContextCompat.getColor(this, R.color.unify_gold));
        icon.setColorFilter(ContextCompat.getColor(this, R.color.unify_blue));
        text.setTextColor(ContextCompat.getColor(this, R.color.unify_blue));
        text.setTypeface(null, Typeface.BOLD);
    }

    private void resetTab(View indicator, ImageView icon, TextView text) {
        indicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        icon.setColorFilter(ContextCompat.getColor(this, R.color.unify_text_secondary));
        text.setTextColor(ContextCompat.getColor(this, R.color.unify_text_secondary));
        text.setTypeface(null, Typeface.NORMAL);
    }
}