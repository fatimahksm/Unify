package com.university.unify.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.constants.ChatConstants;
import com.university.unify.model.ChatModel;
import com.university.unify.utils.ImageLoaderUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClicked(ChatModel chat);
    }

    private final Context context;
    private final List<ChatModel> chats;
    private final String currentUserId;
    private final OnConversationClickListener listener;

    public ConversationAdapter(Context context,
                               List<ChatModel> chats,
                               String currentUserId,
                               OnConversationClickListener listener) {
        this.context = context;
        this.chats = chats;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ChatModel chat = chats.get(position);

        String title = display(chat.getTitle());
        String lastMessage = display(chat.getLastMessageText());
        String type = display(chat.getType());
        String avatarUrl = display(chat.getOtherUserProfileImageUrl());

        if (TextUtils.isEmpty(title)) {
            title = context.getString(R.string.chat);
        }

        holder.textTitle.setText(title);

        if (TextUtils.isEmpty(chat.getLastMessageText())) {
            holder.textLastMessage.setText(context.getString(R.string.last_message_empty));
        } else {
            holder.textLastMessage.setText(lastMessage);
        }

        holder.textTime.setText(formatTime(chat.getLastMessageAt()));

        // Avatar / initial logic
        if (ChatConstants.TYPE_COURSE_GROUP.equals(type)) {
            // Group chat — always a "G" badge, no photo
            holder.textType.setText(context.getString(R.string.course_group_chat));
            holder.imageAvatar.setVisibility(View.GONE);
            holder.textAvatar.setVisibility(View.VISIBLE);
            holder.textAvatar.setText("G");
        } else {
            holder.textType.setText(context.getString(R.string.private_chat));

            if (TextUtils.isEmpty(avatarUrl)) {
                // No photo — show initial
                holder.imageAvatar.setVisibility(View.GONE);
                holder.textAvatar.setVisibility(View.VISIBLE);
                holder.textAvatar.setText(title.substring(0, 1).toUpperCase());
            } else {
                // Photo available — show it, hide initial
                holder.textAvatar.setVisibility(View.GONE);
                holder.imageAvatar.setVisibility(View.VISIBLE);
                ImageLoaderUtil.loadImage(context, holder.imageAvatar, avatarUrl, false);
            }
        }

        int unreadCount = getUnreadCount(chat);
        if (unreadCount > 0) {
            holder.textUnreadBadge.setVisibility(View.VISIBLE);
            holder.textUnreadBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        } else {
            holder.textUnreadBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onConversationClicked(chat);
        });
    }

    @Override
    public int getItemCount() {
        return chats == null ? 0 : chats.size();
    }

    private int getUnreadCount(ChatModel chat) {
        if (chat == null || TextUtils.isEmpty(currentUserId)) return 0;

        Map<String, Integer> unreadCounts = chat.getUnreadCounts();
        if (unreadCounts == null) return 0;

        Object value = unreadCounts.get(currentUserId);
        if (value == null) return 0;

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) return "";
        return value.trim();
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

    static class ConversationViewHolder extends RecyclerView.ViewHolder {

        ImageView imageAvatar;
        TextView textAvatar;
        TextView textTitle;
        TextView textLastMessage;
        TextView textType;
        TextView textTime;
        TextView textUnreadBadge;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageConversationAvatar);
            textAvatar = itemView.findViewById(R.id.textConversationAvatar);
            textTitle = itemView.findViewById(R.id.textConversationTitle);
            textLastMessage = itemView.findViewById(R.id.textConversationLastMessage);
            textType = itemView.findViewById(R.id.textConversationType);
            textTime = itemView.findViewById(R.id.textConversationTime);
            textUnreadBadge = itemView.findViewById(R.id.textConversationUnreadBadge);
        }
    }
}
