package br.edu.ifpb.ifmeetup.service;

import br.edu.ifpb.ifmeetup.config.ApplicationConfig;
import br.edu.ifpb.ifmeetup.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private ApplicationConfig applicationConfig;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_TOKEN = "test-token";
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().when(applicationConfig.getApplicationUrl()).thenReturn(BASE_URL);
        lenient().when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test Content</html>");
    }

    @Test
    void sendWelcomeEmail_Success() {
        // Act
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(TEST_EMAIL, TEST_NAME));

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq("welcome"), any(Context.class));
    }

    @Test
    void sendPasswordResetEmail_Success() {
        // Act
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(TEST_EMAIL, TEST_NAME, TEST_TOKEN));

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq("password-reset"), any(Context.class));
    }

    @Test
    void sendEmailVerification_Success() {
        // Act
        assertDoesNotThrow(() -> emailService.sendEmailVerification(TEST_EMAIL, TEST_NAME, TEST_TOKEN));

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq("email-verification"), any(Context.class));
    }

    @Test
    void sendTemplateEmail_ThrowsEmailSendException_WhenMailSenderFails() throws Exception {
        // Arrange
        doAnswer(invocation -> {
            throw new MessagingException("Failed to send email");
        }).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(EmailSendException.class, () ->
            emailService.sendTemplateEmail(TEST_EMAIL, "Test Subject", "test-template", Map.of()));
    }
} 