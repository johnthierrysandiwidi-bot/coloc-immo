package bf.colocation.immo.web.rest;

import static bf.colocation.immo.domain.TypeImmobilierAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.TypeImmobilier;
import bf.colocation.immo.repository.TypeImmobilierRepository;
import bf.colocation.immo.service.dto.TypeImmobilierDTO;
import bf.colocation.immo.service.mapper.TypeImmobilierMapper;
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
 * Integration tests for the {@link TypeImmobilierResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TypeImmobilierResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/type-immobiliers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TypeImmobilierRepository typeImmobilierRepository;

    @Autowired
    private TypeImmobilierMapper typeImmobilierMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTypeImmobilierMockMvc;

    private TypeImmobilier typeImmobilier;

    private TypeImmobilier insertedTypeImmobilier;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypeImmobilier createEntity() {
        return new TypeImmobilier().nom(DEFAULT_NOM).description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypeImmobilier createUpdatedEntity() {
        return new TypeImmobilier().nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
    }

    @BeforeEach
    void initTest() {
        typeImmobilier = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTypeImmobilier != null) {
            typeImmobilierRepository.delete(insertedTypeImmobilier);
            insertedTypeImmobilier = null;
        }
    }

    @Test
    @Transactional
    void createTypeImmobilier() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TypeImmobilier
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);
        var returnedTypeImmobilierDTO = om.readValue(
            restTypeImmobilierMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeImmobilierDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TypeImmobilierDTO.class
        );

        // Validate the TypeImmobilier in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTypeImmobilier = typeImmobilierMapper.toEntity(returnedTypeImmobilierDTO);
        assertTypeImmobilierUpdatableFieldsEquals(returnedTypeImmobilier, getPersistedTypeImmobilier(returnedTypeImmobilier));

        insertedTypeImmobilier = returnedTypeImmobilier;
    }

    @Test
    @Transactional
    void createTypeImmobilierWithExistingId() throws Exception {
        // Create the TypeImmobilier with an existing ID
        typeImmobilier.setId(1L);
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTypeImmobilierMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeImmobilierDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        typeImmobilier.setNom(null);

        // Create the TypeImmobilier, which fails.
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        restTypeImmobilierMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeImmobilierDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTypeImmobiliers() throws Exception {
        // Initialize the database
        insertedTypeImmobilier = typeImmobilierRepository.saveAndFlush(typeImmobilier);

        // Get all the typeImmobilierList
        restTypeImmobilierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(typeImmobilier.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getTypeImmobilier() throws Exception {
        // Initialize the database
        insertedTypeImmobilier = typeImmobilierRepository.saveAndFlush(typeImmobilier);

        // Get the typeImmobilier
        restTypeImmobilierMockMvc
            .perform(get(ENTITY_API_URL_ID, typeImmobilier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(typeImmobilier.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingTypeImmobilier() throws Exception {
        // Get the typeImmobilier
        restTypeImmobilierMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTypeImmobilier() throws Exception {
        // Initialize the database
        insertedTypeImmobilier = typeImmobilierRepository.saveAndFlush(typeImmobilier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typeImmobilier
        TypeImmobilier updatedTypeImmobilier = typeImmobilierRepository.findById(typeImmobilier.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTypeImmobilier are not directly saved in db
        em.detach(updatedTypeImmobilier);
        updatedTypeImmobilier.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(updatedTypeImmobilier);

        restTypeImmobilierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, typeImmobilierDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typeImmobilierDTO))
            )
            .andExpect(status().isOk());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTypeImmobilierToMatchAllProperties(updatedTypeImmobilier);
    }

    @Test
    @Transactional
    void putNonExistingTypeImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeImmobilier.setId(longCount.incrementAndGet());

        // Create the TypeImmobilier
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypeImmobilierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, typeImmobilierDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typeImmobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTypeImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeImmobilier.setId(longCount.incrementAndGet());

        // Create the TypeImmobilier
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeImmobilierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typeImmobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTypeImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeImmobilier.setId(longCount.incrementAndGet());

        // Create the TypeImmobilier
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeImmobilierMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeImmobilierDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTypeImmobilierWithPatch() throws Exception {
        // Initialize the database
        insertedTypeImmobilier = typeImmobilierRepository.saveAndFlush(typeImmobilier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typeImmobilier using partial update
        TypeImmobilier partialUpdatedTypeImmobilier = new TypeImmobilier();
        partialUpdatedTypeImmobilier.setId(typeImmobilier.getId());

        restTypeImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypeImmobilier.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTypeImmobilier))
            )
            .andExpect(status().isOk());

        // Validate the TypeImmobilier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTypeImmobilierUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTypeImmobilier, typeImmobilier),
            getPersistedTypeImmobilier(typeImmobilier)
        );
    }

    @Test
    @Transactional
    void fullUpdateTypeImmobilierWithPatch() throws Exception {
        // Initialize the database
        insertedTypeImmobilier = typeImmobilierRepository.saveAndFlush(typeImmobilier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typeImmobilier using partial update
        TypeImmobilier partialUpdatedTypeImmobilier = new TypeImmobilier();
        partialUpdatedTypeImmobilier.setId(typeImmobilier.getId());

        partialUpdatedTypeImmobilier.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);

        restTypeImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypeImmobilier.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTypeImmobilier))
            )
            .andExpect(status().isOk());

        // Validate the TypeImmobilier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTypeImmobilierUpdatableFieldsEquals(partialUpdatedTypeImmobilier, getPersistedTypeImmobilier(partialUpdatedTypeImmobilier));
    }

    @Test
    @Transactional
    void patchNonExistingTypeImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeImmobilier.setId(longCount.incrementAndGet());

        // Create the TypeImmobilier
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypeImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, typeImmobilierDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(typeImmobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTypeImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeImmobilier.setId(longCount.incrementAndGet());

        // Create the TypeImmobilier
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(typeImmobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTypeImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeImmobilier.setId(longCount.incrementAndGet());

        // Create the TypeImmobilier
        TypeImmobilierDTO typeImmobilierDTO = typeImmobilierMapper.toDto(typeImmobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeImmobilierMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(typeImmobilierDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypeImmobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTypeImmobilier() throws Exception {
        // Initialize the database
        insertedTypeImmobilier = typeImmobilierRepository.saveAndFlush(typeImmobilier);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the typeImmobilier
        restTypeImmobilierMockMvc
            .perform(delete(ENTITY_API_URL_ID, typeImmobilier.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return typeImmobilierRepository.count();
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

    protected TypeImmobilier getPersistedTypeImmobilier(TypeImmobilier typeImmobilier) {
        return typeImmobilierRepository.findById(typeImmobilier.getId()).orElseThrow();
    }

    protected void assertPersistedTypeImmobilierToMatchAllProperties(TypeImmobilier expectedTypeImmobilier) {
        assertTypeImmobilierAllPropertiesEquals(expectedTypeImmobilier, getPersistedTypeImmobilier(expectedTypeImmobilier));
    }

    protected void assertPersistedTypeImmobilierToMatchUpdatableProperties(TypeImmobilier expectedTypeImmobilier) {
        assertTypeImmobilierAllUpdatablePropertiesEquals(expectedTypeImmobilier, getPersistedTypeImmobilier(expectedTypeImmobilier));
    }
}
