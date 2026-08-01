package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.VueAnnonceAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.VueAnnonce;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.repository.VueAnnonceRepository;
import bf.colocation.immo.service.VueAnnonceService;
import bf.colocation.immo.service.dto.VueAnnonceDTO;
import bf.colocation.immo.service.mapper.VueAnnonceMapper;
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
 * Integration tests for the {@link VueAnnonceResource} REST controller.
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
class VueAnnonceResourceIT {

    private static final Instant DEFAULT_DATE_VUE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_VUE = Instant.ofEpochMilli(1702714037224L);

    private static final String DEFAULT_ADRESSE_IP = "AAAAAAAAAA";
    private static final String UPDATED_ADRESSE_IP = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/vue-annonces";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private VueAnnonceRepository vueAnnonceRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private VueAnnonceRepository vueAnnonceRepositoryMock;

    @Autowired
    private VueAnnonceMapper vueAnnonceMapper;

    @Mock
    private VueAnnonceService vueAnnonceServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restVueAnnonceMockMvc;

    private VueAnnonce vueAnnonce;

    private VueAnnonce insertedVueAnnonce;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VueAnnonce createEntity(EntityManager em) {
        VueAnnonce vueAnnonce = new VueAnnonce().dateVue(DEFAULT_DATE_VUE).adresseIp(DEFAULT_ADRESSE_IP);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        vueAnnonce.setAnnonce(annonce);
        return vueAnnonce;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static VueAnnonce createUpdatedEntity(EntityManager em) {
        VueAnnonce updatedVueAnnonce = new VueAnnonce().dateVue(UPDATED_DATE_VUE).adresseIp(UPDATED_ADRESSE_IP);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createUpdatedEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        updatedVueAnnonce.setAnnonce(annonce);
        return updatedVueAnnonce;
    }

    @BeforeEach
    void initTest() {
        vueAnnonce = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedVueAnnonce != null) {
            vueAnnonceRepository.delete(insertedVueAnnonce);
            insertedVueAnnonce = null;
        }
    }

