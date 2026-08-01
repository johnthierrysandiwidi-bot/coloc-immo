package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.EquipementRepository;
import bf.colocation.immo.service.EquipementService;
import bf.colocation.immo.service.dto.EquipementDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Equipement}.
 */
@RestController
@RequestMapping("/api/equipements")
public class EquipementResource {

    private static final Logger LOG = LoggerFactory.getLogger(EquipementResource.class);

    private static final String ENTITY_NAME = "equipement";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final EquipementService equipementService;

    private final EquipementRepository equipementRepository;

    public EquipementResource(EquipementService equipementService, EquipementRepository equipementRepository) {
        this.equipementService = equipementService;
        this.equipementRepository = equipementRepository;
    }

    /**
     * {@code POST  /equipements} : Create a new equipement.
     *
     * @param equipementDTO the equipementDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new equipementDTO, or with status {@code 400 (Bad Request)} if the equipement has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EquipementDTO> createEquipement(@Valid @RequestBody EquipementDTO equipementDTO) throws URISyntaxException {
        LOG.debug("REST request to save Equipement : {}", equipementDTO);
        if (equipementDTO.getId() != null) {
            throw new BadRequestAlertException("A new equipement cannot already have an ID", ENTITY_NAME, "idexists");
        }
        equipementDTO = equipementService.save(equipementDTO);
        return ResponseEntity.created(new URI("/api/equipements/" + equipementDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, equipementDTO.getId().toString()))
            .body(equipementDTO);
    }

    /**
     * {@code PUT  /equipements/:id} : Updates an existing equipement.
     *
     * @param id the id of the equipementDTO to save.
     * @param equipementDTO the equipementDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated equipementDTO,
     * or with status {@code 400 (Bad Request)} if the equipementDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the equipementDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipementDTO> updateEquipement(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EquipementDTO equipementDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Equipement : {}, {}", id, equipementDTO);
        if (equipementDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, equipementDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!equipementRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        equipementDTO = equipementService.update(equipementDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, equipementDTO.getId().toString()))
            .body(equipementDTO);
    }

    /**
     * {@code PATCH  /equipements/:id} : Partial updates given fields of an existing equipement, field will ignore if it is null
     *
     * @param id the id of the equipementDTO to save.
     * @param equipementDTO the equipementDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated equipementDTO,
     * or with status {@code 400 (Bad Request)} if the equipementDTO is not valid,
     * or with status {@code 404 (Not Found)} if the equipementDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the equipementDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EquipementDTO> partialUpdateEquipement(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EquipementDTO equipementDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Equipement partially : {}, {}", id, equipementDTO);
        if (equipementDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, equipementDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!equipementRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EquipementDTO> result = equipementService.partialUpdate(equipementDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, equipementDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /equipements} : get all the Equipements.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Equipements in body.
     */
    @GetMapping("")
    public List<EquipementDTO> getAllEquipements() {
        LOG.debug("REST request to get all Equipements");
        return equipementService.findAll();
    }

    /**
     * {@code GET  /equipements/:id} : get the "id" equipement.
     *
     * @param id the id of the equipementDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the equipementDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipementDTO> getEquipement(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Equipement : {}", id);
        Optional<EquipementDTO> equipementDTO = equipementService.findOne(id);
        return ResponseUtil.wrapOrNotFound(equipementDTO);
    }

    /**
     * {@code DELETE  /equipements/:id} : delete the "id" equipement.
     *
     * @param id the id of the equipementDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipement(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Equipement : {}", id);
        equipementService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
