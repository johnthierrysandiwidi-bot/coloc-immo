package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByDateEnvoiAsc(Long conversationId);

    @Query(
        "select count(m) from Message m where m.lu = false and m.expediteur.id <> :userId and " +
        "(m.conversation.participant1.id = :userId or m.conversation.participant2.id = :userId)"
    )
    long compterNonLus(@Param("userId") Long userId);

    @Modifying
    @Query(
        "update Message m set m.lu = true where m.conversation.id = :conversationId and " +
        "m.expediteur.id <> :userId and m.lu = false"
    )
    int marquerLus(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
