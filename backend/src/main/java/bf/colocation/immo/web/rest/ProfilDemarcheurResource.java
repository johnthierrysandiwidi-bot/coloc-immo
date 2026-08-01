package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.ProfilDemarcheurRepository;
import bf.colocation.immo.service.ProfilDemarcheurService;
import bf.colocation.immo.service.dto.ProfilDemarcheurDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.ProfilDemarcheur}.
 */
@RestController
@RequestMapping("/api/profil-demarcheurs")
public class ProfilDemarcheurResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilDemarcheurResource.class);

    private static final String ENTITY_NAME = "profilDemarcheur";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final ProfilDemarcheurService profilDemarcheurService;

    private final ProfilDemarcheurRepository profilDemarcheurRepository;

    private final AutorisationService autorisationService;

    public ProfilDemarcheurResource(
        ProfilDemarcheurService profilDemarcheurService,
        ProfilDemarcheurRepository profilDemarcheurRepository,
        AutorisationService autorisationService
    ) {
        this.profilDemarcheurService = profilDemarcheurService;
        this.profilDemarcheurRepository = profilDemarcheurRepository;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /profil-demarcheurs} : Create a new profilDemarcheur.
     *
     * @param profilDemarcheurDTO the profilDemarcheurDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new profilDemarcheurDTO, or with status {@code 400 (Bad Request)} if the profilDemarcheur has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ProfilDemarcheurDTO> createProfilDemarcheur(@Valid @RequestBody ProfilDemarcheurDTO profilDemarcheurDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ProfilDemarcheur : {}", profilDemarcheurDTO);
        if (profilDemarcheurDTO.getId() != null) {
            throw new BadRequestAlertException("A new profilDemarcheur cannot already have an ID", ENTITY_NAME, "idexists");
        }
        profilDemarcheurDTO = profilDemarcheurService.save(profilDemarcheurDTO);
        return ResponseEntity.created(new URI("/api/profil-demarcheurs/" + profilDemarcheurDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, profilDemarcheurDTO.getId().toString()))
            .body(profilDemarcheurDTO);
    }

    /**
     * {@code PUT  /profil-demarcheurs/:id} : Updates an existing profilDemarcheur.
     *
     * @param id the id of the profilDemarcheurDTO to save.
     * @param profilDemarcheurDTO the profilDemarcheurDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated profilDemarcheurDTO,
     * or with status {@code 400 (Bad Request)} if the profilDemarcheurDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the profilDemarcheurDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfilDemarcheurDTO> updateProfilDemarcheur(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProfilDemarcheurDTO profilDemarcheurDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ProfilDemarcheur : {}, {}", id, profilDemarcheurDTO);
        if (profilDemarcheurDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, profilDemarcheurDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!profilDemarcheurRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne modifie que son propre profil.
        profilDemarcheurService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));

        profilDemarcheurDTO = profilDemarcheurService.update(profilDemarcheurDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, profilDemarcheurDTO.getId().toString()))
            .body(profilDemarcheurDTO);
    }

    /**
     * {@code PATCH  /profil-demarcheurs/:id} : Partial updates given fields of an existing profilDemarcheur, field will ignore if it is null
     *
     * @param id the id of the profilDemarcheurDTO to save.
     * @param profilDemarcheurDTO the profilDemarcheurDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated profilDemarcheurDTO,
     * or with status {@code 400 (Bad Request)} if the profilDemarcheurDTO is not valid,
     * or with status {@code 404 (Not Found)} if the profilDemarcheurDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the profilDemarcheurDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProfilDemarcheurDTO> partialUpdateProfilDemarcheur(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProfilDemarcheurDTO profilDemarcheurDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ProfilDemarcheur partially : {}, {}", id, profilDemarcheurDTO);
        if (profilDemarcheurDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, profilDemarcheurDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!profilDemarcheurRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne modifie que son propre profil.
        profilDemarcheurService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));

        Optional<ProfilDemarcheurDTO> result = profilDemarcheurService.partialUpdate(profilDemarcheurDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, profilDemarcheurDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /profil-demarcheurs} : get all the Profil Demarcheurs.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Profil Demarcheurs in body.
     */
    @GetMapping("")
    public List<ProfilDemarcheurDTO> getAllProfilDemarcheurs(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all ProfilDemarcheurs");
        // Sécurité (anti-IDOR) : un non-admin ne voit que SON propre profil.
        if (autorisationService.estAdmin()) {
            return profilDemarcheurService.findAll();
        }
        Long moi = autorisationService.idPourFiltrage();
        return profilDemarcheurService.findAll().stream()
            .filter(d -> d.getUtilisateur() != null && moi.equals(d.getUtilisateur().getId()))
            .toList();
    }

    /**
     * {@code GET  /profil-demarcheurs/:id} : get the "id" profilDemarcheur.
     *
     * @param id the id of the profilDemarcheurDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the profilDemarcheurDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfilDemarcheurDTO> getProfilDemarcheur(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ProfilDemarcheur : {}", id);
        Optional<ProfilDemarcheurDTO> profilDemarcheurDTO = profilDemarcheurService.findOne(id);
        profilDemarcheurDTO.ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));
        return ResponseUtil.wrapOrNotFound(profilDemarcheurDTO);
    }

    /**
     * {@code DELETE  /profil-demarcheurs/:id} : delete the "id" profilDemarcheur.
     *
     * @param id the id of the profilDemarcheurDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfilDemarcheur(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ProfilDemarcheur : {}", id);
        profilDemarcheurService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));
        profilDemarcheurService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
