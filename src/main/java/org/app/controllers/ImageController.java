package org.app.controllers;

import org.app.models.ImageRequest;
import org.app.services.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:4200"}, allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class ImageController {

    @PostMapping("/process-image")
    public ResponseEntity<String> processImage(
            @RequestParam("photoID") String photoID,
            @RequestParam("photoGUID") String photoGUID,
            @RequestParam("file") MultipartFile file) {
        try {


            // Copy InputStream to ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            file.getInputStream().transferTo(byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Debugging: Log content type
            System.out.println("Content Type: " + file.getContentType());

            // Create a new InputStream for each use
            InputStream analysisStream = new ByteArrayInputStream(imageBytes);
            InputStream uploadStream = new ByteArrayInputStream(imageBytes);

            // Create ImageRequest with analysisStream
            ImageRequest imageRequest = new ImageRequest(photoID, photoGUID, analysisStream);

            // Process the image
            String result = ImageService.analyzeAndStoreImage(imageRequest, uploadStream);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing image: " + e.getMessage());
        }
    }

    @GetMapping("/image/{photoGUID}")
    public ResponseEntity<byte[]> getImage(@PathVariable String photoGUID) {
        try {
            // Retrieve the image from Blob Storage
            byte[] imageBytes = ImageService.getImageFromBlobStorage(photoGUID);

            if (imageBytes != null) {
                // Respond with the image bytes
                return ResponseEntity.ok()
                        .header("Content-Type", "image/jpeg") // Assuming it's a JPEG
                        .body(imageBytes);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}

