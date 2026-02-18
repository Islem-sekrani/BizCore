package com.example.front.controllers;

import com.example.front.models.Article;
import com.example.front.models.Comment;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.time.format.DateTimeFormatter;

public class ArticleDetailController {

    @FXML
    private ImageView articleImage;
    @FXML
    private Label articleTitle;
    @FXML
    private Label articleAuthor;
    @FXML
    private Label articleDate;
    @FXML
    private Label articleContent;

    @FXML
    private TextArea newCommentField;
    @FXML
    private VBox commentsContainer;

    private Article currentArticle;

    public void setArticleData(Article article) {
        this.currentArticle = article;

        // Populate Article Details
        articleTitle.setText(article.getTitle());
        articleAuthor.setText("Par " + article.getAuthor());
        articleContent.setText(article.getContent());

        if (article.getPublishedDate() != null) {
            articleDate.setText(
                    "Le " + article.getPublishedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            try {
                articleImage.setImage(new Image(article.getImageUrl(), true));
            } catch (Exception e) {
                // Ignore image error
            }
        } else {
            articleImage.setVisible(false);
            articleImage.setManaged(false);
        }

        // Populate Comments
        refreshComments();
    }

    private void refreshComments() {
        commentsContainer.getChildren().clear();
        if (currentArticle.getComments() != null) {
            for (Comment comment : currentArticle.getComments()) {
                commentsContainer.getChildren().add(createCommentView(comment));
            }
        }
    }

    private VBox createCommentView(Comment comment) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8;");

        Label authorLabel = new Label(comment.getAuthor());
        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A2332;");

        Label dateLabel = new Label(comment.getPostedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #333333;");

        card.getChildren().addAll(authorLabel, dateLabel, contentLabel);
        return card;
    }

    @FXML
    private void handleAddComment() {
        String content = newCommentField.getText().trim();
        if (content.isEmpty() || currentArticle == null)
            return;

        // Create new comment (simulated ID and Author)
        int newId = (int) (Math.random() * 1000);
        Comment newComment = new Comment(newId, content, "Utilisateur Actuel", currentArticle);

        // Add to model
        currentArticle.addComment(newComment);

        // Update UI
        refreshComments();
        newCommentField.clear();
    }
}
