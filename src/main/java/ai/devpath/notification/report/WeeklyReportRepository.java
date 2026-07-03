package ai.devpath.notification.report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {
	boolean existsByUserIdAndWeekOf(Long userId, java.time.LocalDate weekOf);
}
