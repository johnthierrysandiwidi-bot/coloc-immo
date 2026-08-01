package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.DocumentRepository;
import bf.colocation.immo.service.DocumentQueryService;
import bf.colocation.immo.service.DocumentService;
import bf.colocation.immo.service.criteria.DocumentCriteria;
import bf.colocation.immo.service.dto.DocumentDTO;
import bf.colocation.immo.service.security.AutorisationService;
import bf.colocation.immo.web.rest.errors.BadRequestAlertException;
import tech.jhipster.service.filter.LongFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link bf.colocation.immo.domain.Document}.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentResource {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentResource.class);

    private static final String ENTITY_NAME = "document";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final DocumentService documentService;

    private final DocumentRepository documentRepository;

    private final DocumentQueryService documentQueryService;

    private final AutorisationService autorisationService;

    public DocumentResource(
        DocumentService documentService,
        DocumentRepository documentRepository,
        DocumentQueryService documentQueryService,
        AutorisationService autorisationService
    ) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.documentQueryService = documentQueryService;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /documents} : Create a new document.
     *
     * @param documentDTO the documentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new documentDTO, or with status {@code 400 (Bad Request)} if the document has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DocumentDTO> createDocument(@Valid @RequestBody DocumentDTO documentDTO) throws URISyntaxException {
        LOG.debug("REST request to save Document : {}", documentDTO);
        if (documentDTO.getId() != null) {
            throw new BadRequestAlertException("A new document cannot already have an ID", ENTITY_NAME, "idexists");
        }
        documentDTO = documentService.save(documentDTO);
        return ResponseEntity.created(new URI("/api/documents/" + documentDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, documentDTO.getId().toString()))
            .body(documentDTO);
    }

    /**
     * {@code PUT  /documents/:id} : Updates an existing document.
     *
     * @param id the id of the documentDTO to save.
     * @param documentDTO the documentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentDTO,
     * or with status {@code 400 (Bad Request)} if the documentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the documentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DocumentDTO documentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Document : {}, {}", id, documentDTO);
        if (documentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne peut modifier que ses propres enregistrements.
        documentService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getDemarcheur() != null ? d.getDemarcheur().getId() : null));

        documentDTO = documentService.update(documentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, documentDTO.getId().toString()))
            .body(documentDTO);
    }

    /**
     * {@code PATCH  /documents/:id} : Partial updates given fields of an existing document, field will ignore if it is null
     *
     * @param id the id of the documentDTO to save.
     * @param documentDTO the documentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentDTO,
     * or with status {@code 400 (Bad Request)} if the documentDTO is not valid,
     * or with status {@code 404 (Not Found)} if the documentDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the documentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DocumentDTO> partialUpdateDocument(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DocumentDTO documentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Document partially : {}, {}", id, documentDTO);
        if (documentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne peut modifier que ses propres enregistrements.
        documentService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getDemarcheur() != null ? d.getDemarcheur().getId() : null));

        Optional<DocumentDTO> result = documentService.partialUpdate(documentDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, documentDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /documents} : get all the Documents.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Documents in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DocumentDTO>> getAllDocuments(
        DocumentCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Documents by criteria: {}", criteria);
        // Sécurité (anti-IDOR) : un utilisateur non-admin ne voit QUE ses propres enregistrements.
        // Le filtre propriétaire est imposé côté serveur et écrase tout paramètre fourni par le client.
        if (!autorisationService.estAdmin()) {
            LongFilter proprietaire = new LongFilter();
            proprietaire.setEquals(autorisationService.idPourFiltrage());
            criteria.setDemarcheurId(proprietaire);
        }

        Page<DocumentDTO> page = documentQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /documents/count} : count all the documents.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countDocuments(DocumentCriteria criteria) {
        LOG.debug("REST request to count Documents by criteria: {}", criteria);
        return ResponseEntity.ok().body(documentQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /documents/:id} : get the "id" document.
     *
     * @param id the id of the documentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the documentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Document : {}", id);
        Optional<DocumentDTO> documentDTO = documentService.findOne(id);
        documentDTO.ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getDemarcheur() != null ? d.getDemarcheur().getId() : null));
        return ResponseUtil.wrapOrNotFound(documentDTO);
    }

    /**
     * {@code DELETE  /documents/:id} : delete the "id" document.
     *
     * @param id the id of the documentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Document : {}", id);
        documentService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getDemarcheur() != null ? d.getDemarcheur().getId() : null));
        documentService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
