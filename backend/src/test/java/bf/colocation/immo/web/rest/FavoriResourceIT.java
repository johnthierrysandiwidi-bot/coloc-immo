package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.FavoriAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Favori;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.repository.FavoriRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.FavoriService;
import bf.colocation.immo.service.dto.FavoriDTO;
import bf.colocation.immo.service.mapper.FavoriMapper;
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
 * Integration tests for the {@link FavoriResource} REST controller.
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
class FavoriResourceIT {

    private static final Instant DEFAULT_DATE_AJOUT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_AJOUT = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/favoris";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FavoriRepository favoriRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private FavoriRepository favoriRepositoryMock;

    @Autowired
    private FavoriMapper favoriMapper;

    @Mock
    private FavoriService favoriServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFavoriMockMvc;

    private Favori favori;

    private Favori insertedFavori;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Favori createEntity(EntityManager em) {
        Favori favori = new Favori().dateAjout(DEFAULT_DATE_AJOUT);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        favori.setAnnonce(annonce);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        favori.setUtilisateur(user);
        return favori;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Favori createUpdatedEntity(EntityManager em) {
        Favori updatedFavori = new Favori().dateAjout(UPDATED_DATE_AJOUT);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createUpdatedEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        updatedFavori.setAnnonce(annonce);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedFavori.setUtilisateur(user);
        return updatedFavori;
    }

    @BeforeEach
    void initTest() {
        favori = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedFavori != null) {
            favoriRepository.delete(insertedFavori);
            insertedFavori = null;
        }
    }

