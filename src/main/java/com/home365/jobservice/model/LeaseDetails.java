package com.home365.jobservice.model;

import com.home365.jobservice.entities.enums.LeaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaseDetails {
    private LeaseType type;
    private String agreement;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDate moveOutDate;
    private Long totalRent;

    @Override
    public String toString() {
        return
                "type=" + type +
                        ", startDate=" + startDate.toLocalDate() +
                        ", endDate=" + endDate.toLocalDate() +
                        ", moveOutDate=" + moveOutDate +
                        " , total Rent  = " + totalRent
                ;
    }
}
