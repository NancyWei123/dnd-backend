package org.target.dndbackend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookResponse {

    private Long id;

    private Long userId;

    private String title;

    private String description;

    private String coverUrl;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}