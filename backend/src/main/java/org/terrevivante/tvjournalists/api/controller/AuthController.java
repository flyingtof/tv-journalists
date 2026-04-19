package org.terrevivante.tvjournalists.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.terrevivante.tvjournalists.api.dto.CurrentUserDTO;
import org.terrevivante.tvjournalists.api.mapper.UserMapper;
import org.terrevivante.tvjournalists.application.usecase.GetCurrentUserUseCase;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final UserMapper userMapper;

    public AuthController(GetCurrentUserUseCase getCurrentUserUseCase, UserMapper userMapper) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userMapper.toCurrentUserDto(getCurrentUserUseCase.getCurrentUser(authentication.getName())));
    }
}
