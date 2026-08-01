package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.FavoriRepository;
import bf.colocation.immo.service.metier.PhotoAnnonceService;
import bf.colocation.immo.service.FavoriQueryService;
import bf.colocation.immo.service.FavoriService;
import bf.colocation.immo.service.criteria.FavoriCriteria;
import bf.colocation.immo.service.dto.FavoriDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Favori}.
 */
@RestController
@RequestMapping("/api/favoris")
public class FavoriResource {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriResource.class);

    private static final String ENTITY_NAME = "favori";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final FavoriService favoriService;

    private final FavoriRepository favoriRepository;

    private final FavoriQueryService favoriQueryService;

    private final AutorisationService autorisationService;

    private final PhotoAnnonceService photoAnnonceService;

    public FavoriResource(
        FavoriService favoriService,
        FavoriRepository favoriRepository,
        FavoriQueryService favoriQueryService,
        PhotoAnnonceService photoAnnonceService,
        AutorisationService autorisationService
    ) {
        this.favoriService = favoriService;
        this.favoriRepository = favoriRepository;
        this.favoriQueryService = favoriQueryService;
        this.autorisationService = autorisationService;
        this.photoAnnonceService = photoAnnonceService;
    }

    /**
     * {@code POST  /favoris} : Create a new favori.
     *
     * @param favoriDTO the favoriDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new favoriDTO, or with status {@code 400 (Bad Request)} if the favori has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FavoriDTO> createFavori(@Valid @RequestBody FavoriDTO favoriDTO) throws URISyntaxException {
        LOG.debug("REST request to save Favori : {}", favoriDTO);
        if (favoriDTO.getId() != null) {
            throw new BadRequestAlertException("A new favori cannot already have an ID", ENTITY_NAME, "idexists");
        }
        favoriDTO = favoriService.save(favoriDTO);
        return ResponseEntity.created(new URI("/api/favoris/" + favoriDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, favoriDTO.getId().toString()))
            .body(favoriDTO);
    }

    /**
     * {@code PUT  /favoris/:id} : Updates an existing favori.
     *
     * @param id the id of the favoriDTO to save.
     * @param favoriDTO the favoriDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated favoriDTO,
     * or with status {@code 400 (Bad Request)} if the favoriDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the favoriDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FavoriDTO> updateFavori(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FavoriDTO favoriDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Favori : {}, {}", id, favoriDTO);
        if (favoriDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, favoriDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!favoriRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne peut modifier que ses propres enregistrements.
        favoriService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));

        favoriDTO = favoriService.update(favoriDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, favoriDTO.getId().toString()))
            .body(favoriDTO);
    }

    /**
     * {@code PATCH  /favoris/:id} : Partial updates given fields of an existing favori, field will ignore if it is null
     *
     * @param id the id of the favoriDTO to save.
     * @param favoriDTO the favoriDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated favoriDTO,
     * or with status {@code 400 (Bad Request)} if the favoriDTO is not valid,
     * or with status {@code 404 (Not Found)} if the favoriDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the favoriDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FavoriDTO> partialUpdateFavori(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FavoriDTO favoriDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Favori partially : {}, {}", id, favoriDTO);
        if (favoriDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, favoriDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!favoriRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne peut modifier que ses propres enregistrements.
        favoriService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));

        Optional<FavoriDTO> result = favoriService.partialUpdate(favoriDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, favoriDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /favoris} : get all the Favoris.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Favoris in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FavoriDTO>> getAllFavoris(
        FavoriCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Favoris by criteria: {}", criteria);
        // Sécurité (anti-IDOR) : un utilisateur non-admin ne voit QUE ses propres enregistrements.
        // Le filtre propriétaire est imposé côté serveur et écrase tout paramètre fourni par le client.
        if (!autorisationService.estAdmin()) {
            LongFilter proprietaire = new LongFilter();
            proprietaire.setEquals(autorisationService.idPourFiltrage());
            criteria.setUtilisateurId(proprietaire);
        }

        Page<FavoriDTO> page = favoriQueryService.findByCriteria(criteria, pageable);
        photoAnnonceService.enrichirImbriquees(page.getContent(), FavoriDTO::getAnnonce);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /favoris/count} : count all the favoris.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countFavoris(FavoriCriteria criteria) {
        LOG.debug("REST request to count Favoris by criteria: {}", criteria);
        return ResponseEntity.ok().body(favoriQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /favoris/:id} : get the "id" favori.
     *
     * @param id the id of the favoriDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the favoriDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FavoriDTO> getFavori(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Favori : {}", id);
        Optional<FavoriDTO> favoriDTO = favoriService.findOne(id);
        favoriDTO.ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));
        return ResponseUtil.wrapOrNotFound(favoriDTO);
    }

    /**
     * {@code DELETE  /favoris/:id} : delete the "id" favori.
     *
     * @param id the id of the favoriDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavori(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Favori : {}", id);
        favoriService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));
        favoriService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
