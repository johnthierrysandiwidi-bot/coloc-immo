package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.AlerteAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Alerte;
import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.domain.Quartier;
import bf.colocation.immo.domain.TypeImmobilier;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.FrequenceAlerte;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import bf.colocation.immo.repository.AlerteRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.AlerteService;
import bf.colocation.immo.service.dto.AlerteDTO;
import bf.colocation.immo.service.mapper.AlerteMapper;
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
 * Integration tests for the {@link AlerteResource} REST controller.
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
class AlerteResourceIT {

    private static final String DEFAULT_TITRE = "AAAAAAAAAA";
    private static final String UPDATED_TITRE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENU = "AAAAAAAAAA";
    private static final String UPDATED_CONTENU = "BBBBBBBBBB";

    private static final TypeAnnonce DEFAULT_TYPE_ANNONCE = TypeAnnonce.VENTE;
    private static final TypeAnnonce UPDATED_TYPE_ANNONCE = TypeAnnonce.LOCATION;

    private static final Double DEFAULT_PRIX_MIN = 0D;
    private static final Double UPDATED_PRIX_MIN = 1D;
    private static final Double SMALLER_PRIX_MIN = 0D - 1D;

    private static final Double DEFAULT_PRIX_MAX = 0D;
    private static final Double UPDATED_PRIX_MAX = 1D;
    private static final Double SMALLER_PRIX_MAX = 0D - 1D;

    private static final Double DEFAULT_SURFACE_MIN = 0D;
    private static final Double UPDATED_SURFACE_MIN = 1D;
    private static final Double SMALLER_SURFACE_MIN = 0D - 1D;

    private static final Integer DEFAULT_NOMBRE_CHAMBRES_MIN = 0;
    private static final Integer UPDATED_NOMBRE_CHAMBRES_MIN = 1;
    private static final Integer SMALLER_NOMBRE_CHAMBRES_MIN = 0 - 1;

    private static final Boolean DEFAULT_MEUBLE_UNIQUEMENT = false;
    private static final Boolean UPDATED_MEUBLE_UNIQUEMENT = true;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final FrequenceAlerte DEFAULT_FREQUENCE = FrequenceAlerte.IMMEDIATE;
    private static final FrequenceAlerte UPDATED_FREQUENCE = FrequenceAlerte.QUOTIDIENNE;

    private static final Instant DEFAULT_DERNIERE_EXECUTION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DERNIERE_EXECUTION = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/alertes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlerteRepository alerteRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AlerteRepository alerteRepositoryMock;

    @Autowired
    private AlerteMapper alerteMapper;

    @Mock
    private AlerteService alerteServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlerteMockMvc;

    private Alerte alerte;

    private Alerte insertedAlerte;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Alerte createEntity(EntityManager em) {
        Alerte alerte = new Alerte()
            .titre(DEFAULT_TITRE)
            .contenu(DEFAULT_CONTENU)
            .typeAnnonce(DEFAULT_TYPE_ANNONCE)
            .prixMin(DEFAULT_PRIX_MIN)
            .prixMax(DEFAULT_PRIX_MAX)
            .surfaceMin(DEFAULT_SURFACE_MIN)
            .nombreChambresMin(DEFAULT_NOMBRE_CHAMBRES_MIN)
            .meubleUniquement(DEFAULT_MEUBLE_UNIQUEMENT)
            .active(DEFAULT_ACTIVE)
            .frequence(DEFAULT_FREQUENCE)
            .derniereExecution(DEFAULT_DERNIERE_EXECUTION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        alerte.setTitulaire(user);
        return alerte;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Alerte createUpdatedEntity(EntityManager em) {
        Alerte updatedAlerte = new Alerte()
            .titre(UPDATED_TITRE)
            .contenu(UPDATED_CONTENU)
            .typeAnnonce(UPDATED_TYPE_ANNONCE)
            .prixMin(UPDATED_PRIX_MIN)
            .prixMax(UPDATED_PRIX_MAX)
            .surfaceMin(UPDATED_SURFACE_MIN)
            .nombreChambresMin(UPDATED_NOMBRE_CHAMBRES_MIN)
            .meubleUniquement(UPDATED_MEUBLE_UNIQUEMENT)
            .active(UPDATED_ACTIVE)
            .frequence(UPDATED_FREQUENCE)
            .derniereExecution(UPDATED_DERNIERE_EXECUTION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedAlerte.setTitulaire(user);
        return updatedAlerte;
    }

    @BeforeEach
    void initTest() {
        alerte = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAlerte != null) {
            alerteRepository.delete(insertedAlerte);
            insertedAlerte = null;
        }
    }

    @Test
    @Transactional
    void createAlerte() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Alerte
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);
        var returnedAlerteDTO = om.readValue(
            restAlerteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlerteDTO.class
        );

        // Validate the Alerte in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlerte = alerteMapper.toEntity(returnedAlerteDTO);
        assertAlerteUpdatableFieldsEquals(returnedAlerte, getPersistedAlerte(returnedAlerte));

        insertedAlerte = returnedAlerte;
    }

