package hu.sherad.hos.data.models;

import android.support.annotation.NonNull;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.utils.io.Logger;

public final class TopicComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /* Author properties */
    private String authorName;
    private String authorURL;
    private String authorAvatarURL;
    private String authorSignature;
    private String authorRank;
    /* Quoted properties */
    private String quotedName;
    private String quotedURL;
    /* Comment properties */
    private int ID;
    private int ID_quoted;
    private String ID_URL;
    private String ID_URL_quoted;
    private String privateURL;
    private String newURL;
    private String replyURL;
    private String edited;
    private Date date = new Date();
    private String content;
    private boolean off;
    private String editURL;
    /* Moderator properties */
    private String moderatorOffONURL;
    private String moderatorDeleteURL;
    private String moderatorEditURL;

    @NonNull
    public String getAuthorRank() {
        return authorRank == null ? "" : authorRank;
    }

    public void setAuthorRank(String authorRank) {
        this.authorRank = authorRank;
    }

    @NonNull
    public String getAuthorName() {
        return authorName == null ? "" : authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @NonNull
    public String getAuthorURL() {
        return authorURL == null ? "" : authorURL;
    }

    public void setAuthorURL(String authorURL) {
        this.authorURL = authorURL;
    }

    @NonNull
    public String getAuthorAvatarURL() {
        return authorAvatarURL == null ? "" : authorAvatarURL;
    }

    public void setAuthorAvatarURL(String authorAvatarURL) {
        this.authorAvatarURL = authorAvatarURL;
    }

    @NonNull
    public String getAuthorSignature() {
        return authorSignature == null ? "" : authorSignature;
    }

    public void setAuthorSignature(String authorSignature) {
        this.authorSignature = authorSignature;
    }

    @NonNull
    public String getQuotedName() {
        return quotedName == null ? "" : quotedName;
    }

    public void setQuotedName(String quotedName) {
        this.quotedName = quotedName;
    }

    @NonNull
    public String getQuotedURL() {
        return quotedURL == null ? "" : quotedURL;
    }

    public void setQuotedURL(String quotedURL) {
        this.quotedURL = quotedURL;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID_quoted() {
        return ID_quoted;
    }

    public void setID_quoted(int ID_quoted) {
        this.ID_quoted = ID_quoted;
    }

    @NonNull
    public String getID_URL() {
        return ID_URL == null ? "" : ID_URL;
    }

    public void setID_URL(String ID_URL) {
        this.ID_URL = ID_URL;
    }

    @NonNull
    public String getID_URL_quoted() {
        return ID_URL_quoted == null ? "" : ID_URL_quoted;
    }

    public void setID_URL_quoted(String ID_URL_quoted) {
        this.ID_URL_quoted = ID_URL_quoted;
    }

    @NonNull
    public String getPrivateURL() {
        return privateURL == null ? "" : privateURL;
    }

    public void setPrivateURL(String privateURL) {
        this.privateURL = privateURL;
    }

    @NonNull
    public String getNewURL() {
        return newURL == null ? "" : newURL;
    }

    public void setNewURL(String newURL) {
        this.newURL = newURL;
    }

    @NonNull
    public String getReplyURL() {
        return replyURL == null ? "" : replyURL;
    }

    public void setReplyURL(String replyURL) {
        this.replyURL = replyURL;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    @NonNull
    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isOff() {
        return off;
    }

    public void setOff(boolean off) {
        this.off = off;
    }

    @NonNull
    public String getEdited() {
        return edited == null ? "" : edited;
    }

    public void setEdited(String edited) {
        this.edited = edited;
    }

    @NonNull
    public String getEditURL() {
        return editURL == null ? "" : editURL;
    }

    public void setEditURL(String editURL) {
        this.editURL = editURL;
    }

    @NonNull
    public String getModeratorOffONURL() {
        return moderatorOffONURL == null ? "" : moderatorOffONURL;
    }

    public void setModeratorOffONURL(String moderatorOffONURL) {
        this.moderatorOffONURL = moderatorOffONURL;
    }

    @NonNull
    public String getModeratorDeleteURL() {
        return moderatorDeleteURL == null ? "" : moderatorDeleteURL;
    }

    public void setModeratorDeleteURL(String moderatorDeleteURL) {
        this.moderatorDeleteURL = moderatorDeleteURL;
    }

    @NonNull
    public String getModeratorEditURL() {
        return moderatorEditURL == null ? "" : moderatorEditURL;
    }

    public void setModeratorEditURL(String moderatorEditURL) {
        this.moderatorEditURL = moderatorEditURL;
    }

    public static final class Utils {

        public static final Map<String, Integer> allSmiles = getAllSmiles();

        public static void sort(List<TopicComment> comments, boolean isListInc) {
            int multiplier = isListInc ? 1 : -1;
            Collections.sort(comments, (c1, c2) -> {
                long time1 = c1.getDate().getTime();
                long time2 = c2.getDate().getTime();
                return multiplier * (Long.compare(time1, time2));
            });
        }

        private static Map<String, Integer> getAllSmiles() {
            Map<String, Integer> list = new HashMap<>();
            list.put("/dl/s/n1.gif", R.drawable.smiley_n1);
            list.put("/dl/s/n2.gif", R.drawable.smiley_n2);
            list.put("/dl/s/d1.gif", R.drawable.smiley_d1);
            list.put("/dl/s/d2.gif", R.drawable.smiley_d2);
            list.put("/dl/s/d3.gif", R.drawable.smiley_d3);
            list.put("/dl/s/nn.gif", R.drawable.smiley_nn);
            list.put("/dl/s/m1.gif", R.drawable.smiley_m1);
            list.put("/dl/s/m2.gif", R.drawable.smiley_m2);
            list.put("/dl/s/m3.gif", R.drawable.smiley_m3);
            list.put("/dl/s/os.gif", R.drawable.smiley_os);
            list.put("/dl/s/ol.gif", R.drawable.smiley_ol);
            list.put("/dl/s/v1.gif", R.drawable.smiley_v1);
            list.put("/dl/s/ts.gif", R.drawable.smiley_ts);
            list.put("/dl/s/pl.gif", R.drawable.smiley_pl);
            list.put("/dl/s/bl.gif", R.drawable.smiley_bl);
            list.put("/dl/s/ye.gif", R.drawable.smiley_ye);
            list.put("/dl/s/ul.gif", R.drawable.smiley_ul);
            list.put("/dl/s/cl.gif", R.drawable.smiley_cl);
            list.put("/dl/s/fl.gif", R.drawable.smiley_fl);
            list.put("/dl/s/yk.gif", R.drawable.smiley_yk);
            list.put("/dl/s/rl.gif", R.drawable.smiley_rl);
            list.put("/dl/s/wb.gif", R.drawable.smiley_wb);
            return list;
        }

        /**
         * This will remove all the HTML tags, and replace them with the PH editing tags.
         * Note: this will remove the attrs like - tar, tal, tac, taj -.
         * Also when we load the comment/message we replace all 'img' tags to 'href', so the images
         * won't load in the browser. Instead of they will be a link to the image.
         * Feel free, and fix these if you want.
         */
        public static String getContentToEdit(String editableContent) {
            editableContent = editableContent
                    // Off content
                    .replace("<font color=\"#757575\">", "[OFF]")
                    .replace("</font>", "[/OFF]")
                    // Replace smiles
                    .replace("<img src=\"/dl/s/n1.gif\" alt=\":)\">", " :)")
                    .replace("<img src=\"/dl/s/n2.gif\" alt=\":))\">", " :))")
                    .replace("<img src=\"/dl/s/d1.gif\" alt=\":D\">", " :D")
                    .replace("<img src=\"/dl/s/d2.gif\" alt=\":DD\">", " :DD")
                    .replace("<img src=\"/dl/s/d3.gif\" alt=\":DDD\">", " :DDD")
                    .replace("<img src=\"/dl/s/nn.gif\" alt=\":N\">", " :N")
                    .replace("<img src=\"/dl/s/m1.gif\" alt=\":(\">", " :(")
                    .replace("<img src=\"/dl/s/m2.gif\" alt=\":((\">", " :((")
                    .replace("<img src=\"/dl/s/m3.gif\" alt=\":(((\">", " :(((")
                    .replace("<img src=\"/dl/s/os.gif\" alt=\":o\">", " :o")
                    .replace("<img src=\"/dl/s/ol.gif\" alt=\":O\">", " :O")
                    .replace("<img src=\"/dl/s/v1.gif\" alt=\";]\">", " ;]")
                    .replace("<img src=\"/dl/s/ts.gif\" alt=\":P\">", " :P")
                    .replace("<img src=\"/dl/s/pl.gif\" alt=\";)\">", " ;)")
                    .replace("<img src=\"/dl/s/bl.gif\" alt=\":B\">", " :B")
                    .replace("<img src=\"/dl/s/ye.gif\" alt=\":K\">", " :K")
                    .replace("<img src=\"/dl/s/ul.gif\" alt=\":U\">", " :U")
                    .replace("<img src=\"/dl/s/cl.gif\" alt=\":C\">", " :C")
                    .replace("<img src=\"/dl/s/fl.gif\" alt=\":F\">", " :F")
                    .replace("<img src=\"/dl/s/yk.gif\" alt=\":Y\">", " :Y")
                    .replace("<img src=\"/dl/s/rl.gif\" alt=\":R\">", " :R")
                    .replace("<img src=\"/dl/s/wb.gif\" alt=\":W\">", " :W")
                    // Bold
                    .replace("<b>", "[B]")
                    .replace("</b>", "[/B]")
                    // Italic
                    .replace("<i>", "[I]")
                    .replace("</i>", "[/I]")
                    // Underline
                    .replace("<u>", "[U]")
                    .replace("</u>", "[/U]")
                    // Strike Through
                    .replace("<s>", "[S]")
                    .replace("</s>", "[/S]")
                    // Code
                    .replace("<code>", "[CODE]")
                    .replace("</code>", "[/CODE]")
                    // Monospace
                    .replace("<tt>", "[M]")
                    .replace("</tt>", "[/M]")
                    // Special characters
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");
            // Replace links, images
            StringBuilder content = new StringBuilder();
            String[] lines = editableContent.split("<br>");
            for (String line : lines) {

                int startIndexLink = line.indexOf("<a href=\"");
                if (startIndexLink == -1) {
                    // If there is no link, then append the sh_line
                    content.append(line);
                } else {
                    int count = 0;
                    while (startIndexLink != -1) {
                        int endIndexLink = line.indexOf("</a>", startIndexLink);
                        // +9: <a href="
                        String link = line.substring(startIndexLink + 9, line.indexOf("target=\"", startIndexLink) - 2);

                        // Text before the link
                        content.append(line, count, startIndexLink);
                        // The link, and the foreground text
                        content.append("[LINK:\"").append(link).append("\"]").append(line, line.indexOf("\">", startIndexLink) + 2, endIndexLink).append("[/LINK]");

                        startIndexLink = line.indexOf("<a href=\"", endIndexLink);
                        if (startIndexLink == -1) {
                            // If there is no link after this one, add the remaining text
                            content.append(line, endIndexLink + 4, line.length());
                        } else {
                            // If there is link after this one, add the text before that link
                            content.append(line, endIndexLink + 4, startIndexLink);
                            count = startIndexLink;
                        }
                    }
                }
                content.append("\n");
            }
            content = new StringBuilder(content.toString().replace("<br>", "\n"));
            return content.toString().trim();
        }

        /**
         * Order matters!!
         */
        public static String getContentToPublish(String editableContent) {
            Logger.getLogger().i("Content before modification");
            Logger.getLogger().i(editableContent);
            editableContent = editableContent
                    // Special characters
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    // Replace smiles
                    .replace(" :))", "<img src=\"/dl/s/n2.gif\" alt=\":))\"/>")
                    .replace(" :)", "<img src=\"/dl/s/n1.gif\" alt=\":)\"/>")
                    .replace(" :DDD", "<img src=\"/dl/s/d3.gif\" alt=\":DDD\"/>")
                    .replace(" :DD", "<img src=\"/dl/s/d2.gif\" alt=\":DD\"/>")
                    .replace(" :D", "<img src=\"/dl/s/d1.gif\" alt=\":D\"/>")
                    .replace(" :N", "<img src=\"/dl/s/nn.gif\" alt=\":N\"/>")
                    .replace(" :(((", "<img src=\"/dl/s/m3.gif\" alt=\":(((\"/>")
                    .replace(" :((", "<img src=\"/dl/s/m2.gif\" alt=\":((\"/>")
                    .replace(" :(", "<img src=\"/dl/s/m1.gif\" alt=\":(\"/>")
                    .replace(" :o", "<img src=\"/dl/s/os.gif\" alt=\":o\"/>")
                    .replace(" :O", "<img src=\"/dl/s/ol.gif\" alt=\":O\"/>")
                    .replace(" ;]", "<img src=\"/dl/s/v1.gif\" alt=\";]\"/>")
                    .replace(" :P", "<img src=\"/dl/s/ts.gif\" alt=\":P\"/>")
                    .replace(" ;)", "<img src=\"/dl/s/pl.gif\" alt=\";)\"/>")
                    .replace(" :B", "<img src=\"/dl/s/bl.gif\" alt=\":B\"/>")
                    .replace(" :K", "<img src=\"/dl/s/ye.gif\" alt=\":K\"/>")
                    .replace(" :U", "<img src=\"/dl/s/ul.gif\" alt=\":U\"/>")
                    .replace(" :C", "<img src=\"/dl/s/cl.gif\" alt=\":C\"/>")
                    .replace(" :F", "<img src=\"/dl/s/fl.gif\" alt=\":F\"/>")
                    .replace(" :Y", "<img src=\"/dl/s/yk.gif\" alt=\":Y\"/>")
                    .replace(" :R", "<img src=\"/dl/s/rl.gif\" alt=\":R\"/>")
                    .replace(" :W", "<img src=\"/dl/s/wb.gif\" alt=\":W\"/>")
                    // Replace images
                    .replace("[IMG:", "<img src=")
                    .replace("/]", "/>")
                    // Off content
                    .replace("[OFF]", "<small>")
                    .replace("[/OFF]", "</small>")
                    // Bold
                    .replace("[B]", "<b>")
                    .replace("[/B]", "</b>")
                    // Italic
                    .replace("[I]", "<i>")
                    .replace("[/I]", "</i>")
                    // Underline
                    .replace("[U]", "<u>")
                    .replace("[/U]", "</u>")
                    // Strike Through
                    .replace("[S]", "<s>")
                    .replace("[/S]", "</s>")
                    // Code
                    .replace("[CODE]", "<code>")
                    .replace("[/CODE]", "</code>")
                    // Monospace
                    .replace("[M]", "<tt>")
                    .replace("[/M]", "</tt>")
                    // New lines
                    .replace("\n", "</p><p>");
            // Replace links, images
            StringBuilder stringBuilder = new StringBuilder(editableContent);
            int startIndex = stringBuilder.indexOf("[LINK:");
            // Links end "]" character replace to ">"
            while (startIndex != -1) {
                int endIndex = editableContent.indexOf("]", startIndex);
                stringBuilder.replace(endIndex, endIndex + 1, ">");
                startIndex = editableContent.indexOf("[LINK:", endIndex);
            }
            editableContent = stringBuilder.toString()
                    .replace("[LINK:", "<a target=\"_blank\" href=")
                    .replace("[/LINK]", "</a>");
            // Finally wrap it inside <p> and </p>
            editableContent = "<p>" + editableContent + "</p>";
            Logger.getLogger().i("Content after modification");
            Logger.getLogger().i(editableContent);
            return editableContent;
        }

        public static String getContentToDisplay(TopicComment comment) {
            return comment.getContent() + (comment.getEdited().isEmpty() ? "" : ("<i>" + comment.getEdited() + "</i>"));
        }

        /**
         * Get the content without img tags, signature and emojis from a comment or a message
         */
        public static String getContentFromCommentElement(Elements elements, TopicComment comment) {
            StringBuilder content = new StringBuilder();
            for (Element element : elements) {
                // Moderator lines
                if (!element.className().equals("modlinks")) {
                    // Handle sections with multiple lines (mgt0 -> 1 line, mgt1 -> 2 lines)
                    if (element.classNames().contains("mgt0")) {
                        content.append("<br>");
                    } else if (element.classNames().contains("mgt1")) {
                        content.append("<br><br>");
                    }
                    element.classNames(Collections.emptySet());
                    content.append(element.toString());
                }
            }
            // Remove all <p></p>
            content = new StringBuilder(content.toString()
                    .replace("<p>", "")
                    .replace("</p>", ""));
            // Replace small tags with grey (secondary_text from xml) color.. it's "offtopic"
            content = new StringBuilder(content.toString()
                    .replaceAll("<small>", "<font color=\"#757575\">")
                    .replaceAll("</small>", "</font>")
                    .replaceAll("<a href=\"/dl/upc/", "<a href=\"https://prohardver.hu/dl/upc/")
                    .replaceAll("<img src=\"/dl/upc/", "<img src=\"https://prohardver.hu/dl/upc/"));
            // Remove the img tags inside the href
            int startIndex = content.indexOf("<a href=", 0);
            while (startIndex != -1) {
                int endIndex = content.indexOf(">", startIndex) + 1;

                if (content.length() > endIndex + 10) {
                    if (content.substring(endIndex, endIndex + 9).equals("<img src=")) {
                        content.replace(endIndex, content.indexOf(">", endIndex) + 1, "");
                        content.insert(endIndex, "kép link");
                    }
                }
                startIndex = content.indexOf("<a href=", startIndex + 1);
            }
            // Replace img tags with links
            startIndex = content.indexOf("<img src=", 0);
            while (startIndex != -1) {
                int endIndex = content.indexOf(">", startIndex);
                String link = content.substring(startIndex, endIndex + 1);
                String onlyTheLink = link.substring(10, link.indexOf("\"", 10));
                if (!allSmiles.containsKey(onlyTheLink)) {
                    // Not smile, real link
                    if (onlyTheLink.contains("https") || onlyTheLink.contains("http")) {
                        String extension = onlyTheLink.substring(onlyTheLink.length() - 4);
                        if (onlyTheLink.endsWith(".sh_thumb" + extension)) {
                            onlyTheLink = onlyTheLink.substring(0, onlyTheLink.length() - 10);
                            onlyTheLink += extension;
                        }
                        content = new StringBuilder(content.toString().replace(link, "<a href=\"" + onlyTheLink + "\" target=\"_blank\" rel=\"nofollow\">kép link</a>"));
                    }
                }
                startIndex = content.indexOf("<img src=", startIndex + 1);
            }
            return content.toString();
        }

        /**
         * Creating a {@link TopicComment}. Be careful, this only works with <b>messages</b>
         */
        public static TopicComment getMessageFromHTML(Element element) {
            TopicComment comment = new TopicComment();
            // Head
            Element commentHead = element.selectFirst("div.card-header");
            // Body
            Element commentBody = element.selectFirst("div.media-body");
            // Author properties
            Element elementAuthor = commentHead.selectFirst("span.msg-head-author");
            comment.setAuthorName(elementAuthor.selectFirst("a").text());
            comment.setAuthorURL(PH.Api.HOST_PROHARDVER + elementAuthor.selectFirst("a").attr("href"));
            try {
                comment.setAuthorAvatarURL(PH.Api.HOST_PROHARDVER + element.selectFirst("div.msg-user").selectFirst("img").attr("src"));
            } catch (Exception e) {
                comment.setAuthorAvatarURL("");
            }
            // Replier properties
            Element elementReplier = commentHead.selectFirst("span.msg-head-replied");
            comment.setQuotedName(elementReplier.selectFirst("a").text());
            comment.setQuotedURL(PH.Api.HOST_PROHARDVER + elementReplier.selectFirst("a").attr("href"));
            // New comment EXTRA_URL
            if (!commentHead.select("a:contains(Válasz)").isEmpty()) {
                comment.setNewURL(PH.Api.HOST_PROHARDVER + commentHead.select("a:contains(Válasz)").attr("data-action-priv-new"));
                comment.setReplyURL(comment.getNewURL());
            }
            // Modify EXTRA_URL
            if (commentBody.selectFirst("p.modlinks") != null) {
                if (!element.selectFirst("p.modlinks").select("button:contains(Szerkesztés)").isEmpty()) {
                    comment.setEditURL(PH.Api.HOST_PROHARDVER +
                            element.selectFirst("p.modlinks").select("button:contains(Szerkesztés)").attr("data-action-priv-mod"));
                }
            }
            // Signature EXTRA_URL
            if (commentBody.selectFirst("p.msg-sign") != null) {
                comment.setAuthorSignature(commentBody.selectFirst("p.msg-sign").toString());
            }
            // Date
            try {
                comment.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(commentHead.select("time").text()));
            } catch (ParseException e) {
                Logger.getLogger().e(e);
            }
            // Content
            comment.setContent(getContentFromCommentElement(commentBody.selectFirst("div.msg-content").children(), comment));
            return comment;
        }

        /**
         * Creating a {@link TopicComment}. Be careful, this only works with <b>comments</b>
         */
        public static TopicComment getCommentFromHTML(Element element) {
            TopicComment comment = new TopicComment();
            // Head
            Element commentHead = element.selectFirst("div.card-header");
            // Body
            Element commentBody = element.selectFirst("div.media-body");
            // Author properties
            if (commentHead.selectFirst("span.msg-head-author") != null) {
                Element elementAuthor = commentHead.selectFirst("span.msg-head-author");
                comment.setAuthorName(elementAuthor.select("a").last().text());
                comment.setAuthorURL(PH.Api.HOST_PROHARDVER + elementAuthor.select("a").last().attr("href"));
                comment.setID(Integer.parseInt(elementAuthor.selectFirst("a").text().substring(1)));
                comment.setID_URL(PH.Api.HOST_PROHARDVER + elementAuthor.selectFirst("a").attr("href"));
            }
            try {
                comment.setAuthorAvatarURL(PH.Api.HOST_PROHARDVER + element.selectFirst("div.msg-user").selectFirst("img").attr("src"));
            } catch (Exception e) {
                comment.setAuthorAvatarURL("");
            }
            // Replier properties
            if (commentHead.selectFirst("span.msg-head-replied") != null) {
                Element elementReplier = commentHead.selectFirst("span.msg-head-replied");
                comment.setQuotedName(elementReplier.selectFirst("a").text());
                comment.setQuotedURL(PH.Api.HOST_PROHARDVER + elementReplier.selectFirst("a").attr("href"));
                comment.setID_quoted(Integer.parseInt(elementReplier.select("a").last().text().substring(1)));
                comment.setID_URL_quoted(PH.Api.HOST_PROHARDVER + elementReplier.select("a").last().attr("href"));
            }
            // New comment EXTRA_URL
            if (!commentHead.select("a:contains(Új)").isEmpty()) {
                comment.setNewURL(PH.Api.HOST_PROHARDVER + commentHead.select("a:contains(Új)").attr("data-action-msg-new"));
            }
            // Reply EXTRA_URL
            if (!commentHead.select("a:contains(Válasz)").isEmpty()) {
                comment.setReplyURL(PH.Api.HOST_PROHARDVER + commentHead.select("a:contains(Válasz)").attr("data-action-msg-reply"));
            }
            // Private EXTRA_URL
            if (commentHead.selectFirst("span.fas.fa-envelope.fa-fw") != null) {
                comment.setPrivateURL(PH.Api.HOST_PROHARDVER + commentHead.selectFirst("span.fas.fa-envelope.fa-fw").parent().attr("href"));
            }
            // Modify EXTRA_URL
            if (commentBody.selectFirst("p.modlinks") != null) {
                if (!element.selectFirst("p.modlinks").select("button:contains(Szerkesztés)").isEmpty()) {
                    comment.setEditURL(PH.Api.HOST_PROHARDVER +
                            element.selectFirst("p.modlinks").select("button:contains(Szerkesztés)").attr("data-action-msg-mod"));
                }
            }
            // Signature EXTRA_URL
            if (commentBody.selectFirst("p.msg-sign") != null) {
                comment.setAuthorSignature(commentBody.selectFirst("p.msg-sign").toString());
            }
            // Rank
            if (element.selectFirst("p.user-class") != null) {
                comment.setAuthorRank(element.selectFirst("p.user-class").text());
            }
            // Date
            try {
                comment.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(commentHead.select("time").text()));
            } catch (ParseException e) {
                Logger.getLogger().e(e);
            }
            // Moderator properties
            // TODO: 2017. 12. 31. Fix
            /*Element moderatorProperties = commentBody.selectFirst("p.modlinks");
            if (moderatorProperties != null) {
                Element rightModeratorProperties = moderatorProperties.selectFirst("pull-right");
                comment.setModeratorOffONURL(PH.Api.HOST_PROHARDVER + rightModeratorProperties.select("button").attr("data-url"));
            }*/
            // Content
            comment.setContent(getContentFromCommentElement(commentBody.selectFirst("div.msg-content").children(), comment));
            // Off comment?
            comment.setOff(element.className().contains("off"));
            // Edited?
            Element elementModified = commentBody.selectFirst("p.msg-modified");
            if (elementModified != null) {
                comment.setEdited(elementModified.toString().replace("/tag/", "https://prohardver.hu/tag/"));
            }
            return comment;
        }

    }
}
