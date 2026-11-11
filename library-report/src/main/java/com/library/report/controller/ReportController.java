package com.library.report.controller;

import com.library.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReportService reportService;

    @GetMapping("/books")
    public ResponseEntity<byte[]> books() {
        return file("book-report.xlsx", reportService.bookReport());
    }

    @GetMapping("/borrows")
    public ResponseEntity<byte[]> borrows() {
        return file("borrow-report.xlsx", reportService.borrowReport());
    }

    @GetMapping("/returns")
    public ResponseEntity<byte[]> returns() {
        return file("return-report.xlsx", reportService.returnReport());
    }

    private ResponseEntity<byte[]> file(String name, byte[] body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .contentType(XLSX)
                .body(body);
    }
}
