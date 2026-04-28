package org.target.dndbackend.Dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateBookRequest {
    private String title;
    private String description;
    private String coverUrl;
    private String permission;
    private String status;

    private List<Long> selectedReaderIds;
}