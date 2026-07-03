package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.university.unify.R;
import com.university.unify.model.StudentPostModel;
import com.university.unify.utils.ImageLoaderUtil;

import java.util.List;

public class StudentPostAdapter extends RecyclerView.Adapter<StudentPostAdapter.PostViewHolder> {

    public interface OnPostActionListener {
        void onLikeClicked(StudentPostModel post);
        void onCommentClicked(StudentPostModel post);
    }

    private final Context context;
    private final List<StudentPostModel> posts;
    private final OnPostActionListener listener;

    public StudentPostAdapter(Context context,
                              List<StudentPostModel> posts,
                              OnPostActionListener listener) {
        this.context = context;
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        StudentPostModel post = posts.get(position);

        String name = safe(post.getFullName());
        String content = safe(post.getContent());
        String createdAt = safe(post.getCreatedAt());
        String likes = safe(post.getLikesCount());
        String comments = safe(post.getCommentsCount());
        String postImageUrl = safe(post.getImageUrl());
        String authorAvatarUrl = safe(post.getProfileImageUrl());

        if (name.isEmpty()) name = context.getString(R.string.unknown_student);
        if (likes.isEmpty()) likes = "0";
        if (comments.isEmpty()) comments = "0";

        holder.textPostAuthor.setText(name);
        holder.textPostContent.setText(content.isEmpty() ? "-" : content);
        holder.textPostDate.setText(createdAt.isEmpty() ? "-" : createdAt);
        holder.textPostStats.setText(
                context.getString(R.string.post_stats_value, likes, comments)
        );

        // Author avatar (photo or initial)
        ImageLoaderUtil.bindAvatar(
                context,
                holder.imagePostAvatar,
                holder.textPostAvatar,
                name,
                authorAvatarUrl
        );

        if (post.isLikedByMe()) {
            holder.buttonLikePost.setText(context.getString(R.string.liked));
            holder.buttonLikePost.setTextColor(ContextCompat.getColor(context, R.color.unify_primary));
        } else {
            holder.buttonLikePost.setText(context.getString(R.string.like));
            holder.buttonLikePost.setTextColor(ContextCompat.getColor(context, R.color.unify_text_secondary));
        }

        ImageLoaderUtil.loadImage(context, holder.imagePost, postImageUrl, true);

        holder.buttonLikePost.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClicked(post);
        });

        holder.buttonCommentPost.setOnClickListener(v -> {
            if (listener != null) listener.onCommentClicked(post);
        });
    }

    @Override
    public int getItemCount() {
        return posts == null ? 0 : posts.size();
    }

    private String safe(String value) {
        if (value == null || value.equalsIgnoreCase("null")) return "";
        return value.trim();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView imagePostAvatar;
        TextView textPostAvatar;
        TextView textPostAuthor;
        TextView textPostDate;
        TextView textPostContent;
        ImageView imagePost;
        TextView textPostStats;
        TextView buttonLikePost;
        TextView buttonCommentPost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePostAvatar = itemView.findViewById(R.id.imagePostAvatar);
            textPostAvatar = itemView.findViewById(R.id.textPostAvatar);
            textPostAuthor = itemView.findViewById(R.id.textPostAuthor);
            textPostDate = itemView.findViewById(R.id.textPostDate);
            textPostContent = itemView.findViewById(R.id.textPostContent);
            imagePost = itemView.findViewById(R.id.imagePost);
            textPostStats = itemView.findViewById(R.id.textPostStats);
            buttonLikePost = itemView.findViewById(R.id.buttonLikePost);
            buttonCommentPost = itemView.findViewById(R.id.buttonCommentPost);
        }
    }
}
