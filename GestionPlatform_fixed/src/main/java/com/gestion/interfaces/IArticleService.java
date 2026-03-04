package com.gestion.interfaces;

import com.gestion.entities.Article;
import java.util.List;

public interface IArticleService {
    List<Article> getAllArticles();
    boolean addArticle(Article article);
    boolean updateArticle(Article article);
    boolean deleteArticle(int idArticle);
    Article getArticleById(int idArticle);
}
