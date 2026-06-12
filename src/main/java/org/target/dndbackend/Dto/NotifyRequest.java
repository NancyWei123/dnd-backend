package org.target.dndbackend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NotifyRequest {
    private Long bookId;
    private Long chapterId;
    private List<Long> userIds;
}
