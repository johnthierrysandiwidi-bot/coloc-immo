package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.AlerteNotifieeAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Alerte;
import bf.colocation.immo.domain.AlerteNotifiee;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.repository.AlerteNotifieeRepository;
import bf.colocation.immo.service.AlerteNotifieeService;
import bf.colocation.immo.service.dto.AlerteNotifieeDTO;
import bf.colocation.immo.service.mapper.AlerteNotifieeMapper;
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
 * Integration tests for the {@link AlerteNotifieeResource} REST controller.
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
class AlerteNotifieeResourceIT {

    private static final Instant DEFAULT_DATE_ENVOI = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_ENVOI = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/alerte-notifiees";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlerteNotifieeRepository alerteNotifieeRepository;

    @Mock
    private AlerteNotifieeRepository alerteNotifieeRepositoryMock;

    @Autowired
    private AlerteNotifieeMapper alerteNotifieeMapper;

    @Mock
    private AlerteNotifieeService alerteNotifieeServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlerteNotifieeMockMvc;

    private AlerteNotifiee alerteNotifiee;

    private AlerteNotifiee insertedAlerteNotifiee;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlerteNotifiee createEntity(EntityManager em) {
        AlerteNotifiee alerteNotifiee = new AlerteNotifiee().dateEnvoi(DEFAULT_DATE_ENVOI);
        // Add required entity
        Alerte alerte;
        if (TestUtil.findAll(em, Alerte.class).isEmpty()) {
            alerte = AlerteResourceIT.createEntity(em);
            em.persist(alerte);
            em.flush();
        } else {
            alerte = TestUtil.findAll(em, Alerte.class).get(0);
        }
        alerteNotifiee.setAlerte(alerte);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        alerteNotifiee.setAnnonce(annonce);
        return alerteNotifiee;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlerteNotifiee createUpdatedEntity(EntityManager em) {
        AlerteNotifiee updatedAlerteNotifiee = new AlerteNotifiee().dateEnvoi(UPDATED_DATE_ENVOI);
        // Add required entity
        Alerte alerte;
        if (TestUtil.findAll(em, Alerte.class).isEmpty()) {
            alerte = AlerteResourceIT.createUpdatedEntity(em);
            em.persist(alerte);
            em.flush();
        } else {
            alerte = TestUtil.findAll(em, Alerte.class).get(0);
        }
        updatedAlerteNotifiee.setAlerte(alerte);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createUpdatedEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        updatedAlerteNotifiee.setAnnonce(annonce);
        return updatedAlerteNotifiee;
    }

    @BeforeEach
    void initTest() {
        alerteNotifiee = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAlerteNotifiee != null) {
            alerteNotifieeRepository.delete(insertedAlerteNotifiee);
            insertedAlerteNotifiee = null;
        }
    }

