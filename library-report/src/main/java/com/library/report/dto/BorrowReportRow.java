package com.library.report.dto;

public record BorrowReportRow(
        Long id, String bookTitle, String bookAuthor, String customerName, String customerPhone,
        java.time.LocalDate borrowDate, java.time.LocalDate returnDate, Integer amount, Integer owe,
        String borrowStatus, String employeeName) {
}
