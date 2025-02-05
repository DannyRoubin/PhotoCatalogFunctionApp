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
import java.util.Map;

public class ImageController {


    @FunctionName("processImage")
    public HttpResponseMessage processImage(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION, route = "process-image")
            HttpRequestMessage<byte[]> request,
            final ExecutionContext context) {

        try {
            context.getLogger().info("Executing processImage function");

            // Read the request body (raw bytes)
            byte[] requestBody = request.getBody();
            if (requestBody == null || requestBody.length == 0) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is empty or missing")
                        .build();
            }

            // Extract parameters from query (assuming they are sent as form fields)
            Map<String, String> queryParams = request.getQueryParameters();
            String photoID = queryParams.get("photoID");
            String photoGUID = queryParams.get("photoGUID");

            if (photoID == null || photoGUID == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing required parameters: photoID or photoGUID")
                        .build();
            }

            // Convert image bytes to InputStream
            InputStream analysisStream = new ByteArrayInputStream(requestBody);
            InputStream uploadStream = new ByteArrayInputStream(requestBody);

            // Create ImageRequest object
            ImageRequest imageRequest = new ImageRequest(photoID, photoGUID, analysisStream);

            // Call ImageService to process and store the image
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
