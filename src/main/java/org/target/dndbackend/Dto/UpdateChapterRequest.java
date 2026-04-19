package org.target.dndbackend.Dto;

import lombok.Data;

@Data
public class UpdateChapterRequest {

    private String title;

    private String contentMd;

    private Integer chapterOrder;
}