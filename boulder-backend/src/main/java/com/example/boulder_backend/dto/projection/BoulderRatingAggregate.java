package com.example.boulder_backend.dto.projection;

import java.util.UUID;

public interface BoulderRatingAggregate {
    UUID getBoulderId();
    Double getAvgStars();
    Long getCount();
}
