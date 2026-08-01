package bf.colocation.immo.service.metier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import bf.colocation.immo.domain.Document;
import bf.colocation.immo.domain.ProfilDemarcheur;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutValidation;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.DocumentRepository;
import bf.colocation.immo.repository.ProfilDemarcheurRepository;
import bf.colocation.immo.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * Vérification documentaire (EF-02). C'est le verrou central du projet :
 * un démarcheur non validé ne publie pas.
 */
@ExtendWith(MockitoExtension.class)
class ValidationDemarcheurServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ProfilDemarcheurRepository profilDemarcheurRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMetierService notificationService;

    @InjectMocks
    private ValidationDemarcheurService service;

    private User admin;
    private User demarcheur;
    private Document document;
    private ProfilDemarcheur profil;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setLogin("admin");

        demarcheur = new User();
        demarcheur.setId(2L);
        demarcheur.setLogin("moussa");

        document = new Document();
        document.setId(50L);
        document.setNom("CNIB.pdf");
        document.setStatut(StatutValidation.EN_ATTENTE);
        document.setDemarcheur(demarcheur);

        profil = new ProfilDemarcheur();
        profil.setId(7L);
        profil.setUtilisateur(demarcheur);
        profil.setStatutValidation(StatutValidation.EN_ATTENTE);

        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken("admin", "x", java.util.List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Valider un document débloque le démarcheur et le notifie")
    void validerDebloqueLeDemarcheur() {
        when(documentRepository.findById(50L)).thenReturn(Optional.of(document));
        when(userRepository.findOneByLogin("admin")).thenReturn(Optional.of(admin));
        when(profilDemarcheurRepository.findByUtilisateurId(2L)).thenReturn(Optional.of(profil));

        Document resultat = service.valider(50L);

        assertThat(resultat.getStatut()).isEqualTo(StatutValidation.VALIDE);
        assertThat(resultat.getTraitePar()).isEqualTo(admin);
        assertThat(resultat.getDateTraitement()).isNotNull();

        // Le profil bascule : c'est CE changement qui autorise la publication
        assertThat(profil.getStatutValidation()).isEqualTo(StatutValidation.VALIDE);
        verify(profilDemarcheurRepository).save(profil);

        verify(notificationService).notifier(eq(demarcheur), eq(TypeNotification.DOCUMENT_VALIDE), any(), any(), any());
    }

    @Test
    @DisplayName("Un refus sans motif est rejeté (EF-02.3)")
    void refusSansMotifRejete() {
        assertThatThrownBy(() -> service.refuser(50L, "  "))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("motivé");

        verifyNoInteractions(documentRepository);
    }

    @Test
    @DisplayName("Un refus motivé enregistre le motif et notifie")
    void refusMotiveNotifie() {
        when(documentRepository.findById(50L)).thenReturn(Optional.of(document));
        when(userRepository.findOneByLogin("admin")).thenReturn(Optional.of(admin));

        Document resultat = service.refuser(50L, "Document illisible");

        assertThat(resultat.getStatut()).isEqualTo(StatutValidation.REFUSE);
        assertThat(resultat.getMotifRefus()).isEqualTo("Document illisible");
        verify(notificationService).notifier(eq(demarcheur), eq(TypeNotification.DOCUMENT_REFUSE), any(), any(), any());
    }

    @Test
    @DisplayName("peutPublier() est faux tant que le profil n'est pas VALIDE")
    void peutPublierSelonLeStatut() {
        when(profilDemarcheurRepository.findByUtilisateurId(2L)).thenReturn(Optional.of(profil));
        assertThat(service.peutPublier(demarcheur)).isFalse();

        profil.setStatutValidation(StatutValidation.VALIDE);
        assertThat(service.peutPublier(demarcheur)).isTrue();
    }

    @Test
    @DisplayName("Sans profil en base, peutPublier() est faux — jamais une ouverture par défaut")
    void sansProfilPasDePublication() {
        when(profilDemarcheurRepository.findByUtilisateurId(2L)).thenReturn(Optional.empty());
        assertThat(service.peutPublier(demarcheur)).isFalse();
    }
}
