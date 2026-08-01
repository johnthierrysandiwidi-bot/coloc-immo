package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.PrixAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.domain.Prix;
import bf.colocation.immo.domain.enumeration.Periodicite;
import bf.colocation.immo.repository.PrixRepository;
import bf.colocation.immo.service.PrixService;
import bf.colocation.immo.service.dto.PrixDTO;
import bf.colocation.immo.service.mapper.PrixMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
 * Integration tests for the {@link PrixResource} REST controller.
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
class PrixResourceIT {

    private static final Double DEFAULT_PRIX = 0D;
    private static final Double UPDATED_PRIX = 1D;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Double DEFAULT_CHARGES = 0D;
    private static final Double UPDATED_CHARGES = 1D;

    private static final Periodicite DEFAULT_PERIODICITE = Periodicite.MENSUEL;
    private static final Periodicite UPDATED_PERIODICITE = Periodicite.TRIMESTRIEL;

    private static final LocalDate DEFAULT_DATE_EFFET = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_EFFET = LocalDate.parse("2023-12-16");

    private static final String ENTITY_API_URL = "/api/prixes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PrixRepository prixRepository;

    @Mock
    private PrixRepository prixRepositoryMock;

    @Autowired
    private PrixMapper prixMapper;

    @Mock
    private PrixService prixServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrixMockMvc;

    private Prix prix;

    private Prix insertedPrix;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prix createEntity(EntityManager em) {
        Prix prix = new Prix()
            .prix(DEFAULT_PRIX)
            .description(DEFAULT_DESCRIPTION)
            .charges(DEFAULT_CHARGES)
            .periodicite(DEFAULT_PERIODICITE)
            .dateEffet(DEFAULT_DATE_EFFET);
        // Add required entity
        Immobilier immobilier;
        if (TestUtil.findAll(em, Immobilier.class).isEmpty()) {
            immobilier = ImmobilierResourceIT.createEntity(em);
            em.persist(immobilier);
            em.flush();
        } else {
            immobilier = TestUtil.findAll(em, Immobilier.class).get(0);
        }
        prix.setImmobilier(immobilier);
        return prix;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prix createUpdatedEntity(EntityManager em) {
        Prix updatedPrix = new Prix()
            .prix(UPDATED_PRIX)
            .description(UPDATED_DESCRIPTION)
            .charges(UPDATED_CHARGES)
            .periodicite(UPDATED_PERIODICITE)
            .dateEffet(UPDATED_DATE_EFFET);
        // Add required entity
        Immobilier immobilier;
        if (TestUtil.findAll(em, Immobilier.class).isEmpty()) {
            immobilier = ImmobilierResourceIT.createUpdatedEntity(em);
            em.persist(immobilier);
            em.flush();
        } else {
            immobilier = TestUtil.findAll(em, Immobilier.class).get(0);
        }
        updatedPrix.setImmobilier(immobilier);
        return updatedPrix;
    }

    @BeforeEach
    void initTest() {
        prix = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedPrix != null) {
            prixRepository.delete(insertedPrix);
            insertedPrix = null;
        }
    }

    @Test
    @Transactional
    void createPrix() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Prix
        PrixDTO prixDTO = prixMapper.toDto(prix);
        var returnedPrixDTO = om.readValue(
            restPrixMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PrixDTO.class
        );

        // Validate the Prix in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPrix = prixMapper.toEntity(returnedPrixDTO);
        assertPrixUpdatableFieldsEquals(returnedPrix, getPersistedPrix(returnedPrix));

