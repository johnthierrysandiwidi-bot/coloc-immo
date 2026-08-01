package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.SecurityUtils;
import bf.colocation.immo.service.dto.ConversationDTO;
import bf.colocation.immo.service.dto.MessageDTO;
import bf.colocation.immo.service.metier.MessagerieService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** API de messagerie interne. Toutes les routes exigent une authentification. */
@RestController
@RequestMapping("/api")
public class MessagerieResource {

    private final MessagerieService messagerieService;
    private final UserRepository userRepository;

    public MessagerieResource(MessagerieService messagerieService, UserRepository userRepository) {
        this.messagerieService = messagerieService;
        this.userRepository = userRepository;
    }

    private Long moiId() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).map(u -> u.getId()).orElse(-1L);
    }

    @GetMapping("/conversations")
    public List<ConversationDTO> mesConversations() {
        Long moi = moiId();
        return messagerieService.mesConversations().stream().map(c -> ConversationDTO.de(c, moi)).toList();
    }

    @PostMapping("/conversations/pour-annonce/{annonceId}")
    public ResponseEntity<ConversationDTO> ouvrir(@PathVariable Long annonceId) {
        var c = messagerieService.ouvrirPourAnnonce(annonceId);
        return ResponseEntity.ok(ConversationDTO.de(c, moiId()));
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageDTO> messages(@PathVariable Long id) {
        return messagerieService.messages(id).stream().map(MessageDTO::de).toList();
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageDTO> envoyer(@PathVariable Long id, @RequestBody EnvoiVM vm) {
        return ResponseEntity.ok(MessageDTO.de(messagerieService.envoyer(id, vm.getContenu())));
    }

    @GetMapping("/messages/non-lus")
    public Map<String, Long> nonLus() {
        return Map.of("nombre", messagerieService.nombreNonLus());
    }

    public static class EnvoiVM {
        @NotBlank
        @Size(max = 2000)
        private String contenu;

        public String getContenu() { return contenu; }
        public void setContenu(String contenu) { this.contenu = contenu; }
    }
}
