package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.ProfilDemarcheurAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.ProfilDemarcheur;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutValidation;
import bf.colocation.immo.repository.ProfilDemarcheurRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.ProfilDemarcheurService;
import bf.colocation.immo.service.dto.ProfilDemarcheurDTO;
import bf.colocation.immo.service.mapper.ProfilDemarcheurMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProfilDemarcheurResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
// Les listes et les accès par identifiant sont désormais restreints au
// titulaire des données. Ces tests vérifient la mécanique CRUD, non les
// autorisations : ils s'exécutent donc en administrateur, rôle qui conserve
// un accès complet par conception. Le cloisonnement lui-même est à couvrir
// par des tests dédiés (voir AutorisationServiceIT à écrire).
class ProfilDemarcheurResourceIT {

    private static final StatutValidation DEFAULT_STATUT_VALIDATION = StatutValidation.EN_ATTENTE;
    private static final StatutValidation UPDATED_STATUT_VALIDATION = StatutValidation.VALIDE;

    private static final Instant DEFAULT_DATE_VALIDATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_VALIDATION = Instant.ofEpochMilli(1702714037224L);

    private static final Instant DEFAULT_DATE_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_CREATION = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/profil-demarcheurs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProfilDemarcheurRepository profilDemarcheurRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ProfilDemarcheurRepository profilDemarcheurRepositoryMock;

    @Autowired
    private ProfilDemarcheurMapper profilDemarcheurMapper;

    @Mock
    private ProfilDemarcheurService profilDemarcheurServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProfilDemarcheurMockMvc;

    private ProfilDemarcheur profilDemarcheur;

    private ProfilDemarcheur insertedProfilDemarcheur;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProfilDemarcheur createEntity(EntityManager em) {
        ProfilDemarcheur profilDemarcheur = new ProfilDemarcheur()
            .statutValidation(DEFAULT_STATUT_VALIDATION)
            .dateValidation(DEFAULT_DATE_VALIDATION)
            .dateCreation(DEFAULT_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        profilDemarcheur.setUtilisateur(user);
        return profilDemarcheur;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProfilDemarcheur createUpdatedEntity(EntityManager em) {
        ProfilDemarcheur updatedProfilDemarcheur = new ProfilDemarcheur()
            .statutValidation(UPDATED_STATUT_VALIDATION)
            .dateValidation(UPDATED_DATE_VALIDATION)
            .dateCreation(UPDATED_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedProfilDemarcheur.setUtilisateur(user);
        return updatedProfilDemarcheur;
    }

    @BeforeEach
    void initTest() {
        profilDemarcheur = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedProfilDemarcheur != null) {
            profilDemarcheurRepository.delete(insertedProfilDemarcheur);
            insertedProfilDemarcheur = null;
        }
    }

    @Test
    @Transactional
    void createProfilDemarcheur() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ProfilDemarcheur
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);
        var returnedProfilDemarcheurDTO = om.readValue(
            restProfilDemarcheurMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilDemarcheurDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProfilDemarcheurDTO.class
        );

        // Validate the ProfilDemarcheur in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProfilDemarcheur = profilDemarcheurMapper.toEntity(returnedProfilDemarcheurDTO);
        assertProfilDemarcheurUpdatableFieldsEquals(returnedProfilDemarcheur, getPersistedProfilDemarcheur(returnedProfilDemarcheur));

        insertedProfilDemarcheur = returnedProfilDemarcheur;
    }

