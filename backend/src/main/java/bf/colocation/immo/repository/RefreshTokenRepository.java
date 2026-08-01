package bf.colocation.immo.repository;

import bf.colocation.immo.domain.RefreshToken;
import bf.colocation.immo.domain.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken rt set rt.revoque = true where rt.user = :user")
    void revoquerTousPourUtilisateur(@Param("user") User user);

    @Modifying
    @Query("delete from RefreshToken rt where rt.dateExpiration < :date")
    void purgerExpires(@Param("date") Instant date);
}
