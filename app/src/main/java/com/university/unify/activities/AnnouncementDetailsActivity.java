package com.university.unify.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.university.unify.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AnnouncementDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_BODY = "extra_body";
    public static final String EXTRA_COURSE_ID = "extra_course_id";
    public static final String EXTRA_CREATED_AT = "extra_created_at";
    public static final String EXTRA_IS_PINNED = "extra_is_pinned";
    public static final String EXTRA_CREATED_BY = "extra_created_by";

    private TextView textBadge;
    private TextView textTitle;
    private TextView textBody;
    private TextView textCourse;
    private TextView textDate;
    private TextView textCreatedBy;
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_details);

        initViews();
        applyInsets();
        bindData();
        setupListeners();
    }

    private void initViews() {
        textBadge = findViewById(R.id.textDetailsBadge);
        textTitle = findViewById(R.id.textDetailsTitle);
        textBody = findViewById(R.id.textDetailsBody);
        textCourse = findViewById(R.id.textDetailsCourse);
        textDate = findViewById(R.id.textDetailsDate);
        textCreatedBy = findViewById(R.id.textDetailsCreatedBy);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAnnouncementDetails), (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            view.setPadding(
                    view.getPaddingLeft(),
                    top,
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );
            return insets;
        });
    }

    private void bindData() {
        String title = safe(getIntent().getStringExtra(EXTRA_TITLE));
        String body = safe(getIntent().getStringExtra(EXTRA_BODY));
        String courseId = safe(getIntent().getStringExtra(EXTRA_COURSE_ID));
        String createdBy = safe(getIntent().getStringExtra(EXTRA_CREATED_BY));
        long createdAt = getIntent().getLongExtra(EXTRA_CREATED_AT, 0L);
        boolean isPinned = getIntent().getBooleanExtra(EXTRA_IS_PINNED, false);

        textTitle.setText(title);
        textBody.setText(body);

        if (isPinned) {
            textBadge.setText(getString(R.string.pinned_announcement));
            textBadge.setBackgroundResource(R.drawable.bg_announcement_badge_pinned);
        } else {
            textBadge.setText(getString(R.string.general_announcement));
            textBadge.setBackgroundResource(R.drawable.bg_announcement_badge);
        }

        if (courseId.isEmpty()) {
            textCourse.setText(getString(R.string.course_chip_value, getString(R.string.not_available_short)));
        } else {
            textCourse.setText(getString(R.string.course_chip_value, courseId));
        }

        if (createdBy.isEmpty()) {
            textCreatedBy.setText(getString(R.string.created_by_value, getString(R.string.not_available_short)));
        } else {
            textCreatedBy.setText(getString(R.string.created_by_value, createdBy));
        }

        textDate.setText(getString(R.string.announcement_date_value, formatDate(createdAt)));
    }

    private void setupListeners() {
        buttonBack.setOnClickListener(v -> finish());
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) {
            return getString(R.string.not_available_short);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}