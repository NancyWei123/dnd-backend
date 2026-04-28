package org.target.dndbackend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChapterResponse {

    private Long id;

    private Long bookId;

    private String title;

    private String contentMd;

    private Integer chapterOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private String musicUrl;
}