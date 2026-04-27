package org.target.dndbackend.Dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateBookReadersRequest {
    private List<Long> userIds;
}