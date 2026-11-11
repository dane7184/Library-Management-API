package com.library.report.dto;

public record ReturnReportRow(
        Long id, Long borrowId, String customerName, String customerPhone,
        Integer returnAmount, java.math.BigDecimal penaltyAmount, java.time.LocalDate returnDate) {
}
