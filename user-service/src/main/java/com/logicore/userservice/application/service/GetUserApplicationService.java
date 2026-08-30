package com.logicore.userservice.application.service;

import com.logicore.userservice.application.dto.UserResponse;
import com.logicore.userservice.application.port.in.GetUserUseCase;
import com.logicore.userservice.application.port.out.UserRepository;
import com.logicore.userservice.domain.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service orchestrating the {@code GetUser} use case.
 */
@Service
public class GetUserApplicationService implements GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUser(UserId id) {
        return userRepository.findById(id).map(UserResponse::from);
    }
}
