package com.library.scheduler.job;

import com.library.borrow.repository.BorrowRepository;
import com.library.common.enums.BorrowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Marks every BORROW / OWE record past its return date as OVER. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueBorrowJob {

    private final BorrowRepository borrowRepository;

    @Transactional
    @Scheduled(cron = "${library.scheduler.overdue-cron:0 0 1 * * *}")
    public void markOverdueBorrows() {
        var overdue = borrowRepository.findByBorrowStatusInAndReturnDateBefore(
                List.of(BorrowStatus.BORROW, BorrowStatus.OWE), LocalDate.now());
        overdue.forEach(b -> b.setBorrowStatus(BorrowStatus.OVER));
        log.info("Marked {} borrow record(s) as OVER", overdue.size());
    }
}
