package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.AnnonceAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.AnnonceService;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.mapper.AnnonceMapper;
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
 * Integration tests for the {@link AnnonceResource} REST controller.
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
class AnnonceResourceIT {

    private static final String DEFAULT_TITRE = "AAAAAAAAAA";
    private static final String UPDATED_TITRE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENU = "AAAAAAAAAA";
    private static final String UPDATED_CONTENU = "BBBBBBBBBB";

    private static final TypeAnnonce DEFAULT_TYPE = TypeAnnonce.VENTE;
    private static final TypeAnnonce UPDATED_TYPE = TypeAnnonce.LOCATION;

    private static final Double DEFAULT_PRIX = 0D;
    private static final Double UPDATED_PRIX = 1D;
    private static final Double SMALLER_PRIX = 0D - 1D;

    private static final Integer DEFAULT_NOMBRE_VUES = 0;
    private static final Integer UPDATED_NOMBRE_VUES = 1;
    private static final Integer SMALLER_NOMBRE_VUES = 0 - 1;

    private static final Instant DEFAULT_DATE_PUBLICATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_PUBLICATION = Instant.ofEpochMilli(1702714037224L);

    private static final Instant DEFAULT_DATE_EXPIRATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_EXPIRATION = Instant.ofEpochMilli(1702714037224L);

    private static final StatutAnnonce DEFAULT_STATUT = StatutAnnonce.BROUILLON;
    private static final StatutAnnonce UPDATED_STATUT = StatutAnnonce.PUBLIEE;

    private static final String ENTITY_API_URL = "/api/annonces";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AnnonceRepository annonceRepositoryMock;

    @Autowired
    private AnnonceMapper annonceMapper;

    @Mock
    private AnnonceService annonceServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAnnonceMockMvc;

    private Annonce annonce;

    private Annonce insertedAnnonce;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Annonce createEntity(EntityManager em) {
        Annonce annonce = new Annonce()
            .titre(DEFAULT_TITRE)
            .contenu(DEFAULT_CONTENU)
            .type(DEFAULT_TYPE)
            .prix(DEFAULT_PRIX)
            .nombreVues(DEFAULT_NOMBRE_VUES)
            .datePublication(DEFAULT_DATE_PUBLICATION)
            .dateExpiration(DEFAULT_DATE_EXPIRATION)
            .statut(DEFAULT_STATUT);
        // Add required entity
        Immobilier immobilier;
        if (TestUtil.findAll(em, Immobilier.class).isEmpty()) {
            immobilier = ImmobilierResourceIT.createEntity(em);
            em.persist(immobilier);
            em.flush();
        } else {
            immobilier = TestUtil.findAll(em, Immobilier.class).get(0);
        }
        annonce.setImmobilier(immobilier);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        annonce.setAuteur(user);
        return annonce;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Annonce createUpdatedEntity(EntityManager em) {
        Annonce updatedAnnonce = new Annonce()
            .titre(UPDATED_TITRE)
            .contenu(UPDATED_CONTENU)
            .type(UPDATED_TYPE)
            .prix(UPDATED_PRIX)
            .nombreVues(UPDATED_NOMBRE_VUES)
            .datePublication(UPDATED_DATE_PUBLICATION)
            .dateExpiration(UPDATED_DATE_EXPIRATION)
            .statut(UPDATED_STATUT);
        // Add required entity
        Immobilier immobilier;
        if (TestUtil.findAll(em, Immobilier.class).isEmpty()) {
            immobilier = ImmobilierResourceIT.createUpdatedEntity(em);
            em.persist(immobilier);
            em.flush();
        } else {
            immobilier = TestUtil.findAll(em, Immobilier.class).get(0);
        }
        updatedAnnonce.setImmobilier(immobilier);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedAnnonce.setAuteur(user);
        return updatedAnnonce;
    }

    @BeforeEach
    void initTest() {
        annonce = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAnnonce != null) {
            annonceRepository.delete(insertedAnnonce);
            insertedAnnonce = null;
        }
    }

