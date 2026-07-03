package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.PostCommentModel;
import com.university.unify.utils.ImageLoaderUtil;

import java.util.List;

public class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<PostCommentModel> comments;

    public PostCommentAdapter(Context context, List<PostCommentModel> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        PostCommentModel comment = comments.get(position);

        String name = safe(comment.getFullName());
        String body = safe(comment.getCommentText());
        String date = safe(comment.getCreatedAt());
        String avatarUrl = safe(comment.getProfileImageUrl());

        if (name.isEmpty()) name = context.getString(R.string.unknown_student);

        holder.textCommentAuthor.setText(name);
        holder.textCommentBody.setText(body.isEmpty() ? "-" : body);
        holder.textCommentDate.setText(date.isEmpty() ? "-" : date);

        ImageLoaderUtil.bindAvatar(
                context,
                holder.imageCommentAvatar,
                holder.textCommentAvatar,
                name,
                avatarUrl
        );
    }

    @Override
    public int getItemCount() {
        return comments == null ? 0 : comments.size();
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) return "";
        return value.trim();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView imageCommentAvatar;
        TextView textCommentAvatar;
        TextView textCommentAuthor;
        TextView textCommentBody;
        TextView textCommentDate;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCommentAvatar = itemView.findViewById(R.id.imageCommentAvatar);
            textCommentAvatar = itemView.findViewById(R.id.textCommentAvatar);
            textCommentAuthor = itemView.findViewById(R.id.textCommentAuthor);
            textCommentBody = itemView.findViewById(R.id.textCommentBody);
            textCommentDate = itemView.findViewById(R.id.textCommentDate);
        }
    }
}
