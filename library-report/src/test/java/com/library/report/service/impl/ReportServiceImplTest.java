package com.library.report.service.impl;

import com.library.book.entity.Book;
import com.library.book.repository.BookRepository;
import com.library.bookreturn.entity.BookReturn;
import com.library.bookreturn.repository.BookReturnRepository;
import com.library.borrow.entity.Borrow;
import com.library.borrow.repository.BorrowRepository;
import com.library.common.enums.BorrowStatus;
import com.library.common.enums.RecStatus;
import com.library.customer.entity.Customer;
import com.library.employee.entity.Employee;
import com.library.report.excel.ExcelExporter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private BorrowRepository borrowRepository;
    @Mock private BookReturnRepository bookReturnRepository;

    private ReportServiceImpl service;

    private final Book book = new Book();
    private final Customer customer = new Customer();
    private final Employee employee = new Employee();
    private final Borrow borrow = new Borrow();

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(bookRepository, borrowRepository, bookReturnRepository, new ExcelExporter());

        book.setId(1L);
        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setAmount(4);
        book.refreshBookStatus();

        customer.setId(2L);
        customer.setName("Sok");
        customer.setPhoneNumber(null); // nullable column must not break the export

        employee.setId(3L);
        employee.setName("Dara");

        borrow.setId(10L);
        borrow.setBook(book);
        borrow.setCustomer(customer);
        borrow.setEmployee(employee);
        borrow.setBorrowDate(LocalDate.of(2026, 11, 1));
        borrow.setReturnDate(LocalDate.of(2026, 11, 8));
        borrow.setAmount(3);
        borrow.setOwe(1);
        borrow.setBorrowStatus(BorrowStatus.OVER);
    }

    @Test
    void bookReportHasHeaderAndTypedRows() throws IOException {
        when(bookRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE)).thenReturn(List.of(book));

        Sheet sheet = read(service.bookReport());

        assertThat(sheet.getSheetName()).isEqualTo("Books");
        assertThat(strings(sheet.getRow(0))).containsExactly("Id", "Title", "Author", "Book Status", "Amount");
        Row row = sheet.getRow(1);
        assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(1);
        assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Clean Code");
        assertThat(row.getCell(3).getStringCellValue()).isEqualTo("AVAILABLE");
        assertThat(row.getCell(4).getCellType()).isEqualTo(CellType.NUMERIC);
        assertThat(row.getCell(4).getNumericCellValue()).isEqualTo(4);
        assertThat(sheet.getLastRowNum()).isEqualTo(1);
    }

    @Test
    void borrowReportWritesDatesAndBlankPhone() throws IOException {
        when(borrowRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE)).thenReturn(List.of(borrow));

        Sheet sheet = read(service.borrowReport());

        assertThat(strings(sheet.getRow(0))).containsExactly("Id", "Book Title", "Book Author", "Customer Name",
                "Customer Phone", "Borrow Date", "Return Date", "Amount", "Owe", "Borrow Status", "Employee Name");
        Row row = sheet.getRow(1);
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Robert Martin");
        assertThat(row.getCell(4).getCellType()).isEqualTo(CellType.BLANK);
        Cell borrowDate = row.getCell(5);
        assertThat(DateUtil.isCellDateFormatted(borrowDate)).isTrue();
        assertThat(borrowDate.getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(row.getCell(8).getNumericCellValue()).isEqualTo(1);
        assertThat(row.getCell(9).getStringCellValue()).isEqualTo("OVER");
        assertThat(row.getCell(10).getStringCellValue()).isEqualTo("Dara");
    }

    @Test
    void returnReportWritesPenaltyAsNumber() throws IOException {
        BookReturn r = new BookReturn();
        r.setId(20L);
        r.setBorrow(borrow);
        r.setEmployee(employee);
        r.setAmount(2);
        r.setPenaltyAmount(new BigDecimal("8.00"));
        r.setReturnDate(LocalDate.of(2026, 11, 12));
        when(bookReturnRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE)).thenReturn(List.of(r));

        Sheet sheet = read(service.returnReport());

        assertThat(strings(sheet.getRow(0))).containsExactly("Id", "Borrow Id", "Customer Name", "Customer Phone",
                "Return Amount", "Penalty Amount", "Return Date");
        Row row = sheet.getRow(1);
        assertThat(row.getCell(1).getNumericCellValue()).isEqualTo(10);
        assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Sok");
        assertThat(row.getCell(4).getNumericCellValue()).isEqualTo(2);
        assertThat(row.getCell(5).getNumericCellValue()).isEqualTo(8.0);
        assertThat(row.getCell(5).getCellStyle().getDataFormatString()).isEqualTo("#,##0.00");
    }

    @Test
    void emptyDataStillProducesHeaderOnlySheet() throws IOException {
        when(borrowRepository.findAllByRecStatusOrderByIdAsc(RecStatus.ACTIVE)).thenReturn(List.of());

        Sheet sheet = read(service.borrowReport());

        assertThat(sheet.getLastRowNum()).isZero();
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Id");
    }

    private static Sheet read(byte[] xlsx) throws IOException {
        assertThat(xlsx).isNotEmpty();
        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx));
        return wb.getSheetAt(0);
    }

    private static List<String> strings(Row row) {
        List<String> out = new ArrayList<>();
        row.forEach(c -> out.add(c.getStringCellValue()));
        return out;
    }
}
