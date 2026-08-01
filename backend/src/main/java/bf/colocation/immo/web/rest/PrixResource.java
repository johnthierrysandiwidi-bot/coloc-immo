package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.PrixRepository;
import bf.colocation.immo.service.PrixService;
import bf.colocation.immo.service.dto.PrixDTO;
import bf.colocation.immo.repository.ImmobilierRepository;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Prix}.
 */
@RestController
@RequestMapping("/api/prixes")
public class PrixResource {

    private static final Logger LOG = LoggerFactory.getLogger(PrixResource.class);

    private static final String ENTITY_NAME = "prix";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final PrixService prixService;

    private final PrixRepository prixRepository;

    private final AutorisationService autorisationService;

    private final ImmobilierRepository immobilierRepository;

    public PrixResource(PrixService prixService, PrixRepository prixRepository, AutorisationService autorisationService, ImmobilierRepository immobilierRepository) {
        this.prixService = prixService;
        this.prixRepository = prixRepository;
        this.autorisationService = autorisationService;
        this.immobilierRepository = immobilierRepository;
    }

    /**
     * {@code POST  /prixes} : Create a new prix.
     *
     * @param prixDTO the prixDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new prixDTO, or with status {@code 400 (Bad Request)} if the prix has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /**
     * Écriture réservée au titulaire du bien parent (ou à l'administrateur).
     * On lit l'identifiant du titulaire directement en base : les DTO générés n'exposent
     * qu'un extrait des objets imbriqués, la chaîne de propriété y serait toujours nulle.
     */
    private void verifierEcriture(Long id) {
        autorisationService.exigerUnDesProprietairesOuAdmin(
            prixRepository.trouverProprietaireId(id).orElse(null),
            prixRepository.trouverDemarcheurId(id).orElse(null)
        );
    }

    /** Même contrôle, mais sur l'objet parent visé lors d'une création. */
    private void verifierEcritureParent(Long parentId) {
        if (parentId == null) {
            return;
        }
        autorisationService.exigerUnDesProprietairesOuAdmin(
            immobilierRepository.trouverProprietaireId(parentId).orElse(null),
            immobilierRepository.trouverDemarcheurId(parentId).orElse(null)
        );
    }

    @PostMapping("")
    public ResponseEntity<PrixDTO> createPrix(@Valid @RequestBody PrixDTO prixDTO) throws URISyntaxException {
        verifierEcritureParent(prixDTO.getImmobilier() != null ? prixDTO.getImmobilier().getId() : null);
        LOG.debug("REST request to save Prix : {}", prixDTO);
        if (prixDTO.getId() != null) {
            throw new BadRequestAlertException("A new prix cannot already have an ID", ENTITY_NAME, "idexists");
        }
        prixDTO = prixService.save(prixDTO);
        return ResponseEntity.created(new URI("/api/prixes/" + prixDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prixDTO.getId().toString()))
            .body(prixDTO);
    }

    /**
     * {@code PUT  /prixes/:id} : Updates an existing prix.
     *
     * @param id the id of the prixDTO to save.
     * @param prixDTO the prixDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prixDTO,
     * or with status {@code 400 (Bad Request)} if the prixDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the prixDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PrixDTO> updatePrix(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PrixDTO prixDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Prix : {}, {}", id, prixDTO);
        if (prixDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prixDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prixRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcriture(id);

        prixDTO = prixService.update(prixDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prixDTO.getId().toString()))
            .body(prixDTO);
    }

    /**
     * {@code PATCH  /prixes/:id} : Partial updates given fields of an existing prix, field will ignore if it is null
     *
     * @param id the id of the prixDTO to save.
     * @param prixDTO the prixDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prixDTO,
     * or with status {@code 400 (Bad Request)} if the prixDTO is not valid,
     * or with status {@code 404 (Not Found)} if the prixDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the prixDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PrixDTO> partialUpdatePrix(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PrixDTO prixDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Prix partially : {}, {}", id, prixDTO);
        if (prixDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prixDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prixRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcriture(id);

        Optional<PrixDTO> result = prixService.partialUpdate(prixDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prixDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /prixes} : get all the Prixes.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Prixes in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PrixDTO>> getAllPrixes(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Prixes");
        Page<PrixDTO> page;
        if (eagerload) {
            page = prixService.findAllWithEagerRelationships(pageable);
        } else {
            page = prixService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /prixes/:id} : get the "id" prix.
     *
     * @param id the id of the prixDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the prixDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrixDTO> getPrix(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Prix : {}", id);
        Optional<PrixDTO> prixDTO = prixService.findOne(id);
        return ResponseUtil.wrapOrNotFound(prixDTO);
    }

    /**
     * {@code DELETE  /prixes/:id} : delete the "id" prix.
     *
     * @param id the id of the prixDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrix(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Prix : {}", id);
        verifierEcriture(id);
        prixService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