    @Test
    @Transactional
    void createProfilDemarcheurWithExistingId() throws Exception {
        // Create the ProfilDemarcheur with an existing ID
        profilDemarcheur.setId(1L);
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProfilDemarcheurMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilDemarcheurDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkStatutValidationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        profilDemarcheur.setStatutValidation(null);

        // Create the ProfilDemarcheur, which fails.
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        restProfilDemarcheurMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilDemarcheurDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProfilDemarcheurs() throws Exception {
        // Initialize the database
        insertedProfilDemarcheur = profilDemarcheurRepository.saveAndFlush(profilDemarcheur);

        // Get all the profilDemarcheurList
        restProfilDemarcheurMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(profilDemarcheur.getId().intValue())))
            .andExpect(jsonPath("$.[*].statutValidation").value(hasItem(DEFAULT_STATUT_VALIDATION.toString())))
            .andExpect(jsonPath("$.[*].dateValidation").value(hasItem(DEFAULT_DATE_VALIDATION.toString())))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProfilDemarcheursWithEagerRelationshipsIsEnabled() throws Exception {
        when(profilDemarcheurServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProfilDemarcheurMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(profilDemarcheurServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProfilDemarcheursWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(profilDemarcheurServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProfilDemarcheurMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(profilDemarcheurRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getProfilDemarcheur() throws Exception {
        // Initialize the database
        insertedProfilDemarcheur = profilDemarcheurRepository.saveAndFlush(profilDemarcheur);

        // Get the profilDemarcheur
        restProfilDemarcheurMockMvc
            .perform(get(ENTITY_API_URL_ID, profilDemarcheur.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(profilDemarcheur.getId().intValue()))
            .andExpect(jsonPath("$.statutValidation").value(DEFAULT_STATUT_VALIDATION.toString()))
            .andExpect(jsonPath("$.dateValidation").value(DEFAULT_DATE_VALIDATION.toString()))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()));
    }

    @Test
    @Transactional
    void getNonExistingProfilDemarcheur() throws Exception {
        // Get the profilDemarcheur
        restProfilDemarcheurMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProfilDemarcheur() throws Exception {
        // Initialize the database
        insertedProfilDemarcheur = profilDemarcheurRepository.saveAndFlush(profilDemarcheur);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the profilDemarcheur
        ProfilDemarcheur updatedProfilDemarcheur = profilDemarcheurRepository.findById(profilDemarcheur.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProfilDemarcheur are not directly saved in db
        em.detach(updatedProfilDemarcheur);
        updatedProfilDemarcheur
            .statutValidation(UPDATED_STATUT_VALIDATION)
            .dateValidation(UPDATED_DATE_VALIDATION)
            .dateCreation(UPDATED_DATE_CREATION);
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(updatedProfilDemarcheur);

        restProfilDemarcheurMockMvc
            .perform(
                put(ENTITY_API_URL_ID, profilDemarcheurDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(profilDemarcheurDTO))
            )
            .andExpect(status().isOk());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProfilDemarcheurToMatchAllProperties(updatedProfilDemarcheur);
    }

    @Test
    @Transactional
    void putNonExistingProfilDemarcheur() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilDemarcheur.setId(longCount.incrementAndGet());

        // Create the ProfilDemarcheur
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfilDemarcheurMockMvc
            .perform(
                put(ENTITY_API_URL_ID, profilDemarcheurDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(profilDemarcheurDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProfilDemarcheur() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilDemarcheur.setId(longCount.incrementAndGet());

        // Create the ProfilDemarcheur
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilDemarcheurMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(profilDemarcheurDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProfilDemarcheur() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilDemarcheur.setId(longCount.incrementAndGet());

        // Create the ProfilDemarcheur
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilDemarcheurMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilDemarcheurDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProfilDemarcheurWithPatch() throws Exception {
        // Initialize the database
        insertedProfilDemarcheur = profilDemarcheurRepository.saveAndFlush(profilDemarcheur);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the profilDemarcheur using partial update
        ProfilDemarcheur partialUpdatedProfilDemarcheur = new ProfilDemarcheur();
        partialUpdatedProfilDemarcheur.setId(profilDemarcheur.getId());

        partialUpdatedProfilDemarcheur.dateValidation(UPDATED_DATE_VALIDATION);

        restProfilDemarcheurMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfilDemarcheur.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfilDemarcheur))
            )
            .andExpect(status().isOk());

        // Validate the ProfilDemarcheur in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfilDemarcheurUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProfilDemarcheur, profilDemarcheur),
            getPersistedProfilDemarcheur(profilDemarcheur)
        );
    }

    @Test
    @Transactional
    void fullUpdateProfilDemarcheurWithPatch() throws Exception {
        // Initialize the database
        insertedProfilDemarcheur = profilDemarcheurRepository.saveAndFlush(profilDemarcheur);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the profilDemarcheur using partial update
        ProfilDemarcheur partialUpdatedProfilDemarcheur = new ProfilDemarcheur();
        partialUpdatedProfilDemarcheur.setId(profilDemarcheur.getId());

        partialUpdatedProfilDemarcheur
            .statutValidation(UPDATED_STATUT_VALIDATION)
            .dateValidation(UPDATED_DATE_VALIDATION)
            .dateCreation(UPDATED_DATE_CREATION);

        restProfilDemarcheurMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfilDemarcheur.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfilDemarcheur))
            )
            .andExpect(status().isOk());

        // Validate the ProfilDemarcheur in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfilDemarcheurUpdatableFieldsEquals(
            partialUpdatedProfilDemarcheur,
            getPersistedProfilDemarcheur(partialUpdatedProfilDemarcheur)
        );
    }

    @Test
    @Transactional
    void patchNonExistingProfilDemarcheur() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilDemarcheur.setId(longCount.incrementAndGet());

        // Create the ProfilDemarcheur
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfilDemarcheurMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, profilDemarcheurDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(profilDemarcheurDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProfilDemarcheur() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilDemarcheur.setId(longCount.incrementAndGet());

        // Create the ProfilDemarcheur
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilDemarcheurMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(profilDemarcheurDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProfilDemarcheur() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilDemarcheur.setId(longCount.incrementAndGet());

        // Create the ProfilDemarcheur
        ProfilDemarcheurDTO profilDemarcheurDTO = profilDemarcheurMapper.toDto(profilDemarcheur);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilDemarcheurMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(profilDemarcheurDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProfilDemarcheur in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProfilDemarcheur() throws Exception {
        // Initialize the database
        insertedProfilDemarcheur = profilDemarcheurRepository.saveAndFlush(profilDemarcheur);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the profilDemarcheur
        restProfilDemarcheurMockMvc
            .perform(delete(ENTITY_API_URL_ID, profilDemarcheur.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return profilDemarcheurRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ProfilDemarcheur getPersistedProfilDemarcheur(ProfilDemarcheur profilDemarcheur) {
        return profilDemarcheurRepository.findById(profilDemarcheur.getId()).orElseThrow();
    }

    protected void assertPersistedProfilDemarcheurToMatchAllProperties(ProfilDemarcheur expectedProfilDemarcheur) {
        assertProfilDemarcheurAllPropertiesEquals(expectedProfilDemarcheur, getPersistedProfilDemarcheur(expectedProfilDemarcheur));
    }

    protected void assertPersistedProfilDemarcheurToMatchUpdatableProperties(ProfilDemarcheur expectedProfilDemarcheur) {
        assertProfilDemarcheurAllUpdatablePropertiesEquals(
            expectedProfilDemarcheur,
            getPersistedProfilDemarcheur(expectedProfilDemarcheur)
        );
    }
}
