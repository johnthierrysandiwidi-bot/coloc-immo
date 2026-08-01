package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.service.metier.PhotoAnnonceService;
import bf.colocation.immo.service.metier.PublicationAnnonceService;
import bf.colocation.immo.service.AnnonceQueryService;
import bf.colocation.immo.service.AnnonceService;
import bf.colocation.immo.service.criteria.AnnonceCriteria;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Annonce}.
 */
@RestController
@RequestMapping("/api/annonces")
public class AnnonceResource {

    private static final Logger LOG = LoggerFactory.getLogger(AnnonceResource.class);

    private static final String ENTITY_NAME = "annonce";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final AnnonceService annonceService;

    private final AnnonceRepository annonceRepository;

    private final AnnonceQueryService annonceQueryService;

    private final PublicationAnnonceService publicationAnnonceService;
    private final PhotoAnnonceService photoAnnonceService;

    private final AutorisationService autorisationService;

    public AnnonceResource(
        AnnonceService annonceService,
        AnnonceRepository annonceRepository,
        AnnonceQueryService annonceQueryService,
        PublicationAnnonceService publicationAnnonceService,
        PhotoAnnonceService photoAnnonceService,
        AutorisationService autorisationService
    ) {
        this.annonceService = annonceService;
        this.annonceRepository = annonceRepository;
        this.annonceQueryService = annonceQueryService;
        this.publicationAnnonceService = publicationAnnonceService;
        this.photoAnnonceService = photoAnnonceService;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /annonces} : Create a new annonce.
     *
     * @param annonceDTO the annonceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new annonceDTO, or with status {@code 400 (Bad Request)} if the annonce has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AnnonceDTO> createAnnonce(@Valid @RequestBody AnnonceDTO annonceDTO) throws URISyntaxException {
        LOG.debug("REST request to save Annonce : {}", annonceDTO);
        if (annonceDTO.getId() != null) {
            throw new BadRequestAlertException("A new annonce cannot already have an ID", ENTITY_NAME, "idexists");
        }
        annonceDTO = annonceService.save(annonceDTO);
        return ResponseEntity.created(new URI("/api/annonces/" + annonceDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, annonceDTO.getId().toString()))
            .body(annonceDTO);
    }

    /**
     * {@code PUT  /annonces/:id} : Updates an existing annonce.
     *
     * @param id the id of the annonceDTO to save.
     * @param annonceDTO the annonceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated annonceDTO,
     * or with status {@code 400 (Bad Request)} if the annonceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the annonceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnnonceDTO> updateAnnonce(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AnnonceDTO annonceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Annonce : {}, {}", id, annonceDTO);
        publicationAnnonceService.assurerProprietaire(id);
        if (annonceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, annonceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!annonceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        annonceDTO = annonceService.update(annonceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, annonceDTO.getId().toString()))
            .body(annonceDTO);
    }

    /**
     * {@code PATCH  /annonces/:id} : Partial updates given fields of an existing annonce, field will ignore if it is null
     *
     * @param id the id of the annonceDTO to save.
     * @param annonceDTO the annonceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated annonceDTO,
     * or with status {@code 400 (Bad Request)} if the annonceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the annonceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the annonceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AnnonceDTO> partialUpdateAnnonce(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AnnonceDTO annonceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Annonce partially : {}, {}", id, annonceDTO);
        publicationAnnonceService.assurerProprietaire(id);
        if (annonceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, annonceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!annonceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AnnonceDTO> result = annonceService.partialUpdate(annonceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, annonceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /annonces} : get all the Annonces.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Annonces in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AnnonceDTO>> getAllAnnonces(
        AnnonceCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Annonces by criteria: {}", criteria);

        // Sécurité : seul l'admin, ou l'auteur filtrant SES propres annonces (auteurId = moi),
        // peut voir des annonces non publiées. Tout autre appelant (visiteur ou utilisateur tiers)
        // est restreint aux annonces PUBLIEE — pour empêcher l'énumération des brouillons d'autrui.
        if (!autorisationService.estAdmin()) {
            Long moi = autorisationService.idUtilisateurCourantOptionnel().orElse(null);
            boolean filtreSurMesAnnonces =
                moi != null &&
                criteria.getAuteurId() != null &&
                moi.equals(criteria.getAuteurId().getEquals());
            if (!filtreSurMesAnnonces) {
                AnnonceCriteria.StatutAnnonceFilter f = new AnnonceCriteria.StatutAnnonceFilter();
                f.setEquals(StatutAnnonce.PUBLIEE);
                criteria.setStatut(f);
            }
        }

        Page<AnnonceDTO> page = annonceQueryService.findByCriteria(criteria, pageable);
        photoAnnonceService.enrichir(page.getContent());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /annonces/count} : count all the annonces.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAnnonces(AnnonceCriteria criteria) {
        LOG.debug("REST request to count Annonces by criteria: {}", criteria);
        return ResponseEntity.ok().body(annonceQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /annonces/:id} : get the "id" annonce.
     *
     * @param id the id of the annonceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the annonceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnnonceDTO> getAnnonce(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Annonce : {}", id);
        Optional<AnnonceDTO> annonceDTO = annonceService.findOne(id);
        annonceDTO.ifPresent(photoAnnonceService::enrichir);
        return ResponseUtil.wrapOrNotFound(annonceDTO);
    }

    /**
     * {@code DELETE  /annonces/:id} : delete the "id" annonce.
     *
     * @param id the id of the annonceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnonce(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Annonce : {}", id);
        publicationAnnonceService.assurerProprietaire(id);
        annonceService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
