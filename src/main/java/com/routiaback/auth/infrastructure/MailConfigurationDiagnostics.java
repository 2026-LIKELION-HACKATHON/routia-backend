package com.routiaback.auth.infrastructure;

import com.routiaback.global.logging.SensitiveLogSanitizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
class MailConfigurationDiagnostics {

	private static final Logger log = LoggerFactory.getLogger(MailConfigurationDiagnostics.class);

	private final JavaMailSender mailSender;
	private final ConfigurableEnvironment environment;
	private final String from;

	MailConfigurationDiagnostics(
		JavaMailSender mailSender,
		ConfigurableEnvironment environment,
		@Value("${routia.mail.from}") String from
	) {
		this.mailSender = mailSender;
		this.environment = environment;
		this.from = from;
	}

	@EventListener(ApplicationReadyEvent.class)
	void logEffectiveConfiguration() {
		boolean envPropertySourceLoaded = StreamSupport.stream(environment.getPropertySources().spliterator(), false)
			.anyMatch(propertySource -> propertySource.getName().contains(".env"));
		Path envPath = Path.of(System.getProperty("user.dir"), ".env").toAbsolutePath().normalize();

		if (!(mailSender instanceof JavaMailSenderImpl sender)) {
			log.warn(
				"Mail configuration diagnostics unavailable. senderClass={} envFilePresent={} envPropertySourceLoaded={}",
				mailSender.getClass().getName(),
				Files.isRegularFile(envPath),
				envPropertySourceLoaded
			);
			return;
		}

		Properties properties = sender.getJavaMailProperties();
		String password = sender.getPassword();
		log.info(
			"Effective mail configuration. host={} port={} usernamePresent={} passwordPresent={} passwordLength={} smtpAuth={} startTls={} from={} envFilePresent={} envPropertySourceLoaded={} workingDirectory={}",
			sender.getHost(),
			sender.getPort(),
			sender.getUsername() != null && !sender.getUsername().isBlank(),
			password != null && !password.isBlank(),
			password == null ? 0 : password.length(),
			properties.getProperty("mail.smtp.auth", "false"),
			properties.getProperty("mail.smtp.starttls.enable", "false"),
			SensitiveLogSanitizer.maskEmail(from),
			Files.isRegularFile(envPath),
			envPropertySourceLoaded,
			System.getProperty("user.dir")
		);
	}
}
