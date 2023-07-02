package tLogistic.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "text", nullable = false)
    private String article;
    @Column(columnDefinition = "mediumtext", nullable = false)
    private String description;
    @Column(columnDefinition = "text", nullable = false)
    private String code;
    @Column(columnDefinition = "mediumtext", nullable = false)
    private String supportingDoc;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "article")
    private List<Image> images = new ArrayList<>();
    private Long imageId;
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Client client;
    private LocalDateTime dateOfCreated;

    @PrePersist
    private void init(){
        dateOfCreated = LocalDateTime.now();
    }

    public Article(String article, String description, String code, String supportingDoc) {
        this.article = article;
        this.description = description;
        this.code = code;
        this.supportingDoc = supportingDoc;
    }
    public void addImageToArticle(Image image){
        image.setArticle(this);
        images.add(image);
        imageId = image.getId();
    }
}
