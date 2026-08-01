package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.VueAnnonceRepository;
import bf.colocation.immo.service.VueAnnonceService;
import bf.colocation.immo.service.dto.VueAnnonceDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link bf.colocation.immo.domain.VueAnnonce}.
 */
@RestController
@RequestMapping("/api/vue-annonces")
public class VueAnnonceResource {

    private static final Logger LOG = LoggerFactory.getLogger(VueAnnonceResource.class);

    private static final String ENTITY_NAME = "vueAnnonce";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final VueAnnonceService vueAnnonceService;

    private final VueAnnonceRepository vueAnnonceRepository;

    private final AutorisationService autorisationService;

    public VueAnnonceResource(VueAnnonceService vueAnnonceService, VueAnnonceRepository vueAnnonceRepository, AutorisationService autorisationService) {
        this.vueAnnonceService = vueAnnonceService;
        this.vueAnnonceRepository = vueAnnonceRepository;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /vue-annonces} : Create a new vueAnnonce.
     *
     * @param vueAnnonceDTO the vueAnnonceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new vueAnnonceDTO, or with status {@code 400 (Bad Request)} if the vueAnnonce has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /** Accès réservé au titulaire de l'enregistrement (ou à l'administrateur). */
    private void verifierAcces(Long id) {
        autorisationService.exigerProprietaireOuAdmin(vueAnnonceRepository.trouverUtilisateurId(id).orElse(null));
    }

    @PostMapping("")
    public ResponseEntity<VueAnnonceDTO> createVueAnnonce(@Valid @RequestBody VueAnnonceDTO vueAnnonceDTO) throws URISyntaxException {
        LOG.debug("REST request to save VueAnnonce : {}", vueAnnonceDTO);
        if (vueAnnonceDTO.getId() != null) {
            throw new BadRequestAlertException("A new vueAnnonce cannot already have an ID", ENTITY_NAME, "idexists");
        }
        vueAnnonceDTO = vueAnnonceService.save(vueAnnonceDTO);
        return ResponseEntity.created(new URI("/api/vue-annonces/" + vueAnnonceDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, vueAnnonceDTO.getId().toString()))
            .body(vueAnnonceDTO);
    }

    /**
     * {@code PUT  /vue-annonces/:id} : Updates an existing vueAnnonce.
     *
     * @param id the id of the vueAnnonceDTO to save.
     * @param vueAnnonceDTO the vueAnnonceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated vueAnnonceDTO,
     * or with status {@code 400 (Bad Request)} if the vueAnnonceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the vueAnnonceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VueAnnonceDTO> updateVueAnnonce(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VueAnnonceDTO vueAnnonceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update VueAnnonce : {}, {}", id, vueAnnonceDTO);
        if (vueAnnonceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, vueAnnonceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!vueAnnonceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierAcces(id);

        vueAnnonceDTO = vueAnnonceService.update(vueAnnonceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, vueAnnonceDTO.getId().toString()))
            .body(vueAnnonceDTO);
    }

    /**
     * {@code PATCH  /vue-annonces/:id} : Partial updates given fields of an existing vueAnnonce, field will ignore if it is null
     *
     * @param id the id of the vueAnnonceDTO to save.
     * @param vueAnnonceDTO the vueAnnonceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated vueAnnonceDTO,
     * or with status {@code 400 (Bad Request)} if the vueAnnonceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the vueAnnonceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the vueAnnonceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<VueAnnonceDTO> partialUpdateVueAnnonce(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VueAnnonceDTO vueAnnonceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update VueAnnonce partially : {}, {}", id, vueAnnonceDTO);
        if (vueAnnonceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, vueAnnonceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!vueAnnonceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierAcces(id);

        Optional<VueAnnonceDTO> result = vueAnnonceService.partialUpdate(vueAnnonceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, vueAnnonceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /vue-annonces} : get all the Vue Annonces.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Vue Annonces in body.
     */
    // La liste complète expose les enregistrements de tous les utilisateurs.
    // Aucun client ne la consomme : on la réserve à l'administration.
    @GetMapping("")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<List<VueAnnonceDTO>> getAllVueAnnonces(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of VueAnnonces");
        Page<VueAnnonceDTO> page;
        if (eagerload) {
            page = vueAnnonceService.findAllWithEagerRelationships(pageable);
        } else {
            page = vueAnnonceService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /vue-annonces/:id} : get the "id" vueAnnonce.
     *
     * @param id the id of the vueAnnonceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the vueAnnonceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VueAnnonceDTO> getVueAnnonce(@PathVariable("id") Long id) {
        LOG.debug("REST request to get VueAnnonce : {}", id);
        Optional<VueAnnonceDTO> vueAnnonceDTO = vueAnnonceService.findOne(id);
        vueAnnonceDTO.ifPresent(d -> verifierAcces(id));
        return ResponseUtil.wrapOrNotFound(vueAnnonceDTO);
    }

    /**
     * {@code DELETE  /vue-annonces/:id} : delete the "id" vueAnnonce.
     *
     * @param id the id of the vueAnnonceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVueAnnonce(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete VueAnnonce : {}", id);
        verifierAcces(id);
        vueAnnonceService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
