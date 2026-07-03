package com.university.unify.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;

public class RegisterStepsPagerAdapter extends RecyclerView.Adapter<RegisterStepsPagerAdapter.StepViewHolder> {

    private final LayoutInflater inflater;
    private final int[] layouts = {
            R.layout.item_register_step_personal,
            R.layout.item_register_step_academic,
            R.layout.item_register_step_account
    };

    private final SparseArray<View> stepViews = new SparseArray<>();

    public RegisterStepsPagerAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        return layouts[position];
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(viewType, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        stepViews.put(position, holder.itemView);
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    public View getStepView(int position) {
        return stepViews.get(position);
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}