    @Test
    @Transactional
    void createAlerteWithExistingId() throws Exception {
        // Create the Alerte with an existing ID
        alerte.setId(1L);
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlerteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alerte.setTitre(null);

        // Create the Alerte, which fails.
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        restAlerteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alerte.setActive(null);

        // Create the Alerte, which fails.
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        restAlerteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkFrequenceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alerte.setFrequence(null);

        // Create the Alerte, which fails.
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        restAlerteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAlertes() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList
        restAlerteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alerte.getId().intValue())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].contenu").value(hasItem(DEFAULT_CONTENU)))
            .andExpect(jsonPath("$.[*].typeAnnonce").value(hasItem(DEFAULT_TYPE_ANNONCE.toString())))
            .andExpect(jsonPath("$.[*].prixMin").value(hasItem(DEFAULT_PRIX_MIN)))
            .andExpect(jsonPath("$.[*].prixMax").value(hasItem(DEFAULT_PRIX_MAX)))
            .andExpect(jsonPath("$.[*].surfaceMin").value(hasItem(DEFAULT_SURFACE_MIN)))
            .andExpect(jsonPath("$.[*].nombreChambresMin").value(hasItem(DEFAULT_NOMBRE_CHAMBRES_MIN)))
            .andExpect(jsonPath("$.[*].meubleUniquement").value(hasItem(DEFAULT_MEUBLE_UNIQUEMENT)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].frequence").value(hasItem(DEFAULT_FREQUENCE.toString())))
            .andExpect(jsonPath("$.[*].derniereExecution").value(hasItem(DEFAULT_DERNIERE_EXECUTION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlertesWithEagerRelationshipsIsEnabled() throws Exception {
        when(alerteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlerteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(alerteServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlertesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(alerteServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlerteMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(alerteRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAlerte() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get the alerte
        restAlerteMockMvc
            .perform(get(ENTITY_API_URL_ID, alerte.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alerte.getId().intValue()))
            .andExpect(jsonPath("$.titre").value(DEFAULT_TITRE))
            .andExpect(jsonPath("$.contenu").value(DEFAULT_CONTENU))
            .andExpect(jsonPath("$.typeAnnonce").value(DEFAULT_TYPE_ANNONCE.toString()))
            .andExpect(jsonPath("$.prixMin").value(DEFAULT_PRIX_MIN))
            .andExpect(jsonPath("$.prixMax").value(DEFAULT_PRIX_MAX))
            .andExpect(jsonPath("$.surfaceMin").value(DEFAULT_SURFACE_MIN))
            .andExpect(jsonPath("$.nombreChambresMin").value(DEFAULT_NOMBRE_CHAMBRES_MIN))
            .andExpect(jsonPath("$.meubleUniquement").value(DEFAULT_MEUBLE_UNIQUEMENT))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
            .andExpect(jsonPath("$.frequence").value(DEFAULT_FREQUENCE.toString()))
            .andExpect(jsonPath("$.derniereExecution").value(DEFAULT_DERNIERE_EXECUTION.toString()));
    }

    @Test
    @Transactional
    void getAlertesByIdFiltering() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        Long id = alerte.getId();

        defaultAlerteFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAlerteFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAlerteFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAlertesByTitreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where titre equals to
        defaultAlerteFiltering("titre.equals=" + DEFAULT_TITRE, "titre.equals=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllAlertesByTitreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where titre in
        defaultAlerteFiltering("titre.in=" + DEFAULT_TITRE + "," + UPDATED_TITRE, "titre.in=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllAlertesByTitreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where titre is not null
        defaultAlerteFiltering("titre.specified=true", "titre.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByTitreContainsSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where titre contains
        defaultAlerteFiltering("titre.contains=" + DEFAULT_TITRE, "titre.contains=" + UPDATED_TITRE);
    }

    @Test
    @Transactional
    void getAllAlertesByTitreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where titre does not contain
        defaultAlerteFiltering("titre.doesNotContain=" + UPDATED_TITRE, "titre.doesNotContain=" + DEFAULT_TITRE);
    }

    @Test
    @Transactional
    void getAllAlertesByContenuIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where contenu equals to
        defaultAlerteFiltering("contenu.equals=" + DEFAULT_CONTENU, "contenu.equals=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllAlertesByContenuIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where contenu in
        defaultAlerteFiltering("contenu.in=" + DEFAULT_CONTENU + "," + UPDATED_CONTENU, "contenu.in=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllAlertesByContenuIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where contenu is not null
        defaultAlerteFiltering("contenu.specified=true", "contenu.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByContenuContainsSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where contenu contains
        defaultAlerteFiltering("contenu.contains=" + DEFAULT_CONTENU, "contenu.contains=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllAlertesByContenuNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where contenu does not contain
        defaultAlerteFiltering("contenu.doesNotContain=" + UPDATED_CONTENU, "contenu.doesNotContain=" + DEFAULT_CONTENU);
    }

    @Test
    @Transactional
    void getAllAlertesByTypeAnnonceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where typeAnnonce equals to
        defaultAlerteFiltering("typeAnnonce.equals=" + DEFAULT_TYPE_ANNONCE, "typeAnnonce.equals=" + UPDATED_TYPE_ANNONCE);
    }

    @Test
    @Transactional
    void getAllAlertesByTypeAnnonceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where typeAnnonce in
        defaultAlerteFiltering(
            "typeAnnonce.in=" + DEFAULT_TYPE_ANNONCE + "," + UPDATED_TYPE_ANNONCE,
            "typeAnnonce.in=" + UPDATED_TYPE_ANNONCE
        );
    }

    @Test
    @Transactional
    void getAllAlertesByTypeAnnonceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where typeAnnonce is not null
        defaultAlerteFiltering("typeAnnonce.specified=true", "typeAnnonce.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMinIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMin equals to
        defaultAlerteFiltering("prixMin.equals=" + DEFAULT_PRIX_MIN, "prixMin.equals=" + UPDATED_PRIX_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMinIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMin in
        defaultAlerteFiltering("prixMin.in=" + DEFAULT_PRIX_MIN + "," + UPDATED_PRIX_MIN, "prixMin.in=" + UPDATED_PRIX_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMinIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMin is not null
        defaultAlerteFiltering("prixMin.specified=true", "prixMin.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMinIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMin is greater than or equal to
        defaultAlerteFiltering("prixMin.greaterThanOrEqual=" + DEFAULT_PRIX_MIN, "prixMin.greaterThanOrEqual=" + UPDATED_PRIX_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMinIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMin is less than or equal to
        defaultAlerteFiltering("prixMin.lessThanOrEqual=" + DEFAULT_PRIX_MIN, "prixMin.lessThanOrEqual=" + SMALLER_PRIX_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMinIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMin is less than
        defaultAlerteFiltering("prixMin.lessThan=" + UPDATED_PRIX_MIN, "prixMin.lessThan=" + DEFAULT_PRIX_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMinIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMin is greater than
        defaultAlerteFiltering("prixMin.greaterThan=" + SMALLER_PRIX_MIN, "prixMin.greaterThan=" + DEFAULT_PRIX_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMaxIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMax equals to
        defaultAlerteFiltering("prixMax.equals=" + DEFAULT_PRIX_MAX, "prixMax.equals=" + UPDATED_PRIX_MAX);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMaxIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMax in
        defaultAlerteFiltering("prixMax.in=" + DEFAULT_PRIX_MAX + "," + UPDATED_PRIX_MAX, "prixMax.in=" + UPDATED_PRIX_MAX);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMaxIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMax is not null
        defaultAlerteFiltering("prixMax.specified=true", "prixMax.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMaxIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMax is greater than or equal to
        defaultAlerteFiltering("prixMax.greaterThanOrEqual=" + DEFAULT_PRIX_MAX, "prixMax.greaterThanOrEqual=" + UPDATED_PRIX_MAX);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMaxIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMax is less than or equal to
        defaultAlerteFiltering("prixMax.lessThanOrEqual=" + DEFAULT_PRIX_MAX, "prixMax.lessThanOrEqual=" + SMALLER_PRIX_MAX);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMaxIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMax is less than
        defaultAlerteFiltering("prixMax.lessThan=" + UPDATED_PRIX_MAX, "prixMax.lessThan=" + DEFAULT_PRIX_MAX);
    }

    @Test
    @Transactional
    void getAllAlertesByPrixMaxIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where prixMax is greater than
        defaultAlerteFiltering("prixMax.greaterThan=" + SMALLER_PRIX_MAX, "prixMax.greaterThan=" + DEFAULT_PRIX_MAX);
    }

    @Test
    @Transactional
    void getAllAlertesBySurfaceMinIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where surfaceMin equals to
        defaultAlerteFiltering("surfaceMin.equals=" + DEFAULT_SURFACE_MIN, "surfaceMin.equals=" + UPDATED_SURFACE_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesBySurfaceMinIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where surfaceMin in
        defaultAlerteFiltering("surfaceMin.in=" + DEFAULT_SURFACE_MIN + "," + UPDATED_SURFACE_MIN, "surfaceMin.in=" + UPDATED_SURFACE_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesBySurfaceMinIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where surfaceMin is not null
        defaultAlerteFiltering("surfaceMin.specified=true", "surfaceMin.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesBySurfaceMinIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where surfaceMin is greater than or equal to
        defaultAlerteFiltering(
            "surfaceMin.greaterThanOrEqual=" + DEFAULT_SURFACE_MIN,
            "surfaceMin.greaterThanOrEqual=" + UPDATED_SURFACE_MIN
        );
    }

    @Test
    @Transactional
    void getAllAlertesBySurfaceMinIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where surfaceMin is less than or equal to
        defaultAlerteFiltering("surfaceMin.lessThanOrEqual=" + DEFAULT_SURFACE_MIN, "surfaceMin.lessThanOrEqual=" + SMALLER_SURFACE_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesBySurfaceMinIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where surfaceMin is less than
        defaultAlerteFiltering("surfaceMin.lessThan=" + UPDATED_SURFACE_MIN, "surfaceMin.lessThan=" + DEFAULT_SURFACE_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesBySurfaceMinIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where surfaceMin is greater than
        defaultAlerteFiltering("surfaceMin.greaterThan=" + SMALLER_SURFACE_MIN, "surfaceMin.greaterThan=" + DEFAULT_SURFACE_MIN);
    }

    @Test
    @Transactional
    void getAllAlertesByNombreChambresMinIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where nombreChambresMin equals to
        defaultAlerteFiltering(
            "nombreChambresMin.equals=" + DEFAULT_NOMBRE_CHAMBRES_MIN,
            "nombreChambresMin.equals=" + UPDATED_NOMBRE_CHAMBRES_MIN
        );
    }

    @Test
    @Transactional
    void getAllAlertesByNombreChambresMinIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where nombreChambresMin in
        defaultAlerteFiltering(
            "nombreChambresMin.in=" + DEFAULT_NOMBRE_CHAMBRES_MIN + "," + UPDATED_NOMBRE_CHAMBRES_MIN,
            "nombreChambresMin.in=" + UPDATED_NOMBRE_CHAMBRES_MIN
        );
    }

    @Test
    @Transactional
    void getAllAlertesByNombreChambresMinIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where nombreChambresMin is not null
        defaultAlerteFiltering("nombreChambresMin.specified=true", "nombreChambresMin.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByNombreChambresMinIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where nombreChambresMin is greater than or equal to
        defaultAlerteFiltering(
            "nombreChambresMin.greaterThanOrEqual=" + DEFAULT_NOMBRE_CHAMBRES_MIN,
            "nombreChambresMin.greaterThanOrEqual=" + UPDATED_NOMBRE_CHAMBRES_MIN
        );
    }

    @Test
    @Transactional
    void getAllAlertesByNombreChambresMinIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where nombreChambresMin is less than or equal to
        defaultAlerteFiltering(
            "nombreChambresMin.lessThanOrEqual=" + DEFAULT_NOMBRE_CHAMBRES_MIN,
            "nombreChambresMin.lessThanOrEqual=" + SMALLER_NOMBRE_CHAMBRES_MIN
        );
    }

    @Test
    @Transactional
    void getAllAlertesByNombreChambresMinIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where nombreChambresMin is less than
        defaultAlerteFiltering(
            "nombreChambresMin.lessThan=" + UPDATED_NOMBRE_CHAMBRES_MIN,
            "nombreChambresMin.lessThan=" + DEFAULT_NOMBRE_CHAMBRES_MIN
        );
    }

    @Test
    @Transactional
    void getAllAlertesByNombreChambresMinIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where nombreChambresMin is greater than
        defaultAlerteFiltering(
            "nombreChambresMin.greaterThan=" + SMALLER_NOMBRE_CHAMBRES_MIN,
            "nombreChambresMin.greaterThan=" + DEFAULT_NOMBRE_CHAMBRES_MIN
        );
    }

    @Test
    @Transactional
    void getAllAlertesByMeubleUniquementIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where meubleUniquement equals to
        defaultAlerteFiltering(
            "meubleUniquement.equals=" + DEFAULT_MEUBLE_UNIQUEMENT,
            "meubleUniquement.equals=" + UPDATED_MEUBLE_UNIQUEMENT
        );
    }

    @Test
    @Transactional
    void getAllAlertesByMeubleUniquementIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where meubleUniquement in
        defaultAlerteFiltering(
            "meubleUniquement.in=" + DEFAULT_MEUBLE_UNIQUEMENT + "," + UPDATED_MEUBLE_UNIQUEMENT,
            "meubleUniquement.in=" + UPDATED_MEUBLE_UNIQUEMENT
        );
    }

    @Test
    @Transactional
    void getAllAlertesByMeubleUniquementIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where meubleUniquement is not null
        defaultAlerteFiltering("meubleUniquement.specified=true", "meubleUniquement.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where active equals to
        defaultAlerteFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAlertesByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where active in
        defaultAlerteFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllAlertesByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where active is not null
        defaultAlerteFiltering("active.specified=true", "active.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByFrequenceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where frequence equals to
        defaultAlerteFiltering("frequence.equals=" + DEFAULT_FREQUENCE, "frequence.equals=" + UPDATED_FREQUENCE);
    }

    @Test
    @Transactional
    void getAllAlertesByFrequenceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where frequence in
        defaultAlerteFiltering("frequence.in=" + DEFAULT_FREQUENCE + "," + UPDATED_FREQUENCE, "frequence.in=" + UPDATED_FREQUENCE);
    }

    @Test
    @Transactional
    void getAllAlertesByFrequenceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where frequence is not null
        defaultAlerteFiltering("frequence.specified=true", "frequence.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByDerniereExecutionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where derniereExecution equals to
        defaultAlerteFiltering(
            "derniereExecution.equals=" + DEFAULT_DERNIERE_EXECUTION,
            "derniereExecution.equals=" + UPDATED_DERNIERE_EXECUTION
        );
    }

    @Test
    @Transactional
    void getAllAlertesByDerniereExecutionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where derniereExecution in
        defaultAlerteFiltering(
            "derniereExecution.in=" + DEFAULT_DERNIERE_EXECUTION + "," + UPDATED_DERNIERE_EXECUTION,
            "derniereExecution.in=" + UPDATED_DERNIERE_EXECUTION
        );
    }

    @Test
    @Transactional
    void getAllAlertesByDerniereExecutionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        // Get all the alerteList where derniereExecution is not null
        defaultAlerteFiltering("derniereExecution.specified=true", "derniereExecution.specified=false");
    }

    @Test
    @Transactional
    void getAllAlertesByTitulaireIsEqualToSomething() throws Exception {
        User titulaire;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            alerteRepository.saveAndFlush(alerte);
            titulaire = UserResourceIT.createEntity();
        } else {
            titulaire = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(titulaire);
        em.flush();
        alerte.setTitulaire(titulaire);
        alerteRepository.saveAndFlush(alerte);
        Long titulaireId = titulaire.getId();
        // Get all the alerteList where titulaire equals to titulaireId
        defaultAlerteShouldBeFound("titulaireId.equals=" + titulaireId);

        // Get all the alerteList where titulaire equals to (titulaireId + 1)
        defaultAlerteShouldNotBeFound("titulaireId.equals=" + (titulaireId + 1));
    }

    @Test
    @Transactional
    void getAllAlertesByLocaliteIsEqualToSomething() throws Exception {
        Localite localite;
        if (TestUtil.findAll(em, Localite.class).isEmpty()) {
            alerteRepository.saveAndFlush(alerte);
            localite = LocaliteResourceIT.createEntity();
        } else {
            localite = TestUtil.findAll(em, Localite.class).get(0);
        }
        em.persist(localite);
        em.flush();
        alerte.setLocalite(localite);
        alerteRepository.saveAndFlush(alerte);
        Long localiteId = localite.getId();
        // Get all the alerteList where localite equals to localiteId
        defaultAlerteShouldBeFound("localiteId.equals=" + localiteId);

        // Get all the alerteList where localite equals to (localiteId + 1)
        defaultAlerteShouldNotBeFound("localiteId.equals=" + (localiteId + 1));
    }

    @Test
    @Transactional
    void getAllAlertesByQuartierIsEqualToSomething() throws Exception {
        Quartier quartier;
        if (TestUtil.findAll(em, Quartier.class).isEmpty()) {
            alerteRepository.saveAndFlush(alerte);
            quartier = QuartierResourceIT.createEntity(em);
        } else {
            quartier = TestUtil.findAll(em, Quartier.class).get(0);
        }
        em.persist(quartier);
        em.flush();
        alerte.setQuartier(quartier);
        alerteRepository.saveAndFlush(alerte);
        Long quartierId = quartier.getId();
        // Get all the alerteList where quartier equals to quartierId
        defaultAlerteShouldBeFound("quartierId.equals=" + quartierId);

        // Get all the alerteList where quartier equals to (quartierId + 1)
        defaultAlerteShouldNotBeFound("quartierId.equals=" + (quartierId + 1));
    }

    @Test
    @Transactional
    void getAllAlertesByTypeImmobilierIsEqualToSomething() throws Exception {
        TypeImmobilier typeImmobilier;
        if (TestUtil.findAll(em, TypeImmobilier.class).isEmpty()) {
            alerteRepository.saveAndFlush(alerte);
            typeImmobilier = TypeImmobilierResourceIT.createEntity();
        } else {
            typeImmobilier = TestUtil.findAll(em, TypeImmobilier.class).get(0);
        }
        em.persist(typeImmobilier);
        em.flush();
        alerte.setTypeImmobilier(typeImmobilier);
        alerteRepository.saveAndFlush(alerte);
        Long typeImmobilierId = typeImmobilier.getId();
        // Get all the alerteList where typeImmobilier equals to typeImmobilierId
        defaultAlerteShouldBeFound("typeImmobilierId.equals=" + typeImmobilierId);

        // Get all the alerteList where typeImmobilier equals to (typeImmobilierId + 1)
        defaultAlerteShouldNotBeFound("typeImmobilierId.equals=" + (typeImmobilierId + 1));
    }

    private void defaultAlerteFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAlerteShouldBeFound(shouldBeFound);
        defaultAlerteShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAlerteShouldBeFound(String filter) throws Exception {
        restAlerteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alerte.getId().intValue())))
            .andExpect(jsonPath("$.[*].titre").value(hasItem(DEFAULT_TITRE)))
            .andExpect(jsonPath("$.[*].contenu").value(hasItem(DEFAULT_CONTENU)))
            .andExpect(jsonPath("$.[*].typeAnnonce").value(hasItem(DEFAULT_TYPE_ANNONCE.toString())))
            .andExpect(jsonPath("$.[*].prixMin").value(hasItem(DEFAULT_PRIX_MIN)))
            .andExpect(jsonPath("$.[*].prixMax").value(hasItem(DEFAULT_PRIX_MAX)))
            .andExpect(jsonPath("$.[*].surfaceMin").value(hasItem(DEFAULT_SURFACE_MIN)))
            .andExpect(jsonPath("$.[*].nombreChambresMin").value(hasItem(DEFAULT_NOMBRE_CHAMBRES_MIN)))
            .andExpect(jsonPath("$.[*].meubleUniquement").value(hasItem(DEFAULT_MEUBLE_UNIQUEMENT)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].frequence").value(hasItem(DEFAULT_FREQUENCE.toString())))
            .andExpect(jsonPath("$.[*].derniereExecution").value(hasItem(DEFAULT_DERNIERE_EXECUTION.toString())));

        // Check, that the count call also returns 1
        restAlerteMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAlerteShouldNotBeFound(String filter) throws Exception {
        restAlerteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAlerteMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAlerte() throws Exception {
        // Get the alerte
        restAlerteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlerte() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alerte
        Alerte updatedAlerte = alerteRepository.findById(alerte.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAlerte are not directly saved in db
        em.detach(updatedAlerte);
        updatedAlerte
            .titre(UPDATED_TITRE)
            .contenu(UPDATED_CONTENU)
            .typeAnnonce(UPDATED_TYPE_ANNONCE)
            .prixMin(UPDATED_PRIX_MIN)
            .prixMax(UPDATED_PRIX_MAX)
            .surfaceMin(UPDATED_SURFACE_MIN)
            .nombreChambresMin(UPDATED_NOMBRE_CHAMBRES_MIN)
            .meubleUniquement(UPDATED_MEUBLE_UNIQUEMENT)
            .active(UPDATED_ACTIVE)
            .frequence(UPDATED_FREQUENCE)
            .derniereExecution(UPDATED_DERNIERE_EXECUTION);
        AlerteDTO alerteDTO = alerteMapper.toDto(updatedAlerte);

        restAlerteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alerteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlerteToMatchAllProperties(updatedAlerte);
    }

    @Test
    @Transactional
    void putNonExistingAlerte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerte.setId(longCount.incrementAndGet());

        // Create the Alerte
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlerteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alerteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlerte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerte.setId(longCount.incrementAndGet());

        // Create the Alerte
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alerteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlerte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerte.setId(longCount.incrementAndGet());

        // Create the Alerte
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alerteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlerteWithPatch() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alerte using partial update
        Alerte partialUpdatedAlerte = new Alerte();
        partialUpdatedAlerte.setId(alerte.getId());

        partialUpdatedAlerte
            .typeAnnonce(UPDATED_TYPE_ANNONCE)
            .nombreChambresMin(UPDATED_NOMBRE_CHAMBRES_MIN)
            .meubleUniquement(UPDATED_MEUBLE_UNIQUEMENT)
            .active(UPDATED_ACTIVE);

        restAlerteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlerte.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlerte))
            )
            .andExpect(status().isOk());

        // Validate the Alerte in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlerteUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAlerte, alerte), getPersistedAlerte(alerte));
    }

    @Test
    @Transactional
    void fullUpdateAlerteWithPatch() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alerte using partial update
        Alerte partialUpdatedAlerte = new Alerte();
        partialUpdatedAlerte.setId(alerte.getId());

        partialUpdatedAlerte
            .titre(UPDATED_TITRE)
            .contenu(UPDATED_CONTENU)
            .typeAnnonce(UPDATED_TYPE_ANNONCE)
            .prixMin(UPDATED_PRIX_MIN)
            .prixMax(UPDATED_PRIX_MAX)
            .surfaceMin(UPDATED_SURFACE_MIN)
            .nombreChambresMin(UPDATED_NOMBRE_CHAMBRES_MIN)
            .meubleUniquement(UPDATED_MEUBLE_UNIQUEMENT)
            .active(UPDATED_ACTIVE)
            .frequence(UPDATED_FREQUENCE)
            .derniereExecution(UPDATED_DERNIERE_EXECUTION);

        restAlerteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlerte.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlerte))
            )
            .andExpect(status().isOk());

        // Validate the Alerte in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlerteUpdatableFieldsEquals(partialUpdatedAlerte, getPersistedAlerte(partialUpdatedAlerte));
    }

    @Test
    @Transactional
    void patchNonExistingAlerte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerte.setId(longCount.incrementAndGet());

        // Create the Alerte
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlerteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alerteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alerteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlerte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerte.setId(longCount.incrementAndGet());

        // Create the Alerte
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alerteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlerte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alerte.setId(longCount.incrementAndGet());

        // Create the Alerte
        AlerteDTO alerteDTO = alerteMapper.toDto(alerte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlerteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(alerteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Alerte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAlerte() throws Exception {
        // Initialize the database
        insertedAlerte = alerteRepository.saveAndFlush(alerte);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the alerte
        restAlerteMockMvc
            .perform(delete(ENTITY_API_URL_ID, alerte.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return alerteRepository.count();
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

    protected Alerte getPersistedAlerte(Alerte alerte) {
        return alerteRepository.findById(alerte.getId()).orElseThrow();
    }

    protected void assertPersistedAlerteToMatchAllProperties(Alerte expectedAlerte) {
        assertAlerteAllPropertiesEquals(expectedAlerte, getPersistedAlerte(expectedAlerte));
    }

    protected void assertPersistedAlerteToMatchUpdatableProperties(Alerte expectedAlerte) {
        assertAlerteAllUpdatablePropertiesEquals(expectedAlerte, getPersistedAlerte(expectedAlerte));
    }
}
