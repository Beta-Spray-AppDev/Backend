package com.example.boulder_backend.mapper;

import com.example.boulder_backend.dto.CreateFeedbackDto;
import com.example.boulder_backend.dto.FeedbackDto;
import com.example.boulder_backend.model.Feedback;

public final class FeedbackMapper {
    private FeedbackMapper() {}

    public static Feedback toEntity(CreateFeedbackDto src) {
        Feedback f = new Feedback();
        f.setUsername(src.getUsername() != null ? src.getUsername().trim() : "anonym");
        f.setStars(src.getStars());
        f.setMessage(src.getMessage());
        f.setAppVersion(src.getAppVersion());
        f.setDeviceInfo(src.getDeviceInfo());
        return f;
    }

    public static FeedbackDto toDto(Feedback f) {
        FeedbackDto dto = new FeedbackDto();
        dto.setId(String.valueOf(f.getId()));
        dto.setUsername(f.getUsername());
        dto.setStars(f.getStars());
        dto.setMessage(f.getMessage());
        dto.setCreatedAt(f.getCreatedAt());
        dto.setAppVersion(f.getAppVersion());
        dto.setDeviceInfo(f.getDeviceInfo());
        return dto;
    }
}
