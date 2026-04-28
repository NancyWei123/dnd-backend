package org.target.dndbackend.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/music")
    public ResponseEntity<?> uploadMusic(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "File is empty"
                ));
            }

            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("audio/")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Only audio files are allowed"
                ));
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID() + extension;

            Path folderPath = Paths.get(uploadDir, "music");
            Files.createDirectories(folderPath);

            Path filePath = folderPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            String url = "http://localhost:8080/uploads/music/" + filename;

            return ResponseEntity.ok(Map.of(
                    "url", url
            ));
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Music upload failed"
            ));
        }
    }

    @PostMapping("/cover")
    public ResponseEntity<?> uploadCover(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "File is empty"
                ));
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID() + extension;

            Path folderPath = Paths.get(uploadDir, "covers");
            Files.createDirectories(folderPath);

            Path filePath = folderPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            String url = "http://localhost:8080/uploads/covers/" + filename;

            return ResponseEntity.ok(Map.of(
                    "url", url
            ));
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Upload failed"
            ));
        }
    }
}