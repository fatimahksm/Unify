package com.university.unify.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.CourseScheduleInputModel;

import java.util.List;

public class CourseScheduleInputAdapter extends RecyclerView.Adapter<CourseScheduleInputAdapter.ScheduleViewHolder> {

    public interface OnScheduleActionListener {
        void onRemoveClicked(int position);
    }

    private final Context context;
    private final List<CourseScheduleInputModel> items;
    private final OnScheduleActionListener listener;

    public CourseScheduleInputAdapter(Context context,
                                      List<CourseScheduleInputModel> items,
                                      OnScheduleActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule_input, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        CourseScheduleInputModel item = items.get(position);

        String day = safe(item.getDayLabel());
        String start = safe(item.getStartTime());
        String end = safe(item.getEndTime());
        String room = safe(item.getRoom());

        String text = day + " • " + start + " - " + end;

        if (!TextUtils.isEmpty(room)) {
            text += " • " + room;
        }

        holder.textScheduleMain.setText(text);

        holder.buttonRemoveSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();

                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onRemoveClicked(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        TextView textScheduleMain;
        ImageButton buttonRemoveSchedule;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            textScheduleMain = itemView.findViewById(R.id.textScheduleMain);
            buttonRemoveSchedule = itemView.findViewById(R.id.buttonRemoveSchedule);
        }
    }

    private String safe(String value) {
        if (value == null || value.equals("null")) {
            return "";
        }

        return value.trim();
    }
}