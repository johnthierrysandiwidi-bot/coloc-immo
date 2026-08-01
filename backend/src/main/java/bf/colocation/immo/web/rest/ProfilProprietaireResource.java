package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.ProfilProprietaireRepository;
import bf.colocation.immo.service.ProfilProprietaireService;
import bf.colocation.immo.service.dto.ProfilProprietaireDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.ProfilProprietaire}.
 */
@RestController
@RequestMapping("/api/profil-proprietaires")
public class ProfilProprietaireResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilProprietaireResource.class);

    private static final String ENTITY_NAME = "profilProprietaire";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final ProfilProprietaireService profilProprietaireService;

    private final ProfilProprietaireRepository profilProprietaireRepository;

    private final AutorisationService autorisationService;

    public ProfilProprietaireResource(
        ProfilProprietaireService profilProprietaireService,
        ProfilProprietaireRepository profilProprietaireRepository,
        AutorisationService autorisationService
    ) {
        this.profilProprietaireService = profilProprietaireService;
        this.profilProprietaireRepository = profilProprietaireRepository;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /profil-proprietaires} : Create a new profilProprietaire.
     *
     * @param profilProprietaireDTO the profilProprietaireDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new profilProprietaireDTO, or with status {@code 400 (Bad Request)} if the profilProprietaire has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ProfilProprietaireDTO> createProfilProprietaire(@Valid @RequestBody ProfilProprietaireDTO profilProprietaireDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ProfilProprietaire : {}", profilProprietaireDTO);
        if (profilProprietaireDTO.getId() != null) {
            throw new BadRequestAlertException("A new profilProprietaire cannot already have an ID", ENTITY_NAME, "idexists");
        }
        profilProprietaireDTO = profilProprietaireService.save(profilProprietaireDTO);
        return ResponseEntity.created(new URI("/api/profil-proprietaires/" + profilProprietaireDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, profilProprietaireDTO.getId().toString()))
            .body(profilProprietaireDTO);
    }

    /**
     * {@code PUT  /profil-proprietaires/:id} : Updates an existing profilProprietaire.
     *
     * @param id the id of the profilProprietaireDTO to save.
     * @param profilProprietaireDTO the profilProprietaireDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated profilProprietaireDTO,
     * or with status {@code 400 (Bad Request)} if the profilProprietaireDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the profilProprietaireDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfilProprietaireDTO> updateProfilProprietaire(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ProfilProprietaireDTO profilProprietaireDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ProfilProprietaire : {}, {}", id, profilProprietaireDTO);
        if (profilProprietaireDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, profilProprietaireDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!profilProprietaireRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne modifie que son propre profil.
        profilProprietaireService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));

        profilProprietaireDTO = profilProprietaireService.update(profilProprietaireDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, profilProprietaireDTO.getId().toString()))
            .body(profilProprietaireDTO);
    }

    /**
     * {@code PATCH  /profil-proprietaires/:id} : Partial updates given fields of an existing profilProprietaire, field will ignore if it is null
     *
     * @param id the id of the profilProprietaireDTO to save.
     * @param profilProprietaireDTO the profilProprietaireDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated profilProprietaireDTO,
     * or with status {@code 400 (Bad Request)} if the profilProprietaireDTO is not valid,
     * or with status {@code 404 (Not Found)} if the profilProprietaireDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the profilProprietaireDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProfilProprietaireDTO> partialUpdateProfilProprietaire(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ProfilProprietaireDTO profilProprietaireDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ProfilProprietaire partially : {}, {}", id, profilProprietaireDTO);
        if (profilProprietaireDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, profilProprietaireDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!profilProprietaireRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        // Sécurité (anti-IDOR) : on ne modifie que son propre profil.
        profilProprietaireService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));

        Optional<ProfilProprietaireDTO> result = profilProprietaireService.partialUpdate(profilProprietaireDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, profilProprietaireDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /profil-proprietaires} : get all the Profil Proprietaires.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Profil Proprietaires in body.
     */
    @GetMapping("")
    public List<ProfilProprietaireDTO> getAllProfilProprietaires(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all ProfilProprietaires");
        // Sécurité (anti-IDOR) : un non-admin ne voit que SON propre profil.
        if (autorisationService.estAdmin()) {
            return profilProprietaireService.findAll();
        }
        Long moi = autorisationService.idPourFiltrage();
        return profilProprietaireService.findAll().stream()
            .filter(d -> d.getUtilisateur() != null && moi.equals(d.getUtilisateur().getId()))
            .toList();
    }

    /**
     * {@code GET  /profil-proprietaires/:id} : get the "id" profilProprietaire.
     *
     * @param id the id of the profilProprietaireDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the profilProprietaireDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfilProprietaireDTO> getProfilProprietaire(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ProfilProprietaire : {}", id);
        Optional<ProfilProprietaireDTO> profilProprietaireDTO = profilProprietaireService.findOne(id);
        profilProprietaireDTO.ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));
        return ResponseUtil.wrapOrNotFound(profilProprietaireDTO);
    }

    /**
     * {@code DELETE  /profil-proprietaires/:id} : delete the "id" profilProprietaire.
     *
     * @param id the id of the profilProprietaireDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfilProprietaire(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ProfilProprietaire : {}", id);
        profilProprietaireService.findOne(id).ifPresent(d -> autorisationService.exigerProprietaireOuAdmin(
            d.getUtilisateur() != null ? d.getUtilisateur().getId() : null));
        profilProprietaireService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
