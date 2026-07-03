package com.university.unify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.OnboardingItem;

import java.util.List;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> items;

    public OnboardingPagerAdapter(List<OnboardingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onboarding_page_item, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = items.get(position);
        holder.imageOnboarding.setImageResource(item.getImageResId());
        holder.textTitle.setText(item.getTitleResId());
        holder.textDescription.setText(item.getDescriptionResId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //bykhud l item mn l model by3ml create ll iview lkel slide w baabe l info
    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageOnboarding;
        TextView textTitle;
        TextView textDescription;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageOnboarding = itemView.findViewById(R.id.imageOnboarding);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
        }
    }
}