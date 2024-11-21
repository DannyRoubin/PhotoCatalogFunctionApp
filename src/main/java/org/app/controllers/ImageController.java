package org.app.controllers;

import org.app.models.ImageRequest;
import org.app.services.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class ImageController {

    @PostMapping("/process-image")
    public ResponseEntity<String> processImage(
            @RequestParam("photoID") String photoID,
            @RequestParam("photoGUID") String photoGUID,
            @RequestParam("file") MultipartFile file) {
        try {
            // Debugging: Save the received image to disk
            System.out.println("Received file size: " + file.getSize() + " bytes");
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, Paths.get("received_image.jpg"), StandardCopyOption.REPLACE_EXISTING);

            // Debugging: Log content type
            System.out.println("Content Type: " + file.getContentType());

            // Debugging: Pass the input stream for analysis
            ImageRequest imageRequest = new ImageRequest(photoID, photoGUID, file.getInputStream());
            String result = ImageService.analyzeAndStoreImage(imageRequest);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing image: " + e.getMessage());
        }
    }

}
