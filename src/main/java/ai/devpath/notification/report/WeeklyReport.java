package ai.devpath.notification.report;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 스키마: devpath-shared V202607031002__weekly_report.sql. */
@Entity
@Table(name = "weekly_report")
public class WeeklyReport {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(name = "user_id", nullable = false) private Long userId;
	@Column(name = "week_of", nullable = false) private LocalDate weekOf;
	@JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false) private String payload;
	@Column(name = "generated_at", insertable = false, updatable = false) private Instant generatedAt;
	@Column(name = "email_sent_at") private Instant emailSentAt;
	@Column(name = "push_sent_at") private Instant pushSentAt;

	public Long getUserId() { return userId; }
	public void setUserId(Long v) { this.userId = v; }
	public LocalDate getWeekOf() { return weekOf; }
	public void setWeekOf(LocalDate v) { this.weekOf = v; }
	public String getPayload() { return payload; }
	public void setPayload(String v) { this.payload = v; }
	public Instant getEmailSentAt() { return emailSentAt; }
	public void setEmailSentAt(Instant v) { this.emailSentAt = v; }
	public Instant getPushSentAt() { return pushSentAt; }
	public void setPushSentAt(Instant v) { this.pushSentAt = v; }
}
