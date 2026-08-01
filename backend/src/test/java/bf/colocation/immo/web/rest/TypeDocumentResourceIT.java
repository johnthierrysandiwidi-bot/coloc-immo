package bf.colocation.immo.web.rest;

import static bf.colocation.immo.domain.TypeDocumentAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.TypeDocument;
import bf.colocation.immo.repository.TypeDocumentRepository;
import bf.colocation.immo.service.dto.TypeDocumentDTO;
import bf.colocation.immo.service.mapper.TypeDocumentMapper;
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
 * Integration tests for the {@link TypeDocumentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TypeDocumentResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/type-documents";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TypeDocumentRepository typeDocumentRepository;

    @Autowired
    private TypeDocumentMapper typeDocumentMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTypeDocumentMockMvc;

    private TypeDocument typeDocument;

    private TypeDocument insertedTypeDocument;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypeDocument createEntity() {
        return new TypeDocument().nom(DEFAULT_NOM).description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypeDocument createUpdatedEntity() {
        return new TypeDocument().nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
    }

    @BeforeEach
    void initTest() {
        typeDocument = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTypeDocument != null) {
            typeDocumentRepository.delete(insertedTypeDocument);
            insertedTypeDocument = null;
        }
    }

    @Test
    @Transactional
    void createTypeDocument() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TypeDocument
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);
        var returnedTypeDocumentDTO = om.readValue(
            restTypeDocumentMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeDocumentDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TypeDocumentDTO.class
        );

        // Validate the TypeDocument in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTypeDocument = typeDocumentMapper.toEntity(returnedTypeDocumentDTO);
        assertTypeDocumentUpdatableFieldsEquals(returnedTypeDocument, getPersistedTypeDocument(returnedTypeDocument));

        insertedTypeDocument = returnedTypeDocument;
    }

    @Test
    @Transactional
    void createTypeDocumentWithExistingId() throws Exception {
        // Create the TypeDocument with an existing ID
        typeDocument.setId(1L);
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTypeDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeDocumentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        typeDocument.setNom(null);

        // Create the TypeDocument, which fails.
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        restTypeDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeDocumentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTypeDocuments() throws Exception {
        // Initialize the database
        insertedTypeDocument = typeDocumentRepository.saveAndFlush(typeDocument);

        // Get all the typeDocumentList
        restTypeDocumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(typeDocument.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getTypeDocument() throws Exception {
        // Initialize the database
        insertedTypeDocument = typeDocumentRepository.saveAndFlush(typeDocument);

        // Get the typeDocument
        restTypeDocumentMockMvc
            .perform(get(ENTITY_API_URL_ID, typeDocument.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(typeDocument.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getNonExistingTypeDocument() throws Exception {
        // Get the typeDocument
        restTypeDocumentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTypeDocument() throws Exception {
        // Initialize the database
        insertedTypeDocument = typeDocumentRepository.saveAndFlush(typeDocument);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typeDocument
        TypeDocument updatedTypeDocument = typeDocumentRepository.findById(typeDocument.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTypeDocument are not directly saved in db
        em.detach(updatedTypeDocument);
        updatedTypeDocument.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(updatedTypeDocument);

        restTypeDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, typeDocumentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typeDocumentDTO))
            )
            .andExpect(status().isOk());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTypeDocumentToMatchAllProperties(updatedTypeDocument);
    }

    @Test
    @Transactional
    void putNonExistingTypeDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeDocument.setId(longCount.incrementAndGet());

        // Create the TypeDocument
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypeDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, typeDocumentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typeDocumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTypeDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeDocument.setId(longCount.incrementAndGet());

        // Create the TypeDocument
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(typeDocumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTypeDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeDocument.setId(longCount.incrementAndGet());

        // Create the TypeDocument
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeDocumentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(typeDocumentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTypeDocumentWithPatch() throws Exception {
        // Initialize the database
        insertedTypeDocument = typeDocumentRepository.saveAndFlush(typeDocument);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typeDocument using partial update
        TypeDocument partialUpdatedTypeDocument = new TypeDocument();
        partialUpdatedTypeDocument.setId(typeDocument.getId());

        restTypeDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypeDocument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTypeDocument))
            )
            .andExpect(status().isOk());

        // Validate the TypeDocument in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTypeDocumentUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTypeDocument, typeDocument),
            getPersistedTypeDocument(typeDocument)
        );
    }

    @Test
    @Transactional
    void fullUpdateTypeDocumentWithPatch() throws Exception {
        // Initialize the database
        insertedTypeDocument = typeDocumentRepository.saveAndFlush(typeDocument);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the typeDocument using partial update
        TypeDocument partialUpdatedTypeDocument = new TypeDocument();
        partialUpdatedTypeDocument.setId(typeDocument.getId());

        partialUpdatedTypeDocument.nom(UPDATED_NOM).description(UPDATED_DESCRIPTION);

        restTypeDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypeDocument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTypeDocument))
            )
            .andExpect(status().isOk());

        // Validate the TypeDocument in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTypeDocumentUpdatableFieldsEquals(partialUpdatedTypeDocument, getPersistedTypeDocument(partialUpdatedTypeDocument));
    }

    @Test
    @Transactional
    void patchNonExistingTypeDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeDocument.setId(longCount.incrementAndGet());

        // Create the TypeDocument
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypeDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, typeDocumentDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(typeDocumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTypeDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeDocument.setId(longCount.incrementAndGet());

        // Create the TypeDocument
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(typeDocumentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTypeDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        typeDocument.setId(longCount.incrementAndGet());

        // Create the TypeDocument
        TypeDocumentDTO typeDocumentDTO = typeDocumentMapper.toDto(typeDocument);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeDocumentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(typeDocumentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypeDocument in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTypeDocument() throws Exception {
        // Initialize the database
        insertedTypeDocument = typeDocumentRepository.saveAndFlush(typeDocument);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the typeDocument
        restTypeDocumentMockMvc
            .perform(delete(ENTITY_API_URL_ID, typeDocument.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return typeDocumentRepository.count();
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

    protected TypeDocument getPersistedTypeDocument(TypeDocument typeDocument) {
        return typeDocumentRepository.findById(typeDocument.getId()).orElseThrow();
    }

    protected void assertPersistedTypeDocumentToMatchAllProperties(TypeDocument expectedTypeDocument) {
        assertTypeDocumentAllPropertiesEquals(expectedTypeDocument, getPersistedTypeDocument(expectedTypeDocument));
    }

    protected void assertPersistedTypeDocumentToMatchUpdatableProperties(TypeDocument expectedTypeDocument) {
        assertTypeDocumentAllUpdatablePropertiesEquals(expectedTypeDocument, getPersistedTypeDocument(expectedTypeDocument));
    }
}
