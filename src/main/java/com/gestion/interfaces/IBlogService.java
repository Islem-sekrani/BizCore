package com.gestion.interfaces;

import com.gestion.entities.Blog;
import java.util.List;

public interface IBlogService {
    List<Blog> getAllArticles();
    boolean addArticle(Blog article);
    boolean updateArticle(Blog article);
    boolean deleteArticle(int idArticle);
    Blog getArticleById(int idArticle);
}