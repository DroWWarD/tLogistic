package tLogistic.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;

@RestController
@RequiredArgsConstructor
public class ImageController {
    String uploadPath = "C:/JAVA/tLogistic/src/main/resources/images/";

    @GetMapping("/images/{fileName}")
    private ResponseEntity<ByteArrayResource> findAImage(@PathVariable(value = "fileName") String fileName) throws Exception {
        try (FileInputStream fis = new FileInputStream(uploadPath + fileName)) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(new ByteArrayResource(fis.readAllBytes()));
        }
    }
}