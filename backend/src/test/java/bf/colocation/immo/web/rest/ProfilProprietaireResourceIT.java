package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.ProfilProprietaireAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.ProfilProprietaire;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.TypeProprietaire;
import bf.colocation.immo.repository.ProfilProprietaireRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.ProfilProprietaireService;
import bf.colocation.immo.service.dto.ProfilProprietaireDTO;
import bf.colocation.immo.service.mapper.ProfilProprietaireMapper;
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
 * Integration tests for the {@link ProfilProprietaireResource} REST controller.
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
class ProfilProprietaireResourceIT {

    private static final TypeProprietaire DEFAULT_TYPE = TypeProprietaire.PARTICULIER;
    private static final TypeProprietaire UPDATED_TYPE = TypeProprietaire.AGENCE;

    private static final String DEFAULT_RAISON_SOCIALE = "AAAAAAAAAA";
    private static final String UPDATED_RAISON_SOCIALE = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_CREATION = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/profil-proprietaires";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProfilProprietaireRepository profilProprietaireRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ProfilProprietaireRepository profilProprietaireRepositoryMock;

    @Autowired
    private ProfilProprietaireMapper profilProprietaireMapper;

    @Mock
    private ProfilProprietaireService profilProprietaireServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProfilProprietaireMockMvc;

    private ProfilProprietaire profilProprietaire;

    private ProfilProprietaire insertedProfilProprietaire;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProfilProprietaire createEntity(EntityManager em) {
        ProfilProprietaire profilProprietaire = new ProfilProprietaire()
            .type(DEFAULT_TYPE)
            .raisonSociale(DEFAULT_RAISON_SOCIALE)
            .dateCreation(DEFAULT_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        profilProprietaire.setUtilisateur(user);
        return profilProprietaire;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProfilProprietaire createUpdatedEntity(EntityManager em) {
        ProfilProprietaire updatedProfilProprietaire = new ProfilProprietaire()
            .type(UPDATED_TYPE)
            .raisonSociale(UPDATED_RAISON_SOCIALE)
            .dateCreation(UPDATED_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedProfilProprietaire.setUtilisateur(user);
        return updatedProfilProprietaire;
    }

    @BeforeEach
    void initTest() {
        profilProprietaire = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedProfilProprietaire != null) {
            profilProprietaireRepository.delete(insertedProfilProprietaire);
            insertedProfilProprietaire = null;
        }
    }

    @Test
    @Transactional
    void createProfilProprietaire() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ProfilProprietaire
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);
        var returnedProfilProprietaireDTO = om.readValue(
            restProfilProprietaireMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilProprietaireDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProfilProprietaireDTO.class
        );

        // Validate the ProfilProprietaire in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProfilProprietaire = profilProprietaireMapper.toEntity(returnedProfilProprietaireDTO);
        assertProfilProprietaireUpdatableFieldsEquals(
            returnedProfilProprietaire,
            getPersistedProfilProprietaire(returnedProfilProprietaire)
        );

        insertedProfilProprietaire = returnedProfilProprietaire;
    }

