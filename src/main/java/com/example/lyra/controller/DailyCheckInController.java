package com.example.lyra.controller;

import com.example.lyra.dto.request.DailyCheckInRequest;
import com.example.lyra.dto.response.DailyCheckInResponse;
import com.example.lyra.security.services.UserDetailsImpl;
import com.example.lyra.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class DailyCheckInController {

    private final UserService userService;

    /**
     * Salva os dados do check-in diário do usuário autenticado
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<DailyCheckInResponse> saveDailyCheckIn(
            Authentication authentication,
            @Valid @RequestBody DailyCheckInRequest request) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        DailyCheckInResponse response = userService.saveDailyCheckIn(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Recupera os últimos dados do check-in diário do usuário autenticado
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<DailyCheckInResponse> getDailyCheckIn(Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        DailyCheckInResponse response = userService.getDailyCheckIn(userId);
        return ResponseEntity.ok(response);
    }
}
