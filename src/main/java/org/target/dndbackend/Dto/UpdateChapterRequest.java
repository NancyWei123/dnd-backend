package org.target.dndbackend.Dto;

import lombok.Data;

@Data
public class UpdateChapterRequest {

    public String musicUrl;

    private String title;

    private String contentMd;

    private Integer chapterOrder;
}