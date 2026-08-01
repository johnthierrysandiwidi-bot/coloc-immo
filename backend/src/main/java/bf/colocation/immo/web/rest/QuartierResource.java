package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.QuartierRepository;
import bf.colocation.immo.service.QuartierService;
import bf.colocation.immo.service.dto.QuartierDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Quartier}.
 */
@RestController
@RequestMapping("/api/quartiers")
public class QuartierResource {

    private static final Logger LOG = LoggerFactory.getLogger(QuartierResource.class);

    private static final String ENTITY_NAME = "quartier";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final QuartierService quartierService;

    private final QuartierRepository quartierRepository;

    public QuartierResource(QuartierService quartierService, QuartierRepository quartierRepository) {
        this.quartierService = quartierService;
        this.quartierRepository = quartierRepository;
    }

    /**
     * {@code POST  /quartiers} : Create a new quartier.
     *
     * @param quartierDTO the quartierDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new quartierDTO, or with status {@code 400 (Bad Request)} if the quartier has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<QuartierDTO> createQuartier(@Valid @RequestBody QuartierDTO quartierDTO) throws URISyntaxException {
        LOG.debug("REST request to save Quartier : {}", quartierDTO);
        if (quartierDTO.getId() != null) {
            throw new BadRequestAlertException("A new quartier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        quartierDTO = quartierService.save(quartierDTO);
        return ResponseEntity.created(new URI("/api/quartiers/" + quartierDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, quartierDTO.getId().toString()))
            .body(quartierDTO);
    }

    /**
     * {@code PUT  /quartiers/:id} : Updates an existing quartier.
     *
     * @param id the id of the quartierDTO to save.
     * @param quartierDTO the quartierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated quartierDTO,
     * or with status {@code 400 (Bad Request)} if the quartierDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the quartierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuartierDTO> updateQuartier(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody QuartierDTO quartierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Quartier : {}, {}", id, quartierDTO);
        if (quartierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, quartierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!quartierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        quartierDTO = quartierService.update(quartierDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, quartierDTO.getId().toString()))
            .body(quartierDTO);
    }

    /**
     * {@code PATCH  /quartiers/:id} : Partial updates given fields of an existing quartier, field will ignore if it is null
     *
     * @param id the id of the quartierDTO to save.
     * @param quartierDTO the quartierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated quartierDTO,
     * or with status {@code 400 (Bad Request)} if the quartierDTO is not valid,
     * or with status {@code 404 (Not Found)} if the quartierDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the quartierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<QuartierDTO> partialUpdateQuartier(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody QuartierDTO quartierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Quartier partially : {}, {}", id, quartierDTO);
        if (quartierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, quartierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!quartierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<QuartierDTO> result = quartierService.partialUpdate(quartierDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, quartierDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /quartiers} : get all the Quartiers.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Quartiers in body.
     */
    @GetMapping("")
    public List<QuartierDTO> getAllQuartiers(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Quartiers");
        return quartierService.findAll();
    }

    /**
     * {@code GET  /quartiers/:id} : get the "id" quartier.
     *
     * @param id the id of the quartierDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the quartierDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuartierDTO> getQuartier(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Quartier : {}", id);
        Optional<QuartierDTO> quartierDTO = quartierService.findOne(id);
        return ResponseUtil.wrapOrNotFound(quartierDTO);
    }

    /**
     * {@code DELETE  /quartiers/:id} : delete the "id" quartier.
     *
     * @param id the id of the quartierDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuartier(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Quartier : {}", id);
        quartierService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
