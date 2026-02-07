package com.example.front.controllers;

import com.example.front.models.Comment;
import com.example.front.models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class CommentsPopupController {

    @FXML
    private Label productNameLabel;
    @FXML
    private VBox commentsList;
    @FXML
    private TextArea commentInput;

    private Product product;

    public void setProduct(Product product) {
        this.product = product;
        productNameLabel.setText(product.getName());
        refreshComments();
    }

    private void refreshComments() {
        commentsList.getChildren().clear();
        for (Comment comment : product.getComments()) {
            commentsList.getChildren().add(createCommentView(comment));
        }

        if (product.getComments().isEmpty()) {
            Label placeholder = new Label("Soyez le premier à commenter !");
            placeholder.setStyle("-fx-text-fill: #999999; -fx-font-style: italic; -fx-padding: 10;");
            commentsList.getChildren().add(placeholder);
        }
    }

    private VBox createCommentView(Comment comment) {
        VBox box = new VBox(5);
        box.setStyle(
                "-fx-background-color: #F8F9FA; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #EEEEEE; -fx-border-radius: 5;");

        Label author = new Label(comment.getAuthor());
        author.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A2332; -fx-font-size: 13px;");

        Label date = new Label(comment.getPostedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.setStyle("-fx-font-size: 10px; -fx-text-fill: #999999;");

        Label content = new Label(comment.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #333333;");

        box.getChildren().addAll(author, date, content);
        return box;
    }

    @FXML
    private void handleSubmitComment() {
        String text = commentInput.getText().trim();
        if (text.isEmpty() || product == null)
            return;

        // Ajouter le commentaire (Simulé User)
        Comment newComment = new Comment((int) (Math.random() * 10000), text, "Client Visiteur");
        product.addComment(newComment);

        refreshComments();
        commentInput.clear();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) commentInput.getScene().getWindow();
        stage.close();
    }
}
