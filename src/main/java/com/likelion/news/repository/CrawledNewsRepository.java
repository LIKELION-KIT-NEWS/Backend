package com.likelion.news.repository;


import com.likelion.news.entity.CrawledNews;
import com.likelion.news.entity.enums.ArticleCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CrawledNewsRepository extends CrudRepository<CrawledNews, Long> {


    @Query("SELECT n FROM CrawledNews n WHERE n.articleCategory = :articleCategory AND n.articleDatetime BETWEEN :startDate AND :endDate")
    List<CrawledNews> findAllByArticleCategoryAndArticleDateIs(
            @Param("articleCategory") ArticleCategory articleCategory,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
