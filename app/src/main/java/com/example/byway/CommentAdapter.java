package com.example.byway;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final List<CommentData> comments;
    private final Context context;
    private final String spotId;

    public CommentAdapter(Context context, List<CommentData> comments, String spotId) {
        this.context = context;
        this.comments = comments;
        this.spotId = spotId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView header, content;
        Button deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.comment_header);
            content = itemView.findViewById(R.id.comment_content);
            deleteBtn = itemView.findViewById(R.id.delete_comment_button);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentData comment = comments.get(position);
        String emojiSymbol = getEmojiSymbol(comment.emoji);
        holder.header.setText(emojiSymbol + " " + comment.userName);
        holder.content.setText(comment.content);

        holder.deleteBtn.setVisibility(View.VISIBLE);
        holder.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("댓글 삭제")
                    .setMessage("정말 삭제하시겠어요?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("spots")
                                .document(comment.spotId)
                                .collection("comments")
                                .document(comment.commentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    comments.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(holder.itemView.getContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });


    }

    private String getEmojiSymbol(String emojiKey) {
        switch (emojiKey) {
            case "happy": return "😊";
            case "neutral": return "😐";
            case "sad": return "😢";
            default: return "";
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}

