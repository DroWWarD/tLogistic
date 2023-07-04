package tLogistic.services.implementations;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import tLogistic.repositories.ArticleRepository;
import tLogistic.repositories.ClientRepository;
import tLogistic.models.Article;
import tLogistic.models.Client;
import tLogistic.services.ArticleService;
import tLogistic.utils.ExcelReader;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final ClientRepository clientRepository;
    String uploadPath = "C:/JAVA/tLogistic/src/main/resources/images";

    @Override
    public String addArticle(String article, String description, String code, String supportingDoc, MultipartFile file, String clientName) throws IOException {
        article = article.trim();
        description = description.trim();
        code = code.trim();
        supportingDoc = supportingDoc.trim();
        clientName = clientName.trim();
        if (article.isBlank() || description.isBlank() || code.isBlank() || clientName.isBlank()) {
            return "articleAddError";
        }
        List<Client> clientInBase = clientRepository.findByName(clientName);
        if (clientInBase.isEmpty()) {
            return "clientSearchError";
        }
        Client client = clientInBase.get(0);
        List<Article> articleAlreadyInBase = articleRepository.findAllByArticleAndClient(article, clientInBase.get(0));
        if (!articleAlreadyInBase.isEmpty()) {
            return "articleAddExistError";
        }
        Article art = new Article(article, description, code, supportingDoc);
        if (!file.isEmpty()) {
            File uploadDir = new File(uploadPath);
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadDir + "/" + resultFileName));
            art.setImagePath(resultFileName);
        }
        art.setClient(client);
        clientRepository.save(client);
        articleRepository.save(art);
        return "articleAddOk";
    }

    @Override
    public String edit(Long id, String description, String code, String supportingDoc, Model model, MultipartFile file) throws IOException {
        description = description.trim();
        code = code.trim();
        supportingDoc = supportingDoc.trim();
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return "articleSearchError";
        }
        Article article = optionalArticle.get();
        article.setDescription(description);
        article.setCode(code);
        article.setSupportingDoc(supportingDoc);
        if (!file.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (article.getImagePath() != null) {
                File removedImage = new File(uploadPath + "/" + article.getImagePath());
                if (!removedImage.delete()){
                    System.out.println("Не удалось удалить файл " + removedImage);
                }
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadDir + "/" + resultFileName));
            article.setImagePath(resultFileName);
        }
        articleRepository.save(article);
        model.addAttribute("title", "Детализация артикула " + article.getArticle());
        model.addAttribute("article", optionalArticle.get());
        return "articleDetails";
    }

    @Override
    public String findEdit(Long id, Model model) {
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isPresent()) {
            Article article = optionalArticle.get();
            model.addAttribute("article", article);
            return "articleEdit";
        }
        return "articleSearchError";
    }

    @Override
    public String findRemove(Long id, Model model) {
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return "articleSearchError";
        }
        Article article = optionalArticle.get();
        model.addAttribute("article", article);
        return "articleRemove";
    }

    @Override
    public String removeArticle(Long id, String articleName, Model model) {
        articleName = articleName.trim();
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return "articleSearchError";
        }
        Article article = optionalArticle.get();
        if (article.getArticle().equals(articleName)) {
            if (article.getImagePath() != null) {
                File removedImage = new File(uploadPath + "/" + article.getImagePath());
                if(!removedImage.delete()){
                    System.out.println("Не удалось удалить файл " + removedImage);
                }
            }
            articleRepository.delete(article);
            return "articleAddOk";
        } else return "articleSearchError";
    }

    @Override
    public String showDetails(Long id, Model model){
        if (!articleRepository.existsById(id)) {
            return "articleSearchError";
        }
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isPresent()) {
            Article article = optionalArticle.get();
            model.addAttribute("title", "Детализация артикула " + article.getArticle());
            model.addAttribute("article", optionalArticle.get());
            return "articleDetails";
        } else {
            return "articleSearchError";
        }
    }

    @Override
    public String addArticlesFromXlsx(Long clientId, MultipartFile file, Model model){
        if (clientId == null) {
            return "articleSearchErrorNoClient";
        }
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isEmpty()) {
            return "articleSearchErrorNoClient";
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || file.isEmpty() || fileName.isBlank()) {
            return "xlsxError";
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.equals("xlsx")) {
            return "fileExtensionError";
        }
        List<Article> articlesAddedToDB = new ArrayList<>();
        List<Article> articlesAlreadyInBase = new ArrayList<>();
        Client client = optionalClient.get();
        try {
            parseFileAndFillArticleLists(articlesAddedToDB, articlesAlreadyInBase, client, file);
        } catch (Exception e) {
            return "xlsxError";
        }

        if (!articlesAlreadyInBase.isEmpty()) {
            model.addAttribute("articles", articlesAlreadyInBase);
            return "addArticleXLSXError";
        }
        articleRepository.saveAll(articlesAddedToDB);
        model.addAttribute("articles", articlesAddedToDB);
        return "addArticleXLSXOk";
    }

    private void parseFileAndFillArticleLists(List<Article> articlesAddedToDB, List<Article> articlesAlreadyInBase, Client client, MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("searchResult", RandomString.make() + ".xlsx");
        file.transferTo(tempFile);
        tempFile.deleteOnExit();
        ExcelReader excelReader = new ExcelReader();
        HashMap<Integer, List<Object>> rows;
        rows = excelReader.read(String.valueOf(tempFile));
        for (List<Object> row : rows.values()) {
            Article art;
            String article = row.get(0).toString().trim();
            String description = row.get(1).toString();
            String code = row.get(2).toString();
            String supportingDoc = row.size() > 3 ? row.get(3).toString() : " ";
            art = new Article(article, description, code, supportingDoc);
            art.setClient(client);
            List<Article> articlesInBase = articleRepository.findAllByArticleAndClient(article, client);
            if (!articlesInBase.isEmpty()) {
                articlesAlreadyInBase.add(articlesInBase.get(0));
                continue;
            }
            articlesAddedToDB.add(art);
        }
    }

    @Override
    public String showAddForm(Model model) {
        Iterable<Client> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        return "addArticle";
    }

    @Override
    public String showFindForm(Model model) {
        Iterable<Client> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        return "findArticles";
    }

    @Override
    public String showAddXlsxForm(Model model) {
        Iterable<Client> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        return "addArticleXLSX";
    }

}
