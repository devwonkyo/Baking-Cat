package com.example.dnjsr.bakingcat.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostInfo {
    String userId;
    String postId;
    String postImageUrl;
    String postContents;//게시글 내용.
    Boolean postLike = false;
    Map<String,CommentInfo> comments = new HashMap<>();
    List<String> postLikeList = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getPostContents() {
        return postContents;
    }

    public void setPostContents(String postContents) {
        this.postContents = postContents;
    }

    public Map<String, CommentInfo> getComments() {
        return comments;
    }

    public void setComments(Map<String, CommentInfo> comments) {
        this.comments = comments;
    }

    public List<String> getPostLikeList() {
        return postLikeList;
    }

    public void setPostLikeList(List<String> postLikeList) {
        this.postLikeList = postLikeList;
    }

    public Boolean getPostLike() {
        return postLike;
    }

    public void setPostLike(Boolean postLike) {
        this.postLike = postLike;
    }
}
