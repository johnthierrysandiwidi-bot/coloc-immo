package bf.colocation.immo.web.rest;

import static bf.colocation.immo.domain.LocaliteAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.repository.LocaliteRepository;
import bf.colocation.immo.service.dto.LocaliteDTO;
import bf.colocation.immo.service.mapper.LocaliteMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link LocaliteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class LocaliteResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/localites";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LocaliteRepository localiteRepository;

    @Autowired
    private LocaliteMapper localiteMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLocaliteMockMvc;

    private Localite localite;

    private Localite insertedLocalite;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Localite createEntity() {
        return new Localite().nom(DEFAULT_NOM).description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Localite createUpdatedEntity() {
        return new Localite().nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
    }

    @BeforeEach
    void initTest() {
        localite = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedLocalite != null) {
            localiteRepository.delete(insertedLocalite);
            insertedLocalite = null;
        }
    }

    @Test
    @Transactional
    void createLocalite() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Localite
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);
        var returnedLocaliteDTO = om.readValue(
            restLocaliteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(localiteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            LocaliteDTO.class
        );

        // Validate the Localite in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedLocalite = localiteMapper.toEntity(returnedLocaliteDTO);
        assertLocaliteUpdatableFieldsEquals(returnedLocalite, getPersistedLocalite(returnedLocalite));

        insertedLocalite = returnedLocalite;
    }

    @Test
    @Transactional
    void createLocaliteWithExistingId() throws Exception {
        // Create the Localite with an existing ID
        localite.setId(1L);
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLocaliteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(localiteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        localite.setNom(null);

        // Create the Localite, which fails.
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        restLocaliteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(localiteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLocalites() throws Exception {
        // Initialize the database
        insertedLocalite = localiteRepository.saveAndFlush(localite);

        // Get all the localiteList
        restLocaliteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(localite.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getLocalite() throws Exception {
        // Initialize the database
        insertedLocalite = localiteRepository.saveAndFlush(localite);

        // Get the localite
        restLocaliteMockMvc
            .perform(get(ENTITY_API_URL_ID, localite.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(localite.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingLocalite() throws Exception {
        // Get the localite
        restLocaliteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLocalite() throws Exception {
        // Initialize the database
        insertedLocalite = localiteRepository.saveAndFlush(localite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the localite
        Localite updatedLocalite = localiteRepository.findById(localite.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLocalite are not directly saved in db
        em.detach(updatedLocalite);
        updatedLocalite.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
        LocaliteDTO localiteDTO = localiteMapper.toDto(updatedLocalite);

        restLocaliteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, localiteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(localiteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLocaliteToMatchAllProperties(updatedLocalite);
    }

    @Test
    @Transactional
    void putNonExistingLocalite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        localite.setId(longCount.incrementAndGet());

        // Create the Localite
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLocaliteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, localiteDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(localiteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLocalite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        localite.setId(longCount.incrementAndGet());

        // Create the Localite
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLocaliteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(localiteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLocalite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        localite.setId(longCount.incrementAndGet());

        // Create the Localite
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLocaliteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(localiteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLocaliteWithPatch() throws Exception {
        // Initialize the database
        insertedLocalite = localiteRepository.saveAndFlush(localite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the localite using partial update
        Localite partialUpdatedLocalite = new Localite();
        partialUpdatedLocalite.setId(localite.getId());

        partialUpdatedLocalite.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);

        restLocaliteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLocalite.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLocalite))
            )
            .andExpect(status().isOk());

        // Validate the Localite in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLocaliteUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLocalite, localite), getPersistedLocalite(localite));
    }

    @Test
    @Transactional
    void fullUpdateLocaliteWithPatch() throws Exception {
        // Initialize the database
        insertedLocalite = localiteRepository.saveAndFlush(localite);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the localite using partial update
        Localite partialUpdatedLocalite = new Localite();
        partialUpdatedLocalite.setId(localite.getId());

        partialUpdatedLocalite.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);

        restLocaliteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLocalite.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLocalite))
            )
            .andExpect(status().isOk());

        // Validate the Localite in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLocaliteUpdatableFieldsEquals(partialUpdatedLocalite, getPersistedLocalite(partialUpdatedLocalite));
    }

    @Test
    @Transactional
    void patchNonExistingLocalite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        localite.setId(longCount.incrementAndGet());

        // Create the Localite
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLocaliteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, localiteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(localiteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLocalite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        localite.setId(longCount.incrementAndGet());

        // Create the Localite
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLocaliteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(localiteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLocalite() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        localite.setId(longCount.incrementAndGet());

        // Create the Localite
        LocaliteDTO localiteDTO = localiteMapper.toDto(localite);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLocaliteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(localiteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Localite in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLocalite() throws Exception {
        // Initialize the database
        insertedLocalite = localiteRepository.saveAndFlush(localite);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the localite
        restLocaliteMockMvc
            .perform(delete(ENTITY_API_URL_ID, localite.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return localiteRepository.count();
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

    protected Localite getPersistedLocalite(Localite localite) {
        return localiteRepository.findById(localite.getId()).orElseThrow();
    }

    protected void assertPersistedLocaliteToMatchAllProperties(Localite expectedLocalite) {
        assertLocaliteAllPropertiesEquals(expectedLocalite, getPersistedLocalite(expectedLocalite));
    }

    protected void assertPersistedLocaliteToMatchUpdatableProperties(Localite expectedLocalite) {
        assertLocaliteAllUpdatablePropertiesEquals(expectedLocalite, getPersistedLocalite(expectedLocalite));
    }
}
