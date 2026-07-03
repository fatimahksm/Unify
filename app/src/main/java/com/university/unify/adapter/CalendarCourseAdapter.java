package com.university.unify.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.CalendarCourseModel;

import java.util.List;

public class CalendarCourseAdapter extends RecyclerView.Adapter<CalendarCourseAdapter.CalendarViewHolder> {

    private final Context context;
    private final List<CalendarCourseModel> items;

    public CalendarCourseAdapter(Context context, List<CalendarCourseModel> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_course, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarCourseModel item = items.get(position);

        holder.textTime.setText(display(item.getTimeText(), "Time not available"));
        holder.textTitle.setText(display(item.getTitle(), "Course"));
        holder.textCode.setText(display(item.getCode(), "Code not available"));

        String details = "";

        if (!TextUtils.isEmpty(item.getSection())) {
            details = details + "Section: " + item.getSection();
        }

        if (!TextUtils.isEmpty(item.getInstructorName())) {
            if (!TextUtils.isEmpty(details)) {
                details = details + " • ";
            }
            details = details + "Instructor: " + item.getInstructorName();
        }

        if (TextUtils.isEmpty(details)) {
            details = "Course details not available";
        }

        holder.textDetails.setText(details);
        holder.textRoom.setText(display(item.getRoomText(), "Room not available"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String schedule = display(item.getFullScheduleText(), "No schedule details");
                Toast.makeText(context, schedule, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String display(String value, String fallback) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            return fallback;
        }

        return value.trim();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {

        TextView textTime;
        TextView textTitle;
        TextView textCode;
        TextView textDetails;
        TextView textRoom;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);

            textTime = itemView.findViewById(R.id.textCalendarTime);
            textTitle = itemView.findViewById(R.id.textCalendarCourseTitle);
            textCode = itemView.findViewById(R.id.textCalendarCourseCode);
            textDetails = itemView.findViewById(R.id.textCalendarDetails);
            textRoom = itemView.findViewById(R.id.textCalendarRoom);
        }
    }
}