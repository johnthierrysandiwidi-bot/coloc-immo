package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.RendezVousAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.RendezVousService;
import bf.colocation.immo.service.dto.RendezVousDTO;
import bf.colocation.immo.service.mapper.RendezVousMapper;
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
 * Integration tests for the {@link RendezVousResource} REST controller.
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
class RendezVousResourceIT {

    private static final Instant DEFAULT_DATE_HEURE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_HEURE = Instant.ofEpochMilli(1702714037224L);

    private static final Instant DEFAULT_DATE_REPORTEE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_REPORTEE = Instant.ofEpochMilli(1702714037224L);

    private static final String DEFAULT_LIEU = "AAAAAAAAAA";
    private static final String UPDATED_LIEU = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENU = "AAAAAAAAAA";
    private static final String UPDATED_CONTENU = "BBBBBBBBBB";

    private static final String DEFAULT_MOTIF = "AAAAAAAAAA";
    private static final String UPDATED_MOTIF = "BBBBBBBBBB";

    private static final StatutRendezVous DEFAULT_STATUT = StatutRendezVous.DEMANDE;
    private static final StatutRendezVous UPDATED_STATUT = StatutRendezVous.ACCEPTE;

    private static final String ENTITY_API_URL = "/api/rendez-vous";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private RendezVousRepository rendezVousRepositoryMock;

    @Autowired
    private RendezVousMapper rendezVousMapper;

    @Mock
    private RendezVousService rendezVousServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRendezVousMockMvc;

    private RendezVous rendezVous;

    private RendezVous insertedRendezVous;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RendezVous createEntity(EntityManager em) {
        RendezVous rendezVous = new RendezVous()
            .dateHeure(DEFAULT_DATE_HEURE)
            .dateReportee(DEFAULT_DATE_REPORTEE)
            .lieu(DEFAULT_LIEU)
            .contenu(DEFAULT_CONTENU)
            .motif(DEFAULT_MOTIF)
            .statut(DEFAULT_STATUT);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        rendezVous.setAnnonce(annonce);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        rendezVous.setDemandeur(user);
        return rendezVous;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static RendezVous createUpdatedEntity(EntityManager em) {
        RendezVous updatedRendezVous = new RendezVous()
            .dateHeure(UPDATED_DATE_HEURE)
            .dateReportee(UPDATED_DATE_REPORTEE)
            .lieu(UPDATED_LIEU)
            .contenu(UPDATED_CONTENU)
            .motif(UPDATED_MOTIF)
            .statut(UPDATED_STATUT);
        // Add required entity
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            annonce = AnnonceResourceIT.createUpdatedEntity(em);
            em.persist(annonce);
            em.flush();
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        updatedRendezVous.setAnnonce(annonce);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedRendezVous.setDemandeur(user);
        return updatedRendezVous;
    }

    @BeforeEach
    void initTest() {
        rendezVous = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedRendezVous != null) {
            rendezVousRepository.delete(insertedRendezVous);
            insertedRendezVous = null;
        }
    }

