package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.Message;
import java.time.Instant;

public class MessageDTO {
    private Long id;
    private String contenu;
    private Instant dateEnvoi;
    private boolean lu;
    private Long expediteurId;
    private String expediteurLogin;

    public static MessageDTO de(Message m) {
        MessageDTO d = new MessageDTO();
        d.id = m.getId();
        d.contenu = m.getContenu();
        d.dateEnvoi = m.getDateEnvoi();
        d.lu = m.isLu();
        if (m.getExpediteur() != null) {
            d.expediteurId = m.getExpediteur().getId();
            d.expediteurLogin = m.getExpediteur().getLogin();
        }
        return d;
    }

    public Long getId() { return id; }
    public String getContenu() { return contenu; }
    public Instant getDateEnvoi() { return dateEnvoi; }
    public boolean isLu() { return lu; }
    public Long getExpediteurId() { return expediteurId; }
    public String getExpediteurLogin() { return expediteurLogin; }
}
