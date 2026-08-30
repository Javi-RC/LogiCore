package com.logicore.userservice.application.service;

import com.logicore.userservice.application.command.RegisterUserCommand;
import com.logicore.userservice.application.dto.UserResponse;
import com.logicore.userservice.application.port.out.PasswordEncoder;
import com.logicore.userservice.application.port.out.UserRepository;
import com.logicore.userservice.domain.exception.EmailAlreadyExistsException;
import com.logicore.userservice.domain.model.User;
import com.logicore.userservice.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserApplicationService service;

    @Test
    void registersUserWithEncodedPassword() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = service.register(new RegisterUserCommand("a@b.com", "plain", "Alice", UserRole.CUSTOMER));

        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("a@b.com");
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.role()).isEqualTo(UserRole.CUSTOMER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().passwordHash()).isEqualTo("$2a$encoded");
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.findByEmail("dup@b.com")).thenReturn(Optional.of(
                User.create(com.logicore.userservice.domain.model.UserId.newId(), "dup@b.com", "h", "N", UserRole.CUSTOMER)));

        assertThatThrownBy(() -> service.register(new RegisterUserCommand("dup@b.com", "pw", "N", UserRole.CUSTOMER)))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verifyNoInteractions(passwordEncoder);
        verify(userRepository).findByEmail("dup@b.com");
    }
}