    @Test
    @Transactional
    void createRendezVous() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the RendezVous
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);
        var returnedRendezVousDTO = om.readValue(
            restRendezVousMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rendezVousDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            RendezVousDTO.class
        );

        // Validate the RendezVous in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRendezVous = rendezVousMapper.toEntity(returnedRendezVousDTO);
        assertRendezVousUpdatableFieldsEquals(returnedRendezVous, getPersistedRendezVous(returnedRendezVous));

        insertedRendezVous = returnedRendezVous;
    }

    @Test
    @Transactional
    void createRendezVousWithExistingId() throws Exception {
        // Create the RendezVous with an existing ID
        rendezVous.setId(1L);
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRendezVousMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rendezVousDTO)))
            .andExpect(status().isBadRequest());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDateHeureIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rendezVous.setDateHeure(null);

        // Create the RendezVous, which fails.
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        restRendezVousMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rendezVousDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatutIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rendezVous.setStatut(null);

        // Create the RendezVous, which fails.
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        restRendezVousMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rendezVousDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRendezVouses() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList
        restRendezVousMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rendezVous.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateHeure").value(hasItem(DEFAULT_DATE_HEURE.toString())))
            .andExpect(jsonPath("$.[*].dateReportee").value(hasItem(DEFAULT_DATE_REPORTEE.toString())))
            .andExpect(jsonPath("$.[*].lieu").value(hasItem(DEFAULT_LIEU)))
            .andExpect(jsonPath("$.[*].contenu").value(hasItem(DEFAULT_CONTENU)))
            .andExpect(jsonPath("$.[*].motif").value(hasItem(DEFAULT_MOTIF)))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRendezVousesWithEagerRelationshipsIsEnabled() throws Exception {
        when(rendezVousServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRendezVousMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(rendezVousServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRendezVousesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(rendezVousServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRendezVousMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(rendezVousRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getRendezVous() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get the rendezVous
        restRendezVousMockMvc
            .perform(get(ENTITY_API_URL_ID, rendezVous.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(rendezVous.getId().intValue()))
            .andExpect(jsonPath("$.dateHeure").value(DEFAULT_DATE_HEURE.toString()))
            .andExpect(jsonPath("$.dateReportee").value(DEFAULT_DATE_REPORTEE.toString()))
            .andExpect(jsonPath("$.lieu").value(DEFAULT_LIEU))
            .andExpect(jsonPath("$.contenu").value(DEFAULT_CONTENU))
            .andExpect(jsonPath("$.motif").value(DEFAULT_MOTIF))
            .andExpect(jsonPath("$.statut").value(DEFAULT_STATUT.toString()));
    }

    @Test
    @Transactional
    void getRendezVousesByIdFiltering() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        Long id = rendezVous.getId();

        defaultRendezVousFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultRendezVousFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultRendezVousFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllRendezVousesByDateHeureIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where dateHeure equals to
        defaultRendezVousFiltering("dateHeure.equals=" + DEFAULT_DATE_HEURE, "dateHeure.equals=" + UPDATED_DATE_HEURE);
    }

    @Test
    @Transactional
    void getAllRendezVousesByDateHeureIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where dateHeure in
        defaultRendezVousFiltering("dateHeure.in=" + DEFAULT_DATE_HEURE + "," + UPDATED_DATE_HEURE, "dateHeure.in=" + UPDATED_DATE_HEURE);
    }

    @Test
    @Transactional
    void getAllRendezVousesByDateHeureIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where dateHeure is not null
        defaultRendezVousFiltering("dateHeure.specified=true", "dateHeure.specified=false");
    }

    @Test
    @Transactional
    void getAllRendezVousesByDateReporteeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where dateReportee equals to
        defaultRendezVousFiltering("dateReportee.equals=" + DEFAULT_DATE_REPORTEE, "dateReportee.equals=" + UPDATED_DATE_REPORTEE);
    }

    @Test
    @Transactional
    void getAllRendezVousesByDateReporteeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where dateReportee in
        defaultRendezVousFiltering(
            "dateReportee.in=" + DEFAULT_DATE_REPORTEE + "," + UPDATED_DATE_REPORTEE,
            "dateReportee.in=" + UPDATED_DATE_REPORTEE
        );
    }

    @Test
    @Transactional
    void getAllRendezVousesByDateReporteeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where dateReportee is not null
        defaultRendezVousFiltering("dateReportee.specified=true", "dateReportee.specified=false");
    }

    @Test
    @Transactional
    void getAllRendezVousesByLieuIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where lieu equals to
        defaultRendezVousFiltering("lieu.equals=" + DEFAULT_LIEU, "lieu.equals=" + UPDATED_LIEU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByLieuIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where lieu in
        defaultRendezVousFiltering("lieu.in=" + DEFAULT_LIEU + "," + UPDATED_LIEU, "lieu.in=" + UPDATED_LIEU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByLieuIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where lieu is not null
        defaultRendezVousFiltering("lieu.specified=true", "lieu.specified=false");
    }

    @Test
    @Transactional
    void getAllRendezVousesByLieuContainsSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where lieu contains
        defaultRendezVousFiltering("lieu.contains=" + DEFAULT_LIEU, "lieu.contains=" + UPDATED_LIEU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByLieuNotContainsSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where lieu does not contain
        defaultRendezVousFiltering("lieu.doesNotContain=" + UPDATED_LIEU, "lieu.doesNotContain=" + DEFAULT_LIEU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByContenuIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where contenu equals to
        defaultRendezVousFiltering("contenu.equals=" + DEFAULT_CONTENU, "contenu.equals=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByContenuIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where contenu in
        defaultRendezVousFiltering("contenu.in=" + DEFAULT_CONTENU + "," + UPDATED_CONTENU, "contenu.in=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByContenuIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where contenu is not null
        defaultRendezVousFiltering("contenu.specified=true", "contenu.specified=false");
    }

    @Test
    @Transactional
    void getAllRendezVousesByContenuContainsSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where contenu contains
        defaultRendezVousFiltering("contenu.contains=" + DEFAULT_CONTENU, "contenu.contains=" + UPDATED_CONTENU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByContenuNotContainsSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where contenu does not contain
        defaultRendezVousFiltering("contenu.doesNotContain=" + UPDATED_CONTENU, "contenu.doesNotContain=" + DEFAULT_CONTENU);
    }

    @Test
    @Transactional
    void getAllRendezVousesByMotifIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where motif equals to
        defaultRendezVousFiltering("motif.equals=" + DEFAULT_MOTIF, "motif.equals=" + UPDATED_MOTIF);
    }

    @Test
    @Transactional
    void getAllRendezVousesByMotifIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where motif in
        defaultRendezVousFiltering("motif.in=" + DEFAULT_MOTIF + "," + UPDATED_MOTIF, "motif.in=" + UPDATED_MOTIF);
    }

    @Test
    @Transactional
    void getAllRendezVousesByMotifIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where motif is not null
        defaultRendezVousFiltering("motif.specified=true", "motif.specified=false");
    }

    @Test
    @Transactional
    void getAllRendezVousesByMotifContainsSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where motif contains
        defaultRendezVousFiltering("motif.contains=" + DEFAULT_MOTIF, "motif.contains=" + UPDATED_MOTIF);
    }

    @Test
    @Transactional
    void getAllRendezVousesByMotifNotContainsSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where motif does not contain
        defaultRendezVousFiltering("motif.doesNotContain=" + UPDATED_MOTIF, "motif.doesNotContain=" + DEFAULT_MOTIF);
    }

    @Test
    @Transactional
    void getAllRendezVousesByStatutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where statut equals to
        defaultRendezVousFiltering("statut.equals=" + DEFAULT_STATUT, "statut.equals=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllRendezVousesByStatutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where statut in
        defaultRendezVousFiltering("statut.in=" + DEFAULT_STATUT + "," + UPDATED_STATUT, "statut.in=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllRendezVousesByStatutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        // Get all the rendezVousList where statut is not null
        defaultRendezVousFiltering("statut.specified=true", "statut.specified=false");
    }

    @Test
    @Transactional
    void getAllRendezVousesByAnnonceIsEqualToSomething() throws Exception {
        Annonce annonce;
        if (TestUtil.findAll(em, Annonce.class).isEmpty()) {
            rendezVousRepository.saveAndFlush(rendezVous);
            annonce = AnnonceResourceIT.createEntity(em);
        } else {
            annonce = TestUtil.findAll(em, Annonce.class).get(0);
        }
        em.persist(annonce);
        em.flush();
        rendezVous.setAnnonce(annonce);
        rendezVousRepository.saveAndFlush(rendezVous);
        Long annonceId = annonce.getId();
        // Get all the rendezVousList where annonce equals to annonceId
        defaultRendezVousShouldBeFound("annonceId.equals=" + annonceId);

        // Get all the rendezVousList where annonce equals to (annonceId + 1)
        defaultRendezVousShouldNotBeFound("annonceId.equals=" + (annonceId + 1));
    }

    @Test
    @Transactional
    void getAllRendezVousesByDemandeurIsEqualToSomething() throws Exception {
        User demandeur;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            rendezVousRepository.saveAndFlush(rendezVous);
            demandeur = UserResourceIT.createEntity();
        } else {
            demandeur = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(demandeur);
        em.flush();
        rendezVous.setDemandeur(demandeur);
        rendezVousRepository.saveAndFlush(rendezVous);
        Long demandeurId = demandeur.getId();
        // Get all the rendezVousList where demandeur equals to demandeurId
        defaultRendezVousShouldBeFound("demandeurId.equals=" + demandeurId);

        // Get all the rendezVousList where demandeur equals to (demandeurId + 1)
        defaultRendezVousShouldNotBeFound("demandeurId.equals=" + (demandeurId + 1));
    }

    private void defaultRendezVousFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultRendezVousShouldBeFound(shouldBeFound);
        defaultRendezVousShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRendezVousShouldBeFound(String filter) throws Exception {
        restRendezVousMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rendezVous.getId().intValue())))
            .andExpect(jsonPath("$.[*].dateHeure").value(hasItem(DEFAULT_DATE_HEURE.toString())))
            .andExpect(jsonPath("$.[*].dateReportee").value(hasItem(DEFAULT_DATE_REPORTEE.toString())))
            .andExpect(jsonPath("$.[*].lieu").value(hasItem(DEFAULT_LIEU)))
            .andExpect(jsonPath("$.[*].contenu").value(hasItem(DEFAULT_CONTENU)))
            .andExpect(jsonPath("$.[*].motif").value(hasItem(DEFAULT_MOTIF)))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())));

        // Check, that the count call also returns 1
        restRendezVousMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRendezVousShouldNotBeFound(String filter) throws Exception {
        restRendezVousMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRendezVousMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRendezVous() throws Exception {
        // Get the rendezVous
        restRendezVousMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRendezVous() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rendezVous
        RendezVous updatedRendezVous = rendezVousRepository.findById(rendezVous.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedRendezVous are not directly saved in db
        em.detach(updatedRendezVous);
        updatedRendezVous
            .dateHeure(UPDATED_DATE_HEURE)
            .dateReportee(UPDATED_DATE_REPORTEE)
            .lieu(UPDATED_LIEU)
            .contenu(UPDATED_CONTENU)
            .motif(UPDATED_MOTIF)
            .statut(UPDATED_STATUT);
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(updatedRendezVous);

        restRendezVousMockMvc
            .perform(
                put(ENTITY_API_URL_ID, rendezVousDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(rendezVousDTO))
            )
            .andExpect(status().isOk());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRendezVousToMatchAllProperties(updatedRendezVous);
    }

    @Test
    @Transactional
    void putNonExistingRendezVous() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rendezVous.setId(longCount.incrementAndGet());

        // Create the RendezVous
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRendezVousMockMvc
            .perform(
                put(ENTITY_API_URL_ID, rendezVousDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(rendezVousDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRendezVous() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rendezVous.setId(longCount.incrementAndGet());

        // Create the RendezVous
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRendezVousMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(rendezVousDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRendezVous() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rendezVous.setId(longCount.incrementAndGet());

        // Create the RendezVous
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRendezVousMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rendezVousDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRendezVousWithPatch() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rendezVous using partial update
        RendezVous partialUpdatedRendezVous = new RendezVous();
        partialUpdatedRendezVous.setId(rendezVous.getId());

        partialUpdatedRendezVous
            .dateHeure(UPDATED_DATE_HEURE)
            .dateReportee(UPDATED_DATE_REPORTEE)
            .contenu(UPDATED_CONTENU)
            .statut(UPDATED_STATUT);

        restRendezVousMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRendezVous.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRendezVous))
            )
            .andExpect(status().isOk());

        // Validate the RendezVous in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRendezVousUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedRendezVous, rendezVous),
            getPersistedRendezVous(rendezVous)
        );
    }

    @Test
    @Transactional
    void fullUpdateRendezVousWithPatch() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rendezVous using partial update
        RendezVous partialUpdatedRendezVous = new RendezVous();
        partialUpdatedRendezVous.setId(rendezVous.getId());

        partialUpdatedRendezVous
            .dateHeure(UPDATED_DATE_HEURE)
            .dateReportee(UPDATED_DATE_REPORTEE)
            .lieu(UPDATED_LIEU)
            .contenu(UPDATED_CONTENU)
            .motif(UPDATED_MOTIF)
            .statut(UPDATED_STATUT);

        restRendezVousMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRendezVous.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRendezVous))
            )
            .andExpect(status().isOk());

        // Validate the RendezVous in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRendezVousUpdatableFieldsEquals(partialUpdatedRendezVous, getPersistedRendezVous(partialUpdatedRendezVous));
    }

    @Test
    @Transactional
    void patchNonExistingRendezVous() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rendezVous.setId(longCount.incrementAndGet());

        // Create the RendezVous
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRendezVousMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, rendezVousDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(rendezVousDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRendezVous() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rendezVous.setId(longCount.incrementAndGet());

        // Create the RendezVous
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRendezVousMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(rendezVousDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRendezVous() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rendezVous.setId(longCount.incrementAndGet());

        // Create the RendezVous
        RendezVousDTO rendezVousDTO = rendezVousMapper.toDto(rendezVous);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRendezVousMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(rendezVousDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the RendezVous in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRendezVous() throws Exception {
        // Initialize the database
        insertedRendezVous = rendezVousRepository.saveAndFlush(rendezVous);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the rendezVous
        restRendezVousMockMvc
            .perform(delete(ENTITY_API_URL_ID, rendezVous.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return rendezVousRepository.count();
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

    protected RendezVous getPersistedRendezVous(RendezVous rendezVous) {
        return rendezVousRepository.findById(rendezVous.getId()).orElseThrow();
    }

    protected void assertPersistedRendezVousToMatchAllProperties(RendezVous expectedRendezVous) {
        assertRendezVousAllPropertiesEquals(expectedRendezVous, getPersistedRendezVous(expectedRendezVous));
    }

    protected void assertPersistedRendezVousToMatchUpdatableProperties(RendezVous expectedRendezVous) {
        assertRendezVousAllUpdatablePropertiesEquals(expectedRendezVous, getPersistedRendezVous(expectedRendezVous));
    }
}
