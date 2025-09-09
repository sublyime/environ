package com.fairchild.envmonitor.service;

import com.fairchild.envmonitor.entity.WebcamData;
import com.fairchild.envmonitor.repository.WebcamDataRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WebcamDataService {

    private final WebcamDataRepository webcamDataRepository;

    public WebcamDataService(WebcamDataRepository webcamDataRepository) {
        this.webcamDataRepository = webcamDataRepository;
    }

    public List<WebcamData> getActiveWebcams() {
        return webcamDataRepository.findByIsActiveTrue();
    }

    public List<WebcamData> getWebcamsByCategory(String category) {
        return webcamDataRepository.findByCategoryAndIsActiveTrue(category);
    }

    public List<String> getAvailableCategories() {
        return webcamDataRepository.findDistinctCategories();
    }
}
