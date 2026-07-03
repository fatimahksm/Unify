package com.university.unify.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.MessageModel;
import com.university.unify.utils.ImageLoaderUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private final List<MessageModel> messages;
    private final String currentUserId;

    public MessageAdapter(Context context,
                          List<MessageModel> messages,
                          String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messages.get(position);

        String senderId = safe(message.getSenderId());
        String senderName = safe(message.getSenderName());
        String senderAvatar = safe(message.getSenderProfileImageUrl());
        String text = safe(message.getText());

        boolean isMine = senderId.equals(currentUserId);

        holder.textBody.setText(text.isEmpty() ? "-" : text);
        holder.textTime.setText(formatTime(message.getCreatedAt()));

        if (isMine) {
            bindMyMessage(holder);
        } else {
            bindOtherMessage(holder, senderId, senderName, senderAvatar);
        }
    }

    private void bindMyMessage(MessageViewHolder holder) {
        holder.root.setGravity(Gravity.END);
        holder.row.setGravity(Gravity.END);

        // Hide avatar for own messages
        holder.imageAvatar.setVisibility(View.GONE);

        holder.textSender.setVisibility(View.GONE);

        holder.textBody.setBackground(createMyBubble());
        holder.textBody.setTextColor(Color.WHITE);

        holder.textTime.setGravity(Gravity.END);
        holder.textTime.setTextColor(ContextCompat.getColor(context, R.color.unify_text_secondary));
    }

    private void bindOtherMessage(MessageViewHolder holder,
                                  String senderId,
                                  String senderName,
                                  String senderAvatar) {
        holder.root.setGravity(Gravity.START);
        holder.row.setGravity(Gravity.START);

        // Avatar for incoming messages
        holder.imageAvatar.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(senderAvatar)) {
            holder.imageAvatar.setImageResource(R.drawable.baseline_person_24);
        } else {
            ImageLoaderUtil.loadImage(context, holder.imageAvatar, senderAvatar, false);
        }

        if (TextUtils.isEmpty(senderName)) {
            holder.textSender.setVisibility(View.GONE);
        } else {
            holder.textSender.setVisibility(View.VISIBLE);
            holder.textSender.setText(senderName);
            holder.textSender.setTextColor(getSenderAccentColor(senderId));
        }

        holder.textBody.setBackground(createOtherBubble(senderId));
        holder.textBody.setTextColor(ContextCompat.getColor(context, R.color.unify_text_primary));

        holder.textTime.setGravity(Gravity.START);
        holder.textTime.setTextColor(ContextCompat.getColor(context, R.color.unify_text_secondary));
    }

    private GradientDrawable createMyBubble() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(context, R.color.unify_primary));
        drawable.setCornerRadii(new float[]{
                dp(18), dp(18),
                dp(18), dp(18),
                dp(18), dp(18),
                dp(4), dp(4)
        });
        return drawable;
    }

    private GradientDrawable createOtherBubble(String senderId) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(getSenderBubbleColor(senderId));
        drawable.setStroke(dpInt(1), ContextCompat.getColor(context, R.color.unify_border));
        drawable.setCornerRadii(new float[]{
                dp(18), dp(18),
                dp(18), dp(18),
                dp(4), dp(4),
                dp(18), dp(18)
        });
        return drawable;
    }

    private int getSenderBubbleColor(String senderId) {
        String[] palette = new String[]{
                "#EAF3FF", "#EAFBF1", "#FFF4E8", "#F4EEFF",
                "#FFF0F5", "#EDF7F7", "#FFFBEA", "#F1F5F9"
        };
        return Color.parseColor(palette[getStableColorIndex(senderId, palette.length)]);
    }

    private int getSenderAccentColor(String senderId) {
        String[] palette = new String[]{
                "#2F6BFF", "#1E9E5A", "#E67E22", "#7A4DFF",
                "#E84393", "#149ECA", "#C9A100", "#4B5D73"
        };
        return Color.parseColor(palette[getStableColorIndex(senderId, palette.length)]);
    }

    private int getStableColorIndex(String senderId, int size) {
        if (TextUtils.isEmpty(senderId)) return 0;
        return Math.abs(senderId.hashCode()) % size;
    }

    private float dp(int value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    private int dpInt(int value) {
        return Math.round(dp(value));
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) return "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return format.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) return "";
        return value.trim();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayout root;
        LinearLayout row;
        ImageView imageAvatar;
        TextView textSender;
        TextView textBody;
        TextView textTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.rootMessageItem);
            row = itemView.findViewById(R.id.layoutMessageRow);
            imageAvatar = itemView.findViewById(R.id.imageMessageAvatar);
            textSender = itemView.findViewById(R.id.textMessageSender);
            textBody = itemView.findViewById(R.id.textMessageBody);
            textTime = itemView.findViewById(R.id.textMessageTime);
        }
    }
}
