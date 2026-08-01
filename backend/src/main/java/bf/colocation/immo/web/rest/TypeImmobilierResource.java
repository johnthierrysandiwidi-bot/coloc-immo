package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.TypeImmobilierRepository;
import bf.colocation.immo.service.TypeImmobilierService;
import bf.colocation.immo.service.dto.TypeImmobilierDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.TypeImmobilier}.
 */
@RestController
@RequestMapping("/api/type-immobiliers")
public class TypeImmobilierResource {

    private static final Logger LOG = LoggerFactory.getLogger(TypeImmobilierResource.class);

    private static final String ENTITY_NAME = "typeImmobilier";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final TypeImmobilierService typeImmobilierService;

    private final TypeImmobilierRepository typeImmobilierRepository;

    public TypeImmobilierResource(TypeImmobilierService typeImmobilierService, TypeImmobilierRepository typeImmobilierRepository) {
        this.typeImmobilierService = typeImmobilierService;
        this.typeImmobilierRepository = typeImmobilierRepository;
    }

    /**
     * {@code POST  /type-immobiliers} : Create a new typeImmobilier.
     *
     * @param typeImmobilierDTO the typeImmobilierDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new typeImmobilierDTO, or with status {@code 400 (Bad Request)} if the typeImmobilier has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TypeImmobilierDTO> createTypeImmobilier(@Valid @RequestBody TypeImmobilierDTO typeImmobilierDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TypeImmobilier : {}", typeImmobilierDTO);
        if (typeImmobilierDTO.getId() != null) {
            throw new BadRequestAlertException("A new typeImmobilier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        typeImmobilierDTO = typeImmobilierService.save(typeImmobilierDTO);
        return ResponseEntity.created(new URI("/api/type-immobiliers/" + typeImmobilierDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, typeImmobilierDTO.getId().toString()))
            .body(typeImmobilierDTO);
    }

    /**
     * {@code PUT  /type-immobiliers/:id} : Updates an existing typeImmobilier.
     *
     * @param id the id of the typeImmobilierDTO to save.
     * @param typeImmobilierDTO the typeImmobilierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated typeImmobilierDTO,
     * or with status {@code 400 (Bad Request)} if the typeImmobilierDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the typeImmobilierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TypeImmobilierDTO> updateTypeImmobilier(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TypeImmobilierDTO typeImmobilierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TypeImmobilier : {}, {}", id, typeImmobilierDTO);
        if (typeImmobilierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, typeImmobilierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!typeImmobilierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        typeImmobilierDTO = typeImmobilierService.update(typeImmobilierDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, typeImmobilierDTO.getId().toString()))
            .body(typeImmobilierDTO);
    }

    /**
     * {@code PATCH  /type-immobiliers/:id} : Partial updates given fields of an existing typeImmobilier, field will ignore if it is null
     *
     * @param id the id of the typeImmobilierDTO to save.
     * @param typeImmobilierDTO the typeImmobilierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated typeImmobilierDTO,
     * or with status {@code 400 (Bad Request)} if the typeImmobilierDTO is not valid,
     * or with status {@code 404 (Not Found)} if the typeImmobilierDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the typeImmobilierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TypeImmobilierDTO> partialUpdateTypeImmobilier(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TypeImmobilierDTO typeImmobilierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TypeImmobilier partially : {}, {}", id, typeImmobilierDTO);
        if (typeImmobilierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, typeImmobilierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!typeImmobilierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TypeImmobilierDTO> result = typeImmobilierService.partialUpdate(typeImmobilierDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, typeImmobilierDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /type-immobiliers} : get all the Type Immobiliers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Type Immobiliers in body.
     */
    @GetMapping("")
    public List<TypeImmobilierDTO> getAllTypeImmobiliers() {
        LOG.debug("REST request to get all TypeImmobiliers");
        return typeImmobilierService.findAll();
    }

    /**
     * {@code GET  /type-immobiliers/:id} : get the "id" typeImmobilier.
     *
     * @param id the id of the typeImmobilierDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the typeImmobilierDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TypeImmobilierDTO> getTypeImmobilier(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TypeImmobilier : {}", id);
        Optional<TypeImmobilierDTO> typeImmobilierDTO = typeImmobilierService.findOne(id);
        return ResponseUtil.wrapOrNotFound(typeImmobilierDTO);
    }

    /**
     * {@code DELETE  /type-immobiliers/:id} : delete the "id" typeImmobilier.
     *
     * @param id the id of the typeImmobilierDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTypeImmobilier(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TypeImmobilier : {}", id);
        typeImmobilierService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
