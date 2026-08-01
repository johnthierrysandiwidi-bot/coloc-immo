package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.DetailColocationAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.DetailColocation;
import bf.colocation.immo.domain.enumeration.SexeRecherche;
import bf.colocation.immo.repository.DetailColocationRepository;
import bf.colocation.immo.service.DetailColocationService;
import bf.colocation.immo.service.dto.DetailColocationDTO;
import bf.colocation.immo.service.mapper.DetailColocationMapper;
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
 * Integration tests for the {@link DetailColocationResource} REST controller.
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
class DetailColocationResourceIT {

    private static final Integer DEFAULT_NOMBRE_PLACES = 1;
    private static final Integer UPDATED_NOMBRE_PLACES = 2;

    private static final Integer DEFAULT_PLACES_RESTANTES = 0;
    private static final Integer UPDATED_PLACES_RESTANTES = 1;

    private static final SexeRecherche DEFAULT_SEXE_RECHERCHE = SexeRecherche.HOMME;
    private static final SexeRecherche UPDATED_SEXE_RECHERCHE = SexeRecherche.FEMME;

    private static final Integer DEFAULT_AGE_MIN = 16;
    private static final Integer UPDATED_AGE_MIN = 17;

    private static final Integer DEFAULT_AGE_MAX = 16;
    private static final Integer UPDATED_AGE_MAX = 17;

    private static final Double DEFAULT_LOYER = 0D;
    private static final Double UPDATED_LOYER = 1D;

    private static final Double DEFAULT_CAUTION = 0D;
    private static final Double UPDATED_CAUTION = 1D;

    private static final Double DEFAULT_CHARGES = 0D;
    private static final Double UPDATED_CHARGES = 1D;

    private static final String DEFAULT_REGLES_DE_VIE = "AAAAAAAAAA";
    private static final String UPDATED_REGLES_DE_VIE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/detail-colocations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DetailColocationRepository detailColocationRepository;

    @Mock
    private DetailColocationRepository detailColocationRepositoryMock;

    @Autowired
    private DetailColocationMapper detailColocationMapper;

    @Mock
    private DetailColocationService detailColocationServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDetailColocationMockMvc;

    private DetailColocation detailColocation;

    private DetailColocation insertedDetailColocation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DetailColocation createEntity(EntityManager em) {
        DetailColocation detailColocation = new DetailColocation()
            .nombrePlaces(DEFAULT_NOMBRE_PLACES)
            .placesRestantes(DEFAULT_PLACES_RESTANTES)
            .sexeRecherche(DEFAULT_SEXE_RECHERCHE)
            .ageMin(DEFAULT_AGE_MIN)
            .ageMax(DEFAULT_AGE_MAX)
            .loyer(DEFAULT_LOYER)
            .caution(DEFAULT_CAUTION)
            .charges(DEFAULT_CHARGES)
            .reglesDeVie(DEFAULT_REGLES_DE_VIE);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        detailColocation.setAnnonce(annonce);
        return detailColocation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DetailColocation createUpdatedEntity(EntityManager em) {
        DetailColocation updatedDetailColocation = new DetailColocation()
            .nombrePlaces(UPDATED_NOMBRE_PLACES)
            .placesRestantes(UPDATED_PLACES_RESTANTES)
            .sexeRecherche(UPDATED_SEXE_RECHERCHE)
            .ageMin(UPDATED_AGE_MIN)
            .ageMax(UPDATED_AGE_MAX)
            .loyer(UPDATED_LOYER)
            .caution(UPDATED_CAUTION)
            .charges(UPDATED_CHARGES)
            .reglesDeVie(UPDATED_REGLES_DE_VIE);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createUpdatedEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        updatedDetailColocation.setAnnonce(annonce);
        return updatedDetailColocation;
    }

    @BeforeEach
    void initTest() {
        detailColocation = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDetailColocation != null) {
            detailColocationRepository.delete(insertedDetailColocation);
            insertedDetailColocation = null;
        }
    }

