package com.university.unify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.university.unify.R;
import com.university.unify.model.StudentParticipantModel;
import com.university.unify.utils.ImageLoaderUtil;

import java.util.List;

public class StudentParticipantAdapter extends RecyclerView.Adapter<StudentParticipantAdapter.ParticipantViewHolder> {

    public interface OnParticipantActionListener {
        void onChatClicked(StudentParticipantModel participant);
    }

    private final Context context;
    private final List<StudentParticipantModel> participants;
    private final OnParticipantActionListener listener;

    public StudentParticipantAdapter(Context context,
                                     List<StudentParticipantModel> participants,
                                     OnParticipantActionListener listener) {
        this.context = context;
        this.participants = participants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        final StudentParticipantModel participant = participants.get(position);

        String fullName = display(participant.getFullName());
        String email = display(participant.getEmail());
        String studyYear = display(participant.getStudyYear());
        String major = display(participant.getMajorName());
        String avatarUrl = participant.getProfileImageUrl();

        holder.textParticipantName.setText(fullName);
        holder.textParticipantEmail.setText(email);

        holder.textParticipantMeta.setText(context.getString(
                R.string.year_major_value,
                studyYear,
                major
        ));

        ImageLoaderUtil.bindAvatar(
                context,
                holder.imageParticipantAvatar,
                holder.textParticipantInitial,
                fullName,
                avatarUrl
        );

        holder.buttonChatParticipant.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onChatClicked(participant);
            }
        });

        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onChatClicked(participant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return participants == null ? 0 : participants.size();
    }

    private String display(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return context.getString(R.string.not_available_short);
        }
        return value.trim();
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {

        ImageView imageParticipantAvatar;
        TextView textParticipantInitial;
        TextView textParticipantName;
        TextView textParticipantEmail;
        TextView textParticipantMeta;
        MaterialButton buttonChatParticipant;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);

            imageParticipantAvatar = itemView.findViewById(R.id.imageParticipantAvatar);
            textParticipantInitial = itemView.findViewById(R.id.textParticipantInitial);
            textParticipantName = itemView.findViewById(R.id.textParticipantName);
            textParticipantEmail = itemView.findViewById(R.id.textParticipantEmail);
            textParticipantMeta = itemView.findViewById(R.id.textParticipantMeta);
            buttonChatParticipant = itemView.findViewById(R.id.buttonChatParticipant);
        }
    }
}
