package hu.sherad.hos.data.models;


import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String items;
    private String signature;
    private String messageLink;
    private String avatarLink;
    private String rank;

    private int commentProfessional;
    private int commentSocial;
    private int commentMarket;
    private int blogCount;

    private Date registered;
    private Date lastVisited;

    @NonNull
    public String getRank() {
        return rank == null ? "" : rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    @NonNull
    public String getName() {
        return name == null ? "-" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    public String getItems() {
        return items == null ? "-" : items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    @NonNull
    public String getSignature() {
        return signature == null ? "-" : signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getCommentProfessional() {
        return commentProfessional;
    }

    public void setCommentProfessional(int commentProfessional) {
        this.commentProfessional = commentProfessional;
    }

    public int getCommentSocial() {
        return commentSocial;
    }

    public void setCommentSocial(int commentSocial) {
        this.commentSocial = commentSocial;
    }

    public int getCommentMarket() {
        return commentMarket;
    }

    public void setCommentMarket(int commentMarket) {
        this.commentMarket = commentMarket;
    }

    public int getBlogCount() {
        return blogCount;
    }

    public void setBlogCount(int blogCount) {
        this.blogCount = blogCount;
    }

    public Date getRegistered() {
        return registered;
    }

    public void setRegistered(Date register) {
        this.registered = register;
    }

    public Date getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(Date lastVisited) {
        this.lastVisited = lastVisited;
    }

    @NonNull
    public String getMessageLink() {
        return messageLink == null ? "" : messageLink;
    }

    public void setMessageLink(String messageLink) {
        this.messageLink = messageLink;
    }

    @NonNull
    public String getAvatarLink() {
        return avatarLink == null ? "" : avatarLink;
    }

    public void setAvatarLink(String avatarLink) {
        this.avatarLink = avatarLink;
    }
}
