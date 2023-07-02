package tLogistic.Repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tLogistic.models.Article;
import tLogistic.models.Client;

import java.util.List;

@Repository
public interface ArticleRepository extends CrudRepository<Article,Long> {
    List<Article> findByArticle(String article);
    List<Article> findAllByArticleAndClient(String article, Client client);

    Iterable<Article> findAllByClient(Client client);
}
