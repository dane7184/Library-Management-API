package com.library.book.specification;

import com.library.book.entity.Book;
import com.library.common.enums.BookStatus;
import com.library.common.enums.RecStatus;
import org.springframework.data.jpa.domain.Specification;

/** Composable filters for book search. A null / blank argument means "no filter". */
public final class BookSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private BookSpecifications() {
    }

    public static Specification<Book> isActive() {
        return (root, query, cb) -> cb.equal(root.get("recStatus"), RecStatus.ACTIVE);
    }

    public static Specification<Book> titleContains(String title) {
        return containsIgnoreCase("title", title);
    }

    public static Specification<Book> authorContains(String author) {
        return containsIgnoreCase("author", author);
    }

    public static Specification<Book> yearEquals(Integer year) {
        return year == null ? null : (root, query, cb) -> cb.equal(root.get("year"), year);
    }

    public static Specification<Book> statusEquals(BookStatus status) {
        return status == null ? null : (root, query, cb) -> cb.equal(root.get("bookStatus"), status);
    }

    private static Specification<Book> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String pattern = "%" + escapeLike(value.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), pattern, LIKE_ESCAPE);
    }

    /** Treat user input literally: % and _ are not wildcards. */
    static String escapeLike(String value) {
        String esc = String.valueOf(LIKE_ESCAPE);
        return value.replace(esc, esc + esc).replace("%", esc + "%").replace("_", esc + "_");
    }
}
