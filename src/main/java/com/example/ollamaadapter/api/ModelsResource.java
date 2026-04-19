package com.example.ollamaadapter.api;

import com.example.ollamaadapter.dto.openai.ModelsResponse;
import com.example.ollamaadapter.service.ModelListingService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/models")
public class ModelsResource {

    private final ModelListingService modelListingService;

    public ModelsResource(ModelListingService modelListingService) {
        this.modelListingService = modelListingService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ModelsResponse listModels() {
        return modelListingService.listModels();
    }
}
