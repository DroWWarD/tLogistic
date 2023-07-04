package tLogistic.services;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ArticleService {
    String addArticle(String article, String description, String code, String supportingDoc, MultipartFile file, String clientName) throws IOException;

    String edit(Long id, String description, String code, String supportingDoc, Model model, MultipartFile file) throws IOException;

    String findEdit(Long id, Model model);

    String findRemove(Long id, Model model);

    String removeArticle(Long id, String article, Model model);

    String showDetails(Long id, Model model) throws IOException;

    String addArticlesFromXlsx(Long clientId, MultipartFile file, Model model) throws IOException, InvalidFormatException;

    String showAddForm(Model model);

    String showFindForm(Model model);

    String showAddXlsxForm(Model model);
}
