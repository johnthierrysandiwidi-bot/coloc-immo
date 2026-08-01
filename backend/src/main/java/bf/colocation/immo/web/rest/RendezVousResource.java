package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.service.metier.PhotoAnnonceService;
import bf.colocation.immo.service.RendezVousQueryService;
import bf.colocation.immo.service.RendezVousService;
import bf.colocation.immo.service.criteria.RendezVousCriteria;
import bf.colocation.immo.service.dto.RendezVousDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.RendezVous}.
 */
@RestController
@RequestMapping("/api/rendez-vous")
public class RendezVousResource {

    private static final Logger LOG = LoggerFactory.getLogger(RendezVousResource.class);

    private static final String ENTITY_NAME = "rendezVous";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final RendezVousService rendezVousService;

    private final RendezVousRepository rendezVousRepository;

    private final RendezVousQueryService rendezVousQueryService;

    private final PhotoAnnonceService photoAnnonceService;

    private final AutorisationService autorisationService;

    public RendezVousResource(
        RendezVousService rendezVousService,
        RendezVousRepository rendezVousRepository,
        RendezVousQueryService rendezVousQueryService,
        PhotoAnnonceService photoAnnonceService,
        AutorisationService autorisationService
    ) {
        this.rendezVousService = rendezVousService;
        this.rendezVousRepository = rendezVousRepository;
        this.rendezVousQueryService = rendezVousQueryService;
        this.photoAnnonceService = photoAnnonceService;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /rendez-vous} : Create a new rendezVous.
     *
     * @param rendezVousDTO the rendezVousDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rendezVousDTO, or with status {@code 400 (Bad Request)} if the rendezVous has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /**
     * Vérifie que l'utilisateur courant est légitime sur ce rendez-vous : soit le demandeur (locataire),
     * soit l'auteur de l'annonce (démarcheur/propriétaire), soit l'administrateur. Sinon, HTTP 403.
     */
    private void verifierAccesRendezVous(RendezVousDTO dto) {
        Long demandeurId = dto.getDemandeur() != null ? dto.getDemandeur().getId() : null;
        Long auteurId = (dto.getAnnonce() != null && dto.getAnnonce().getAuteur() != null)
            ? dto.getAnnonce().getAuteur().getId()
            : null;
        autorisationService.exigerUnDesProprietairesOuAdmin(demandeurId, auteurId);
    }

    @PostMapping("")
    public ResponseEntity<RendezVousDTO> createRendezVous(@Valid @RequestBody RendezVousDTO rendezVousDTO) throws URISyntaxException {
        LOG.debug("REST request to save RendezVous : {}", rendezVousDTO);
        if (rendezVousDTO.getId() != null) {
            throw new BadRequestAlertException("A new rendezVous cannot already have an ID", ENTITY_NAME, "idexists");
        }
        rendezVousDTO = rendezVousService.save(rendezVousDTO);
        return ResponseEntity.created(new URI("/api/rendez-vous/" + rendezVousDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, rendezVousDTO.getId().toString()))
            .body(rendezVousDTO);
    }

    /**
     * {@code PUT  /rendez-vous/:id} : Updates an existing rendezVous.
     *
     * @param id the id of the rendezVousDTO to save.
     * @param rendezVousDTO the rendezVousDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rendezVousDTO,
     * or with status {@code 400 (Bad Request)} if the rendezVousDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rendezVousDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RendezVousDTO> updateRendezVous(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RendezVousDTO rendezVousDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update RendezVous : {}, {}", id, rendezVousDTO);
        if (rendezVousDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, rendezVousDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!rendezVousRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : seul le demandeur, l'auteur de l'annonce ou l'admin peut modifier.
        rendezVousService.findOne(id).ifPresent(this::verifierAccesRendezVous);

        rendezVousDTO = rendezVousService.update(rendezVousDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, rendezVousDTO.getId().toString()))
            .body(rendezVousDTO);
    }

    /**
     * {@code PATCH  /rendez-vous/:id} : Partial updates given fields of an existing rendezVous, field will ignore if it is null
     *
     * @param id the id of the rendezVousDTO to save.
     * @param rendezVousDTO the rendezVousDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rendezVousDTO,
     * or with status {@code 400 (Bad Request)} if the rendezVousDTO is not valid,
     * or with status {@code 404 (Not Found)} if the rendezVousDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the rendezVousDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<RendezVousDTO> partialUpdateRendezVous(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RendezVousDTO rendezVousDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update RendezVous partially : {}, {}", id, rendezVousDTO);
        if (rendezVousDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, rendezVousDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!rendezVousRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : seul le demandeur, l'auteur de l'annonce ou l'admin peut modifier.
        rendezVousService.findOne(id).ifPresent(this::verifierAccesRendezVous);

        Optional<RendezVousDTO> result = rendezVousService.partialUpdate(rendezVousDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, rendezVousDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /rendez-vous} : get all the Rendez Vous.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Rendez Vous in body.
     */
    @GetMapping("")
    public ResponseEntity<List<RendezVousDTO>> getAllRendezVouses(
        RendezVousCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get RendezVouses by criteria: {}", criteria);

        // Sécurité (anti-IDOR) : un non-admin ne reçoit QUE les rendez-vous qui le concernent
        // (ceux qu'il a demandés ou portant sur une de ses annonces). Le scoping est imposé côté serveur.
        Page<RendezVousDTO> page = autorisationService.estAdmin()
            ? rendezVousQueryService.findByCriteria(criteria, pageable)
            : rendezVousService.findVisiblesPar(autorisationService.idPourFiltrage(), pageable);
        photoAnnonceService.enrichirImbriquees(page.getContent(), RendezVousDTO::getAnnonce);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /rendez-vous/count} : count all the rendezVouses.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countRendezVouses(RendezVousCriteria criteria) {
        LOG.debug("REST request to count RendezVouses by criteria: {}", criteria);
        return ResponseEntity.ok().body(rendezVousQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /rendez-vous/:id} : get the "id" rendezVous.
     *
     * @param id the id of the rendezVousDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rendezVousDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RendezVousDTO> getRendezVous(@PathVariable("id") Long id) {
        LOG.debug("REST request to get RendezVous : {}", id);
        Optional<RendezVousDTO> rendezVousDTO = rendezVousService.findOne(id);
        rendezVousDTO.ifPresent(this::verifierAccesRendezVous);
        return ResponseUtil.wrapOrNotFound(rendezVousDTO);
    }

    /**
     * {@code DELETE  /rendez-vous/:id} : delete the "id" rendezVous.
     *
     * @param id the id of the rendezVousDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRendezVous(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete RendezVous : {}", id);
        rendezVousService.findOne(id).ifPresent(this::verifierAccesRendezVous);
        rendezVousService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
