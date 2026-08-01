package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.Conversation;
import java.time.Instant;

public class ConversationDTO {
    private Long id;
    private Instant dernierMessageLe;
    private Long annonceId;
    private String annonceTitre;
    private Long interlocuteurId;
    private String interlocuteurLogin;

    public static ConversationDTO de(Conversation c, Long moiId) {
        ConversationDTO d = new ConversationDTO();
        d.id = c.getId();
        d.dernierMessageLe = c.getDernierMessageLe();
        if (c.getAnnonce() != null) {
            d.annonceId = c.getAnnonce().getId();
            d.annonceTitre = c.getAnnonce().getTitre();
        }
        var autre = c.autre(moiId);
        if (autre != null) {
            d.interlocuteurId = autre.getId();
            d.interlocuteurLogin = autre.getLogin();
        }
        return d;
    }

    public Long getId() { return id; }
    public Instant getDernierMessageLe() { return dernierMessageLe; }
    public Long getAnnonceId() { return annonceId; }
    public String getAnnonceTitre() { return annonceTitre; }
    public Long getInterlocuteurId() { return interlocuteurId; }
    public String getInterlocuteurLogin() { return interlocuteurLogin; }
}
