package com.firstticket.bookingservice.seat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record SeatedInfo(

    @Column(name = "row_num")
    Integer rowNum,

    @Column(name = "col_num")
    Integer colNum

) {

    public static SeatedInfo of(Integer rowNum, Integer colNum) {
        if (rowNum == null || rowNum < 1) {
            throw new IllegalArgumentException("행 번호는 1 이상이어야 합니다.");
        }
        if (colNum == null || colNum < 1) {
            throw new IllegalArgumentException("열 번호는 1 이상이어야 합니다.");
        }
        return new SeatedInfo(rowNum, colNum);
    }

    public String display(String sectionName) {
        return "%s구역 %d행 %d열".formatted(sectionName, rowNum, colNum);
    }
}