    @Test
    @Transactional
    void createAlerteNotifiee() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AlerteNotifiee
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);
        var returnedAlerteNotifieeDTO = om.readValue(
            restAlerteNotifieeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteNotifieeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlerteNotifieeDTO.class
        );

        // Validate the AlerteNotifiee in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlerteNotifiee = alerteNotifieeMapper.toEntity(returnedAlerteNotifieeDTO);
        assertAlerteNotifieeUpdatableFieldsEquals(returnedAlerteNotifiee, getPersistedAlerteNotifiee(returnedAlerteNotifiee));

        insertedAlerteNotifiee = returnedAlerteNotifiee;
    }

    @Test
    @Transactional
    void createAlerteNotifieeWithExistingId() throws Exception {
        // Create the AlerteNotifiee with an existing ID
        alerteNotifiee.setId(1L);
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlerteNotifieeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteNotifieeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateEnvoiIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alerteNotifiee.setDateEnvoi(null);

        // Create the AlerteNotifiee, which fails.
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        restAlerteNotifieeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteNotifieeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAlerteNotifiees() throws Exception {
        // Initialize the database
        insertedAlerteNotifiee = alerteNotifieeRepository.saveAndFlush(alerteNotifiee);

        // Get all the alerteNotifieeList
        restAlerteNotifieeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alerteNotifiee.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateEnvoi").value(hasItem(DEFAULT_DATE_ENVOI.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlerteNotifieesWithEagerRelationshipsIsEnabled() throws Exception {
        when(alerteNotifieeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlerteNotifieeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(alerteNotifieeServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlerteNotifieesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(alerteNotifieeServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlerteNotifieeMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(alerteNotifieeRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAlerteNotifiee() throws Exception {
        // Initialize the database
        insertedAlerteNotifiee = alerteNotifieeRepository.saveAndFlush(alerteNotifiee);

        // Get the alerteNotifiee
        restAlerteNotifieeMockMvc
            .perform(get(ENTITY_API_URL_ID, alerteNotifiee.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alerteNotifiee.getId().intValue()))
            .andExpect(jsonPath("$.dateEnvoi").value(DEFAULT_DATE_ENVOI.toString()));
    }

    @Test
    @Transactional
    void getNonExistingAlerteNotifiee() throws Exception {
        // Get the alerteNotifiee
        restAlerteNotifieeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlerteNotifiee() throws Exception {
        // Initialize the database
        insertedAlerteNotifiee = alerteNotifieeRepository.saveAndFlush(alerteNotifiee);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alerteNotifiee
        AlerteNotifiee updatedAlerteNotifiee = alerteNotifieeRepository.findById(alerteNotifiee.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAlerteNotifiee are not directly saved in db
        em.detach(updatedAlerteNotifiee);
        updatedAlerteNotifiee.dateEnvoi(UPDATED_DATE_ENVOI);
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(updatedAlerteNotifiee);

        restAlerteNotifieeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alerteNotifieeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alerteNotifieeDTO))
            )
            .andExpect(status().isOk());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlerteNotifieeToMatchAllProperties(updatedAlerteNotifiee);
    }

    @Test
    @Transactional
    void putNonExistingAlerteNotifiee() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerteNotifiee.setId(longCount.incrementAndGet());

        // Create the AlerteNotifiee
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlerteNotifieeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alerteNotifieeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alerteNotifieeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlerteNotifiee() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerteNotifiee.setId(longCount.incrementAndGet());

        // Create the AlerteNotifiee
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteNotifieeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alerteNotifieeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlerteNotifiee() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerteNotifiee.setId(longCount.incrementAndGet());

        // Create the AlerteNotifiee
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteNotifieeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteNotifieeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlerteNotifieeWithPatch() throws Exception {
        // Initialize the database
        insertedAlerteNotifiee = alerteNotifieeRepository.saveAndFlush(alerteNotifiee);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alerteNotifiee using partial update
        AlerteNotifiee partialUpdatedAlerteNotifiee = new AlerteNotifiee();
        partialUpdatedAlerteNotifiee.setId(alerteNotifiee.getId());

        restAlerteNotifieeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlerteNotifiee.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlerteNotifiee))
            )
            .andExpect(status().isOk());

        // Validate the AlerteNotifiee in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlerteNotifieeUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAlerteNotifiee, alerteNotifiee),
            getPersistedAlerteNotifiee(alerteNotifiee)
        );
    }

    @Test
    @Transactional
    void fullUpdateAlerteNotifieeWithPatch() throws Exception {
        // Initialize the database
        insertedAlerteNotifiee = alerteNotifieeRepository.saveAndFlush(alerteNotifiee);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alerteNotifiee using partial update
        AlerteNotifiee partialUpdatedAlerteNotifiee = new AlerteNotifiee();
        partialUpdatedAlerteNotifiee.setId(alerteNotifiee.getId());

        partialUpdatedAlerteNotifiee.dateEnvoi(UPDATED_DATE_ENVOI);

        restAlerteNotifieeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlerteNotifiee.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlerteNotifiee))
            )
            .andExpect(status().isOk());

        // Validate the AlerteNotifiee in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlerteNotifieeUpdatableFieldsEquals(partialUpdatedAlerteNotifiee, getPersistedAlerteNotifiee(partialUpdatedAlerteNotifiee));
    }

    @Test
    @Transactional
    void patchNonExistingAlerteNotifiee() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerteNotifiee.setId(longCount.incrementAndGet());

        // Create the AlerteNotifiee
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlerteNotifieeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alerteNotifieeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alerteNotifieeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlerteNotifiee() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerteNotifiee.setId(longCount.incrementAndGet());

        // Create the AlerteNotifiee
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteNotifieeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alerteNotifieeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlerteNotifiee() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerteNotifiee.setId(longCount.incrementAndGet());

        // Create the AlerteNotifiee
        AlerteNotifieeDTO alerteNotifieeDTO = alerteNotifieeMapper.toDto(alerteNotifiee);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteNotifieeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(alerteNotifieeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlerteNotifiee in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAlerteNotifiee() throws Exception {
        // Initialize the database
        insertedAlerteNotifiee = alerteNotifieeRepository.saveAndFlush(alerteNotifiee);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the alerteNotifiee
        restAlerteNotifieeMockMvc
            .perform(delete(ENTITY_API_URL_ID, alerteNotifiee.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return alerteNotifieeRepository.count();
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

    protected AlerteNotifiee getPersistedAlerteNotifiee(AlerteNotifiee alerteNotifiee) {
        return alerteNotifieeRepository.findById(alerteNotifiee.getId()).orElseThrow();
    }

    protected void assertPersistedAlerteNotifieeToMatchAllProperties(AlerteNotifiee expectedAlerteNotifiee) {
        assertAlerteNotifieeAllPropertiesEquals(expectedAlerteNotifiee, getPersistedAlerteNotifiee(expectedAlerteNotifiee));
    }

    protected void assertPersistedAlerteNotifieeToMatchUpdatableProperties(AlerteNotifiee expectedAlerteNotifiee) {
        assertAlerteNotifieeAllUpdatablePropertiesEquals(expectedAlerteNotifiee, getPersistedAlerteNotifiee(expectedAlerteNotifiee));
    }
}
