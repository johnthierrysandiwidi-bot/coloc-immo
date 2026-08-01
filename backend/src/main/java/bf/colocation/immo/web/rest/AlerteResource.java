package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.AlerteRepository;
import bf.colocation.immo.service.AlerteQueryService;
import bf.colocation.immo.service.AlerteService;
import bf.colocation.immo.service.criteria.AlerteCriteria;
import bf.colocation.immo.service.dto.AlerteDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Alerte}.
 */
@RestController
@RequestMapping("/api/alertes")
public class AlerteResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlerteResource.class);

    private static final String ENTITY_NAME = "alerte";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final AlerteService alerteService;

    private final AlerteRepository alerteRepository;

    private final AlerteQueryService alerteQueryService;

    private final AutorisationService autorisationService;

    public AlerteResource(AlerteService alerteService, AlerteRepository alerteRepository, AlerteQueryService alerteQueryService,
        AutorisationService autorisationService) {
        this.alerteService = alerteService;
        this.alerteRepository = alerteRepository;
        this.alerteQueryService = alerteQueryService;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /alertes} : Create a new alerte.
     *
     * @param alerteDTO the alerteDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alerteDTO, or with status {@code 400 (Bad Request)} if the alerte has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AlerteDTO> createAlerte(@Valid @RequestBody AlerteDTO alerteDTO) throws URISyntaxException {
        LOG.debug("REST request to save Alerte : {}", alerteDTO);
        if (alerteDTO.getId() != null) {
            throw new BadRequestAlertException("A new alerte cannot already have an ID", ENTITY_NAME, "idexists");
        }
        alerteDTO = alerteService.save(alerteDTO);
        return ResponseEntity.created(new URI("/api/alertes/" + alerteDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, alerteDTO.getId().toString()))
            .body(alerteDTO);
    }

    /**
     * {@code PUT  /alertes/:id} : Updates an existing alerte.
     *
     * @param id the id of the alerteDTO to save.
     * @param alerteDTO the alerteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alerteDTO,
     * or with status {@code 400 (Bad Request)} if the alerteDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alerteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlerteDTO> updateAlerte(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AlerteDTO alerteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Alerte : {}, {}", id, alerteDTO);
        if (alerteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alerteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alerteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne peut modifier que ses propres enregistrements.
        alerteService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getTitulaire() != null ? d.getTitulaire().getId() : null));

        alerteDTO = alerteService.update(alerteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alerteDTO.getId().toString()))
            .body(alerteDTO);
    }

    /**
     * {@code PATCH  /alertes/:id} : Partial updates given fields of an existing alerte, field will ignore if it is null
     *
     * @param id the id of the alerteDTO to save.
     * @param alerteDTO the alerteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alerteDTO,
     * or with status {@code 400 (Bad Request)} if the alerteDTO is not valid,
     * or with status {@code 404 (Not Found)} if the alerteDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the alerteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlerteDTO> partialUpdateAlerte(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AlerteDTO alerteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Alerte partially : {}, {}", id, alerteDTO);
        if (alerteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alerteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alerteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne peut modifier que ses propres enregistrements.
        alerteService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getTitulaire() != null ? d.getTitulaire().getId() : null));

        Optional<AlerteDTO> result = alerteService.partialUpdate(alerteDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, alerteDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /alertes} : get all the Alertes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Alertes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AlerteDTO>> getAllAlertes(
        AlerteCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Alertes by criteria: {}", criteria);
        // Sécurité (anti-IDOR) : un utilisateur non-admin ne voit QUE ses propres enregistrements.
        // Le filtre propriétaire est imposé côté serveur et écrase tout paramètre fourni par le client.
        if (!autorisationService.estAdmin()) {
            LongFilter proprietaire = new LongFilter();
            proprietaire.setEquals(autorisationService.idPourFiltrage());
            criteria.setTitulaireId(proprietaire);
        }

        Page<AlerteDTO> page = alerteQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /alertes/count} : count all the alertes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAlertes(AlerteCriteria criteria) {
        LOG.debug("REST request to count Alertes by criteria: {}", criteria);
        return ResponseEntity.ok().body(alerteQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /alertes/:id} : get the "id" alerte.
     *
     * @param id the id of the alerteDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alerteDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlerteDTO> getAlerte(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Alerte : {}", id);
        Optional<AlerteDTO> alerteDTO = alerteService.findOne(id);
        alerteDTO.ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getTitulaire() != null ? d.getTitulaire().getId() : null));
        return ResponseUtil.wrapOrNotFound(alerteDTO);
    }

    /**
     * {@code DELETE  /alertes/:id} : delete the "id" alerte.
     *
     * @param id the id of the alerteDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlerte(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Alerte : {}", id);
        alerteService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getTitulaire() != null ? d.getTitulaire().getId() : null));
        alerteService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