        insertedPrix = returnedPrix;
    }

    @Test
    @Transactional
    void createPrixWithExistingId() throws Exception {
        // Create the Prix with an existing ID
        prix.setId(1L);
        PrixDTO prixDTO = prixMapper.toDto(prix);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrixMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkPrixIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prix.setPrix(null);

        // Create the Prix, which fails.
        PrixDTO prixDTO = prixMapper.toDto(prix);

        restPrixMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPeriodiciteIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prix.setPeriodicite(null);

        // Create the Prix, which fails.
        PrixDTO prixDTO = prixMapper.toDto(prix);

        restPrixMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDateEffetIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        prix.setDateEffet(null);

        // Create the Prix, which fails.
        PrixDTO prixDTO = prixMapper.toDto(prix);

        restPrixMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPrixes() throws Exception {
        // Initialize the database
        insertedPrix = prixRepository.saveAndFlush(prix);

        // Get all the prixList
        restPrixMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prix.getId().intValue())))
            .andExpect(jsonPath("$.[*].prix").value(hasItem(DEFAULT_PRIX)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].charges").value(hasItem(DEFAULT_CHARGES)))
            .andExpect(jsonPath("$.[*].periodicite").value(hasItem(DEFAULT_PERIODICITE.toString())))
            .andExpect(jsonPath("$.[*].dateEffet").value(hasItem(DEFAULT_DATE_EFFET.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPrixesWithEagerRelationshipsIsEnabled() throws Exception {
        when(prixServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPrixMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(prixServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPrixesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(prixServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPrixMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(prixRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPrix() throws Exception {
        // Initialize the database
        insertedPrix = prixRepository.saveAndFlush(prix);

        // Get the prix
        restPrixMockMvc
            .perform(get(ENTITY_API_URL_ID, prix.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prix.getId().intValue()))
            .andExpect(jsonPath("$.prix").value(DEFAULT_PRIX))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.charges").value(DEFAULT_CHARGES))
            .andExpect(jsonPath("$.periodicite").value(DEFAULT_PERIODICITE.toString()))
            .andExpect(jsonPath("$.dateEffet").value(DEFAULT_DATE_EFFET.toString()));
    }

    @Test
    @Transactional
    void getNonExistingPrix() throws Exception {
        // Get the prix
        restPrixMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPrix() throws Exception {
        // Initialize the database
        insertedPrix = prixRepository.saveAndFlush(prix);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prix
        Prix updatedPrix = prixRepository.findById(prix.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPrix are not directly saved in db
        em.detach(updatedPrix);
        updatedPrix
            .prix(UPDATED_PRIX)
            .description(UPDATED_DESCRIPTION)
            .charges(UPDATED_CHARGES)
            .periodicite(UPDATED_PERIODICITE)
            .dateEffet(UPDATED_DATE_EFFET);
        PrixDTO prixDTO = prixMapper.toDto(updatedPrix);

        restPrixMockMvc
            .perform(put(ENTITY_API_URL_ID, prixDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isOk());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPrixToMatchAllProperties(updatedPrix);
    }

    @Test
    @Transactional
    void putNonExistingPrix() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prix.setId(longCount.incrementAndGet());

        // Create the Prix
        PrixDTO prixDTO = prixMapper.toDto(prix);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrixMockMvc
            .perform(put(ENTITY_API_URL_ID, prixDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPrix() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prix.setId(longCount.incrementAndGet());

        // Create the Prix
        PrixDTO prixDTO = prixMapper.toDto(prix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrixMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPrix() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prix.setId(longCount.incrementAndGet());

        // Create the Prix
        PrixDTO prixDTO = prixMapper.toDto(prix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrixMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePrixWithPatch() throws Exception {
        // Initialize the database
        insertedPrix = prixRepository.saveAndFlush(prix);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prix using partial update
        Prix partialUpdatedPrix = new Prix();
        partialUpdatedPrix.setId(prix.getId());

        partialUpdatedPrix.charges(UPDATED_CHARGES).dateEffet(UPDATED_DATE_EFFET);

        restPrixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrix.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrix))
            )
            .andExpect(status().isOk());

        // Validate the Prix in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrixUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPrix, prix), getPersistedPrix(prix));
    }

    @Test
    @Transactional
    void fullUpdatePrixWithPatch() throws Exception {
        // Initialize the database
        insertedPrix = prixRepository.saveAndFlush(prix);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prix using partial update
        Prix partialUpdatedPrix = new Prix();
        partialUpdatedPrix.setId(prix.getId());

        partialUpdatedPrix
            .prix(UPDATED_PRIX)
            .description(UPDATED_DESCRIPTION)
            .charges(UPDATED_CHARGES)
            .periodicite(UPDATED_PERIODICITE)
            .dateEffet(UPDATED_DATE_EFFET);

        restPrixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrix.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrix))
            )
            .andExpect(status().isOk());

        // Validate the Prix in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrixUpdatableFieldsEquals(partialUpdatedPrix, getPersistedPrix(partialUpdatedPrix));
    }

    @Test
    @Transactional
    void patchNonExistingPrix() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prix.setId(longCount.incrementAndGet());

        // Create the Prix
        PrixDTO prixDTO = prixMapper.toDto(prix);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, prixDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPrix() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prix.setId(longCount.incrementAndGet());

        // Create the Prix
        PrixDTO prixDTO = prixMapper.toDto(prix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrixMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prixDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPrix() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        prix.setId(longCount.incrementAndGet());

        // Create the Prix
        PrixDTO prixDTO = prixMapper.toDto(prix);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrixMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prixDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prix in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePrix() throws Exception {
        // Initialize the database
        insertedPrix = prixRepository.saveAndFlush(prix);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the prix
        restPrixMockMvc
            .perform(delete(ENTITY_API_URL_ID, prix.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return prixRepository.count();
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

    protected Prix getPersistedPrix(Prix prix) {
        return prixRepository.findById(prix.getId()).orElseThrow();
    }

    protected void assertPersistedPrixToMatchAllProperties(Prix expectedPrix) {
        assertPrixAllPropertiesEquals(expectedPrix, getPersistedPrix(expectedPrix));
    }

    protected void assertPersistedPrixToMatchUpdatableProperties(Prix expectedPrix) {
        assertPrixAllUpdatablePropertiesEquals(expectedPrix, getPersistedPrix(expectedPrix));
    }
}
