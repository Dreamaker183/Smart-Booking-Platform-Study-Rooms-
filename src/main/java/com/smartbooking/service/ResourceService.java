package com.smartbooking.service;

import com.smartbooking.domain.Resource;
import com.smartbooking.persistence.ResourceRepository;

import java.util.List;

public class ResourceService {
    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<Resource> listResources() {
        return resourceRepository.findAll();
    }

    public Resource getResource(long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }
}
