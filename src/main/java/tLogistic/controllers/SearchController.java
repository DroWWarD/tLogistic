package tLogistic.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tLogistic.repositories.ImageRepository;
import tLogistic.services.SearchingService;


@RestController
@RequiredArgsConstructor
public class SearchController {
    private final ImageRepository imageRepository;
    @Autowired
    private final SearchingService searchingService;

    @PostMapping("/findArticles")
    private ResponseEntity<ByteArrayResource> findArticles(@RequestParam(required = false) Long clientId,
                                                           @RequestParam String articles) throws Exception {
        return searchingService.search(clientId, articles);
    }
}
