package io.polaris.sebrae.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "sessions")
public class Session {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Long userId;
	
	@CreationTimestamp
	private LocalDateTime startTime;
	
	private LocalDateTime endTime;
	
	private String device;
	private String browser;
	
	public Session() {}
	
	public Session(Long userId, String device, String browser) {
		this.userId = userId;
		this.device = device;
		this.browser = browser;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public LocalDateTime getStartTime() {
		return startTime;
	}
	
	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}
	
	public LocalDateTime getEndTime() {
		return endTime;
	}
	
	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}
	
	public String getDevice() {
		return device;
	}
	
	public void setDevice(String device) {
		this.device = device;
	}
	
	public String getBrowser() {
		return browser;
	}
	
	public void setBrowser(String browser) {
		this.browser = browser;
	}
}