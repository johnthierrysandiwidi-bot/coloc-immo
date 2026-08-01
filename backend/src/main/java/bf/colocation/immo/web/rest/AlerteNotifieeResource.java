package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.AlerteNotifieeRepository;
import bf.colocation.immo.service.AlerteNotifieeService;
import bf.colocation.immo.service.dto.AlerteNotifieeDTO;
import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.service.security.AutorisationService;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link bf.colocation.immo.domain.AlerteNotifiee}.
 */
@RestController
@RequestMapping("/api/alerte-notifiees")
public class AlerteNotifieeResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlerteNotifieeResource.class);

    private static final String ENTITY_NAME = "alerteNotifiee";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final AlerteNotifieeService alerteNotifieeService;

    private final AlerteNotifieeRepository alerteNotifieeRepository;

    private final AutorisationService autorisationService;

    public AlerteNotifieeResource(AlerteNotifieeService alerteNotifieeService, AlerteNotifieeRepository alerteNotifieeRepository, AutorisationService autorisationService) {
        this.alerteNotifieeService = alerteNotifieeService;
        this.alerteNotifieeRepository = alerteNotifieeRepository;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /alerte-notifiees} : Create a new alerteNotifiee.
     *
     * @param alerteNotifieeDTO the alerteNotifieeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alerteNotifieeDTO, or with status {@code 400 (Bad Request)} if the alerteNotifiee has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /** Accès réservé au titulaire de l'enregistrement (ou à l'administrateur). */
    private void verifierAcces(Long id) {
        autorisationService.exigerProprietaireOuAdmin(alerteNotifieeRepository.trouverTitulaireId(id).orElse(null));
    }

    @PostMapping("")
    public ResponseEntity<AlerteNotifieeDTO> createAlerteNotifiee(@Valid @RequestBody AlerteNotifieeDTO alerteNotifieeDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save AlerteNotifiee : {}", alerteNotifieeDTO);
        if (alerteNotifieeDTO.getId() != null) {
            throw new BadRequestAlertException("A new alerteNotifiee cannot already have an ID", ENTITY_NAME, "idexists");
        }
        alerteNotifieeDTO = alerteNotifieeService.save(alerteNotifieeDTO);
        return ResponseEntity.created(new URI("/api/alerte-notifiees/" + alerteNotifieeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, alerteNotifieeDTO.getId().toString()))
            .body(alerteNotifieeDTO);
    }

    /**
     * {@code PUT  /alerte-notifiees/:id} : Updates an existing alerteNotifiee.
     *
     * @param id the id of the alerteNotifieeDTO to save.
     * @param alerteNotifieeDTO the alerteNotifieeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alerteNotifieeDTO,
     * or with status {@code 400 (Bad Request)} if the alerteNotifieeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alerteNotifieeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlerteNotifieeDTO> updateAlerteNotifiee(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AlerteNotifieeDTO alerteNotifieeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AlerteNotifiee : {}, {}", id, alerteNotifieeDTO);
        if (alerteNotifieeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alerteNotifieeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alerteNotifieeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierAcces(id);

        alerteNotifieeDTO = alerteNotifieeService.update(alerteNotifieeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alerteNotifieeDTO.getId().toString()))
            .body(alerteNotifieeDTO);
    }

    /**
     * {@code PATCH  /alerte-notifiees/:id} : Partial updates given fields of an existing alerteNotifiee, field will ignore if it is null
     *
     * @param id the id of the alerteNotifieeDTO to save.
     * @param alerteNotifieeDTO the alerteNotifieeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alerteNotifieeDTO,
     * or with status {@code 400 (Bad Request)} if the alerteNotifieeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the alerteNotifieeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the alerteNotifieeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlerteNotifieeDTO> partialUpdateAlerteNotifiee(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AlerteNotifieeDTO alerteNotifieeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AlerteNotifiee partially : {}, {}", id, alerteNotifieeDTO);
        if (alerteNotifieeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alerteNotifieeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alerteNotifieeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierAcces(id);

        Optional<AlerteNotifieeDTO> result = alerteNotifieeService.partialUpdate(alerteNotifieeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alerteNotifieeDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /alerte-notifiees} : get all the Alerte Notifiees.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Alerte Notifiees in body.
     */
    // La liste complète expose les enregistrements de tous les utilisateurs.
    // Aucun client ne la consomme : on la réserve à l'administration.
    @GetMapping("")
    @Secured(AuthoritiesConstants.ADMIN)
    public List<AlerteNotifieeDTO> getAllAlerteNotifiees(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all AlerteNotifiees");
        return alerteNotifieeService.findAll();
    }

    /**
     * {@code GET  /alerte-notifiees/:id} : get the "id" alerteNotifiee.
     *
     * @param id the id of the alerteNotifieeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alerteNotifieeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlerteNotifieeDTO> getAlerteNotifiee(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AlerteNotifiee : {}", id);
        Optional<AlerteNotifieeDTO> alerteNotifieeDTO = alerteNotifieeService.findOne(id);
        alerteNotifieeDTO.ifPresent(d -> verifierAcces(id));
        return ResponseUtil.wrapOrNotFound(alerteNotifieeDTO);
    }

    /**
     * {@code DELETE  /alerte-notifiees/:id} : delete the "id" alerteNotifiee.
     *
     * @param id the id of the alerteNotifieeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlerteNotifiee(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AlerteNotifiee : {}", id);
        verifierAcces(id);
        alerteNotifieeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
