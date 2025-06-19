package com.example.byway;

public class CommentData {
    public String userName;
    public String content;
    public String emoji;
    public String commentId;
    public String userId;
    public String spotId;

    public CommentData() {} // Firestore용 기본 생성자

    public CommentData(String userName, String content, String emoji, String commentId, String userId, String spotId) {
        this.userName = userName;
        this.content = content;
        this.emoji = emoji;
        this.commentId = commentId;
        this.userId = userId;
        this.spotId = spotId;
    }
}
