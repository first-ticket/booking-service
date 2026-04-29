package com.firstticket.bookingservice.seat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record Section(

    @Column(name = "section_id")
    UUID sectionId,

    @Column(name = "section_name", nullable = false)
    String sectionName

) {

    public static Section of(UUID sectionId, String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            throw new IllegalArgumentException("섹션 이름은 필수입니다.");
        }
        return new Section(sectionId, sectionName);
    }
}
