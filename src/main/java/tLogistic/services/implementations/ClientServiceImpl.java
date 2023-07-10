package tLogistic.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import tLogistic.repositories.ClientRepository;
import tLogistic.models.Client;
import tLogistic.services.ClientService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    @Override
    public String addClient(String name, String description, Model model) {
        name = name.trim();
        description = description.trim();
        if (name.isBlank() || description.isBlank()) {
            return "clientAddError";
        }
        List<Client> clientsInBase = clientRepository.findByName(name);
        if (!clientsInBase.isEmpty()) {
            return "clientAddExistError";
        }
        Client client = new Client();
        client.setName(name);
        client.setDescription(description);
        clientRepository.save(client);
        return "clientAddOk";
    }

    @Override
    public String showDetails(Long id, Model model) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isPresent()) {
            model.addAttribute("title", "Детализация по контрагенту " + optionalClient.get().getName());
            model.addAttribute("client", optionalClient.get());
            return "clientDetails";
        } else {
            return "clientSearchError";
        }
    }

    @Override
    public String findEdit(Long id, Model model) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            model.addAttribute("client", client);
            return "clientEdit";
        }
        return "clientSearchError";
    }

    @Override
    public String edit(Long id, String name, String description, Model model) {
        name = name.trim();
        description = description.trim();
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
            return "articleSearchError";
        }
        Client client = optionalClient.get();
        client.setName(name);
        client.setDescription(description);
        clientRepository.save(client);
        model.addAttribute("title", "Детализация по контрагенту " + optionalClient.get().getName());
        model.addAttribute("client", optionalClient.get());
        return "clientDetails";
    }

    @Override
    public String findRemove(Long id, Model model) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
            return "clientSearchError";
        }
        Client client = optionalClient.get();
        model.addAttribute("client", client);
        return "clientRemove";
    }

    @Override
    public String remove(Long id, String name, Model model) {
        name = name. trim();
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
            return "clientSearchError";
        }
        Client client = optionalClient.get();
        if (client.getName().trim().equals(name)) {
            clientRepository.delete(client);
            return "articleAddOk";
        } else return "clientSearchError";
    }

}
