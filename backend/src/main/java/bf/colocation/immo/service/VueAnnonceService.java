package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.VueAnnonceDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.VueAnnonce}.
 */
public interface VueAnnonceService {
    /**
     * Save a vueAnnonce.
     *
     * @param vueAnnonceDTO the entity to save.
     * @return the persisted entity.
     */
    VueAnnonceDTO save(VueAnnonceDTO vueAnnonceDTO);

    /**
     * Updates a vueAnnonce.
     *
     * @param vueAnnonceDTO the entity to update.
     * @return the persisted entity.
     */
    VueAnnonceDTO update(VueAnnonceDTO vueAnnonceDTO);

    /**
     * Partially updates a vueAnnonce.
     *
     * @param vueAnnonceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<VueAnnonceDTO> partialUpdate(VueAnnonceDTO vueAnnonceDTO);

    /**
     * Get all the vueAnnonces.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<VueAnnonceDTO> findAll(Pageable pageable);

    /**
     * Get all the vueAnnonces with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<VueAnnonceDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" vueAnnonce.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<VueAnnonceDTO> findOne(Long id);

    /**
     * Delete the "id" vueAnnonce.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
