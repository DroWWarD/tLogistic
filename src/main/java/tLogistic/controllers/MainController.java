package tLogistic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import tLogistic.repositories.ArticleRepository;
import tLogistic.repositories.ClientRepository;
import tLogistic.models.Client;

@Controller
public class MainController {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/")
    public String home(Model model){
        Iterable<Client> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        return "home";
    }
    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("title", "Инструкции");
        return "faq";
    }
    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("title", "Поддержка");
        return "support";
    }
}