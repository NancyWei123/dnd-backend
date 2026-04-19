package org.target.dndbackend.Dto;

import lombok.Data;

@Data
public class CreateBookRequest {

    private String title;

    private String description;

    private String coverUrl;

    private String status;
}