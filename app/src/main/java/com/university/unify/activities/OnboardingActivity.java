package com.university.unify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.university.unify.R;
import com.university.unify.adapter.OnboardingPagerAdapter;
import com.university.unify.model.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPagerOnboarding;
    private Button buttonNext;
    private TextView textSkip;
    private LinearLayout layoutDots;

    private final List<OnboardingItem> onboardingItems = new ArrayList<>();
    private OnboardingPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initViews();
        setupOnboardingItems();
        setupViewPager();
        setupDots(0);
        setupListeners();
    }

    private void initViews() {
        viewPagerOnboarding = findViewById(R.id.viewPagerOnboarding);
        buttonNext = findViewById(R.id.buttonNext);
        textSkip = findViewById(R.id.textSkip);
        layoutDots = findViewById(R.id.layoutDots);
    }

    //onboradingitem claa bl model , laanu kl slide balbu img, title descrp fa htjtu
    private void setupOnboardingItems() {
        onboardingItems.add(createItem(
                R.drawable.onboarding_1,
                R.string.onboarding_title_1,
                R.string.onboarding_desc_1
        ));

        onboardingItems.add(createItem(
                R.drawable.onboarding_2,
                R.string.onboarding_title_2,
                R.string.onboarding_desc_2
        ));

        onboardingItems.add(createItem(
                R.drawable.onboarding_3,
                R.string.onboarding_title_3,
                R.string.onboarding_desc_3
        ));

        onboardingItems.add(createItem(
                R.drawable.onboarding_4,
                R.string.onboarding_title_4,
                R.string.onboarding_desc_4
        ));
    }

    private OnboardingItem createItem(@DrawableRes int imageRes, @StringRes int titleRes, @StringRes int descRes) {
        return new OnboardingItem(imageRes, titleRes, descRes);
    }

    //bdl m a3ml 4 activities st3ml viewpagers krmel l onboarding
    private void setupViewPager() {
        adapter = new OnboardingPagerAdapter(onboardingItems);
        viewPagerOnboarding.setAdapter(adapter);

        viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupDots(position);

                if (position == onboardingItems.size() - 1) {
                    buttonNext.setText(R.string.get_started);
                } else {
                    buttonNext.setText(R.string.next);
                }
            }
        });
    }

    private void setupDots(int currentPosition) {
        layoutDots.removeAllViews();

        for (int i = 0; i < onboardingItems.size(); i++) {
            TextView dot = new TextView(this);
            dot.setText("•");
            dot.setTextSize(28);
            dot.setPadding(8, 0, 8, 0);
            dot.setTextColor(ContextCompat.getColor(
                    this,
                    i == currentPosition ? R.color.unify_gold : R.color.unify_border
            ));
            layoutDots.addView(dot);
        }
    }

    private void setupListeners() {
        buttonNext.setOnClickListener(v -> {
            int current = viewPagerOnboarding.getCurrentItem();

            if (current < onboardingItems.size() - 1) {
                viewPagerOnboarding.setCurrentItem(current + 1, true);
            } else {
                goToLogin();
            }
        });

        textSkip.setOnClickListener(v -> goToLogin());
    }

    private void goToLogin() {
        Intent intent = new Intent(OnboardingActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}