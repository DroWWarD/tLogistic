package tLogistic.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface SearchingService {

    ResponseEntity<ByteArrayResource> search(Long clientId, String articles) throws Exception;
}
