package org.app.controllers;

import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;

import com.microsoft.azure.functions.*;
import org.app.models.ImageRequest;
import org.app.services.ImageService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

public class ImageController {


    @FunctionName("processImage")
    public HttpResponseMessage processImage(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        try {
            // Log function execution
            context.getLogger().info("Executing processImage function");

            // Extract parameters from the request
            String photoID = request.getQueryParameters().get("photoID");
            String photoGUID = request.getQueryParameters().get("photoGUID");
            String fileContent = request.getBody().orElseThrow(() -> new IllegalArgumentException("File content is missing"));

            // Convert file content to byte array
            byte[] imageBytes = fileContent.getBytes();

            // Create a new InputStream for each use
            InputStream analysisStream = new ByteArrayInputStream(imageBytes);
            InputStream uploadStream = new ByteArrayInputStream(imageBytes);

            // Create ImageRequest with analysisStream
            ImageRequest imageRequest = new ImageRequest(photoID, photoGUID, analysisStream);

            // Process the image
            String result = ImageService.analyzeAndStoreImage(imageRequest, uploadStream);

            return request.createResponseBuilder(HttpStatus.OK).body(result).build();

        } catch (Exception e) {
            context.getLogger().severe("Error processing image: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing image: " + e.getMessage()).build();
        }
    }

    @FunctionName("getImage")
    public HttpResponseMessage getImage(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION, route = "image/{photoGUID}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("photoGUID") String photoGUID,
            final ExecutionContext context) {
        try {
            // Log function execution
            context.getLogger().info("Executing getImage function for GUID: " + photoGUID);

            // Retrieve the image from Blob Storage
            byte[] imageBytes = ImageService.getImageFromBlobStorage(photoGUID);

            if (imageBytes != null) {
                // Respond with the image bytes
                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "image/jpeg") // Assuming it's a JPEG
                        .body(imageBytes).build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("Image not found").build();
            }

        } catch (Exception e) {
            context.getLogger().severe("Error retrieving image: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving image: " + e.getMessage()).build();
        }
    }
}
