package tLogistic.Repository;

import org.springframework.data.repository.CrudRepository;
import tLogistic.models.Client;

import java.util.List;

public interface ClientRepository extends CrudRepository<Client, Long> {
    List<Client> findByName(String name);
}
