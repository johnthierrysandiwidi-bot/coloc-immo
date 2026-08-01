package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.ImmobilierAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.domain.Quartier;
import bf.colocation.immo.domain.TypeImmobilier;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutBien;
import bf.colocation.immo.repository.ImmobilierRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.ImmobilierService;
import bf.colocation.immo.service.dto.ImmobilierDTO;
import bf.colocation.immo.service.mapper.ImmobilierMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
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
 * Integration tests for the {@link ImmobilierResource} REST controller.
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
class ImmobilierResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_ADRESSE = "AAAAAAAAAA";
    private static final String UPDATED_ADRESSE = "BBBBBBBBBB";

    private static final Double DEFAULT_SURFACE = 0D;
    private static final Double UPDATED_SURFACE = 1D;
    private static final Double SMALLER_SURFACE = 0D - 1D;

    private static final Integer DEFAULT_NOMBRE_PIECES = 0;
    private static final Integer UPDATED_NOMBRE_PIECES = 1;
    private static final Integer SMALLER_NOMBRE_PIECES = 0 - 1;

    private static final Integer DEFAULT_NOMBRE_CHAMBRES = 0;
    private static final Integer UPDATED_NOMBRE_CHAMBRES = 1;
    private static final Integer SMALLER_NOMBRE_CHAMBRES = 0 - 1;

    private static final Integer DEFAULT_NOMBRE_SALLES_BAIN = 0;
    private static final Integer UPDATED_NOMBRE_SALLES_BAIN = 1;
    private static final Integer SMALLER_NOMBRE_SALLES_BAIN = 0 - 1;

    private static final Integer DEFAULT_NOMBRE_SALONS = 0;
    private static final Integer UPDATED_NOMBRE_SALONS = 1;
    private static final Integer SMALLER_NOMBRE_SALONS = 0 - 1;

    private static final Boolean DEFAULT_GARAGE = false;
    private static final Boolean UPDATED_GARAGE = true;

    private static final Boolean DEFAULT_PISCINE = false;
    private static final Boolean UPDATED_PISCINE = true;

    private static final Boolean DEFAULT_MEUBLE = false;
    private static final Boolean UPDATED_MEUBLE = true;

    private static final LocalDate DEFAULT_DISPONIBLE_A = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DISPONIBLE_A = LocalDate.parse("2023-12-16");
    private static final LocalDate SMALLER_DISPONIBLE_A = LocalDate.ofEpochDay(-1L);

    private static final StatutBien DEFAULT_STATUT = StatutBien.BROUILLON;
    private static final StatutBien UPDATED_STATUT = StatutBien.DISPONIBLE;

    private static final Double DEFAULT_LATITUDE = 1D;
    private static final Double UPDATED_LATITUDE = 2D;
    private static final Double SMALLER_LATITUDE = 1D - 1D;

    private static final Double DEFAULT_LONGITUDE = 1D;
    private static final Double UPDATED_LONGITUDE = 2D;
    private static final Double SMALLER_LONGITUDE = 1D - 1D;

    private static final Instant DEFAULT_DATE_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_CREATION = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/immobiliers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ImmobilierRepository immobilierRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ImmobilierRepository immobilierRepositoryMock;

    @Autowired
    private ImmobilierMapper immobilierMapper;

    @Mock
    private ImmobilierService immobilierServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restImmobilierMockMvc;

    private Immobilier immobilier;

    private Immobilier insertedImmobilier;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Immobilier createEntity(EntityManager em) {
        Immobilier immobilier = new Immobilier()
            .nom(DEFAULT_NOM)
            .description(DEFAULT_DESCRIPTION)
            .adresse(DEFAULT_ADRESSE)
            .surface(DEFAULT_SURFACE)
            .nombrePieces(DEFAULT_NOMBRE_PIECES)
            .nombreChambres(DEFAULT_NOMBRE_CHAMBRES)
            .nombreSallesBain(DEFAULT_NOMBRE_SALLES_BAIN)
            .nombreSalons(DEFAULT_NOMBRE_SALONS)
            .garage(DEFAULT_GARAGE)
            .piscine(DEFAULT_PISCINE)
            .meuble(DEFAULT_MEUBLE)
            .disponibleA(DEFAULT_DISPONIBLE_A)
            .statut(DEFAULT_STATUT)
            .latitude(DEFAULT_LATITUDE)
            .longitude(DEFAULT_LONGITUDE)
            .dateCreation(DEFAULT_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        immobilier.setProprietaire(user);
        return immobilier;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Immobilier createUpdatedEntity(EntityManager em) {
        Immobilier updatedImmobilier = new Immobilier()
            .nom(UPDATED_NOM)
            .description(UPDATED_DESCRIPTION)
            .adresse(UPDATED_ADRESSE)
            .surface(UPDATED_SURFACE)
            .nombrePieces(UPDATED_NOMBRE_PIECES)
            .nombreChambres(UPDATED_NOMBRE_CHAMBRES)
            .nombreSallesBain(UPDATED_NOMBRE_SALLES_BAIN)
            .nombreSalons(UPDATED_NOMBRE_SALONS)
            .garage(UPDATED_GARAGE)
            .piscine(UPDATED_PISCINE)
            .meuble(UPDATED_MEUBLE)
            .disponibleA(UPDATED_DISPONIBLE_A)
            .statut(UPDATED_STATUT)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE)
            .dateCreation(UPDATED_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedImmobilier.setProprietaire(user);
        return updatedImmobilier;
    }

    @BeforeEach
    void initTest() {
        immobilier = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedImmobilier != null) {
            immobilierRepository.delete(insertedImmobilier);
            insertedImmobilier = null;
        }
    }

    @Test
    @Transactional
    void createImmobilier() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Immobilier
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);
        var returnedImmobilierDTO = om.readValue(
            restImmobilierMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(immobilierDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ImmobilierDTO.class
        );

        // Validate the Immobilier in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedImmobilier = immobilierMapper.toEntity(returnedImmobilierDTO);
        assertImmobilierUpdatableFieldsEquals(returnedImmobilier, getPersistedImmobilier(returnedImmobilier));

        insertedImmobilier = returnedImmobilier;
    }

    @Test
    @Transactional
    void createImmobilierWithExistingId() throws Exception {
        // Create the Immobilier with an existing ID
        immobilier.setId(1L);
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restImmobilierMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(immobilierDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        immobilier.setNom(null);

        // Create the Immobilier, which fails.
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        restImmobilierMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(immobilierDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatutIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        immobilier.setStatut(null);

        // Create the Immobilier, which fails.
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        restImmobilierMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(immobilierDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllImmobiliers() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList
        restImmobilierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(immobilier.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].adresse").value(hasItem(DEFAULT_ADRESSE)))
            .andExpect(jsonPath("$.[*].surface").value(hasItem(DEFAULT_SURFACE)))
            .andExpect(jsonPath("$.[*].nombrePieces").value(hasItem(DEFAULT_NOMBRE_PIECES)))
            .andExpect(jsonPath("$.[*].nombreChambres").value(hasItem(DEFAULT_NOMBRE_CHAMBRES)))
            .andExpect(jsonPath("$.[*].nombreSallesBain").value(hasItem(DEFAULT_NOMBRE_SALLES_BAIN)))
            .andExpect(jsonPath("$.[*].nombreSalons").value(hasItem(DEFAULT_NOMBRE_SALONS)))
            .andExpect(jsonPath("$.[*].garage").value(hasItem(DEFAULT_GARAGE)))
            .andExpect(jsonPath("$.[*].piscine").value(hasItem(DEFAULT_PISCINE)))
            .andExpect(jsonPath("$.[*].meuble").value(hasItem(DEFAULT_MEUBLE)))
            .andExpect(jsonPath("$.[*].disponibleA").value(hasItem(DEFAULT_DISPONIBLE_A.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())))
            .andExpect(jsonPath("$.[*].latitude").value(hasItem(DEFAULT_LATITUDE)))
            .andExpect(jsonPath("$.[*].longitude").value(hasItem(DEFAULT_LONGITUDE)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllImmobiliersWithEagerRelationshipsIsEnabled() throws Exception {
        when(immobilierServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restImmobilierMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(immobilierServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllImmobiliersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(immobilierServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restImmobilierMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(immobilierRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getImmobilier() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get the immobilier
        restImmobilierMockMvc
            .perform(get(ENTITY_API_URL_ID, immobilier.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(immobilier.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.adresse").value(DEFAULT_ADRESSE))
            .andExpect(jsonPath("$.surface").value(DEFAULT_SURFACE))
            .andExpect(jsonPath("$.nombrePieces").value(DEFAULT_NOMBRE_PIECES))
            .andExpect(jsonPath("$.nombreChambres").value(DEFAULT_NOMBRE_CHAMBRES))
            .andExpect(jsonPath("$.nombreSallesBain").value(DEFAULT_NOMBRE_SALLES_BAIN))
            .andExpect(jsonPath("$.nombreSalons").value(DEFAULT_NOMBRE_SALONS))
            .andExpect(jsonPath("$.garage").value(DEFAULT_GARAGE))
            .andExpect(jsonPath("$.piscine").value(DEFAULT_PISCINE))
            .andExpect(jsonPath("$.meuble").value(DEFAULT_MEUBLE))
            .andExpect(jsonPath("$.disponibleA").value(DEFAULT_DISPONIBLE_A.toString()))
            .andExpect(jsonPath("$.statut").value(DEFAULT_STATUT.toString()))
            .andExpect(jsonPath("$.latitude").value(DEFAULT_LATITUDE))
            .andExpect(jsonPath("$.longitude").value(DEFAULT_LONGITUDE))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()));
    }

    @Test
    @Transactional
    void getImmobiliersByIdFiltering() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        Long id = immobilier.getId();

        defaultImmobilierFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultImmobilierFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultImmobilierFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNomIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nom equals to
        defaultImmobilierFiltering("nom.equals=" + DEFAULT_NOM, "nom.equals=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNomIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nom in
        defaultImmobilierFiltering("nom.in=" + DEFAULT_NOM + "," + UPDATED_NOM, "nom.in=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNomIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nom is not null
        defaultImmobilierFiltering("nom.specified=true", "nom.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByNomContainsSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nom contains
        defaultImmobilierFiltering("nom.contains=" + DEFAULT_NOM, "nom.contains=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNomNotContainsSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nom does not contain
        defaultImmobilierFiltering("nom.doesNotContain=" + UPDATED_NOM, "nom.doesNotContain=" + DEFAULT_NOM);
    }

    @Test
    @Transactional
    void getAllImmobiliersByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where description equals to
        defaultImmobilierFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllImmobiliersByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where description in
        defaultImmobilierFiltering(
            "description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION,
            "description.in=" + UPDATED_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where description is not null
        defaultImmobilierFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where description contains
        defaultImmobilierFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllImmobiliersByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where description does not contain
        defaultImmobilierFiltering(
            "description.doesNotContain=" + UPDATED_DESCRIPTION,
            "description.doesNotContain=" + DEFAULT_DESCRIPTION
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByAdresseIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where adresse equals to
        defaultImmobilierFiltering("adresse.equals=" + DEFAULT_ADRESSE, "adresse.equals=" + UPDATED_ADRESSE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByAdresseIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where adresse in
        defaultImmobilierFiltering("adresse.in=" + DEFAULT_ADRESSE + "," + UPDATED_ADRESSE, "adresse.in=" + UPDATED_ADRESSE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByAdresseIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where adresse is not null
        defaultImmobilierFiltering("adresse.specified=true", "adresse.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByAdresseContainsSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where adresse contains
        defaultImmobilierFiltering("adresse.contains=" + DEFAULT_ADRESSE, "adresse.contains=" + UPDATED_ADRESSE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByAdresseNotContainsSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where adresse does not contain
        defaultImmobilierFiltering("adresse.doesNotContain=" + UPDATED_ADRESSE, "adresse.doesNotContain=" + DEFAULT_ADRESSE);
    }

    @Test
    @Transactional
    void getAllImmobiliersBySurfaceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where surface equals to
        defaultImmobilierFiltering("surface.equals=" + DEFAULT_SURFACE, "surface.equals=" + UPDATED_SURFACE);
    }

    @Test
    @Transactional
    void getAllImmobiliersBySurfaceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where surface in
        defaultImmobilierFiltering("surface.in=" + DEFAULT_SURFACE + "," + UPDATED_SURFACE, "surface.in=" + UPDATED_SURFACE);
    }

    @Test
    @Transactional
    void getAllImmobiliersBySurfaceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where surface is not null
        defaultImmobilierFiltering("surface.specified=true", "surface.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersBySurfaceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where surface is greater than or equal to
        defaultImmobilierFiltering("surface.greaterThanOrEqual=" + DEFAULT_SURFACE, "surface.greaterThanOrEqual=" + UPDATED_SURFACE);
    }

    @Test
    @Transactional
    void getAllImmobiliersBySurfaceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where surface is less than or equal to
        defaultImmobilierFiltering("surface.lessThanOrEqual=" + DEFAULT_SURFACE, "surface.lessThanOrEqual=" + SMALLER_SURFACE);
    }

    @Test
    @Transactional
    void getAllImmobiliersBySurfaceIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where surface is less than
        defaultImmobilierFiltering("surface.lessThan=" + UPDATED_SURFACE, "surface.lessThan=" + DEFAULT_SURFACE);
    }

    @Test
    @Transactional
    void getAllImmobiliersBySurfaceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where surface is greater than
        defaultImmobilierFiltering("surface.greaterThan=" + SMALLER_SURFACE, "surface.greaterThan=" + DEFAULT_SURFACE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombrePiecesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombrePieces equals to
        defaultImmobilierFiltering("nombrePieces.equals=" + DEFAULT_NOMBRE_PIECES, "nombrePieces.equals=" + UPDATED_NOMBRE_PIECES);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombrePiecesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombrePieces in
        defaultImmobilierFiltering(
            "nombrePieces.in=" + DEFAULT_NOMBRE_PIECES + "," + UPDATED_NOMBRE_PIECES,
            "nombrePieces.in=" + UPDATED_NOMBRE_PIECES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombrePiecesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombrePieces is not null
        defaultImmobilierFiltering("nombrePieces.specified=true", "nombrePieces.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombrePiecesIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombrePieces is greater than or equal to
        defaultImmobilierFiltering(
            "nombrePieces.greaterThanOrEqual=" + DEFAULT_NOMBRE_PIECES,
            "nombrePieces.greaterThanOrEqual=" + UPDATED_NOMBRE_PIECES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombrePiecesIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombrePieces is less than or equal to
        defaultImmobilierFiltering(
            "nombrePieces.lessThanOrEqual=" + DEFAULT_NOMBRE_PIECES,
            "nombrePieces.lessThanOrEqual=" + SMALLER_NOMBRE_PIECES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombrePiecesIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombrePieces is less than
        defaultImmobilierFiltering("nombrePieces.lessThan=" + UPDATED_NOMBRE_PIECES, "nombrePieces.lessThan=" + DEFAULT_NOMBRE_PIECES);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombrePiecesIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombrePieces is greater than
        defaultImmobilierFiltering(
            "nombrePieces.greaterThan=" + SMALLER_NOMBRE_PIECES,
            "nombrePieces.greaterThan=" + DEFAULT_NOMBRE_PIECES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreChambresIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreChambres equals to
        defaultImmobilierFiltering("nombreChambres.equals=" + DEFAULT_NOMBRE_CHAMBRES, "nombreChambres.equals=" + UPDATED_NOMBRE_CHAMBRES);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreChambresIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreChambres in
        defaultImmobilierFiltering(
            "nombreChambres.in=" + DEFAULT_NOMBRE_CHAMBRES + "," + UPDATED_NOMBRE_CHAMBRES,
            "nombreChambres.in=" + UPDATED_NOMBRE_CHAMBRES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreChambresIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreChambres is not null
        defaultImmobilierFiltering("nombreChambres.specified=true", "nombreChambres.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreChambresIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreChambres is greater than or equal to
        defaultImmobilierFiltering(
            "nombreChambres.greaterThanOrEqual=" + DEFAULT_NOMBRE_CHAMBRES,
            "nombreChambres.greaterThanOrEqual=" + UPDATED_NOMBRE_CHAMBRES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreChambresIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreChambres is less than or equal to
        defaultImmobilierFiltering(
            "nombreChambres.lessThanOrEqual=" + DEFAULT_NOMBRE_CHAMBRES,
            "nombreChambres.lessThanOrEqual=" + SMALLER_NOMBRE_CHAMBRES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreChambresIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreChambres is less than
        defaultImmobilierFiltering(
            "nombreChambres.lessThan=" + UPDATED_NOMBRE_CHAMBRES,
            "nombreChambres.lessThan=" + DEFAULT_NOMBRE_CHAMBRES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreChambresIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreChambres is greater than
        defaultImmobilierFiltering(
            "nombreChambres.greaterThan=" + SMALLER_NOMBRE_CHAMBRES,
            "nombreChambres.greaterThan=" + DEFAULT_NOMBRE_CHAMBRES
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSallesBainIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSallesBain equals to
        defaultImmobilierFiltering(
            "nombreSallesBain.equals=" + DEFAULT_NOMBRE_SALLES_BAIN,
            "nombreSallesBain.equals=" + UPDATED_NOMBRE_SALLES_BAIN
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSallesBainIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSallesBain in
        defaultImmobilierFiltering(
            "nombreSallesBain.in=" + DEFAULT_NOMBRE_SALLES_BAIN + "," + UPDATED_NOMBRE_SALLES_BAIN,
            "nombreSallesBain.in=" + UPDATED_NOMBRE_SALLES_BAIN
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSallesBainIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSallesBain is not null
        defaultImmobilierFiltering("nombreSallesBain.specified=true", "nombreSallesBain.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSallesBainIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSallesBain is greater than or equal to
        defaultImmobilierFiltering(
            "nombreSallesBain.greaterThanOrEqual=" + DEFAULT_NOMBRE_SALLES_BAIN,
            "nombreSallesBain.greaterThanOrEqual=" + UPDATED_NOMBRE_SALLES_BAIN
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSallesBainIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSallesBain is less than or equal to
        defaultImmobilierFiltering(
            "nombreSallesBain.lessThanOrEqual=" + DEFAULT_NOMBRE_SALLES_BAIN,
            "nombreSallesBain.lessThanOrEqual=" + SMALLER_NOMBRE_SALLES_BAIN
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSallesBainIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSallesBain is less than
        defaultImmobilierFiltering(
            "nombreSallesBain.lessThan=" + UPDATED_NOMBRE_SALLES_BAIN,
            "nombreSallesBain.lessThan=" + DEFAULT_NOMBRE_SALLES_BAIN
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSallesBainIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSallesBain is greater than
        defaultImmobilierFiltering(
            "nombreSallesBain.greaterThan=" + SMALLER_NOMBRE_SALLES_BAIN,
            "nombreSallesBain.greaterThan=" + DEFAULT_NOMBRE_SALLES_BAIN
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSalonsIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSalons equals to
        defaultImmobilierFiltering("nombreSalons.equals=" + DEFAULT_NOMBRE_SALONS, "nombreSalons.equals=" + UPDATED_NOMBRE_SALONS);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSalonsIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSalons in
        defaultImmobilierFiltering(
            "nombreSalons.in=" + DEFAULT_NOMBRE_SALONS + "," + UPDATED_NOMBRE_SALONS,
            "nombreSalons.in=" + UPDATED_NOMBRE_SALONS
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSalonsIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSalons is not null
        defaultImmobilierFiltering("nombreSalons.specified=true", "nombreSalons.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSalonsIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSalons is greater than or equal to
        defaultImmobilierFiltering(
            "nombreSalons.greaterThanOrEqual=" + DEFAULT_NOMBRE_SALONS,
            "nombreSalons.greaterThanOrEqual=" + UPDATED_NOMBRE_SALONS
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSalonsIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSalons is less than or equal to
        defaultImmobilierFiltering(
            "nombreSalons.lessThanOrEqual=" + DEFAULT_NOMBRE_SALONS,
            "nombreSalons.lessThanOrEqual=" + SMALLER_NOMBRE_SALONS
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSalonsIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSalons is less than
        defaultImmobilierFiltering("nombreSalons.lessThan=" + UPDATED_NOMBRE_SALONS, "nombreSalons.lessThan=" + DEFAULT_NOMBRE_SALONS);
    }

    @Test
    @Transactional
    void getAllImmobiliersByNombreSalonsIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where nombreSalons is greater than
        defaultImmobilierFiltering(
            "nombreSalons.greaterThan=" + SMALLER_NOMBRE_SALONS,
            "nombreSalons.greaterThan=" + DEFAULT_NOMBRE_SALONS
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByGarageIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where garage equals to
        defaultImmobilierFiltering("garage.equals=" + DEFAULT_GARAGE, "garage.equals=" + UPDATED_GARAGE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByGarageIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where garage in
        defaultImmobilierFiltering("garage.in=" + DEFAULT_GARAGE + "," + UPDATED_GARAGE, "garage.in=" + UPDATED_GARAGE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByGarageIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where garage is not null
        defaultImmobilierFiltering("garage.specified=true", "garage.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByPiscineIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where piscine equals to
        defaultImmobilierFiltering("piscine.equals=" + DEFAULT_PISCINE, "piscine.equals=" + UPDATED_PISCINE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByPiscineIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where piscine in
        defaultImmobilierFiltering("piscine.in=" + DEFAULT_PISCINE + "," + UPDATED_PISCINE, "piscine.in=" + UPDATED_PISCINE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByPiscineIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where piscine is not null
        defaultImmobilierFiltering("piscine.specified=true", "piscine.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByMeubleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where meuble equals to
        defaultImmobilierFiltering("meuble.equals=" + DEFAULT_MEUBLE, "meuble.equals=" + UPDATED_MEUBLE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByMeubleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where meuble in
        defaultImmobilierFiltering("meuble.in=" + DEFAULT_MEUBLE + "," + UPDATED_MEUBLE, "meuble.in=" + UPDATED_MEUBLE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByMeubleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where meuble is not null
        defaultImmobilierFiltering("meuble.specified=true", "meuble.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByDisponibleAIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where disponibleA equals to
        defaultImmobilierFiltering("disponibleA.equals=" + DEFAULT_DISPONIBLE_A, "disponibleA.equals=" + UPDATED_DISPONIBLE_A);
    }

    @Test
    @Transactional
    void getAllImmobiliersByDisponibleAIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where disponibleA in
        defaultImmobilierFiltering(
            "disponibleA.in=" + DEFAULT_DISPONIBLE_A + "," + UPDATED_DISPONIBLE_A,
            "disponibleA.in=" + UPDATED_DISPONIBLE_A
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByDisponibleAIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where disponibleA is not null
        defaultImmobilierFiltering("disponibleA.specified=true", "disponibleA.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByDisponibleAIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where disponibleA is greater than or equal to
        defaultImmobilierFiltering(
            "disponibleA.greaterThanOrEqual=" + DEFAULT_DISPONIBLE_A,
            "disponibleA.greaterThanOrEqual=" + UPDATED_DISPONIBLE_A
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByDisponibleAIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where disponibleA is less than or equal to
        defaultImmobilierFiltering(
            "disponibleA.lessThanOrEqual=" + DEFAULT_DISPONIBLE_A,
            "disponibleA.lessThanOrEqual=" + SMALLER_DISPONIBLE_A
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByDisponibleAIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where disponibleA is less than
        defaultImmobilierFiltering("disponibleA.lessThan=" + UPDATED_DISPONIBLE_A, "disponibleA.lessThan=" + DEFAULT_DISPONIBLE_A);
    }

    @Test
    @Transactional
    void getAllImmobiliersByDisponibleAIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where disponibleA is greater than
        defaultImmobilierFiltering("disponibleA.greaterThan=" + SMALLER_DISPONIBLE_A, "disponibleA.greaterThan=" + DEFAULT_DISPONIBLE_A);
    }

    @Test
    @Transactional
    void getAllImmobiliersByStatutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where statut equals to
        defaultImmobilierFiltering("statut.equals=" + DEFAULT_STATUT, "statut.equals=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllImmobiliersByStatutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where statut in
        defaultImmobilierFiltering("statut.in=" + DEFAULT_STATUT + "," + UPDATED_STATUT, "statut.in=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllImmobiliersByStatutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where statut is not null
        defaultImmobilierFiltering("statut.specified=true", "statut.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByLatitudeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where latitude equals to
        defaultImmobilierFiltering("latitude.equals=" + DEFAULT_LATITUDE, "latitude.equals=" + UPDATED_LATITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLatitudeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where latitude in
        defaultImmobilierFiltering("latitude.in=" + DEFAULT_LATITUDE + "," + UPDATED_LATITUDE, "latitude.in=" + UPDATED_LATITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLatitudeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where latitude is not null
        defaultImmobilierFiltering("latitude.specified=true", "latitude.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByLatitudeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where latitude is greater than or equal to
        defaultImmobilierFiltering("latitude.greaterThanOrEqual=" + DEFAULT_LATITUDE, "latitude.greaterThanOrEqual=" + UPDATED_LATITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLatitudeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where latitude is less than or equal to
        defaultImmobilierFiltering("latitude.lessThanOrEqual=" + DEFAULT_LATITUDE, "latitude.lessThanOrEqual=" + SMALLER_LATITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLatitudeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where latitude is less than
        defaultImmobilierFiltering("latitude.lessThan=" + UPDATED_LATITUDE, "latitude.lessThan=" + DEFAULT_LATITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLatitudeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where latitude is greater than
        defaultImmobilierFiltering("latitude.greaterThan=" + SMALLER_LATITUDE, "latitude.greaterThan=" + DEFAULT_LATITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLongitudeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where longitude equals to
        defaultImmobilierFiltering("longitude.equals=" + DEFAULT_LONGITUDE, "longitude.equals=" + UPDATED_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLongitudeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where longitude in
        defaultImmobilierFiltering("longitude.in=" + DEFAULT_LONGITUDE + "," + UPDATED_LONGITUDE, "longitude.in=" + UPDATED_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLongitudeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where longitude is not null
        defaultImmobilierFiltering("longitude.specified=true", "longitude.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByLongitudeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where longitude is greater than or equal to
        defaultImmobilierFiltering(
            "longitude.greaterThanOrEqual=" + DEFAULT_LONGITUDE,
            "longitude.greaterThanOrEqual=" + UPDATED_LONGITUDE
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByLongitudeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where longitude is less than or equal to
        defaultImmobilierFiltering("longitude.lessThanOrEqual=" + DEFAULT_LONGITUDE, "longitude.lessThanOrEqual=" + SMALLER_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLongitudeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where longitude is less than
        defaultImmobilierFiltering("longitude.lessThan=" + UPDATED_LONGITUDE, "longitude.lessThan=" + DEFAULT_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByLongitudeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where longitude is greater than
        defaultImmobilierFiltering("longitude.greaterThan=" + SMALLER_LONGITUDE, "longitude.greaterThan=" + DEFAULT_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllImmobiliersByDateCreationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where dateCreation equals to
        defaultImmobilierFiltering("dateCreation.equals=" + DEFAULT_DATE_CREATION, "dateCreation.equals=" + UPDATED_DATE_CREATION);
    }

    @Test
    @Transactional
    void getAllImmobiliersByDateCreationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where dateCreation in
        defaultImmobilierFiltering(
            "dateCreation.in=" + DEFAULT_DATE_CREATION + "," + UPDATED_DATE_CREATION,
            "dateCreation.in=" + UPDATED_DATE_CREATION
        );
    }

    @Test
    @Transactional
    void getAllImmobiliersByDateCreationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        // Get all the immobilierList where dateCreation is not null
        defaultImmobilierFiltering("dateCreation.specified=true", "dateCreation.specified=false");
    }

    @Test
    @Transactional
    void getAllImmobiliersByProprietaireIsEqualToSomething() throws Exception {
        User proprietaire;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            immobilierRepository.saveAndFlush(immobilier);
            proprietaire = UserResourceIT.createEntity();
        } else {
            proprietaire = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(proprietaire);
        em.flush();
        immobilier.setProprietaire(proprietaire);
        immobilierRepository.saveAndFlush(immobilier);
        Long proprietaireId = proprietaire.getId();
        // Get all the immobilierList where proprietaire equals to proprietaireId
        defaultImmobilierShouldBeFound("proprietaireId.equals=" + proprietaireId);

        // Get all the immobilierList where proprietaire equals to (proprietaireId + 1)
        defaultImmobilierShouldNotBeFound("proprietaireId.equals=" + (proprietaireId + 1));
    }

    @Test
    @Transactional
    void getAllImmobiliersByDemarcheurIsEqualToSomething() throws Exception {
        User demarcheur;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            immobilierRepository.saveAndFlush(immobilier);
            demarcheur = UserResourceIT.createEntity();
        } else {
            demarcheur = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(demarcheur);
        em.flush();
        immobilier.setDemarcheur(demarcheur);
        immobilierRepository.saveAndFlush(immobilier);
        Long demarcheurId = demarcheur.getId();
        // Get all the immobilierList where demarcheur equals to demarcheurId
        defaultImmobilierShouldBeFound("demarcheurId.equals=" + demarcheurId);

        // Get all the immobilierList where demarcheur equals to (demarcheurId + 1)
        defaultImmobilierShouldNotBeFound("demarcheurId.equals=" + (demarcheurId + 1));
    }

    @Test
    @Transactional
    void getAllImmobiliersByLocaliteIsEqualToSomething() throws Exception {
        Localite localite;
        if (TestUtil.findAll(em, Localite.class).isEmpty()) {
            immobilierRepository.saveAndFlush(immobilier);
            localite = LocaliteResourceIT.createEntity();
        } else {
            localite = TestUtil.findAll(em, Localite.class).get(0);
        }
        em.persist(localite);
        em.flush();
        immobilier.setLocalite(localite);
        immobilierRepository.saveAndFlush(immobilier);
        Long localiteId = localite.getId();
        // Get all the immobilierList where localite equals to localiteId
        defaultImmobilierShouldBeFound("localiteId.equals=" + localiteId);

        // Get all the immobilierList where localite equals to (localiteId + 1)
        defaultImmobilierShouldNotBeFound("localiteId.equals=" + (localiteId + 1));
    }

    @Test
    @Transactional
    void getAllImmobiliersByQuartierIsEqualToSomething() throws Exception {
        Quartier quartier;
        if (TestUtil.findAll(em, Quartier.class).isEmpty()) {
            immobilierRepository.saveAndFlush(immobilier);
            quartier = QuartierResourceIT.createEntity(em);
        } else {
            quartier = TestUtil.findAll(em, Quartier.class).get(0);
        }
        em.persist(quartier);
        em.flush();
        immobilier.setQuartier(quartier);
        immobilierRepository.saveAndFlush(immobilier);
        Long quartierId = quartier.getId();
        // Get all the immobilierList where quartier equals to quartierId
        defaultImmobilierShouldBeFound("quartierId.equals=" + quartierId);

        // Get all the immobilierList where quartier equals to (quartierId + 1)
        defaultImmobilierShouldNotBeFound("quartierId.equals=" + (quartierId + 1));
    }

    @Test
    @Transactional
    void getAllImmobiliersByTypeImmobilierIsEqualToSomething() throws Exception {
        TypeImmobilier typeImmobilier;
        if (TestUtil.findAll(em, TypeImmobilier.class).isEmpty()) {
            immobilierRepository.saveAndFlush(immobilier);
            typeImmobilier = TypeImmobilierResourceIT.createEntity();
        } else {
            typeImmobilier = TestUtil.findAll(em, TypeImmobilier.class).get(0);
        }
        em.persist(typeImmobilier);
        em.flush();
        immobilier.setTypeImmobilier(typeImmobilier);
        immobilierRepository.saveAndFlush(immobilier);
        Long typeImmobilierId = typeImmobilier.getId();
        // Get all the immobilierList where typeImmobilier equals to typeImmobilierId
        defaultImmobilierShouldBeFound("typeImmobilierId.equals=" + typeImmobilierId);

        // Get all the immobilierList where typeImmobilier equals to (typeImmobilierId + 1)
        defaultImmobilierShouldNotBeFound("typeImmobilierId.equals=" + (typeImmobilierId + 1));
    }

    private void defaultImmobilierFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultImmobilierShouldBeFound(shouldBeFound);
        defaultImmobilierShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultImmobilierShouldBeFound(String filter) throws Exception {
        restImmobilierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(immobilier.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].adresse").value(hasItem(DEFAULT_ADRESSE)))
            .andExpect(jsonPath("$.[*].surface").value(hasItem(DEFAULT_SURFACE)))
            .andExpect(jsonPath("$.[*].nombrePieces").value(hasItem(DEFAULT_NOMBRE_PIECES)))
            .andExpect(jsonPath("$.[*].nombreChambres").value(hasItem(DEFAULT_NOMBRE_CHAMBRES)))
            .andExpect(jsonPath("$.[*].nombreSallesBain").value(hasItem(DEFAULT_NOMBRE_SALLES_BAIN)))
            .andExpect(jsonPath("$.[*].nombreSalons").value(hasItem(DEFAULT_NOMBRE_SALONS)))
            .andExpect(jsonPath("$.[*].garage").value(hasItem(DEFAULT_GARAGE)))
            .andExpect(jsonPath("$.[*].piscine").value(hasItem(DEFAULT_PISCINE)))
            .andExpect(jsonPath("$.[*].meuble").value(hasItem(DEFAULT_MEUBLE)))
            .andExpect(jsonPath("$.[*].disponibleA").value(hasItem(DEFAULT_DISPONIBLE_A.toString())))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())))
            .andExpect(jsonPath("$.[*].latitude").value(hasItem(DEFAULT_LATITUDE)))
            .andExpect(jsonPath("$.[*].longitude").value(hasItem(DEFAULT_LONGITUDE)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));

        // Check, that the count call also returns 1
        restImmobilierMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultImmobilierShouldNotBeFound(String filter) throws Exception {
        restImmobilierMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restImmobilierMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingImmobilier() throws Exception {
        // Get the immobilier
        restImmobilierMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingImmobilier() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the immobilier
        Immobilier updatedImmobilier = immobilierRepository.findById(immobilier.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedImmobilier are not directly saved in db
        em.detach(updatedImmobilier);
        updatedImmobilier
            .nom(UPDATED_NOM)
            .description(UPDATED_DESCRIPTION)
            .adresse(UPDATED_ADRESSE)
            .surface(UPDATED_SURFACE)
            .nombrePieces(UPDATED_NOMBRE_PIECES)
            .nombreChambres(UPDATED_NOMBRE_CHAMBRES)
            .nombreSallesBain(UPDATED_NOMBRE_SALLES_BAIN)
            .nombreSalons(UPDATED_NOMBRE_SALONS)
            .garage(UPDATED_GARAGE)
            .piscine(UPDATED_PISCINE)
            .meuble(UPDATED_MEUBLE)
            .disponibleA(UPDATED_DISPONIBLE_A)
            .statut(UPDATED_STATUT)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE)
            .dateCreation(UPDATED_DATE_CREATION);
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(updatedImmobilier);

        restImmobilierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, immobilierDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(immobilierDTO))
            )
            .andExpect(status().isOk());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedImmobilierToMatchAllProperties(updatedImmobilier);
    }

    @Test
    @Transactional
    void putNonExistingImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        immobilier.setId(longCount.incrementAndGet());

        // Create the Immobilier
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restImmobilierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, immobilierDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(immobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        immobilier.setId(longCount.incrementAndGet());

        // Create the Immobilier
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImmobilierMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(immobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        immobilier.setId(longCount.incrementAndGet());

        // Create the Immobilier
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImmobilierMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(immobilierDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateImmobilierWithPatch() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the immobilier using partial update
        Immobilier partialUpdatedImmobilier = new Immobilier();
        partialUpdatedImmobilier.setId(immobilier.getId());

        partialUpdatedImmobilier
            .nom(UPDATED_NOM)
            .description(UPDATED_DESCRIPTION)
            .adresse(UPDATED_ADRESSE)
            .nombrePieces(UPDATED_NOMBRE_PIECES)
            .nombreChambres(UPDATED_NOMBRE_CHAMBRES)
            .nombreSalons(UPDATED_NOMBRE_SALONS)
            .piscine(UPDATED_PISCINE)
            .statut(UPDATED_STATUT)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE);

        restImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedImmobilier.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedImmobilier))
            )
            .andExpect(status().isOk());

        // Validate the Immobilier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertImmobilierUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedImmobilier, immobilier),
            getPersistedImmobilier(immobilier)
        );
    }

    @Test
    @Transactional
    void fullUpdateImmobilierWithPatch() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the immobilier using partial update
        Immobilier partialUpdatedImmobilier = new Immobilier();
        partialUpdatedImmobilier.setId(immobilier.getId());

        partialUpdatedImmobilier
            .nom(UPDATED_NOM)
            .description(UPDATED_DESCRIPTION)
            .adresse(UPDATED_ADRESSE)
            .surface(UPDATED_SURFACE)
            .nombrePieces(UPDATED_NOMBRE_PIECES)
            .nombreChambres(UPDATED_NOMBRE_CHAMBRES)
            .nombreSallesBain(UPDATED_NOMBRE_SALLES_BAIN)
            .nombreSalons(UPDATED_NOMBRE_SALONS)
            .garage(UPDATED_GARAGE)
            .piscine(UPDATED_PISCINE)
            .meuble(UPDATED_MEUBLE)
            .disponibleA(UPDATED_DISPONIBLE_A)
            .statut(UPDATED_STATUT)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE)
            .dateCreation(UPDATED_DATE_CREATION);

        restImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedImmobilier.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedImmobilier))
            )
            .andExpect(status().isOk());

        // Validate the Immobilier in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertImmobilierUpdatableFieldsEquals(partialUpdatedImmobilier, getPersistedImmobilier(partialUpdatedImmobilier));
    }

    @Test
    @Transactional
    void patchNonExistingImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        immobilier.setId(longCount.incrementAndGet());

        // Create the Immobilier
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, immobilierDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(immobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        immobilier.setId(longCount.incrementAndGet());

        // Create the Immobilier
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImmobilierMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(immobilierDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamImmobilier() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        immobilier.setId(longCount.incrementAndGet());

        // Create the Immobilier
        ImmobilierDTO immobilierDTO = immobilierMapper.toDto(immobilier);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImmobilierMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(immobilierDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Immobilier in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteImmobilier() throws Exception {
        // Initialize the database
        insertedImmobilier = immobilierRepository.saveAndFlush(immobilier);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the immobilier
        restImmobilierMockMvc
            .perform(delete(ENTITY_API_URL_ID, immobilier.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return immobilierRepository.count();
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

    protected Immobilier getPersistedImmobilier(Immobilier immobilier) {
        return immobilierRepository.findById(immobilier.getId()).orElseThrow();
    }

    protected void assertPersistedImmobilierToMatchAllProperties(Immobilier expectedImmobilier) {
        assertImmobilierAllPropertiesEquals(expectedImmobilier, getPersistedImmobilier(expectedImmobilier));
    }

    protected void assertPersistedImmobilierToMatchUpdatableProperties(Immobilier expectedImmobilier) {
        assertImmobilierAllUpdatablePropertiesEquals(expectedImmobilier, getPersistedImmobilier(expectedImmobilier));
    }
}
