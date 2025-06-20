package whispy_server.web.whispy.whispy_web_be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whispy_server.web.whispy.whispy_web_be.domain.admin.domain.Admin;
import whispy_server.web.whispy.whispy_web_be.domain.admin.domain.repository.AdminRepository;
import whispy_server.web.whispy.whispy_web_be.domain.admin.exception.UserNotFoundException;
import whispy_server.web.whispy.whispy_web_be.domain.auth.exception.PasswordMisMatchException;
import whispy_server.web.whispy.whispy_web_be.domain.auth.presentation.dto.request.LoginRequestDto;
import whispy_server.web.whispy.whispy_web_be.domain.auth.presentation.dto.response.TokenResponse;
import whispy_server.web.whispy.whispy_web_be.global.security.jwt.JwtProperties;
import whispy_server.web.whispy.whispy_web_be.global.security.jwt.JwtTokenProvider;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final AdminRepository adminRepository;
    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.timezone:Asia/Seoul}")
    private String timezone;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequestDto dto){
        Admin admin = adminRepository.findByEmail(dto.email())
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);

        if(!passwordEncoder.matches(dto.password(), admin.getPassword())) throw PasswordMisMatchException.EXCEPTION;

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
        return TokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(dto.email()))
                .accessExpiredAt(now.plusSeconds(jwtProperties.accessExp()))
                .refreshToken(jwtTokenProvider.generateRefreshToken(dto.email()))
                .refreshExpiredAt(now.plusSeconds(jwtProperties.refreshExp()))
                .build();
    }
}