    @Test
    @Transactional
    void createDetailColocation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DetailColocation
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);
        var returnedDetailColocationDTO = om.readValue(
            restDetailColocationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(detailColocationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DetailColocationDTO.class
        );

        // Validate the DetailColocation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDetailColocation = detailColocationMapper.toEntity(returnedDetailColocationDTO);
        assertDetailColocationUpdatableFieldsEquals(returnedDetailColocation, getPersistedDetailColocation(returnedDetailColocation));

        insertedDetailColocation = returnedDetailColocation;
    }

    @Test
    @Transactional
    void createDetailColocationWithExistingId() throws Exception {
        // Create the DetailColocation with an existing ID
        detailColocation.setId(1L);
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDetailColocationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(detailColocationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombrePlacesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        detailColocation.setNombrePlaces(null);

        // Create the DetailColocation, which fails.
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        restDetailColocationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(detailColocationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPlacesRestantesIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        detailColocation.setPlacesRestantes(null);

        // Create the DetailColocation, which fails.
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        restDetailColocationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(detailColocationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSexeRechercheIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        detailColocation.setSexeRecherche(null);

        // Create the DetailColocation, which fails.
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        restDetailColocationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(detailColocationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLoyerIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        detailColocation.setLoyer(null);

        // Create the DetailColocation, which fails.
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        restDetailColocationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(detailColocationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDetailColocations() throws Exception {
        // Initialize the database
        insertedDetailColocation = detailColocationRepository.saveAndFlush(detailColocation);

        // Get all the detailColocationList
        restDetailColocationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(detailColocation.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombrePlaces").value(hasItem(DEFAULT_NOMBRE_PLACES)))
            .andExpect(jsonPath("$.[*].placesRestantes").value(hasItem(DEFAULT_PLACES_RESTANTES)))
            .andExpect(jsonPath("$.[*].sexeRecherche").value(hasItem(DEFAULT_SEXE_RECHERCHE.toString())))
            .andExpect(jsonPath("$.[*].ageMin").value(hasItem(DEFAULT_AGE_MIN)))
            .andExpect(jsonPath("$.[*].ageMax").value(hasItem(DEFAULT_AGE_MAX)))
            .andExpect(jsonPath("$.[*].loyer").value(hasItem(DEFAULT_LOYER)))
            .andExpect(jsonPath("$.[*].caution").value(hasItem(DEFAULT_CAUTION)))
            .andExpect(jsonPath("$.[*].charges").value(hasItem(DEFAULT_CHARGES)))
            .andExpect(jsonPath("$.[*].reglesDeVie").value(hasItem(DEFAULT_REGLES_DE_VIE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDetailColocationsWithEagerRelationshipsIsEnabled() throws Exception {
        when(detailColocationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDetailColocationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(detailColocationServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDetailColocationsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(detailColocationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDetailColocationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(detailColocationRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDetailColocation() throws Exception {
        // Initialize the database
        insertedDetailColocation = detailColocationRepository.saveAndFlush(detailColocation);

        // Get the detailColocation
        restDetailColocationMockMvc
            .perform(get(ENTITY_API_URL_ID, detailColocation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(detailColocation.getId().intValue()))
            .andExpect(jsonPath("$.nombrePlaces").value(DEFAULT_NOMBRE_PLACES))
            .andExpect(jsonPath("$.placesRestantes").value(DEFAULT_PLACES_RESTANTES))
            .andExpect(jsonPath("$.sexeRecherche").value(DEFAULT_SEXE_RECHERCHE.toString()))
            .andExpect(jsonPath("$.ageMin").value(DEFAULT_AGE_MIN))
            .andExpect(jsonPath("$.ageMax").value(DEFAULT_AGE_MAX))
            .andExpect(jsonPath("$.loyer").value(DEFAULT_LOYER))
            .andExpect(jsonPath("$.caution").value(DEFAULT_CAUTION))
            .andExpect(jsonPath("$.charges").value(DEFAULT_CHARGES))
            .andExpect(jsonPath("$.reglesDeVie").value(DEFAULT_REGLES_DE_VIE));
    }

    @Test
    @Transactional
    void getNonExistingDetailColocation() throws Exception {
        // Get the detailColocation
        restDetailColocationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDetailColocation() throws Exception {
        // Initialize the database
        insertedDetailColocation = detailColocationRepository.saveAndFlush(detailColocation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the detailColocation
        DetailColocation updatedDetailColocation = detailColocationRepository.findById(detailColocation.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDetailColocation are not directly saved in db
        em.detach(updatedDetailColocation);
        updatedDetailColocation
            .nombrePlaces(UPDATED_NOMBRE_PLACES)
            .placesRestantes(UPDATED_PLACES_RESTANTES)
            .sexeRecherche(UPDATED_SEXE_RECHERCHE)
            .ageMin(UPDATED_AGE_MIN)
            .ageMax(UPDATED_AGE_MAX)
            .loyer(UPDATED_LOYER)
            .caution(UPDATED_CAUTION)
            .charges(UPDATED_CHARGES)
            .reglesDeVie(UPDATED_REGLES_DE_VIE);
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(updatedDetailColocation);

        restDetailColocationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, detailColocationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(detailColocationDTO))
            )
            .andExpect(status().isOk());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDetailColocationToMatchAllProperties(updatedDetailColocation);
    }

    @Test
    @Transactional
    void putNonExistingDetailColocation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        detailColocation.setId(longCount.incrementAndGet());

        // Create the DetailColocation
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDetailColocationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, detailColocationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(detailColocationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDetailColocation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        detailColocation.setId(longCount.incrementAndGet());

        // Create the DetailColocation
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDetailColocationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(detailColocationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDetailColocation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        detailColocation.setId(longCount.incrementAndGet());

        // Create the DetailColocation
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDetailColocationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(detailColocationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDetailColocationWithPatch() throws Exception {
        // Initialize the database
        insertedDetailColocation = detailColocationRepository.saveAndFlush(detailColocation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the detailColocation using partial update
        DetailColocation partialUpdatedDetailColocation = new DetailColocation();
        partialUpdatedDetailColocation.setId(detailColocation.getId());

        partialUpdatedDetailColocation
            .nombrePlaces(UPDATED_NOMBRE_PLACES)
            .placesRestantes(UPDATED_PLACES_RESTANTES)
            .ageMin(UPDATED_AGE_MIN)
            .ageMax(UPDATED_AGE_MAX);

        restDetailColocationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDetailColocation.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDetailColocation))
            )
            .andExpect(status().isOk());

        // Validate the DetailColocation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDetailColocationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDetailColocation, detailColocation),
            getPersistedDetailColocation(detailColocation)
        );
    }

    @Test
    @Transactional
    void fullUpdateDetailColocationWithPatch() throws Exception {
        // Initialize the database
        insertedDetailColocation = detailColocationRepository.saveAndFlush(detailColocation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the detailColocation using partial update
        DetailColocation partialUpdatedDetailColocation = new DetailColocation();
        partialUpdatedDetailColocation.setId(detailColocation.getId());

        partialUpdatedDetailColocation
            .nombrePlaces(UPDATED_NOMBRE_PLACES)
            .placesRestantes(UPDATED_PLACES_RESTANTES)
            .sexeRecherche(UPDATED_SEXE_RECHERCHE)
            .ageMin(UPDATED_AGE_MIN)
            .ageMax(UPDATED_AGE_MAX)
            .loyer(UPDATED_LOYER)
            .caution(UPDATED_CAUTION)
            .charges(UPDATED_CHARGES)
            .reglesDeVie(UPDATED_REGLES_DE_VIE);

        restDetailColocationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDetailColocation.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDetailColocation))
            )
            .andExpect(status().isOk());

        // Validate the DetailColocation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDetailColocationUpdatableFieldsEquals(
            partialUpdatedDetailColocation,
            getPersistedDetailColocation(partialUpdatedDetailColocation)
        );
    }

    @Test
    @Transactional
    void patchNonExistingDetailColocation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        detailColocation.setId(longCount.incrementAndGet());

        // Create the DetailColocation
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDetailColocationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, detailColocationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(detailColocationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDetailColocation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        detailColocation.setId(longCount.incrementAndGet());

        // Create the DetailColocation
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDetailColocationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(detailColocationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDetailColocation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        detailColocation.setId(longCount.incrementAndGet());

        // Create the DetailColocation
        DetailColocationDTO detailColocationDTO = detailColocationMapper.toDto(detailColocation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDetailColocationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(detailColocationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DetailColocation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDetailColocation() throws Exception {
        // Initialize the database
        insertedDetailColocation = detailColocationRepository.saveAndFlush(detailColocation);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the detailColocation
        restDetailColocationMockMvc
            .perform(delete(ENTITY_API_URL_ID, detailColocation.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return detailColocationRepository.count();
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

    protected DetailColocation getPersistedDetailColocation(DetailColocation detailColocation) {
        return detailColocationRepository.findById(detailColocation.getId()).orElseThrow();
    }

    protected void assertPersistedDetailColocationToMatchAllProperties(DetailColocation expectedDetailColocation) {
        assertDetailColocationAllPropertiesEquals(expectedDetailColocation, getPersistedDetailColocation(expectedDetailColocation));
    }

    protected void assertPersistedDetailColocationToMatchUpdatableProperties(DetailColocation expectedDetailColocation) {
        assertDetailColocationAllUpdatablePropertiesEquals(
            expectedDetailColocation,
            getPersistedDetailColocation(expectedDetailColocation)
        );
    }
}
