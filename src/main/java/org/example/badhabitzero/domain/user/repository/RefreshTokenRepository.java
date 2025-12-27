package org.example.badhabitzero.domain.user.repository;

import org.example.badhabitzero.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserIdAndDeviceInfo(Long userId, String deviceInfo);

    void deleteByUserId(Long userId);

    void deleteByToken(String token);

}