    @Test
    @Transactional
    void createAnnonce() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Annonce
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);
        var returnedAnnonceDTO = om.readValue(
            restAnnonceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AnnonceDTO.class
        );

        // Validate the Annonce in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAnnonce = annonceMapper.toEntity(returnedAnnonceDTO);
        assertAnnonceUpdatableFieldsEquals(returnedAnnonce, getPersistedAnnonce(returnedAnnonce));

        insertedAnnonce = returnedAnnonce;
    }

    @Test
    @Transactional
    void createAnnonceWithExistingId() throws Exception {
        // Create the Annonce with an existing ID
        annonce.setId(1L);
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAnnonceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        annonce.setTitre(null);

        // Create the Annonce, which fails.
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        restAnnonceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        annonce.setType(null);

        // Create the Annonce, which fails.
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        restAnnonceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPrixIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        annonce.setPrix(null);

        // Create the Annonce, which fails.
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        restAnnonceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatutIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        annonce.setStatut(null);

        // Create the Annonce, which fails.
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        restAnnonceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAnnonces() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList
        restAnnonceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(annonce.getId().intValue())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].contenu").value(hasItem(DEFAULT_CONTENU)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].prix").value(hasItem(DEFAULT_PRIX)))
            .andExpect(jsonPath("$.[*].nombreVues").value(hasItem(DEFAULT_NOMBRE_VUES)))
            .andExpect(jsonPath("$.[*].datePublication").value(hasItem(DEFAULT_DATE_PUBLICATION.toString())))
            .andExpect(jsonPath("$.[*].dateExpiration").value(hasItem(DEFAULT_DATE_EXPIRATION.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAnnoncesWithEagerRelationshipsIsEnabled() throws Exception {
        when(annonceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAnnonceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(annonceServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAnnoncesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(annonceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAnnonceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(annonceRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAnnonce() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get the annonce
        restAnnonceMockMvc
            .perform(get(ENTITY_API_URL_ID, annonce.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(annonce.getId().intValue()))
            .andExpect(jsonPath("$.titre").value(DEFAULT_TITRE))
            .andExpect(jsonPath("$.contenu").value(DEFAULT_CONTENU))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.prix").value(DEFAULT_PRIX))
            .andExpect(jsonPath("$.nombreVues").value(DEFAULT_NOMBRE_VUES))
            .andExpect(jsonPath("$.datePublication").value(DEFAULT_DATE_PUBLICATION.toString()))
            .andExpect(jsonPath("$.dateExpiration").value(DEFAULT_DATE_EXPIRATION.toString()))
            .andExpect(jsonPath("$.statut").value(DEFAULT_STATUT.toString()));
    }

    @Test
    @Transactional
    void getAnnoncesByIdFiltering() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        Long id = annonce.getId();

        defaultAnnonceFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAnnonceFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAnnonceFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAnnoncesByTitreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where titre equals to
        defaultAnnonceFiltering("titre.equals=" + DEFAULT_TITRE, "titre.equals=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllAnnoncesByTitreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where titre in
        defaultAnnonceFiltering("titre.in=" + DEFAULT_TITRE + "," + UPDATED_TITRE, "titre.in=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllAnnoncesByTitreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where titre is not null
        defaultAnnonceFiltering("titre.specified=true", "titre.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByTitreContainsSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where titre contains
        defaultAnnonceFiltering("titre.contains=" + DEFAULT_TITRE, "titre.contains=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllAnnoncesByTitreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where titre does not contain
        defaultAnnonceFiltering("titre.doesNotContain=" + UPDATED_TITRE, "titre.doesNotContain=" + DEFAULT_TITRE);
    }

    @Test
    @Transactional
    void getAllAnnoncesByContenuIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where contenu equals to
        defaultAnnonceFiltering("contenu.equals=" + DEFAULT_CONTENU, "contenu.equals=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllAnnoncesByContenuIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where contenu in
        defaultAnnonceFiltering("contenu.in=" + DEFAULT_CONTENU + "," + UPDATED_CONTENU, "contenu.in=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllAnnoncesByContenuIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where contenu is not null
        defaultAnnonceFiltering("contenu.specified=true", "contenu.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByContenuContainsSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where contenu contains
        defaultAnnonceFiltering("contenu.contains=" + DEFAULT_CONTENU, "contenu.contains=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllAnnoncesByContenuNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where contenu does not contain
        defaultAnnonceFiltering("contenu.doesNotContain=" + UPDATED_CONTENU, "contenu.doesNotContain=" + DEFAULT_CONTENU);
    }

    @Test
    @Transactional
    void getAllAnnoncesByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where type equals to
        defaultAnnonceFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllAnnoncesByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where type in
        defaultAnnonceFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllAnnoncesByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where type is not null
        defaultAnnonceFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByPrixIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where prix equals to
        defaultAnnonceFiltering("prix.equals=" + DEFAULT_PRIX, "prix.equals=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    void getAllAnnoncesByPrixIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where prix in
        defaultAnnonceFiltering("prix.in=" + DEFAULT_PRIX + "," + UPDATED_PRIX, "prix.in=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    void getAllAnnoncesByPrixIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where prix is not null
        defaultAnnonceFiltering("prix.specified=true", "prix.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByPrixIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where prix is greater than or equal to
        defaultAnnonceFiltering("prix.greaterThanOrEqual=" + DEFAULT_PRIX, "prix.greaterThanOrEqual=" + UPDATED_PRIX);
    }

    @Test
    @Transactional
    void getAllAnnoncesByPrixIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where prix is less than or equal to
        defaultAnnonceFiltering("prix.lessThanOrEqual=" + DEFAULT_PRIX, "prix.lessThanOrEqual=" + SMALLER_PRIX);
    }

    @Test
    @Transactional
    void getAllAnnoncesByPrixIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where prix is less than
        defaultAnnonceFiltering("prix.lessThan=" + UPDATED_PRIX, "prix.lessThan=" + DEFAULT_PRIX);
    }

    @Test
    @Transactional
    void getAllAnnoncesByPrixIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where prix is greater than
        defaultAnnonceFiltering("prix.greaterThan=" + SMALLER_PRIX, "prix.greaterThan=" + DEFAULT_PRIX);
    }

    @Test
    @Transactional
    void getAllAnnoncesByNombreVuesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where nombreVues equals to
        defaultAnnonceFiltering("nombreVues.equals=" + DEFAULT_NOMBRE_VUES, "nombreVues.equals=" + UPDATED_NOMBRE_VUES);
    }

    @Test
    @Transactional
    void getAllAnnoncesByNombreVuesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where nombreVues in
        defaultAnnonceFiltering("nombreVues.in=" + DEFAULT_NOMBRE_VUES + "," + UPDATED_NOMBRE_VUES, "nombreVues.in=" + UPDATED_NOMBRE_VUES);
    }

    @Test
    @Transactional
    void getAllAnnoncesByNombreVuesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where nombreVues is not null
        defaultAnnonceFiltering("nombreVues.specified=true", "nombreVues.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByNombreVuesIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where nombreVues is greater than or equal to
        defaultAnnonceFiltering(
            "nombreVues.greaterThanOrEqual=" + DEFAULT_NOMBRE_VUES,
            "nombreVues.greaterThanOrEqual=" + UPDATED_NOMBRE_VUES
        );
    }

    @Test
    @Transactional
    void getAllAnnoncesByNombreVuesIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where nombreVues is less than or equal to
        defaultAnnonceFiltering("nombreVues.lessThanOrEqual=" + DEFAULT_NOMBRE_VUES, "nombreVues.lessThanOrEqual=" + SMALLER_NOMBRE_VUES);
    }

    @Test
    @Transactional
    void getAllAnnoncesByNombreVuesIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where nombreVues is less than
        defaultAnnonceFiltering("nombreVues.lessThan=" + UPDATED_NOMBRE_VUES, "nombreVues.lessThan=" + DEFAULT_NOMBRE_VUES);
    }

    @Test
    @Transactional
    void getAllAnnoncesByNombreVuesIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where nombreVues is greater than
        defaultAnnonceFiltering("nombreVues.greaterThan=" + SMALLER_NOMBRE_VUES, "nombreVues.greaterThan=" + DEFAULT_NOMBRE_VUES);
    }

    @Test
    @Transactional
    void getAllAnnoncesByDatePublicationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where datePublication equals to
        defaultAnnonceFiltering("datePublication.equals=" + DEFAULT_DATE_PUBLICATION, "datePublication.equals=" + UPDATED_DATE_PUBLICATION);
    }

    @Test
    @Transactional
    void getAllAnnoncesByDatePublicationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where datePublication in
        defaultAnnonceFiltering(
            "datePublication.in=" + DEFAULT_DATE_PUBLICATION + "," + UPDATED_DATE_PUBLICATION,
            "datePublication.in=" + UPDATED_DATE_PUBLICATION
        );
    }

    @Test
    @Transactional
    void getAllAnnoncesByDatePublicationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where datePublication is not null
        defaultAnnonceFiltering("datePublication.specified=true", "datePublication.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByDateExpirationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where dateExpiration equals to
        defaultAnnonceFiltering("dateExpiration.equals=" + DEFAULT_DATE_EXPIRATION, "dateExpiration.equals=" + UPDATED_DATE_EXPIRATION);
    }

    @Test
    @Transactional
    void getAllAnnoncesByDateExpirationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where dateExpiration in
        defaultAnnonceFiltering(
            "dateExpiration.in=" + DEFAULT_DATE_EXPIRATION + "," + UPDATED_DATE_EXPIRATION,
            "dateExpiration.in=" + UPDATED_DATE_EXPIRATION
        );
    }

    @Test
    @Transactional
    void getAllAnnoncesByDateExpirationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where dateExpiration is not null
        defaultAnnonceFiltering("dateExpiration.specified=true", "dateExpiration.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByStatutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where statut equals to
        defaultAnnonceFiltering("statut.equals=" + DEFAULT_STATUT, "statut.equals=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllAnnoncesByStatutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where statut in
        defaultAnnonceFiltering("statut.in=" + DEFAULT_STATUT + "," + UPDATED_STATUT, "statut.in=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllAnnoncesByStatutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        // Get all the annonceList where statut is not null
        defaultAnnonceFiltering("statut.specified=true", "statut.specified=false");
    }

    @Test
    @Transactional
    void getAllAnnoncesByImmobilierIsEqualToSomething() throws Exception {
        Immobilier immobilier;
        if (TestUtil.findAll(em, Immobilier.class).isEmpty()) {
            annonceRepository.saveAndFlush(annonce);
            immobilier = ImmobilierResourceIT.createEntity(em);
        } else {
            immobilier = TestUtil.findAll(em, Immobilier.class).get(0);
        }
        em.persist(immobilier);
        em.flush();
        annonce.setImmobilier(immobilier);
        annonceRepository.saveAndFlush(annonce);
        Long immobilierId = immobilier.getId();
        // Get all the annonceList where immobilier equals to immobilierId
        defaultAnnonceShouldBeFound("immobilierId.equals=" + immobilierId);

        // Get all the annonceList where immobilier equals to (immobilierId + 1)
        defaultAnnonceShouldNotBeFound("immobilierId.equals=" + (immobilierId + 1));
    }

    @Test
    @Transactional
    void getAllAnnoncesByAuteurIsEqualToSomething() throws Exception {
        User auteur;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            annonceRepository.saveAndFlush(annonce);
            auteur = UserResourceIT.createEntity();
        } else {
            auteur = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(auteur);
        em.flush();
        annonce.setAuteur(auteur);
        annonceRepository.saveAndFlush(annonce);
        Long auteurId = auteur.getId();
        // Get all the annonceList where auteur equals to auteurId
        defaultAnnonceShouldBeFound("auteurId.equals=" + auteurId);

        // Get all the annonceList where auteur equals to (auteurId + 1)
        defaultAnnonceShouldNotBeFound("auteurId.equals=" + (auteurId + 1));
    }

    private void defaultAnnonceFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAnnonceShouldBeFound(shouldBeFound);
        defaultAnnonceShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAnnonceShouldBeFound(String filter) throws Exception {
        restAnnonceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(annonce.getId().intValue())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].contenu").value(hasItem(DEFAULT_CONTENU)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].prix").value(hasItem(DEFAULT_PRIX)))
            .andExpect(jsonPath("$.[*].nombreVues").value(hasItem(DEFAULT_NOMBRE_VUES)))
            .andExpect(jsonPath("$.[*].datePublication").value(hasItem(DEFAULT_DATE_PUBLICATION.toString())))
            .andExpect(jsonPath("$.[*].dateExpiration").value(hasItem(DEFAULT_DATE_EXPIRATION.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())));

        // Check, that the count call also returns 1
        restAnnonceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAnnonceShouldNotBeFound(String filter) throws Exception {
        restAnnonceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAnnonceMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAnnonce() throws Exception {
        // Get the annonce
        restAnnonceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAnnonce() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the annonce
        Annonce updatedAnnonce = annonceRepository.findById(annonce.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAnnonce are not directly saved in db
        em.detach(updatedAnnonce);
        updatedAnnonce
            .titre(UPDATED_TITRE)
            .contenu(UPDATED_CONTENU)
            .type(UPDATED_TYPE)
            .prix(UPDATED_PRIX)
            .nombreVues(UPDATED_NOMBRE_VUES)
            .datePublication(UPDATED_DATE_PUBLICATION)
            .dateExpiration(UPDATED_DATE_EXPIRATION)
            .statut(UPDATED_STATUT);
        AnnonceDTO annonceDTO = annonceMapper.toDto(updatedAnnonce);

        restAnnonceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, annonceDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO))
            )
            .andExpect(status().isOk());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAnnonceToMatchAllProperties(updatedAnnonce);
    }

    @Test
    @Transactional
    void putNonExistingAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        annonce.setId(longCount.incrementAndGet());

        // Create the Annonce
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnnonceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, annonceDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        annonce.setId(longCount.incrementAndGet());

        // Create the Annonce
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnnonceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(annonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        annonce.setId(longCount.incrementAndGet());

        // Create the Annonce
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnnonceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(annonceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAnnonceWithPatch() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the annonce using partial update
        Annonce partialUpdatedAnnonce = new Annonce();
        partialUpdatedAnnonce.setId(annonce.getId());

        partialUpdatedAnnonce.contenu(UPDATED_CONTENU).type(UPDATED_TYPE).prix(UPDATED_PRIX).dateExpiration(UPDATED_DATE_EXPIRATION);

        restAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAnnonce.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAnnonce))
            )
            .andExpect(status().isOk());

        // Validate the Annonce in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAnnonceUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAnnonce, annonce), getPersistedAnnonce(annonce));
    }

    @Test
    @Transactional
    void fullUpdateAnnonceWithPatch() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the annonce using partial update
        Annonce partialUpdatedAnnonce = new Annonce();
        partialUpdatedAnnonce.setId(annonce.getId());

        partialUpdatedAnnonce
            .titre(UPDATED_TITRE)
            .contenu(UPDATED_CONTENU)
            .type(UPDATED_TYPE)
            .prix(UPDATED_PRIX)
            .nombreVues(UPDATED_NOMBRE_VUES)
            .datePublication(UPDATED_DATE_PUBLICATION)
            .dateExpiration(UPDATED_DATE_EXPIRATION)
            .statut(UPDATED_STATUT);

        restAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAnnonce.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAnnonce))
            )
            .andExpect(status().isOk());

        // Validate the Annonce in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAnnonceUpdatableFieldsEquals(partialUpdatedAnnonce, getPersistedAnnonce(partialUpdatedAnnonce));
    }

    @Test
    @Transactional
    void patchNonExistingAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        annonce.setId(longCount.incrementAndGet());

        // Create the Annonce
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, annonceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(annonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        annonce.setId(longCount.incrementAndGet());

        // Create the Annonce
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnnonceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(annonceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAnnonce() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        annonce.setId(longCount.incrementAndGet());

        // Create the Annonce
        AnnonceDTO annonceDTO = annonceMapper.toDto(annonce);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAnnonceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(annonceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Annonce in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAnnonce() throws Exception {
        // Initialize the database
        insertedAnnonce = annonceRepository.saveAndFlush(annonce);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the annonce
        restAnnonceMockMvc
            .perform(delete(ENTITY_API_URL_ID, annonce.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return annonceRepository.count();
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

    protected Annonce getPersistedAnnonce(Annonce annonce) {
        return annonceRepository.findById(annonce.getId()).orElseThrow();
    }

    protected void assertPersistedAnnonceToMatchAllProperties(Annonce expectedAnnonce) {
        assertAnnonceAllPropertiesEquals(expectedAnnonce, getPersistedAnnonce(expectedAnnonce));
    }

    protected void assertPersistedAnnonceToMatchUpdatableProperties(Annonce expectedAnnonce) {
        assertAnnonceAllUpdatablePropertiesEquals(expectedAnnonce, getPersistedAnnonce(expectedAnnonce));
    }
}
