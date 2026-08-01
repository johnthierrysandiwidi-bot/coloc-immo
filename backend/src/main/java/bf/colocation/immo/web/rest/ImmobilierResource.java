package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.ImmobilierRepository;
import bf.colocation.immo.service.metier.HistoriquePrixService;
import bf.colocation.immo.service.metier.MandatDemarcheurService;
import bf.colocation.immo.service.dto.PointPrixDTO;
import bf.colocation.immo.service.ImmobilierQueryService;
import bf.colocation.immo.service.ImmobilierService;
import bf.colocation.immo.service.criteria.ImmobilierCriteria;
import bf.colocation.immo.service.dto.ImmobilierDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Immobilier}.
 */
@RestController
@RequestMapping("/api/immobiliers")
public class ImmobilierResource {

    private static final Logger LOG = LoggerFactory.getLogger(ImmobilierResource.class);

    private static final String ENTITY_NAME = "immobilier";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final ImmobilierService immobilierService;

    private final ImmobilierRepository immobilierRepository;
    private final HistoriquePrixService historiquePrixService;

    private final ImmobilierQueryService immobilierQueryService;

    private final AutorisationService autorisationService;
    private final MandatDemarcheurService mandatDemarcheurService;

    public ImmobilierResource(
        ImmobilierService immobilierService,
        ImmobilierRepository immobilierRepository,
        ImmobilierQueryService immobilierQueryService,
        HistoriquePrixService historiquePrixService,
        AutorisationService autorisationService,
        MandatDemarcheurService mandatDemarcheurService
    ) {
        this.immobilierService = immobilierService;
        this.immobilierRepository = immobilierRepository;
        this.immobilierQueryService = immobilierQueryService;
        this.historiquePrixService = historiquePrixService;
        this.autorisationService = autorisationService;
        this.mandatDemarcheurService = mandatDemarcheurService;
    }

    /**
     * {@code GET  /immobiliers/:id/historique-prix} : historique de prix d'un bien.
     * Accessible publiquement (transparence des prix).
     */
    @org.springframework.web.bind.annotation.GetMapping("/{id}/historique-prix")
    public java.util.List<PointPrixDTO> historiquePrix(@PathVariable Long id) {
        return historiquePrixService.historique(id).stream().map(PointPrixDTO::de).toList();
    }

    /**
     * {@code POST  /immobiliers} : Create a new immobilier.
     *
     * @param immobilierDTO the immobilierDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new immobilierDTO, or with status {@code 400 (Bad Request)} if the immobilier has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ImmobilierDTO> createImmobilier(@Valid @RequestBody ImmobilierDTO immobilierDTO) throws URISyntaxException {
        LOG.debug("REST request to save Immobilier : {}", immobilierDTO);
        if (immobilierDTO.getId() != null) {
            throw new BadRequestAlertException("A new immobilier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        immobilierDTO = immobilierService.save(immobilierDTO);
        return ResponseEntity.created(new URI("/api/immobiliers/" + immobilierDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, immobilierDTO.getId().toString()))
            .body(immobilierDTO);
    }

    /**
     * {@code PUT  /immobiliers/:id} : Updates an existing immobilier.
     *
     * @param id the id of the immobilierDTO to save.
     * @param immobilierDTO the immobilierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated immobilierDTO,
     * or with status {@code 400 (Bad Request)} if the immobilierDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the immobilierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /**
     * La lecture des biens est volontairement publique (ils s'affichent dans le catalogue),
     * mais leur modification ne l'est pas : seul le propriétaire, le démarcheur mandaté
     * ou l'administrateur peut écrire.
     */
    private void verifierEcritureBien(Long id) {
        immobilierService
            .findOne(id)
            .ifPresent(d ->
                autorisationService.exigerUnDesProprietairesOuAdmin(
                    d.getProprietaire() != null ? d.getProprietaire().getId() : null,
                    d.getDemarcheur() != null ? d.getDemarcheur().getId() : null
                )
            );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImmobilierDTO> updateImmobilier(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ImmobilierDTO immobilierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Immobilier : {}, {}", id, immobilierDTO);
        if (immobilierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, immobilierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!immobilierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcritureBien(id);

        immobilierDTO = immobilierService.update(immobilierDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, immobilierDTO.getId().toString()))
            .body(immobilierDTO);
    }

    /**
     * {@code PATCH  /immobiliers/:id} : Partial updates given fields of an existing immobilier, field will ignore if it is null
     *
     * @param id the id of the immobilierDTO to save.
     * @param immobilierDTO the immobilierDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated immobilierDTO,
     * or with status {@code 400 (Bad Request)} if the immobilierDTO is not valid,
     * or with status {@code 404 (Not Found)} if the immobilierDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the immobilierDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ImmobilierDTO> partialUpdateImmobilier(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ImmobilierDTO immobilierDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Immobilier partially : {}, {}", id, immobilierDTO);
        if (immobilierDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, immobilierDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!immobilierRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcritureBien(id);

        Optional<ImmobilierDTO> result = immobilierService.partialUpdate(immobilierDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, immobilierDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /immobiliers} : get all the Immobiliers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Immobiliers in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ImmobilierDTO>> getAllImmobiliers(
        ImmobilierCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Immobiliers by criteria: {}", criteria);

        Page<ImmobilierDTO> page = immobilierQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /immobiliers/count} : count all the immobiliers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countImmobiliers(ImmobilierCriteria criteria) {
        LOG.debug("REST request to count Immobiliers by criteria: {}", criteria);
        return ResponseEntity.ok().body(immobilierQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /immobiliers/:id} : get the "id" immobilier.
     *
     * @param id the id of the immobilierDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the immobilierDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImmobilierDTO> getImmobilier(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Immobilier : {}", id);
        Optional<ImmobilierDTO> immobilierDTO = immobilierService.findOne(id);
        return ResponseUtil.wrapOrNotFound(immobilierDTO);
    }

    /**
     * {@code DELETE  /immobiliers/:id} : delete the "id" immobilier.
     *
     * @param id the id of the immobilierDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    /**
     * Démarcheurs vérifiés pouvant être mandatés. Sert à alimenter le sélecteur côté client.
     */
    @GetMapping("/demarcheurs-disponibles")
    public ResponseEntity<java.util.List<bf.colocation.immo.service.dto.UserDTO>> demarcheursDisponibles() {
        java.util.List<bf.colocation.immo.service.dto.UserDTO> liste = mandatDemarcheurService
            .demarcheursDisponibles()
            .stream()
            .map(u -> {
                bf.colocation.immo.service.dto.UserDTO dto = new bf.colocation.immo.service.dto.UserDTO();
                dto.setId(u.getId());
                dto.setLogin(u.getLogin());
                return dto;
            })
            .toList();
        return ResponseEntity.ok(liste);
    }

    /** Confie le bien à un démarcheur vérifié (réservé au propriétaire). */
    @PatchMapping("/{id}/demarcheur/{demarcheurId}")
    public ResponseEntity<ImmobilierDTO> mandater(@PathVariable Long id, @PathVariable Long demarcheurId) {
        mandatDemarcheurService.mandater(id, demarcheurId);
        return ResponseEntity.ok(immobilierService.findOne(id).orElseThrow());
    }

    /** Retire le mandat en cours sur le bien. */
    @DeleteMapping("/{id}/demarcheur")
    public ResponseEntity<ImmobilierDTO> retirerMandat(@PathVariable Long id) {
        mandatDemarcheurService.retirerMandat(id);
        return ResponseEntity.ok(immobilierService.findOne(id).orElseThrow());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImmobilier(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Immobilier : {}", id);
        verifierEcritureBien(id);
        immobilierService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
