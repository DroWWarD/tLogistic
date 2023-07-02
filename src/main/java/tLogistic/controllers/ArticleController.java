package tLogistic.controllers;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tLogistic.services.ArticleService;

import java.io.IOException;

@Controller
public class ArticleController {
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/addArticle")
    public String addArticle(Model model) {
        return articleService.showAddForm(model);
    }

    @GetMapping("/addArticleXLSX")
    public String addArticleXlsx(Model model) {
        return articleService.showAddXlsxForm(model);
    }

    @PostMapping("/addArticleXLSX")
    public String parseXLSX(@RequestParam(required = false) Long clientId,
                            @RequestParam("file") MultipartFile file,
                            Model model) throws IOException, InvalidFormatException {
        return articleService.parseXLSX(clientId ,file, model);
    }

    @PostMapping("/addArticle")
    public String editArticle(@RequestParam String clientName,
                              @RequestParam String article,
                              @RequestParam String description,
                              @RequestParam String code,
                              @RequestParam String supportingDoc,
                              @RequestParam (required = false) MultipartFile file,
                              Model model) throws IOException {
        return articleService.addArticle(article, description, code, supportingDoc, file, clientName);
    }

    @GetMapping("/articleDetails/{id}")
    public String articleDetails(@PathVariable(value = "id") Long id, Model model) {
        return articleService.showDetails(id, model);
    }
    @GetMapping("/articleDetails/{id}/edit")
    public String articleEditSearch(@PathVariable(value = "id") Long id, Model model) {
        return articleService.findEdit(id, model);
    }
    @PostMapping("/articleDetails/{id}/edit")
    public String articleEdit(@PathVariable(value = "id") Long id,
                              @RequestParam String description,
                              @RequestParam String code,
                              @RequestParam String supportingDoc,
                              Model model) {
        return articleService.edit(id, description, code, supportingDoc, model);
    }
    @GetMapping("/articleDetails/{id}/remove")
    public String articleRemoveSearch(@PathVariable(value = "id") Long id, Model model) {
        return articleService.findRemove(id, model);
    }
    @PostMapping("/articleDetails/{id}/remove")
    public String articleRemove(@PathVariable(value = "id") Long id, @RequestParam String article, Model model) {
        return articleService.removeArticle(id, article, model);
    }

    @GetMapping("/findArticles")
    public String showPageFindArticles(Model model) {
        return articleService.showFindForm(model);
    }

}
