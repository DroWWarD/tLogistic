package tLogistic.services;

import org.springframework.ui.Model;

public interface ClientService {
    String addClient(String name, String description, Model model);

    String showDetails(Long id, Model model);

    String findEdit(Long id, Model model);

    String edit(Long id, String name, String description, Model model);

    String findRemove(Long id, Model model);

    String remove(Long id, String name, Model model);
}
