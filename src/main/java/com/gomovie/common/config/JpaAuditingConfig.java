package com.gomovie.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing//Without this no it is like Auditing is disabled
public class JpaAuditingConfig {

}