    @Test
    @Transactional
    void createProfilProprietaireWithExistingId() throws Exception {
        // Create the ProfilProprietaire with an existing ID
        profilProprietaire.setId(1L);
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProfilProprietaireMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilProprietaireDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        profilProprietaire.setType(null);

        // Create the ProfilProprietaire, which fails.
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        restProfilProprietaireMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilProprietaireDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProfilProprietaires() throws Exception {
        // Initialize the database
        insertedProfilProprietaire = profilProprietaireRepository.saveAndFlush(profilProprietaire);

        // Get all the profilProprietaireList
        restProfilProprietaireMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(profilProprietaire.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].raisonSociale").value(hasItem(DEFAULT_RAISON_SOCIALE)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProfilProprietairesWithEagerRelationshipsIsEnabled() throws Exception {
        when(profilProprietaireServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProfilProprietaireMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(profilProprietaireServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllProfilProprietairesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(profilProprietaireServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restProfilProprietaireMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(profilProprietaireRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getProfilProprietaire() throws Exception {
        // Initialize the database
        insertedProfilProprietaire = profilProprietaireRepository.saveAndFlush(profilProprietaire);

        // Get the profilProprietaire
        restProfilProprietaireMockMvc
            .perform(get(ENTITY_API_URL_ID, profilProprietaire.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(profilProprietaire.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.raisonSociale").value(DEFAULT_RAISON_SOCIALE))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()));
    }

    @Test
    @Transactional
    void getNonExistingProfilProprietaire() throws Exception {
        // Get the profilProprietaire
        restProfilProprietaireMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProfilProprietaire() throws Exception {
        // Initialize the database
        insertedProfilProprietaire = profilProprietaireRepository.saveAndFlush(profilProprietaire);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the profilProprietaire
        ProfilProprietaire updatedProfilProprietaire = profilProprietaireRepository.findById(profilProprietaire.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProfilProprietaire are not directly saved in db
        em.detach(updatedProfilProprietaire);
        updatedProfilProprietaire.type(UPDATED_TYPE).raisonSociale(UPDATED_RAISON_SOCIALE).dateCreation(UPDATED_DATE_CREATION);
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(updatedProfilProprietaire);

        restProfilProprietaireMockMvc
            .perform(
                put(ENTITY_API_URL_ID, profilProprietaireDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(profilProprietaireDTO))
            )
            .andExpect(status().isOk());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProfilProprietaireToMatchAllProperties(updatedProfilProprietaire);
    }

    @Test
    @Transactional
    void putNonExistingProfilProprietaire() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilProprietaire.setId(longCount.incrementAndGet());

        // Create the ProfilProprietaire
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfilProprietaireMockMvc
            .perform(
                put(ENTITY_API_URL_ID, profilProprietaireDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(profilProprietaireDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProfilProprietaire() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilProprietaire.setId(longCount.incrementAndGet());

        // Create the ProfilProprietaire
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilProprietaireMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(profilProprietaireDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProfilProprietaire() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilProprietaire.setId(longCount.incrementAndGet());

        // Create the ProfilProprietaire
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilProprietaireMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(profilProprietaireDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProfilProprietaireWithPatch() throws Exception {
        // Initialize the database
        insertedProfilProprietaire = profilProprietaireRepository.saveAndFlush(profilProprietaire);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the profilProprietaire using partial update
        ProfilProprietaire partialUpdatedProfilProprietaire = new ProfilProprietaire();
        partialUpdatedProfilProprietaire.setId(profilProprietaire.getId());

        partialUpdatedProfilProprietaire.raisonSociale(UPDATED_RAISON_SOCIALE).dateCreation(UPDATED_DATE_CREATION);

        restProfilProprietaireMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfilProprietaire.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfilProprietaire))
            )
            .andExpect(status().isOk());

        // Validate the ProfilProprietaire in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfilProprietaireUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProfilProprietaire, profilProprietaire),
            getPersistedProfilProprietaire(profilProprietaire)
        );
    }

    @Test
    @Transactional
    void fullUpdateProfilProprietaireWithPatch() throws Exception {
        // Initialize the database
        insertedProfilProprietaire = profilProprietaireRepository.saveAndFlush(profilProprietaire);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the profilProprietaire using partial update
        ProfilProprietaire partialUpdatedProfilProprietaire = new ProfilProprietaire();
        partialUpdatedProfilProprietaire.setId(profilProprietaire.getId());

        partialUpdatedProfilProprietaire.type(UPDATED_TYPE).raisonSociale(UPDATED_RAISON_SOCIALE).dateCreation(UPDATED_DATE_CREATION);

        restProfilProprietaireMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfilProprietaire.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProfilProprietaire))
            )
            .andExpect(status().isOk());

        // Validate the ProfilProprietaire in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProfilProprietaireUpdatableFieldsEquals(
            partialUpdatedProfilProprietaire,
            getPersistedProfilProprietaire(partialUpdatedProfilProprietaire)
        );
    }

    @Test
    @Transactional
    void patchNonExistingProfilProprietaire() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilProprietaire.setId(longCount.incrementAndGet());

        // Create the ProfilProprietaire
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfilProprietaireMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, profilProprietaireDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(profilProprietaireDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProfilProprietaire() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilProprietaire.setId(longCount.incrementAndGet());

        // Create the ProfilProprietaire
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilProprietaireMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(profilProprietaireDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProfilProprietaire() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        profilProprietaire.setId(longCount.incrementAndGet());

        // Create the ProfilProprietaire
        ProfilProprietaireDTO profilProprietaireDTO = profilProprietaireMapper.toDto(profilProprietaire);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfilProprietaireMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(profilProprietaireDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ProfilProprietaire in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProfilProprietaire() throws Exception {
        // Initialize the database
        insertedProfilProprietaire = profilProprietaireRepository.saveAndFlush(profilProprietaire);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the profilProprietaire
        restProfilProprietaireMockMvc
            .perform(delete(ENTITY_API_URL_ID, profilProprietaire.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return profilProprietaireRepository.count();
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

    protected ProfilProprietaire getPersistedProfilProprietaire(ProfilProprietaire profilProprietaire) {
        return profilProprietaireRepository.findById(profilProprietaire.getId()).orElseThrow();
    }

    protected void assertPersistedProfilProprietaireToMatchAllProperties(ProfilProprietaire expectedProfilProprietaire) {
        assertProfilProprietaireAllPropertiesEquals(expectedProfilProprietaire, getPersistedProfilProprietaire(expectedProfilProprietaire));
    }

    protected void assertPersistedProfilProprietaireToMatchUpdatableProperties(ProfilProprietaire expectedProfilProprietaire) {
        assertProfilProprietaireAllUpdatablePropertiesEquals(
            expectedProfilProprietaire,
            getPersistedProfilProprietaire(expectedProfilProprietaire)
        );
    }
}
