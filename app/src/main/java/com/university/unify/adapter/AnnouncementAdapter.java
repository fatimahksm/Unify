package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.university.unify.R;
import com.university.unify.model.AnnouncementModel;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    public interface AnnouncementActionListener {
        void onDeleteClicked(int position, String announcementId);
    }

    private final Context context;
    private final List<AnnouncementModel> announcements;
    private final AnnouncementActionListener listener;

    public AnnouncementAdapter(Context context, List<AnnouncementModel> announcements, AnnouncementActionListener listener) {
        this.context = context;
        this.announcements = announcements;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AnnouncementViewHolder holder, int position) {
        AnnouncementModel item = announcements.get(position);

        holder.textTitle.setText(safe(item.getTitle()).isEmpty() ? "-" : safe(item.getTitle()));
        holder.textBody.setText(safe(item.getBody()).isEmpty() ? "-" : safe(item.getBody()));

        String author = safe(item.getCreatedByName());
        String date = safe(item.getCreatedAt());

        if (author.isEmpty()) {
            author = context.getString(R.string.instructor);
        }

        holder.textMeta.setText(author + " • " + date);
        holder.textPinned.setVisibility(item.isPinned() ? View.VISIBLE : View.GONE);

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();

                if (listener != null
                        && adapterPosition != RecyclerView.NO_POSITION
                        && adapterPosition < announcements.size()) {

                    AnnouncementModel selected = announcements.get(adapterPosition);
                    listener.onDeleteClicked(adapterPosition, safe(selected.getAnnouncementId()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        TextView textPinned;
        TextView textTitle;
        TextView textBody;
        TextView textMeta;
        MaterialButton buttonDelete;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);

            textPinned = itemView.findViewById(R.id.textAnnouncementPinned);
            textTitle = itemView.findViewById(R.id.textAnnouncementTitle);
            textBody = itemView.findViewById(R.id.textAnnouncementBody);
            textMeta = itemView.findViewById(R.id.textAnnouncementMeta);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteAnnouncement);
        }
    }

    private String safe(String value) {
        return value == null || value.equals("null") ? "" : value.trim();
    }
}