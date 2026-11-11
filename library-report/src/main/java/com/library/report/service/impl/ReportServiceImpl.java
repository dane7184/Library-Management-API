package com.library.report.service.impl;

import com.library.book.repository.BookRepository;
import com.library.bookreturn.repository.BookReturnRepository;
import com.library.borrow.repository.BorrowRepository;
import com.library.report.excel.ExcelExporter;
import com.library.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final BookReturnRepository bookReturnRepository;
    private final ExcelExporter excelExporter;

    /** Columns: Id, title, author, book status, amount */
    @Override
    public byte[] bookReport() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Columns: Id, book title, book author, customer name, customer phone, borrow date,
     *  return date, amount, owe, borrow status, employee name */
    @Override
    public byte[] borrowReport() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Columns: id, borrow id, customer name, customer phone, return amount, penalty amount, return date */
    @Override
    public byte[] returnReport() {
        throw new UnsupportedOperationException("TODO");
    }
}
