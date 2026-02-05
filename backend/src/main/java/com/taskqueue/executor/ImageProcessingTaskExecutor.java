package com.taskqueue.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ImageProcessingTaskExecutor implements TaskExecutor {
    
    @Override
    public String getTaskType() {
        return "IMAGE_PROCESS";
    }
    
    @Override
    public void execute(Map<String, Object> payload) throws Exception {
        String imageUrl = (String) payload.get("imageUrl");
        String operation = (String) payload.get("operation");
        
        log.info("Processing image: {}, operation: {}", imageUrl, operation);
        
        // Simulate image processing
        Thread.sleep(2000); // Simulate CPU-intensive work
        
        // In production, use libraries like ImageMagick, Thumbnailator, or AWS Rekognition
        switch (operation) {
            case "resize":
                Integer width = (Integer) payload.get("width");
                Integer height = (Integer) payload.get("height");
                log.info("Resizing image to {}x{}", width, height);
                break;
            case "compress":
                Integer quality = (Integer) payload.get("quality");
                log.info("Compressing image with quality: {}", quality);
                break;
            case "thumbnail":
                log.info("Creating thumbnail for image");
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        
        log.info("Image processing completed for: {}", imageUrl);
    }
}
