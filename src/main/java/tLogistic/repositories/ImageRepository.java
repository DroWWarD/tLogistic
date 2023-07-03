package tLogistic.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tLogistic.models.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}