package com.example.app.settings.db;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_setting")
@Getter
@Setter
@NoArgsConstructor
public class SettingEntity {

	@Id
	@Column(name = "setting_key", nullable = false, length = 100)
	private String key;

	@Column(name = "setting_value", nullable = false, length = 500)
	private String value;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public SettingEntity(String key, String value) {
		this.key = key;
		this.value = value;
	}
}
