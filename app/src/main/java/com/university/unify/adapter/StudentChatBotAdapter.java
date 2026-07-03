package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.ChatBotMessageModel;

import java.util.List;

public class StudentChatBotAdapter extends RecyclerView.Adapter<StudentChatBotAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatBotMessageModel> messages;

    public StudentChatBotAdapter(Context context, List<ChatBotMessageModel> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chatbot_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatBotMessageModel item = messages.get(position);

        holder.textMessage.setText(item.getMessage());

        if (item.getType() == ChatBotMessageModel.TYPE_USER) {
            holder.containerRoot.setGravity(android.view.Gravity.END);
            holder.textMessage.setBackgroundResource(R.drawable.bg_chat_user);
            holder.textMessage.setTextColor(context.getColor(R.color.unify_text_on_dark));
        } else {
            holder.containerRoot.setGravity(android.view.Gravity.START);
            holder.textMessage.setBackgroundResource(R.drawable.bg_chat_bot);
            holder.textMessage.setTextColor(context.getColor(R.color.unify_text_primary));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout containerRoot;
        TextView textMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            containerRoot = itemView.findViewById(R.id.containerChatMessageRoot);
            textMessage = itemView.findViewById(R.id.textChatMessage);
        }
    }
}