package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.TypeDocumentRepository;
import bf.colocation.immo.service.TypeDocumentService;
import bf.colocation.immo.service.dto.TypeDocumentDTO;
import bf.colocation.immo.web.rest.errors.BadRequestAlertException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link bf.colocation.immo.domain.TypeDocument}.
 */
@RestController
@RequestMapping("/api/type-documents")
public class TypeDocumentResource {

    private static final Logger LOG = LoggerFactory.getLogger(TypeDocumentResource.class);

    private static final String ENTITY_NAME = "typeDocument";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final TypeDocumentService typeDocumentService;

    private final TypeDocumentRepository typeDocumentRepository;

    public TypeDocumentResource(TypeDocumentService typeDocumentService, TypeDocumentRepository typeDocumentRepository) {
        this.typeDocumentService = typeDocumentService;
        this.typeDocumentRepository = typeDocumentRepository;
    }

    /**
     * {@code POST  /type-documents} : Create a new typeDocument.
     *
     * @param typeDocumentDTO the typeDocumentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new typeDocumentDTO, or with status {@code 400 (Bad Request)} if the typeDocument has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TypeDocumentDTO> createTypeDocument(@Valid @RequestBody TypeDocumentDTO typeDocumentDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TypeDocument : {}", typeDocumentDTO);
        if (typeDocumentDTO.getId() != null) {
            throw new BadRequestAlertException("A new typeDocument cannot already have an ID", ENTITY_NAME, "idexists");
        }
        typeDocumentDTO = typeDocumentService.save(typeDocumentDTO);
        return ResponseEntity.created(new URI("/api/type-documents/" + typeDocumentDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, typeDocumentDTO.getId().toString()))
            .body(typeDocumentDTO);
    }

    /**
     * {@code PUT  /type-documents/:id} : Updates an existing typeDocument.
     *
     * @param id the id of the typeDocumentDTO to save.
     * @param typeDocumentDTO the typeDocumentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated typeDocumentDTO,
     * or with status {@code 400 (Bad Request)} if the typeDocumentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the typeDocumentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TypeDocumentDTO> updateTypeDocument(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TypeDocumentDTO typeDocumentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TypeDocument : {}, {}", id, typeDocumentDTO);
        if (typeDocumentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, typeDocumentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!typeDocumentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        typeDocumentDTO = typeDocumentService.update(typeDocumentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, typeDocumentDTO.getId().toString()))
            .body(typeDocumentDTO);
    }

    /**
     * {@code PATCH  /type-documents/:id} : Partial updates given fields of an existing typeDocument, field will ignore if it is null
     *
     * @param id the id of the typeDocumentDTO to save.
     * @param typeDocumentDTO the typeDocumentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated typeDocumentDTO,
     * or with status {@code 400 (Bad Request)} if the typeDocumentDTO is not valid,
     * or with status {@code 404 (Not Found)} if the typeDocumentDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the typeDocumentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TypeDocumentDTO> partialUpdateTypeDocument(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TypeDocumentDTO typeDocumentDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TypeDocument partially : {}, {}", id, typeDocumentDTO);
        if (typeDocumentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, typeDocumentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!typeDocumentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TypeDocumentDTO> result = typeDocumentService.partialUpdate(typeDocumentDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, typeDocumentDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /type-documents} : get all the Type Documents.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Type Documents in body.
     */
    @GetMapping("")
    public List<TypeDocumentDTO> getAllTypeDocuments() {
        LOG.debug("REST request to get all TypeDocuments");
        return typeDocumentService.findAll();
    }

    /**
     * {@code GET  /type-documents/:id} : get the "id" typeDocument.
     *
     * @param id the id of the typeDocumentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the typeDocumentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TypeDocumentDTO> getTypeDocument(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TypeDocument : {}", id);
        Optional<TypeDocumentDTO> typeDocumentDTO = typeDocumentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(typeDocumentDTO);
    }

    /**
     * {@code DELETE  /type-documents/:id} : delete the "id" typeDocument.
     *
     * @param id the id of the typeDocumentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTypeDocument(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TypeDocument : {}", id);
        typeDocumentService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
