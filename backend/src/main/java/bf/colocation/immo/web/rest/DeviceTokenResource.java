package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.DeviceTokenRepository;
import bf.colocation.immo.service.DeviceTokenService;
import bf.colocation.immo.service.dto.DeviceTokenDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link bf.colocation.immo.domain.DeviceToken}.
 */
@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenResource {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceTokenResource.class);

    private static final String ENTITY_NAME = "deviceToken";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final DeviceTokenService deviceTokenService;

    private final DeviceTokenRepository deviceTokenRepository;

    private final AutorisationService autorisationService;

    public DeviceTokenResource(DeviceTokenService deviceTokenService, DeviceTokenRepository deviceTokenRepository, AutorisationService autorisationService) {
        this.deviceTokenService = deviceTokenService;
        this.deviceTokenRepository = deviceTokenRepository;
        this.autorisationService = autorisationService;
    }

    /**
     * {@code POST  /device-tokens} : Create a new deviceToken.
     *
     * @param deviceTokenDTO the deviceTokenDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new deviceTokenDTO, or with status {@code 400 (Bad Request)} if the deviceToken has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /** Accès réservé au titulaire de l'enregistrement (ou à l'administrateur). */
    private void verifierAcces(Long id) {
        autorisationService.exigerProprietaireOuAdmin(deviceTokenRepository.trouverUtilisateurId(id).orElse(null));
    }

    @PostMapping("")
    public ResponseEntity<DeviceTokenDTO> createDeviceToken(@Valid @RequestBody DeviceTokenDTO deviceTokenDTO) throws URISyntaxException {
        LOG.debug("REST request to save DeviceToken : {}", deviceTokenDTO);
        if (deviceTokenDTO.getId() != null) {
            throw new BadRequestAlertException("A new deviceToken cannot already have an ID", ENTITY_NAME, "idexists");
        }
        deviceTokenDTO = deviceTokenService.save(deviceTokenDTO);
        return ResponseEntity.created(new URI("/api/device-tokens/" + deviceTokenDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, deviceTokenDTO.getId().toString()))
            .body(deviceTokenDTO);
    }

    /**
     * {@code PUT  /device-tokens/:id} : Updates an existing deviceToken.
     *
     * @param id the id of the deviceTokenDTO to save.
     * @param deviceTokenDTO the deviceTokenDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deviceTokenDTO,
     * or with status {@code 400 (Bad Request)} if the deviceTokenDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the deviceTokenDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceTokenDTO> updateDeviceToken(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DeviceTokenDTO deviceTokenDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DeviceToken : {}, {}", id, deviceTokenDTO);
        if (deviceTokenDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deviceTokenDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!deviceTokenRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierAcces(id);

        deviceTokenDTO = deviceTokenService.update(deviceTokenDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, deviceTokenDTO.getId().toString()))
            .body(deviceTokenDTO);
    }

    /**
     * {@code PATCH  /device-tokens/:id} : Partial updates given fields of an existing deviceToken, field will ignore if it is null
     *
     * @param id the id of the deviceTokenDTO to save.
     * @param deviceTokenDTO the deviceTokenDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deviceTokenDTO,
     * or with status {@code 400 (Bad Request)} if the deviceTokenDTO is not valid,
     * or with status {@code 404 (Not Found)} if the deviceTokenDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the deviceTokenDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DeviceTokenDTO> partialUpdateDeviceToken(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DeviceTokenDTO deviceTokenDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DeviceToken partially : {}, {}", id, deviceTokenDTO);
        if (deviceTokenDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deviceTokenDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!deviceTokenRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierAcces(id);

        Optional<DeviceTokenDTO> result = deviceTokenService.partialUpdate(deviceTokenDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, deviceTokenDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /device-tokens} : get all the Device Tokens.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Device Tokens in body.
     */
    // La liste complète expose les enregistrements de tous les utilisateurs.
    // Aucun client ne la consomme : on la réserve à l'administration.
    @GetMapping("")
    @Secured(AuthoritiesConstants.ADMIN)
    public List<DeviceTokenDTO> getAllDeviceTokens(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all DeviceTokens");
        return deviceTokenService.findAll();
    }

    /**
     * {@code GET  /device-tokens/:id} : get the "id" deviceToken.
     *
     * @param id the id of the deviceTokenDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the deviceTokenDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceTokenDTO> getDeviceToken(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DeviceToken : {}", id);
        Optional<DeviceTokenDTO> deviceTokenDTO = deviceTokenService.findOne(id);
        deviceTokenDTO.ifPresent(d -> verifierAcces(id));
        return ResponseUtil.wrapOrNotFound(deviceTokenDTO);
    }

    /**
     * {@code DELETE  /device-tokens/:id} : delete the "id" deviceToken.
     *
     * @param id the id of the deviceTokenDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceToken(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DeviceToken : {}", id);
        verifierAcces(id);
        deviceTokenService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
