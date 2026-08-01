package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.LocaliteRepository;
import bf.colocation.immo.service.LocaliteService;
import bf.colocation.immo.service.dto.LocaliteDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Localite}.
 */
@RestController
@RequestMapping("/api/localites")
public class LocaliteResource {

    private static final Logger LOG = LoggerFactory.getLogger(LocaliteResource.class);

    private static final String ENTITY_NAME = "localite";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final LocaliteService localiteService;

    private final LocaliteRepository localiteRepository;

    public LocaliteResource(LocaliteService localiteService, LocaliteRepository localiteRepository) {
        this.localiteService = localiteService;
        this.localiteRepository = localiteRepository;
    }

    /**
     * {@code POST  /localites} : Create a new localite.
     *
     * @param localiteDTO the localiteDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new localiteDTO, or with status {@code 400 (Bad Request)} if the localite has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<LocaliteDTO> createLocalite(@Valid @RequestBody LocaliteDTO localiteDTO) throws URISyntaxException {
        LOG.debug("REST request to save Localite : {}", localiteDTO);
        if (localiteDTO.getId() != null) {
            throw new BadRequestAlertException("A new localite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        localiteDTO = localiteService.save(localiteDTO);
        return ResponseEntity.created(new URI("/api/localites/" + localiteDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, localiteDTO.getId().toString()))
            .body(localiteDTO);
    }

    /**
     * {@code PUT  /localites/:id} : Updates an existing localite.
     *
     * @param id the id of the localiteDTO to save.
     * @param localiteDTO the localiteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated localiteDTO,
     * or with status {@code 400 (Bad Request)} if the localiteDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the localiteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LocaliteDTO> updateLocalite(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody LocaliteDTO localiteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Localite : {}, {}", id, localiteDTO);
        if (localiteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, localiteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!localiteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        localiteDTO = localiteService.update(localiteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, localiteDTO.getId().toString()))
            .body(localiteDTO);
    }

    /**
     * {@code PATCH  /localites/:id} : Partial updates given fields of an existing localite, field will ignore if it is null
     *
     * @param id the id of the localiteDTO to save.
     * @param localiteDTO the localiteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated localiteDTO,
     * or with status {@code 400 (Bad Request)} if the localiteDTO is not valid,
     * or with status {@code 404 (Not Found)} if the localiteDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the localiteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<LocaliteDTO> partialUpdateLocalite(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody LocaliteDTO localiteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Localite partially : {}, {}", id, localiteDTO);
        if (localiteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, localiteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!localiteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<LocaliteDTO> result = localiteService.partialUpdate(localiteDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, localiteDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /localites} : get all the Localites.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Localites in body.
     */
    @GetMapping("")
    public List<LocaliteDTO> getAllLocalites() {
        LOG.debug("REST request to get all Localites");
        return localiteService.findAll();
    }

    /**
     * {@code GET  /localites/:id} : get the "id" localite.
     *
     * @param id the id of the localiteDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the localiteDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocaliteDTO> getLocalite(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Localite : {}", id);
        Optional<LocaliteDTO> localiteDTO = localiteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(localiteDTO);
    }

    /**
     * {@code DELETE  /localites/:id} : delete the "id" localite.
     *
     * @param id the id of the localiteDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocalite(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Localite : {}", id);
        localiteService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
