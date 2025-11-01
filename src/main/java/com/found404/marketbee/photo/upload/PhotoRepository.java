package com.found404.marketbee.photo.upload;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByPictureId(String pictureId);
}
