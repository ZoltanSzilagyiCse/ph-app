package hu.sherad.hos.data.models;

import java.io.Serializable;
import java.util.Date;

public class TopicRear implements Serializable {

    private static final long serialVersionUID = 1L;
    private String title;
    private Date pubDate;
    private String description;
    private String photoURL;
    private String topicLink;
    private String topicRearLink;
    private int commentCount;

    public TopicRear() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getTopicLink() {
        return topicLink;
    }

    public void setTopicLink(String topicLink) {
        this.topicLink = topicLink;
    }

    public String getTopicRearLink() {
        return topicRearLink;
    }

    public void setTopicRearLink(String topicRearLink) {
        this.topicRearLink = topicRearLink;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

}