    @Test
    @Transactional
    void createVueAnnonce() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the VueAnnonce
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);
        var returnedVueAnnonceDTO = om.readValue(
            restVueAnnonceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vueAnnonceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            VueAnnonceDTO.class
        );

        // Validate the VueAnnonce in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedVueAnnonce = vueAnnonceMapper.toEntity(returnedVueAnnonceDTO);
        assertVueAnnonceUpdatableFieldsEquals(returnedVueAnnonce, getPersistedVueAnnonce(returnedVueAnnonce));

        insertedVueAnnonce = returnedVueAnnonce;
    }

    @Test
    @Transactional
    void createVueAnnonceWithExistingId() throws Exception {
        // Create the VueAnnonce with an existing ID
        vueAnnonce.setId(1L);
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restVueAnnonceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vueAnnonceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateVueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        vueAnnonce.setDateVue(null);

        // Create the VueAnnonce, which fails.
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        restVueAnnonceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vueAnnonceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllVueAnnonces() throws Exception {
        // Initialize the database
        insertedVueAnnonce = vueAnnonceRepository.saveAndFlush(vueAnnonce);

        // Get all the vueAnnonceList
        restVueAnnonceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(vueAnnonce.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateVue").value(hasItem(DEFAULT_DATE_VUE.toString())))
            .andExpect(jsonPath("$.[*].adresseIp").value(hasItem(DEFAULT_ADRESSE_IP)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVueAnnoncesWithEagerRelationshipsIsEnabled() throws Exception {
        when(vueAnnonceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVueAnnonceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(vueAnnonceServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllVueAnnoncesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(vueAnnonceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restVueAnnonceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(vueAnnonceRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getVueAnnonce() throws Exception {
        // Initialize the database
        insertedVueAnnonce = vueAnnonceRepository.saveAndFlush(vueAnnonce);

        // Get the vueAnnonce
        restVueAnnonceMockMvc
            .perform(get(ENTITY_API_URL_ID, vueAnnonce.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(vueAnnonce.getId().intValue()))
            .andExpect(jsonPath("$.dateVue").value(DEFAULT_DATE_VUE.toString()))
            .andExpect(jsonPath("$.adresseIp").value(DEFAULT_ADRESSE_IP));
    }

    @Test
    @Transactional
    void getNonExistingVueAnnonce() throws Exception {
        // Get the vueAnnonce
        restVueAnnonceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingVueAnnonce() throws Exception {
        // Initialize the database
        insertedVueAnnonce = vueAnnonceRepository.saveAndFlush(vueAnnonce);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vueAnnonce
        VueAnnonce updatedVueAnnonce = vueAnnonceRepository.findById(vueAnnonce.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedVueAnnonce are not directly saved in db
        em.detach(updatedVueAnnonce);
        updatedVueAnnonce.dateVue(UPDATED_DATE_VUE).adresseIp(UPDATED_ADRESSE_IP);
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(updatedVueAnnonce);

        restVueAnnonceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, vueAnnonceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vueAnnonceDTO))
            )
            .andExpect(status().isOk());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedVueAnnonceToMatchAllProperties(updatedVueAnnonce);
    }

    @Test
    @Transactional
    void putNonExistingVueAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vueAnnonce.setId(longCount.incrementAndGet());

        // Create the VueAnnonce
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVueAnnonceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, vueAnnonceDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vueAnnonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchVueAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vueAnnonce.setId(longCount.incrementAndGet());

        // Create the VueAnnonce
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVueAnnonceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(vueAnnonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamVueAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vueAnnonce.setId(longCount.incrementAndGet());

        // Create the VueAnnonce
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVueAnnonceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(vueAnnonceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateVueAnnonceWithPatch() throws Exception {
        // Initialize the database
        insertedVueAnnonce = vueAnnonceRepository.saveAndFlush(vueAnnonce);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vueAnnonce using partial update
        VueAnnonce partialUpdatedVueAnnonce = new VueAnnonce();
        partialUpdatedVueAnnonce.setId(vueAnnonce.getId());

        partialUpdatedVueAnnonce.adresseIp(UPDATED_ADRESSE_IP);

        restVueAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVueAnnonce.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVueAnnonce))
            )
            .andExpect(status().isOk());

        // Validate the VueAnnonce in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVueAnnonceUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedVueAnnonce, vueAnnonce),
            getPersistedVueAnnonce(vueAnnonce)
        );
    }

    @Test
    @Transactional
    void fullUpdateVueAnnonceWithPatch() throws Exception {
        // Initialize the database
        insertedVueAnnonce = vueAnnonceRepository.saveAndFlush(vueAnnonce);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the vueAnnonce using partial update
        VueAnnonce partialUpdatedVueAnnonce = new VueAnnonce();
        partialUpdatedVueAnnonce.setId(vueAnnonce.getId());

        partialUpdatedVueAnnonce.dateVue(UPDATED_DATE_VUE).adresseIp(UPDATED_ADRESSE_IP);

        restVueAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedVueAnnonce.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedVueAnnonce))
            )
            .andExpect(status().isOk());

        // Validate the VueAnnonce in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertVueAnnonceUpdatableFieldsEquals(partialUpdatedVueAnnonce, getPersistedVueAnnonce(partialUpdatedVueAnnonce));
    }

    @Test
    @Transactional
    void patchNonExistingVueAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vueAnnonce.setId(longCount.incrementAndGet());

        // Create the VueAnnonce
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restVueAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, vueAnnonceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(vueAnnonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchVueAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vueAnnonce.setId(longCount.incrementAndGet());

        // Create the VueAnnonce
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVueAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(vueAnnonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamVueAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        vueAnnonce.setId(longCount.incrementAndGet());

        // Create the VueAnnonce
        VueAnnonceDTO vueAnnonceDTO = vueAnnonceMapper.toDto(vueAnnonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restVueAnnonceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(vueAnnonceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the VueAnnonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteVueAnnonce() throws Exception {
        // Initialize the database
        insertedVueAnnonce = vueAnnonceRepository.saveAndFlush(vueAnnonce);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the vueAnnonce
        restVueAnnonceMockMvc
            .perform(delete(ENTITY_API_URL_ID, vueAnnonce.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return vueAnnonceRepository.count();
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

    protected VueAnnonce getPersistedVueAnnonce(VueAnnonce vueAnnonce) {
        return vueAnnonceRepository.findById(vueAnnonce.getId()).orElseThrow();
    }

    protected void assertPersistedVueAnnonceToMatchAllProperties(VueAnnonce expectedVueAnnonce) {
        assertVueAnnonceAllPropertiesEquals(expectedVueAnnonce, getPersistedVueAnnonce(expectedVueAnnonce));
    }

    protected void assertPersistedVueAnnonceToMatchUpdatableProperties(VueAnnonce expectedVueAnnonce) {
        assertVueAnnonceAllUpdatablePropertiesEquals(expectedVueAnnonce, getPersistedVueAnnonce(expectedVueAnnonce));
    }
}
