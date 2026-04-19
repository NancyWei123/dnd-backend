package org.target.dndbackend.Dto;

import lombok.Data;

@Data
public class CreateChapterRequest {

    private String title;

    private String contentMd;

    private Integer chapterOrder;
}