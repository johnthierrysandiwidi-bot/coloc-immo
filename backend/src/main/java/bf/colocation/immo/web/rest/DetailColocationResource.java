package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.DetailColocationRepository;
import bf.colocation.immo.service.DetailColocationService;
import bf.colocation.immo.service.dto.DetailColocationDTO;
import bf.colocation.immo.repository.AnnonceRepository;
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
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link bf.colocation.immo.domain.DetailColocation}.
 */
@RestController
@RequestMapping("/api/detail-colocations")
public class DetailColocationResource {

    private static final Logger LOG = LoggerFactory.getLogger(DetailColocationResource.class);

    private static final String ENTITY_NAME = "detailColocation";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final DetailColocationService detailColocationService;

    private final DetailColocationRepository detailColocationRepository;

    private final AutorisationService autorisationService;

    private final AnnonceRepository annonceRepository;

    public DetailColocationResource(
        DetailColocationService detailColocationService,
        DetailColocationRepository detailColocationRepository,
        AutorisationService autorisationService,
        AnnonceRepository annonceRepository
    ) {
        this.detailColocationService = detailColocationService;
        this.detailColocationRepository = detailColocationRepository;
        this.autorisationService = autorisationService;
        this.annonceRepository = annonceRepository;
    }

    /**
     * {@code POST  /detail-colocations} : Create a new detailColocation.
     *
     * @param detailColocationDTO the detailColocationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new detailColocationDTO, or with status {@code 400 (Bad Request)} if the detailColocation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /**
     * Écriture réservée au titulaire du bien parent (ou à l'administrateur).
     * On lit l'identifiant du titulaire directement en base : les DTO générés n'exposent
     * qu'un extrait des objets imbriqués, la chaîne de propriété y serait toujours nulle.
     */
    private void verifierEcriture(Long id) {
        autorisationService.exigerUnDesProprietairesOuAdmin(
            detailColocationRepository.trouverAuteurId(id).orElse(null)
        );
    }

    /** Même contrôle, mais sur l'objet parent visé lors d'une création. */
    private void verifierEcritureParent(Long parentId) {
        if (parentId == null) {
            return;
        }
        autorisationService.exigerUnDesProprietairesOuAdmin(
            annonceRepository.trouverAuteurId(parentId).orElse(null)
        );
    }

    @PostMapping("")
    public ResponseEntity<DetailColocationDTO> createDetailColocation(@Valid @RequestBody DetailColocationDTO detailColocationDTO)
        throws URISyntaxException {
        verifierEcritureParent(detailColocationDTO.getAnnonce() != null ? detailColocationDTO.getAnnonce().getId() : null);
        LOG.debug("REST request to save DetailColocation : {}", detailColocationDTO);
        if (detailColocationDTO.getId() != null) {
            throw new BadRequestAlertException("A new detailColocation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        detailColocationDTO = detailColocationService.save(detailColocationDTO);
        return ResponseEntity.created(new URI("/api/detail-colocations/" + detailColocationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, detailColocationDTO.getId().toString()))
            .body(detailColocationDTO);
    }

    /**
     * {@code PUT  /detail-colocations/:id} : Updates an existing detailColocation.
     *
     * @param id the id of the detailColocationDTO to save.
     * @param detailColocationDTO the detailColocationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated detailColocationDTO,
     * or with status {@code 400 (Bad Request)} if the detailColocationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the detailColocationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DetailColocationDTO> updateDetailColocation(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DetailColocationDTO detailColocationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DetailColocation : {}, {}", id, detailColocationDTO);
        if (detailColocationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, detailColocationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!detailColocationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcriture(id);

        detailColocationDTO = detailColocationService.update(detailColocationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, detailColocationDTO.getId().toString()))
            .body(detailColocationDTO);
    }

    /**
     * {@code PATCH  /detail-colocations/:id} : Partial updates given fields of an existing detailColocation, field will ignore if it is null
     *
     * @param id the id of the detailColocationDTO to save.
     * @param detailColocationDTO the detailColocationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated detailColocationDTO,
     * or with status {@code 400 (Bad Request)} if the detailColocationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the detailColocationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the detailColocationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DetailColocationDTO> partialUpdateDetailColocation(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DetailColocationDTO detailColocationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DetailColocation partially : {}, {}", id, detailColocationDTO);
        if (detailColocationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, detailColocationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!detailColocationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcriture(id);

        Optional<DetailColocationDTO> result = detailColocationService.partialUpdate(detailColocationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, detailColocationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /detail-colocations} : get all the Detail Colocations.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Detail Colocations in body.
     */
    @GetMapping("")
    public List<DetailColocationDTO> getAllDetailColocations(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all DetailColocations");
        return detailColocationService.findAll();
    }

    /**
     * {@code GET  /detail-colocations/:id} : get the "id" detailColocation.
     *
     * @param id the id of the detailColocationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the detailColocationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DetailColocationDTO> getDetailColocation(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DetailColocation : {}", id);
        Optional<DetailColocationDTO> detailColocationDTO = detailColocationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(detailColocationDTO);
    }

    /**
     * {@code DELETE  /detail-colocations/:id} : delete the "id" detailColocation.
     *
     * @param id the id of the detailColocationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetailColocation(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DetailColocation : {}", id);
        verifierEcriture(id);
        detailColocationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
