package bf.colocation.immo.web.rest;

import static bf.colocation.immo.domain.QuartierAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.domain.Quartier;
import bf.colocation.immo.repository.QuartierRepository;
import bf.colocation.immo.service.QuartierService;
import bf.colocation.immo.service.dto.QuartierDTO;
import bf.colocation.immo.service.mapper.QuartierMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link QuartierResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class QuartierResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/quartiers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private QuartierRepository quartierRepository;

    @Mock
    private QuartierRepository quartierRepositoryMock;

    @Autowired
    private QuartierMapper quartierMapper;

    @Mock
    private QuartierService quartierServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restQuartierMockMvc;

    private Quartier quartier;

    private Quartier insertedQuartier;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Quartier createEntity(EntityManager em) {
        Quartier quartier = new Quartier().nom(DEFAULT_NOM).description(DEFAULT_DESCRIPTION);
        // Add required entity
        Localite localite;
        if (TestUtil.findAll(em, Localite.class).isEmpty()) {
            localite = LocaliteResourceIT.createEntity();
            em.persist(localite);
            em.flush();
        } else {
            localite = TestUtil.findAll(em, Localite.class).get(0);
        }
        quartier.setLocalite(localite);
        return quartier;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Quartier createUpdatedEntity(EntityManager em) {
        Quartier updatedQuartier = new Quartier().nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
        // Add required entity
        Localite localite;
        if (TestUtil.findAll(em, Localite.class).isEmpty()) {
            localite = LocaliteResourceIT.createUpdatedEntity();
            em.persist(localite);
            em.flush();
        } else {
            localite = TestUtil.findAll(em, Localite.class).get(0);
        }
        updatedQuartier.setLocalite(localite);
        return updatedQuartier;
    }

    @BeforeEach
    void initTest() {
        quartier = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedQuartier != null) {
            quartierRepository.delete(insertedQuartier);
            insertedQuartier = null;
        }
    }

    @Test
    @Transactional
    void createQuartier() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Quartier
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);
        var returnedQuartierDTO = om.readValue(
            restQuartierMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quartierDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            QuartierDTO.class
        );

        // Validate the Quartier in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedQuartier = quartierMapper.toEntity(returnedQuartierDTO);
        assertQuartierUpdatableFieldsEquals(returnedQuartier, getPersistedQuartier(returnedQuartier));

        insertedQuartier = returnedQuartier;
    }

    @Test
    @Transactional
    void createQuartierWithExistingId() throws Exception {
        // Create the Quartier with an existing ID
        quartier.setId(1L);
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restQuartierMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quartierDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        quartier.setNom(null);

        // Create the Quartier, which fails.
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        restQuartierMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quartierDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllQuartiers() throws Exception {
        // Initialize the database
        insertedQuartier = quartierRepository.saveAndFlush(quartier);

        // Get all the quartierList
        restQuartierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(quartier.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllQuartiersWithEagerRelationshipsIsEnabled() throws Exception {
        when(quartierServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restQuartierMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(quartierServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllQuartiersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(quartierServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restQuartierMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(quartierRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getQuartier() throws Exception {
        // Initialize the database
        insertedQuartier = quartierRepository.saveAndFlush(quartier);

        // Get the quartier
        restQuartierMockMvc
            .perform(get(ENTITY_API_URL_ID, quartier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(quartier.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingQuartier() throws Exception {
        // Get the quartier
        restQuartierMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingQuartier() throws Exception {
        // Initialize the database
        insertedQuartier = quartierRepository.saveAndFlush(quartier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quartier
        Quartier updatedQuartier = quartierRepository.findById(quartier.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedQuartier are not directly saved in db
        em.detach(updatedQuartier);
        updatedQuartier.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
        QuartierDTO quartierDTO = quartierMapper.toDto(updatedQuartier);

        restQuartierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, quartierDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(quartierDTO))
            )
            .andExpect(status().isOk());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedQuartierToMatchAllProperties(updatedQuartier);
    }

    @Test
    @Transactional
    void putNonExistingQuartier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quartier.setId(longCount.incrementAndGet());

        // Create the Quartier
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restQuartierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, quartierDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(quartierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchQuartier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quartier.setId(longCount.incrementAndGet());

        // Create the Quartier
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuartierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(quartierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamQuartier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quartier.setId(longCount.incrementAndGet());

        // Create the Quartier
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuartierMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(quartierDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateQuartierWithPatch() throws Exception {
        // Initialize the database
        insertedQuartier = quartierRepository.saveAndFlush(quartier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quartier using partial update
        Quartier partialUpdatedQuartier = new Quartier();
        partialUpdatedQuartier.setId(quartier.getId());

        restQuartierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedQuartier.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedQuartier))
            )
            .andExpect(status().isOk());

        // Validate the Quartier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertQuartierUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedQuartier, quartier), getPersistedQuartier(quartier));
    }

    @Test
    @Transactional
    void fullUpdateQuartierWithPatch() throws Exception {
        // Initialize the database
        insertedQuartier = quartierRepository.saveAndFlush(quartier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the quartier using partial update
        Quartier partialUpdatedQuartier = new Quartier();
        partialUpdatedQuartier.setId(quartier.getId());

        partialUpdatedQuartier.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);

        restQuartierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedQuartier.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedQuartier))
            )
            .andExpect(status().isOk());

        // Validate the Quartier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertQuartierUpdatableFieldsEquals(partialUpdatedQuartier, getPersistedQuartier(partialUpdatedQuartier));
    }

    @Test
    @Transactional
    void patchNonExistingQuartier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quartier.setId(longCount.incrementAndGet());

        // Create the Quartier
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restQuartierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, quartierDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(quartierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchQuartier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quartier.setId(longCount.incrementAndGet());

        // Create the Quartier
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuartierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(quartierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamQuartier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        quartier.setId(longCount.incrementAndGet());

        // Create the Quartier
        QuartierDTO quartierDTO = quartierMapper.toDto(quartier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restQuartierMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(quartierDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Quartier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteQuartier() throws Exception {
        // Initialize the database
        insertedQuartier = quartierRepository.saveAndFlush(quartier);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the quartier
        restQuartierMockMvc
            .perform(delete(ENTITY_API_URL_ID, quartier.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return quartierRepository.count();
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

    protected Quartier getPersistedQuartier(Quartier quartier) {
        return quartierRepository.findById(quartier.getId()).orElseThrow();
    }

    protected void assertPersistedQuartierToMatchAllProperties(Quartier expectedQuartier) {
        assertQuartierAllPropertiesEquals(expectedQuartier, getPersistedQuartier(expectedQuartier));
    }

    protected void assertPersistedQuartierToMatchUpdatableProperties(Quartier expectedQuartier) {
        assertQuartierAllUpdatablePropertiesEquals(expectedQuartier, getPersistedQuartier(expectedQuartier));
    }
}
