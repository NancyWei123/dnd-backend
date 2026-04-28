package org.target.dndbackend.Dto;

import lombok.Data;
import java.util.List;

@Data
public class BookReadersRequest {
    private List<Long> userIds;
}