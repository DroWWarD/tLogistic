package tLogistic.services.implementations;

import com.aspose.cells.Cells;
import com.aspose.cells.Style;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tLogistic.repositories.ArticleRepository;
import tLogistic.repositories.ClientRepository;
import tLogistic.models.Article;
import tLogistic.models.Client;
import tLogistic.services.SearchingService;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService {
    private final ClientRepository clientRepository;
    private final ArticleRepository articleRepository;
    @Override
    public ResponseEntity<ByteArrayResource> search(Long clientId, String articles) throws Exception {
        articles = articles.trim();
        if (clientId == null) {
            File file = new File("src/main/resources/templates/searchErrorClient.html");
            return ResponseEntity.badRequest().body(new ByteArrayResource(new FileInputStream(file).readAllBytes()));
        }
        if (articles.isBlank()) {
            File file = new File("src/main/resources/templates/searchErrorArticle.html");
            return ResponseEntity.badRequest().body(new ByteArrayResource(new FileInputStream(file).readAllBytes()));
        }
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (!optionalClient.isPresent()) {
            File file = new File("src/main/resources/templates/searchErrorClient.html");
            return ResponseEntity.badRequest().body(new ByteArrayResource(new FileInputStream(file).readAllBytes()));
        }
        Workbook workbook = new Workbook();
        Worksheet worksheetResult = workbook.getWorksheets().get(0);
        worksheetResult.setName("Результаты поиска");
        Worksheet worksheetMissing = workbook.getWorksheets().add("Нет в БД");
        findArticlesAndFillWorkSheets(worksheetResult,worksheetMissing,articles, optionalClient.get());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(" dd-MM-yyyy HH:mm ");
        File tempFile = File.createTempFile("searchResult" + LocalDateTime.now().format(formatter), ".xlsx");
        workbook.save(String.valueOf(tempFile));
        tempFile.deleteOnExit();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tempFile.getName() + "\"")
                .body(new ByteArrayResource(new FileInputStream(tempFile).readAllBytes()));
    }

    private void findArticlesAndFillWorkSheets(Worksheet worksheetResult, Worksheet worksheetMissing, String articles, Client client) {
        articles = articles.replaceAll("\"", "");
        List<Article> searchResult = new ArrayList<>();
        List<String> missingArticles = new ArrayList<>();
        String[] articlesFromRequest = articles.split("[\\n]+");
        Iterable<Article> articlesInBase = articleRepository.findAllByClient(client);
        Map<String, Article> map = new HashMap<>();
        for (Article a : articlesInBase) {
            map.put(a.getArticle(), a);
        }
        for (String s : articlesFromRequest) {
            if (s.isBlank()){
                continue;
            }
            s = s.trim();
            if (!map.containsKey(s)) {
                missingArticles.add(s);
            } else {
                searchResult.add(map.get(s));
            }
        }
        Cells cellsResult = worksheetResult.getCells();
        Cells cellsMissing = worksheetMissing.getCells();
        setColumnsFormat(cellsMissing,cellsResult);
        fillCells(cellsMissing,cellsResult,missingArticles,searchResult);
    }

    private void fillCells(Cells cellsMissing, Cells cellsResult, List<String> missingArticles, List<Article> searchResult) {
        if (!missingArticles.isEmpty()) {
            cellsMissing.get(0, 0).putValue("Следующие артикулы отсутствуют в БД");
            for (int i = 0; i < missingArticles.size(); i++) {
                cellsMissing.get(i + 1, 0).putValue(missingArticles.get(i));
            }
        }
        if (!searchResult.isEmpty()) {
            for (int i = 0; i < searchResult.size(); i++) {
                Article article = searchResult.get(i);
                cellsResult.get(i, 0).putValue(article.getArticle());
                cellsResult.get(i, 1).putValue(article.getDescription());
                cellsResult.get(i, 2).putValue(article.getCode());
                cellsResult.get(i, 3).putValue(article.getSupportingDoc());
                cellsResult.get(i, 4).putValue("");
            }
        }
    }

    private void setColumnsFormat(Cells cellsMissing, Cells cellsResult) {
        Style style = cellsResult.getStyle();
        style.setTextWrapped(true);
        cellsResult.setStyle(style);
        cellsMissing.setColumnWidth(0, 50);
        cellsMissing.setColumnWidth(1, 50);
        cellsResult.setColumnWidth(0, 15);
        cellsResult.setColumnWidth(1, 100);
        cellsResult.setColumnWidth(2, 15);
        cellsResult.setColumnWidth(3, 50);
        cellsResult.setColumnWidth(4, 20);
    }
}
