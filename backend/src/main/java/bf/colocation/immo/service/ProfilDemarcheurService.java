package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.ProfilDemarcheurDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.ProfilDemarcheur}.
 */
public interface ProfilDemarcheurService {
    /**
     * Save a profilDemarcheur.
     *
     * @param profilDemarcheurDTO the entity to save.
     * @return the persisted entity.
     */
    ProfilDemarcheurDTO save(ProfilDemarcheurDTO profilDemarcheurDTO);

    /**
     * Updates a profilDemarcheur.
     *
     * @param profilDemarcheurDTO the entity to update.
     * @return the persisted entity.
     */
    ProfilDemarcheurDTO update(ProfilDemarcheurDTO profilDemarcheurDTO);

    /**
     * Partially updates a profilDemarcheur.
     *
     * @param profilDemarcheurDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ProfilDemarcheurDTO> partialUpdate(ProfilDemarcheurDTO profilDemarcheurDTO);

    /**
     * Get all the profilDemarcheurs.
     *
     * @return the list of entities.
     */
    List<ProfilDemarcheurDTO> findAll();

    /**
     * Get all the profilDemarcheurs with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ProfilDemarcheurDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" profilDemarcheur.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProfilDemarcheurDTO> findOne(Long id);

    /**
     * Delete the "id" profilDemarcheur.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
