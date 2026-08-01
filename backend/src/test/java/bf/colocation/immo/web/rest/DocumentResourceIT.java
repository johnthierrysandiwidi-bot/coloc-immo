package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.DocumentAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Document;
import bf.colocation.immo.domain.TypeDocument;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutValidation;
import bf.colocation.immo.repository.DocumentRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.DocumentService;
import bf.colocation.immo.service.dto.DocumentDTO;
import bf.colocation.immo.service.mapper.DocumentMapper;
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
 * Integration tests for the {@link DocumentResource} REST controller.
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
class DocumentResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final StatutValidation DEFAULT_STATUT = StatutValidation.EN_ATTENTE;
    private static final StatutValidation UPDATED_STATUT = StatutValidation.VALIDE;

    private static final String DEFAULT_MOTIF_REFUS = "AAAAAAAAAA";
    private static final String UPDATED_MOTIF_REFUS = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE_AJOUT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_AJOUT = Instant.ofEpochMilli(1702714037224L);

    private static final Instant DEFAULT_DATE_TRAITEMENT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_TRAITEMENT = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/documents";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private DocumentRepository documentRepositoryMock;

    @Autowired
    private DocumentMapper documentMapper;

    @Mock
    private DocumentService documentServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDocumentMockMvc;

    private Document document;

    private Document insertedDocument;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Document createEntity(EntityManager em) {
        Document document = new Document()
            .nom(DEFAULT_NOM)
            .url(DEFAULT_URL)
            .statut(DEFAULT_STATUT)
            .motifRefus(DEFAULT_MOTIF_REFUS)
            .dateAjout(DEFAULT_DATE_AJOUT)
            .dateTraitement(DEFAULT_DATE_TRAITEMENT);
        // Add required entity
        TypeDocument typeDocument;
        if (TestUtil.findAll(em, TypeDocument.class).isEmpty()) {
            typeDocument = TypeDocumentResourceIT.createEntity();
            em.persist(typeDocument);
            em.flush();
        } else {
            typeDocument = TestUtil.findAll(em, TypeDocument.class).get(0);
        }
        document.setTypeDocument(typeDocument);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        document.setDemarcheur(user);
        return document;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Document createUpdatedEntity(EntityManager em) {
        Document updatedDocument = new Document()
            .nom(UPDATED_NOM)
            .url(UPDATED_URL)
            .statut(UPDATED_STATUT)
            .motifRefus(UPDATED_MOTIF_REFUS)
            .dateAjout(UPDATED_DATE_AJOUT)
            .dateTraitement(UPDATED_DATE_TRAITEMENT);
        // Add required entity
        TypeDocument typeDocument;
        if (TestUtil.findAll(em, TypeDocument.class).isEmpty()) {
            typeDocument = TypeDocumentResourceIT.createUpdatedEntity();
            em.persist(typeDocument);
            em.flush();
        } else {
            typeDocument = TestUtil.findAll(em, TypeDocument.class).get(0);
        }
        updatedDocument.setTypeDocument(typeDocument);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedDocument.setDemarcheur(user);
        return updatedDocument;
    }

    @BeforeEach
    void initTest() {
        document = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDocument != null) {
            documentRepository.delete(insertedDocument);
            insertedDocument = null;
        }
    }

    @Test
    @Transactional
    void createDocument() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Document
        DocumentDTO documentDTO = documentMapper.toDto(document);
        var returnedDocumentDTO = om.readValue(
            restDocumentMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DocumentDTO.class
        );

        // Validate the Document in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDocument = documentMapper.toEntity(returnedDocumentDTO);
        assertDocumentUpdatableFieldsEquals(returnedDocument, getPersistedDocument(returnedDocument));

        insertedDocument = returnedDocument;
    }

    @Test
    @Transactional
    void createDocumentWithExistingId() throws Exception {
        // Create the Document with an existing ID
        document.setId(1L);
        DocumentDTO documentDTO = documentMapper.toDto(document);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNomIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        document.setNom(null);

        // Create the Document, which fails.
        DocumentDTO documentDTO = documentMapper.toDto(document);

        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUrlIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        document.setUrl(null);

        // Create the Document, which fails.
        DocumentDTO documentDTO = documentMapper.toDto(document);

        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatutIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        document.setStatut(null);

        // Create the Document, which fails.
        DocumentDTO documentDTO = documentMapper.toDto(document);

        restDocumentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDocuments() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(document.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())))
            .andExpect(jsonPath("$.[*].motifRefus").value(hasItem(DEFAULT_MOTIF_REFUS)))
            .andExpect(jsonPath("$.[*].dateAjout").value(hasItem(DEFAULT_DATE_AJOUT.toString())))
            .andExpect(jsonPath("$.[*].dateTraitement").value(hasItem(DEFAULT_DATE_TRAITEMENT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDocumentsWithEagerRelationshipsIsEnabled() throws Exception {
        when(documentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDocumentMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(documentServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDocumentsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(documentServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDocumentMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(documentRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDocument() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get the document
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL_ID, document.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(document.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL))
            .andExpect(jsonPath("$.statut").value(DEFAULT_STATUT.toString()))
            .andExpect(jsonPath("$.motifRefus").value(DEFAULT_MOTIF_REFUS))
            .andExpect(jsonPath("$.dateAjout").value(DEFAULT_DATE_AJOUT.toString()))
            .andExpect(jsonPath("$.dateTraitement").value(DEFAULT_DATE_TRAITEMENT.toString()));
    }

    @Test
    @Transactional
    void getDocumentsByIdFiltering() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        Long id = document.getId();

        defaultDocumentFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultDocumentFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultDocumentFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllDocumentsByNomIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where nom equals to
        defaultDocumentFiltering("nom.equals=" + DEFAULT_NOM, "nom.equals=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllDocumentsByNomIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where nom in
        defaultDocumentFiltering("nom.in=" + DEFAULT_NOM + "," + UPDATED_NOM, "nom.in=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllDocumentsByNomIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where nom is not null
        defaultDocumentFiltering("nom.specified=true", "nom.specified=false");
    }

    @Test
    @Transactional
    void getAllDocumentsByNomContainsSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where nom contains
        defaultDocumentFiltering("nom.contains=" + DEFAULT_NOM, "nom.contains=" + UPDATED_NOM);
    }

    @Test
    @Transactional
    void getAllDocumentsByNomNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where nom does not contain
        defaultDocumentFiltering("nom.doesNotContain=" + UPDATED_NOM, "nom.doesNotContain=" + DEFAULT_NOM);
    }

    @Test
    @Transactional
    void getAllDocumentsByUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where url equals to
        defaultDocumentFiltering("url.equals=" + DEFAULT_URL, "url.equals=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllDocumentsByUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where url in
        defaultDocumentFiltering("url.in=" + DEFAULT_URL + "," + UPDATED_URL, "url.in=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllDocumentsByUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where url is not null
        defaultDocumentFiltering("url.specified=true", "url.specified=false");
    }

    @Test
    @Transactional
    void getAllDocumentsByUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where url contains
        defaultDocumentFiltering("url.contains=" + DEFAULT_URL, "url.contains=" + UPDATED_URL);
    }

    @Test
    @Transactional
    void getAllDocumentsByUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where url does not contain
        defaultDocumentFiltering("url.doesNotContain=" + UPDATED_URL, "url.doesNotContain=" + DEFAULT_URL);
    }

    @Test
    @Transactional
    void getAllDocumentsByStatutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where statut equals to
        defaultDocumentFiltering("statut.equals=" + DEFAULT_STATUT, "statut.equals=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllDocumentsByStatutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where statut in
        defaultDocumentFiltering("statut.in=" + DEFAULT_STATUT + "," + UPDATED_STATUT, "statut.in=" + UPDATED_STATUT);
    }

    @Test
    @Transactional
    void getAllDocumentsByStatutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where statut is not null
        defaultDocumentFiltering("statut.specified=true", "statut.specified=false");
    }

    @Test
    @Transactional
    void getAllDocumentsByMotifRefusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where motifRefus equals to
        defaultDocumentFiltering("motifRefus.equals=" + DEFAULT_MOTIF_REFUS, "motifRefus.equals=" + UPDATED_MOTIF_REFUS);
    }

    @Test
    @Transactional
    void getAllDocumentsByMotifRefusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where motifRefus in
        defaultDocumentFiltering(
            "motifRefus.in=" + DEFAULT_MOTIF_REFUS + "," + UPDATED_MOTIF_REFUS,
            "motifRefus.in=" + UPDATED_MOTIF_REFUS
        );
    }

    @Test
    @Transactional
    void getAllDocumentsByMotifRefusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where motifRefus is not null
        defaultDocumentFiltering("motifRefus.specified=true", "motifRefus.specified=false");
    }

    @Test
    @Transactional
    void getAllDocumentsByMotifRefusContainsSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where motifRefus contains
        defaultDocumentFiltering("motifRefus.contains=" + DEFAULT_MOTIF_REFUS, "motifRefus.contains=" + UPDATED_MOTIF_REFUS);
    }

    @Test
    @Transactional
    void getAllDocumentsByMotifRefusNotContainsSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where motifRefus does not contain
        defaultDocumentFiltering("motifRefus.doesNotContain=" + UPDATED_MOTIF_REFUS, "motifRefus.doesNotContain=" + DEFAULT_MOTIF_REFUS);
    }

    @Test
    @Transactional
    void getAllDocumentsByDateAjoutIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where dateAjout equals to
        defaultDocumentFiltering("dateAjout.equals=" + DEFAULT_DATE_AJOUT, "dateAjout.equals=" + UPDATED_DATE_AJOUT);
    }

    @Test
    @Transactional
    void getAllDocumentsByDateAjoutIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where dateAjout in
        defaultDocumentFiltering("dateAjout.in=" + DEFAULT_DATE_AJOUT + "," + UPDATED_DATE_AJOUT, "dateAjout.in=" + UPDATED_DATE_AJOUT);
    }

    @Test
    @Transactional
    void getAllDocumentsByDateAjoutIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where dateAjout is not null
        defaultDocumentFiltering("dateAjout.specified=true", "dateAjout.specified=false");
    }

    @Test
    @Transactional
    void getAllDocumentsByDateTraitementIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where dateTraitement equals to
        defaultDocumentFiltering("dateTraitement.equals=" + DEFAULT_DATE_TRAITEMENT, "dateTraitement.equals=" + UPDATED_DATE_TRAITEMENT);
    }

    @Test
    @Transactional
    void getAllDocumentsByDateTraitementIsInShouldWork() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where dateTraitement in
        defaultDocumentFiltering(
            "dateTraitement.in=" + DEFAULT_DATE_TRAITEMENT + "," + UPDATED_DATE_TRAITEMENT,
            "dateTraitement.in=" + UPDATED_DATE_TRAITEMENT
        );
    }

    @Test
    @Transactional
    void getAllDocumentsByDateTraitementIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        // Get all the documentList where dateTraitement is not null
        defaultDocumentFiltering("dateTraitement.specified=true", "dateTraitement.specified=false");
    }

    @Test
    @Transactional
    void getAllDocumentsByTypeDocumentIsEqualToSomething() throws Exception {
        TypeDocument typeDocument;
        if (TestUtil.findAll(em, TypeDocument.class).isEmpty()) {
            documentRepository.saveAndFlush(document);
            typeDocument = TypeDocumentResourceIT.createEntity();
        } else {
            typeDocument = TestUtil.findAll(em, TypeDocument.class).get(0);
        }
        em.persist(typeDocument);
        em.flush();
        document.setTypeDocument(typeDocument);
        documentRepository.saveAndFlush(document);
        Long typeDocumentId = typeDocument.getId();
        // Get all the documentList where typeDocument equals to typeDocumentId
        defaultDocumentShouldBeFound("typeDocumentId.equals=" + typeDocumentId);

        // Get all the documentList where typeDocument equals to (typeDocumentId + 1)
        defaultDocumentShouldNotBeFound("typeDocumentId.equals=" + (typeDocumentId + 1));
    }

    @Test
    @Transactional
    void getAllDocumentsByDemarcheurIsEqualToSomething() throws Exception {
        User demarcheur;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            documentRepository.saveAndFlush(document);
            demarcheur = UserResourceIT.createEntity();
        } else {
            demarcheur = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(demarcheur);
        em.flush();
        document.setDemarcheur(demarcheur);
        documentRepository.saveAndFlush(document);
        Long demarcheurId = demarcheur.getId();
        // Get all the documentList where demarcheur equals to demarcheurId
        defaultDocumentShouldBeFound("demarcheurId.equals=" + demarcheurId);

        // Get all the documentList where demarcheur equals to (demarcheurId + 1)
        defaultDocumentShouldNotBeFound("demarcheurId.equals=" + (demarcheurId + 1));
    }

    @Test
    @Transactional
    void getAllDocumentsByTraiteParIsEqualToSomething() throws Exception {
        User traitePar;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            documentRepository.saveAndFlush(document);
            traitePar = UserResourceIT.createEntity();
        } else {
            traitePar = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(traitePar);
        em.flush();
        document.setTraitePar(traitePar);
        documentRepository.saveAndFlush(document);
        Long traiteParId = traitePar.getId();
        // Get all the documentList where traitePar equals to traiteParId
        defaultDocumentShouldBeFound("traiteParId.equals=" + traiteParId);

        // Get all the documentList where traitePar equals to (traiteParId + 1)
        defaultDocumentShouldNotBeFound("traiteParId.equals=" + (traiteParId + 1));
    }

    private void defaultDocumentFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultDocumentShouldBeFound(shouldBeFound);
        defaultDocumentShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDocumentShouldBeFound(String filter) throws Exception {
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(document.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].statut").value(hasItem(DEFAULT_STATUT.toString())))
            .andExpect(jsonPath("$.[*].motifRefus").value(hasItem(DEFAULT_MOTIF_REFUS)))
            .andExpect(jsonPath("$.[*].dateAjout").value(hasItem(DEFAULT_DATE_AJOUT.toString())))
            .andExpect(jsonPath("$.[*].dateTraitement").value(hasItem(DEFAULT_DATE_TRAITEMENT.toString())));

        // Check, that the count call also returns 1
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDocumentShouldNotBeFound(String filter) throws Exception {
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDocumentMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDocument() throws Exception {
        // Get the document
        restDocumentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDocument() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the document
        Document updatedDocument = documentRepository.findById(document.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDocument are not directly saved in db
        em.detach(updatedDocument);
        updatedDocument
            .nom(UPDATED_NOM)
            .url(UPDATED_URL)
            .statut(UPDATED_STATUT)
            .motifRefus(UPDATED_MOTIF_REFUS)
            .dateAjout(UPDATED_DATE_AJOUT)
            .dateTraitement(UPDATED_DATE_TRAITEMENT);
        DocumentDTO documentDTO = documentMapper.toDto(updatedDocument);

        restDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, documentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentDTO))
            )
            .andExpect(status().isOk());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDocumentToMatchAllProperties(updatedDocument);
    }

    @Test
    @Transactional
    void putNonExistingDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // Create the Document
        DocumentDTO documentDTO = documentMapper.toDto(document);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, documentDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // Create the Document
        DocumentDTO documentDTO = documentMapper.toDto(document);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // Create the Document
        DocumentDTO documentDTO = documentMapper.toDto(document);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDocumentWithPatch() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the document using partial update
        Document partialUpdatedDocument = new Document();
        partialUpdatedDocument.setId(document.getId());

        partialUpdatedDocument.nom(UPDATED_NOM).dateAjout(UPDATED_DATE_AJOUT);

        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocument))
            )
            .andExpect(status().isOk());

        // Validate the Document in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedDocument, document), getPersistedDocument(document));
    }

    @Test
    @Transactional
    void fullUpdateDocumentWithPatch() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the document using partial update
        Document partialUpdatedDocument = new Document();
        partialUpdatedDocument.setId(document.getId());

        partialUpdatedDocument
            .nom(UPDATED_NOM)
            .url(UPDATED_URL)
            .statut(UPDATED_STATUT)
            .motifRefus(UPDATED_MOTIF_REFUS)
            .dateAjout(UPDATED_DATE_AJOUT)
            .dateTraitement(UPDATED_DATE_TRAITEMENT);

        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocument.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocument))
            )
            .andExpect(status().isOk());

        // Validate the Document in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentUpdatableFieldsEquals(partialUpdatedDocument, getPersistedDocument(partialUpdatedDocument));
    }

    @Test
    @Transactional
    void patchNonExistingDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // Create the Document
        DocumentDTO documentDTO = documentMapper.toDto(document);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, documentDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(documentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // Create the Document
        DocumentDTO documentDTO = documentMapper.toDto(document);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(documentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDocument() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        document.setId(longCount.incrementAndGet());

        // Create the Document
        DocumentDTO documentDTO = documentMapper.toDto(document);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(documentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Document in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDocument() throws Exception {
        // Initialize the database
        insertedDocument = documentRepository.saveAndFlush(document);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the document
        restDocumentMockMvc
            .perform(delete(ENTITY_API_URL_ID, document.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return documentRepository.count();
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

    protected Document getPersistedDocument(Document document) {
        return documentRepository.findById(document.getId()).orElseThrow();
    }

    protected void assertPersistedDocumentToMatchAllProperties(Document expectedDocument) {
        assertDocumentAllPropertiesEquals(expectedDocument, getPersistedDocument(expectedDocument));
    }

    protected void assertPersistedDocumentToMatchUpdatableProperties(Document expectedDocument) {
        assertDocumentAllUpdatablePropertiesEquals(expectedDocument, getPersistedDocument(expectedDocument));
    }
}