    @Test
    @Transactional
    void createFavori() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Favori
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);
        var returnedFavoriDTO = om.readValue(
            restFavoriMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FavoriDTO.class
        );

        // Validate the Favori in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFavori = favoriMapper.toEntity(returnedFavoriDTO);
        assertFavoriUpdatableFieldsEquals(returnedFavori, getPersistedFavori(returnedFavori));

        insertedFavori = returnedFavori;
    }

    @Test
    @Transactional
    void createFavoriWithExistingId() throws Exception {
        // Create the Favori with an existing ID
        favori.setId(1L);
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restFavoriMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateAjoutIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        favori.setDateAjout(null);

        // Create the Favori, which fails.
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        restFavoriMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllFavoris() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        // Get all the favoriList
        restFavoriMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favori.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateAjout").value(hasItem(DEFAULT_DATE_AJOUT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFavorisWithEagerRelationshipsIsEnabled() throws Exception {
        when(favoriServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFavoriMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(favoriServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFavorisWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(favoriServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFavoriMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(favoriRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFavori() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        // Get the favori
        restFavoriMockMvc
            .perform(get(ENTITY_API_URL_ID, favori.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(favori.getId().intValue()))
            .andExpect(jsonPath("$.dateAjout").value(DEFAULT_DATE_AJOUT.toString()));
    }

    @Test
    @Transactional
    void getFavorisByIdFiltering() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        Long id = favori.getId();

        defaultFavoriFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultFavoriFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultFavoriFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllFavorisByDateAjoutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        // Get all the favoriList where dateAjout equals to
        defaultFavoriFiltering("dateAjout.equals=" + DEFAULT_DATE_AJOUT, "dateAjout.equals=" + UPDATED_DATE_AJOUT);
    }

    @Test
    @Transactional
    void getAllFavorisByDateAjoutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        // Get all the favoriList where dateAjout in
        defaultFavoriFiltering("dateAjout.in=" + DEFAULT_DATE_AJOUT + "," + UPDATED_DATE_AJOUT, "dateAjout.in=" + UPDATED_DATE_AJOUT);
    }

    @Test
    @Transactional
    void getAllFavorisByDateAjoutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        // Get all the favoriList where dateAjout is not null
        defaultFavoriFiltering("dateAjout.specified=true", "dateAjout.specified=false");
    }

    @Test
    @Transactional
    void getAllFavorisByAnnonceIsEqualToSomething() throws Exception {
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            favoriRepository.saveAndFlush(favori);
            annonce = AnnonceResourceIT.createEntity(em);
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        em.persist(annonce);
        em.flush();
        favori.setAnnonce(annonce);
        favoriRepository.saveAndFlush(favori);
        Long annonceId = annonce.getId();
        // Get all the favoriList where annonce equals to annonceId
        defaultFavoriShouldBeFound("annonceId.equals=" + annonceId);

        // Get all the favoriList where annonce equals to (annonceId + 1)
        defaultFavoriShouldNotBeFound("annonceId.equals=" + (annonceId + 1));
    }

    @Test
    @Transactional
    void getAllFavorisByUtilisateurIsEqualToSomething() throws Exception {
        User utilisateur;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            favoriRepository.saveAndFlush(favori);
            utilisateur = UserResourceIT.createEntity();
        } else {
            utilisateur = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(utilisateur);
        em.flush();
        favori.setUtilisateur(utilisateur);
        favoriRepository.saveAndFlush(favori);
        Long utilisateurId = utilisateur.getId();
        // Get all the favoriList where utilisateur equals to utilisateurId
        defaultFavoriShouldBeFound("utilisateurId.equals=" + utilisateurId);

        // Get all the favoriList where utilisateur equals to (utilisateurId + 1)
        defaultFavoriShouldNotBeFound("utilisateurId.equals=" + (utilisateurId + 1));
    }

    private void defaultFavoriFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultFavoriShouldBeFound(shouldBeFound);
        defaultFavoriShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFavoriShouldBeFound(String filter) throws Exception {
        restFavoriMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(favori.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateAjout").value(hasItem(DEFAULT_DATE_AJOUT.toString())));

        // Check, that the count call also returns 1
        restFavoriMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFavoriShouldNotBeFound(String filter) throws Exception {
        restFavoriMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFavoriMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingFavori() throws Exception {
        // Get the favori
        restFavoriMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFavori() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favori
        Favori updatedFavori = favoriRepository.findById(favori.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFavori are not directly saved in db
        em.detach(updatedFavori);
        updatedFavori.dateAjout(UPDATED_DATE_AJOUT);
        FavoriDTO favoriDTO = favoriMapper.toDto(updatedFavori);

        restFavoriMockMvc
            .perform(
                put(ENTITY_API_URL_ID, favoriDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriDTO))
            )
            .andExpect(status().isOk());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFavoriToMatchAllProperties(updatedFavori);
    }

    @Test
    @Transactional
    void putNonExistingFavori() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favori.setId(longCount.incrementAndGet());

        // Create the Favori
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFavoriMockMvc
            .perform(
                put(ENTITY_API_URL_ID, favoriDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchFavori() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favori.setId(longCount.incrementAndGet());

        // Create the Favori
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(favoriDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFavori() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favori.setId(longCount.incrementAndGet());

        // Create the Favori
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(favoriDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateFavoriWithPatch() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favori using partial update
        Favori partialUpdatedFavori = new Favori();
        partialUpdatedFavori.setId(favori.getId());

        partialUpdatedFavori.dateAjout(UPDATED_DATE_AJOUT);

        restFavoriMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFavori.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFavori))
            )
            .andExpect(status().isOk());

        // Validate the Favori in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFavoriUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedFavori, favori), getPersistedFavori(favori));
    }

    @Test
    @Transactional
    void fullUpdateFavoriWithPatch() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the favori using partial update
        Favori partialUpdatedFavori = new Favori();
        partialUpdatedFavori.setId(favori.getId());

        partialUpdatedFavori.dateAjout(UPDATED_DATE_AJOUT);

        restFavoriMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFavori.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFavori))
            )
            .andExpect(status().isOk());

        // Validate the Favori in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFavoriUpdatableFieldsEquals(partialUpdatedFavori, getPersistedFavori(partialUpdatedFavori));
    }

    @Test
    @Transactional
    void patchNonExistingFavori() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favori.setId(longCount.incrementAndGet());

        // Create the Favori
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFavoriMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, favoriDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(favoriDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFavori() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favori.setId(longCount.incrementAndGet());

        // Create the Favori
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(favoriDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFavori() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        favori.setId(longCount.incrementAndGet());

        // Create the Favori
        FavoriDTO favoriDTO = favoriMapper.toDto(favori);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFavoriMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(favoriDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Favori in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteFavori() throws Exception {
        // Initialize the database
        insertedFavori = favoriRepository.saveAndFlush(favori);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the favori
        restFavoriMockMvc
            .perform(delete(ENTITY_API_URL_ID, favori.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return favoriRepository.count();
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

    protected Favori getPersistedFavori(Favori favori) {
        return favoriRepository.findById(favori.getId()).orElseThrow();
    }

    protected void assertPersistedFavoriToMatchAllProperties(Favori expectedFavori) {
        assertFavoriAllPropertiesEquals(expectedFavori, getPersistedFavori(expectedFavori));
    }

    protected void assertPersistedFavoriToMatchUpdatableProperties(Favori expectedFavori) {
        assertFavoriAllUpdatablePropertiesEquals(expectedFavori, getPersistedFavori(expectedFavori));
    }
}
