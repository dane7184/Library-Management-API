package com.library.report.service.impl;

import com.library.book.entity.Book;
import com.library.book.repository.BookRepository;
import com.library.bookreturn.entity.BookReturn;
import com.library.bookreturn.repository.BookReturnRepository;
import com.library.borrow.entity.Borrow;
import com.library.borrow.repository.BorrowRepository;
import com.library.common.enums.RecStatus;
import com.library.report.dto.BookReportRow;
import com.library.report.dto.BorrowReportRow;
import com.library.report.dto.ReturnReportRow;
import com.library.report.excel.ExcelExporter;
import com.library.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/** Excel exports of active (not soft-deleted) records, ordered by id. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final List<String> BOOK_HEADERS =
            List.of("Id", "Title", "Author", "Book Status", "Amount");
    private static final List<String> BORROW_HEADERS =
            List.of("Id", "Book Title", "Book Author", "Customer Name", "Customer Phone", "Borrow Date",
                    "Return Date", "Amount", "Owe", "Borrow Status", "Employee Name");
    private static final List<String> RETURN_HEADERS =
            List.of("Id", "Borrow Id", "Customer Name", "Customer Phone", "Return Amount", "Penalty Amount",
                    "Return Date");

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final BookReturnRepository bookReturnRepository;
    private final ExcelExporter excelExporter;

    /** Columns: Id, title, author, book status, amount */
    @Override
    public byte[] bookReport() {
        List<BookReportRow> rows = bookRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(ReportServiceImpl::toRow)
                .toList();
        return excelExporter.export("Books", BOOK_HEADERS, rows, r -> nullable(
                r.id(), r.title(), r.author(), r.bookStatus(), r.amount()));
    }

    /** Columns: Id, book title, book author, customer name, customer phone, borrow date,
     *  return date, amount, owe, borrow status, employee name */
    @Override
    public byte[] borrowReport() {
        List<BorrowReportRow> rows = borrowRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(ReportServiceImpl::toRow)
                .toList();
        return excelExporter.export("Borrows", BORROW_HEADERS, rows, r -> nullable(
                r.id(), r.bookTitle(), r.bookAuthor(), r.customerName(), r.customerPhone(), r.borrowDate(),
                r.returnDate(), r.amount(), r.owe(), r.borrowStatus(), r.employeeName()));
    }

    /** Columns: id, borrow id, customer name, customer phone, return amount, penalty amount, return date */
    @Override
    public byte[] returnReport() {
        List<ReturnReportRow> rows = bookReturnRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE).stream()
                .map(ReportServiceImpl::toRow)
                .toList();
        return excelExporter.export("Returns", RETURN_HEADERS, rows, r -> nullable(
                r.id(), r.borrowId(), r.customerName(), r.customerPhone(), r.returnAmount(), r.penaltyAmount(),
                r.returnDate()));
    }

    private static BookReportRow toRow(Book b) {
        return new BookReportRow(b.getId(), b.getTitle(), b.getAuthor(), b.getBookStatus().name(), b.getAmount());
    }

    private static BorrowReportRow toRow(Borrow b) {
        return new BorrowReportRow(b.getId(), b.getBook().getTitle(), b.getBook().getAuthor(),
                b.getCustomer().getName(), b.getCustomer().getPhoneNumber(), b.getBorrowDate(), b.getReturnDate(),
                b.getAmount(), b.getOwe(), b.getBorrowStatus().name(), b.getEmployee().getName());
    }

    private static ReturnReportRow toRow(BookReturn r) {
        Borrow borrow = r.getBorrow();
        return new ReturnReportRow(r.getId(), borrow.getId(), borrow.getCustomer().getName(),
                borrow.getCustomer().getPhoneNumber(), r.getAmount(), r.getPenaltyAmount(), r.getReturnDate());
    }

    /** List.of rejects nulls; e.g. customer phone and penalty columns are nullable in the DB. */
    private static List<Object> nullable(Object... values) {
        return Arrays.asList(values);
    }
}
