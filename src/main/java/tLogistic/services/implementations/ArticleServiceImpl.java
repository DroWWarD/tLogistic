package tLogistic.services.implementations;

import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import tLogistic.repositories.ArticleRepository;
import tLogistic.repositories.ClientRepository;
import tLogistic.repositories.ImageRepository;
import tLogistic.models.Article;
import tLogistic.models.Client;
import tLogistic.models.Image;
import tLogistic.services.ArticleService;
import tLogistic.utils.ExcelReader;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final ClientRepository clientRepository;
    private final ImageRepository imageRepository;

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
        Image image;
        if (!file.isEmpty()) {
            image = toImageEntity(file);
            image.setPreviewImage(true);
            art.addImageToArticle(image);
        }
        art.setClient(client);
        clientRepository.save(client);
        articleRepository.save(art);
        return "articleAddOk";
    }

    private Image toImageEntity(MultipartFile file) throws IOException {
        Image image = new Image();
        image.setName(file.getName());
        image.setOriginalFileName(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setSize(file.getSize());
        image.setBytes(file.getBytes());
        return image;
    }

    @Override
    public String edit(Long id, String description, String code, String supportingDoc, Model model) {
        description = description.trim();
        code = code.trim();
        supportingDoc = supportingDoc.trim();
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isPresent()) {
            Article art = optionalArticle.get();
            art.setDescription(description);
            art.setCode(code);
            art.setSupportingDoc(supportingDoc);
            articleRepository.save(art);
            return "articleAddOk";
        }
        return "articleSearchError";
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
        if (!articleRepository.existsById(id)) {
            return "articleSearchError";
        }
        Article article = articleRepository.findById(id).get();
        model.addAttribute("article", article);
        return "articleRemove";
    }

    @Override
    public String removeArticle(Long id, String article, Model model) {
        article = article.trim();
        if (!articleRepository.existsById(id)) {
            return "articleSearchError";
        }
        Article art = articleRepository.findById(id).get();
        if (art.getArticle().equals(article)) {
            articleRepository.delete(art);
            return "articleAddOk";
        } else return "articleSearchError";
    }

    @Override
    public String showDetails(Long id, Model model) {
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
    public String findArticlesList(Long clientId, String articles, Model model) throws Exception {
        articles = articles.trim();
        if (clientId == null) {
            return "articleSearchErrorNoClient";
        }
        List<Article> result = new ArrayList<>();
        String[] articlesFromRequest = articles.split(" ");
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            for (int i = 0; i < articlesFromRequest.length; i++) {
                result.addAll(articleRepository.findAllByArticleAndClient(articlesFromRequest[i], client));
            }
            Workbook workbook = new Workbook();
            Worksheet worksheet = workbook.getWorksheets().get(0);
            if (!result.isEmpty()) {
                for (int i = 0; i < result.size(); i++) {
                    Article article = result.get(i);
                    worksheet.getCells().get(i, 0).putValue(article.getArticle());
                    worksheet.getCells().get(i, 1).putValue(article.getDescription());
                    worksheet.getCells().get(i, 2).putValue(article.getCode());
                    worksheet.getCells().get(i, 3).putValue(article.getSupportingDoc());
                }
            }
            File tempFile = File.createTempFile("temp", RandomString.make() + ".xlsx");
            workbook.save(String.valueOf(tempFile));
            model.addAttribute("file", tempFile);
            model.addAttribute("client", client);
            model.addAttribute("articles", result);
            return "articleSearchResult";
        }
        return "articleSearchErrorNoClient";
    }

    @Override
    public String parseXLSX(Long clientId, MultipartFile file, Model model) throws IOException{
        if (clientId == null) {
            return "articleSearchErrorNoClient";
        }
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isEmpty()) {
            return "articleSearchErrorNoClient";
        }
        Client client = optionalClient.get();
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        var extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.equals("xlsx")) {
            return "fileExtensionError";
        }
        File tempFile = File.createTempFile("searchResult", RandomString.make() + ".xlsx");
        file.transferTo(tempFile);
        tempFile.deleteOnExit();
        ExcelReader excelReader = new ExcelReader();
        HashMap<Integer, List<Object>> rows;
        try {
            rows = excelReader.read(String.valueOf(tempFile));
        } catch (Exception e) {
            return "xlsxError";
        }
        List<Article> articles = new ArrayList<>();
        List<Article> articlesAlreadyInBase = new ArrayList<>();
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
            articles.add(art);
        }
        if (!articlesAlreadyInBase.isEmpty()) {
            model.addAttribute("articles", articlesAlreadyInBase);
            return "addArticleXLSXError";
        }
        articleRepository.saveAll(articles);
        model.addAttribute("articles", articles);
        return "addArticleXLSXOk";
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
