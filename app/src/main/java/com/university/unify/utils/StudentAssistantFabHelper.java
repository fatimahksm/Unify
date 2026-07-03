package com.university.unify.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.university.unify.R;
import com.university.unify.activities.StudentChatBotActivity;

public class StudentAssistantFabHelper {

    private static final String FAB_TAG = "student_assistant_fab";

    public static void attach(Activity activity) {
        if (activity == null) {
            return;
        }

        FrameLayout root = activity.findViewById(android.R.id.content);

        if (root == null) {
            return;
        }

        View existing = root.findViewWithTag(FAB_TAG);

        if (existing != null) {
            return;
        }

        FloatingActionButton fab = new FloatingActionButton(activity);
        fab.setTag(FAB_TAG);
        fab.setImageResource(R.drawable.ic_ai_assistant_24);
        fab.setContentDescription(activity.getString(R.string.student_assistant_button));
        fab.setSize(FloatingActionButton.SIZE_NORMAL);
        fab.setBackgroundTintList(activity.getColorStateList(R.color.unify_primary));
        fab.setColorFilter(activity.getColor(R.color.unify_text_on_dark));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(0, 0, dp(activity, 20), dp(activity, 88));

        fab.setLayoutParams(params);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(activity, StudentChatBotActivity.class);
            activity.startActivity(intent);
        });

        root.addView(fab);
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }
}