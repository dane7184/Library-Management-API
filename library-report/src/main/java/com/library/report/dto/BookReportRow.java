package com.library.report.dto;

public record BookReportRow(
        Long id, String title, String author, String bookStatus, Integer amount) {
}
