package tLogistic.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tLogistic.models.Client;
import tLogistic.services.ClientService;

@Controller
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    @GetMapping("/clients")
    public String showClients(Model model){
        return clientService.showClients(model);

    }
    @GetMapping("/addClient")
    public String addClient(Model model) {
        return "addClient";
    }
    @PostMapping("/addClient")
    public String addClientToBase(@RequestParam String name, @RequestParam String description, Model model){
        return clientService.addClient(name,description, model);
    }

    @GetMapping("/clientDetails/{id}")
    public String articleDetails(@PathVariable(value = "id") Long id, Model model) {
        return clientService.showDetails(id, model);
    }

    @GetMapping("/clientDetails/{id}/edit")
    public String clientEditSearch(@PathVariable(value = "id") Long id, Model model) {
        return clientService.findEdit(id, model);
    }
    @PostMapping("/clientDetails/{id}/edit")
    public String clientEdit(@PathVariable(value = "id") Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              Model model) {
        return clientService.edit(id, name, description, model);
    }
    @GetMapping("/clientDetails/{id}/remove")
    public String clientRemoveSearch(@PathVariable(value = "id") Long id, Model model) {
        return clientService.findRemove(id, model);
    }
    @PostMapping("/clientDetails/{id}/remove")
    public String clientRemove(@PathVariable(value = "id") Long id, @RequestParam String name, Model model) {
        return clientService.remove(id, name, model);
    }
}
