package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.ImageRepository;
import bf.colocation.immo.service.ImageService;
import bf.colocation.immo.service.dto.ImageDTO;
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
 * REST controller for managing {@link bf.colocation.immo.domain.Image}.
 */
@RestController
@RequestMapping("/api/images")
public class ImageResource {

    private static final Logger LOG = LoggerFactory.getLogger(ImageResource.class);

    private static final String ENTITY_NAME = "image";

    @Value("${jhipster.clientApp.name:colocationImmo}")
    private String applicationName;

    private final ImageService imageService;

    private final ImageRepository imageRepository;

    private final AutorisationService autorisationService;

    private final ImmobilierRepository immobilierRepository;

    public ImageResource(ImageService imageService, ImageRepository imageRepository, AutorisationService autorisationService, ImmobilierRepository immobilierRepository) {
        this.imageService = imageService;
        this.imageRepository = imageRepository;
        this.autorisationService = autorisationService;
        this.immobilierRepository = immobilierRepository;
    }

    /**
     * {@code POST  /images} : Create a new image.
     *
     * @param imageDTO the imageDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new imageDTO, or with status {@code 400 (Bad Request)} if the image has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    /**
     * Écriture réservée au titulaire du bien parent (ou à l'administrateur).
     * On lit l'identifiant du titulaire directement en base : les DTO générés n'exposent
     * qu'un extrait des objets imbriqués, la chaîne de propriété y serait toujours nulle.
     */
    private void verifierEcriture(Long id) {
        autorisationService.exigerUnDesProprietairesOuAdmin(
            imageRepository.trouverProprietaireId(id).orElse(null),
            imageRepository.trouverDemarcheurId(id).orElse(null)
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
    public ResponseEntity<ImageDTO> createImage(@Valid @RequestBody ImageDTO imageDTO) throws URISyntaxException {
        verifierEcritureParent(imageDTO.getImmobilier() != null ? imageDTO.getImmobilier().getId() : null);
        LOG.debug("REST request to save Image : {}", imageDTO);
        if (imageDTO.getId() != null) {
            throw new BadRequestAlertException("A new image cannot already have an ID", ENTITY_NAME, "idexists");
        }
        imageDTO = imageService.save(imageDTO);
        return ResponseEntity.created(new URI("/api/images/" + imageDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, imageDTO.getId().toString()))
            .body(imageDTO);
    }

    /**
     * {@code PUT  /images/:id} : Updates an existing image.
     *
     * @param id the id of the imageDTO to save.
     * @param imageDTO the imageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated imageDTO,
     * or with status {@code 400 (Bad Request)} if the imageDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the imageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ImageDTO> updateImage(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ImageDTO imageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Image : {}, {}", id, imageDTO);
        if (imageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, imageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!imageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcriture(id);

        imageDTO = imageService.update(imageDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, imageDTO.getId().toString()))
            .body(imageDTO);
    }

    /**
     * {@code PATCH  /images/:id} : Partial updates given fields of an existing image, field will ignore if it is null
     *
     * @param id the id of the imageDTO to save.
     * @param imageDTO the imageDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated imageDTO,
     * or with status {@code 400 (Bad Request)} if the imageDTO is not valid,
     * or with status {@code 404 (Not Found)} if the imageDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the imageDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ImageDTO> partialUpdateImage(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ImageDTO imageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Image partially : {}, {}", id, imageDTO);
        if (imageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, imageDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!imageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        verifierEcriture(id);

        Optional<ImageDTO> result = imageService.partialUpdate(imageDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, imageDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /images} : get all the Images.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Images in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ImageDTO>> getAllImages(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Images");
        Page<ImageDTO> page;
        if (eagerload) {
            page = imageService.findAllWithEagerRelationships(pageable);
        } else {
            page = imageService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /images/:id} : get the "id" image.
     *
     * @param id the id of the imageDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the imageDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImageDTO> getImage(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Image : {}", id);
        Optional<ImageDTO> imageDTO = imageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(imageDTO);
    }

    /**
     * {@code DELETE  /images/:id} : delete the "id" image.
     *
     * @param id the id of the imageDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Image : {}", id);
        verifierEcriture(id);
        imageService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
