package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query(
        "select c from Conversation c " +
        "where c.participant1.id = :userId or c.participant2.id = :userId " +
        "order by c.dernierMessageLe desc nulls last"
    )
    List<Conversation> findForUser(@Param("userId") Long userId);

    @Query(
        "select c from Conversation c where c.annonce.id = :annonceId and (" +
        "(c.participant1.id = :a and c.participant2.id = :b) or " +
        "(c.participant1.id = :b and c.participant2.id = :a))"
    )
    Optional<Conversation> findExistante(@Param("annonceId") Long annonceId, @Param("a") Long a, @Param("b") Long b);
}
