package com.university.unify.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.constants.NotificationConstants;
import com.university.unify.model.NotificationModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClicked(NotificationModel notification);
    }

    private final Context context;
    private final List<NotificationModel> notifications;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(Context context,
                               List<NotificationModel> notifications,
                               OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel item = notifications.get(position);

        holder.textTitle.setText(display(item.getTitle()));
        holder.textBody.setText(display(item.getBody()));
        holder.textTime.setText(formatTime(item.getCreatedAt()));

        if (item.isRead()) {
            holder.viewUnreadDot.setVisibility(View.GONE);
            holder.textNewBadge.setVisibility(View.GONE);
        } else {
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.textNewBadge.setVisibility(View.VISIBLE);
        }

        String type = item.getType();

        if (NotificationConstants.TYPE_PRIVATE_MESSAGE.equals(type)) {
            holder.textIcon.setText("💬");
        } else if (NotificationConstants.TYPE_GROUP_MESSAGE.equals(type)) {
            holder.textIcon.setText("👥");
        } else if (NotificationConstants.TYPE_COURSE_MATERIAL.equals(type)) {
            holder.textIcon.setText("📄");
        } else if (NotificationConstants.TYPE_COURSE_ANNOUNCEMENT.equals(type)) {
            holder.textIcon.setText("📢");
        } else if (NotificationConstants.TYPE_GRADE_UPDATED.equals(type)) {
            holder.textIcon.setText("🎓");
        } else {
            holder.textIcon.setText("🔔");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications == null ? 0 : notifications.size();
    }

    private String display(String value) {
        if (TextUtils.isEmpty(value) || value.equalsIgnoreCase("null")) {
            return "-";
        }

        return value.trim();
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            return format.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView textIcon;
        TextView textTitle;
        TextView textBody;
        TextView textTime;
        TextView textNewBadge;
        View viewUnreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            textIcon = itemView.findViewById(R.id.textNotificationIcon);
            textTitle = itemView.findViewById(R.id.textNotificationTitle);
            textBody = itemView.findViewById(R.id.textNotificationBody);
            textTime = itemView.findViewById(R.id.textNotificationTime);
            textNewBadge = itemView.findViewById(R.id.textNotificationNewBadge);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}