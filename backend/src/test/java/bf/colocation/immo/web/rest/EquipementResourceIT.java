package bf.colocation.immo.web.rest;

import static bf.colocation.immo.domain.EquipementAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Equipement;
import bf.colocation.immo.repository.EquipementRepository;
import bf.colocation.immo.service.dto.EquipementDTO;
import bf.colocation.immo.service.mapper.EquipementMapper;
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
 * Integration tests for the {@link EquipementResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EquipementResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_ICONE = "AAAAAAAAAA";
    private static final String UPDATED_ICONE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/equipements";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private EquipementMapper equipementMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEquipementMockMvc;

    private Equipement equipement;

    private Equipement insertedEquipement;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Equipement createEntity() {
        return new Equipement().nom(DEFAULT_NOM).icone(DEFAULT_ICONE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Equipement createUpdatedEntity() {
        return new Equipement().nom(UPDATED_NOM).icone(UPDATED_ICONE);
    }

    @BeforeEach
    void initTest() {
        equipement = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEquipement != null) {
            equipementRepository.delete(insertedEquipement);
            insertedEquipement = null;
        }
    }

    @Test
    @Transactional
    void createEquipement() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Equipement
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);
        var returnedEquipementDTO = om.readValue(
            restEquipementMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipementDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EquipementDTO.class
        );

        // Validate the Equipement in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEquipement = equipementMapper.toEntity(returnedEquipementDTO);
        assertEquipementUpdatableFieldsEquals(returnedEquipement, getPersistedEquipement(returnedEquipement));

        insertedEquipement = returnedEquipement;
    }

    @Test
    @Transactional
    void createEquipementWithExistingId() throws Exception {
        // Create the Equipement with an existing ID
        equipement.setId(1L);
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEquipementMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipementDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        equipement.setNom(null);

        // Create the Equipement, which fails.
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        restEquipementMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipementDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEquipements() throws Exception {
        // Initialize the database
        insertedEquipement = equipementRepository.saveAndFlush(equipement);

        // Get all the equipementList
        restEquipementMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(equipement.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].icone").value(hasItem(DEFAULT_ICONE)));
    }

    @Test
    @Transactional
    void getEquipement() throws Exception {
        // Initialize the database
        insertedEquipement = equipementRepository.saveAndFlush(equipement);

        // Get the equipement
        restEquipementMockMvc
            .perform(get(ENTITY_API_URL_ID, equipement.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(equipement.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.icone").value(DEFAULT_ICONE));
    }

    @Test
    @Transactional
    void getNonExistingEquipement() throws Exception {
        // Get the equipement
        restEquipementMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEquipement() throws Exception {
        // Initialize the database
        insertedEquipement = equipementRepository.saveAndFlush(equipement);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the equipement
        Equipement updatedEquipement = equipementRepository.findById(equipement.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEquipement are not directly saved in db
        em.detach(updatedEquipement);
        updatedEquipement.nom(UPDATED_NOM).icone(UPDATED_ICONE);
        EquipementDTO equipementDTO = equipementMapper.toDto(updatedEquipement);

        restEquipementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, equipementDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(equipementDTO))
            )
            .andExpect(status().isOk());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEquipementToMatchAllProperties(updatedEquipement);
    }

    @Test
    @Transactional
    void putNonExistingEquipement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipement.setId(longCount.incrementAndGet());

        // Create the Equipement
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEquipementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, equipementDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(equipementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEquipement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipement.setId(longCount.incrementAndGet());

        // Create the Equipement
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipementMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(equipementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEquipement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipement.setId(longCount.incrementAndGet());

        // Create the Equipement
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipementMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipementDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEquipementWithPatch() throws Exception {
        // Initialize the database
        insertedEquipement = equipementRepository.saveAndFlush(equipement);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the equipement using partial update
        Equipement partialUpdatedEquipement = new Equipement();
        partialUpdatedEquipement.setId(equipement.getId());

        partialUpdatedEquipement.icone(UPDATED_ICONE);

        restEquipementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEquipement.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEquipement))
            )
            .andExpect(status().isOk());

        // Validate the Equipement in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEquipementUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEquipement, equipement),
            getPersistedEquipement(equipement)
        );
    }

    @Test
    @Transactional
    void fullUpdateEquipementWithPatch() throws Exception {
        // Initialize the database
        insertedEquipement = equipementRepository.saveAndFlush(equipement);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the equipement using partial update
        Equipement partialUpdatedEquipement = new Equipement();
        partialUpdatedEquipement.setId(equipement.getId());

        partialUpdatedEquipement.nom(UPDATED_NOM).icone(UPDATED_ICONE);

        restEquipementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEquipement.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEquipement))
            )
            .andExpect(status().isOk());

        // Validate the Equipement in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEquipementUpdatableFieldsEquals(partialUpdatedEquipement, getPersistedEquipement(partialUpdatedEquipement));
    }

    @Test
    @Transactional
    void patchNonExistingEquipement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipement.setId(longCount.incrementAndGet());

        // Create the Equipement
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEquipementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, equipementDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(equipementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEquipement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipement.setId(longCount.incrementAndGet());

        // Create the Equipement
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipementMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(equipementDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEquipement() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipement.setId(longCount.incrementAndGet());

        // Create the Equipement
        EquipementDTO equipementDTO = equipementMapper.toDto(equipement);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipementMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(equipementDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Equipement in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEquipement() throws Exception {
        // Initialize the database
        insertedEquipement = equipementRepository.saveAndFlush(equipement);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the equipement
        restEquipementMockMvc
            .perform(delete(ENTITY_API_URL_ID, equipement.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return equipementRepository.count();
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

    protected Equipement getPersistedEquipement(Equipement equipement) {
        return equipementRepository.findById(equipement.getId()).orElseThrow();
    }

    protected void assertPersistedEquipementToMatchAllProperties(Equipement expectedEquipement) {
        assertEquipementAllPropertiesEquals(expectedEquipement, getPersistedEquipement(expectedEquipement));
    }

    protected void assertPersistedEquipementToMatchUpdatableProperties(Equipement expectedEquipement) {
        assertEquipementAllUpdatablePropertiesEquals(expectedEquipement, getPersistedEquipement(expectedEquipement));
    }
}
