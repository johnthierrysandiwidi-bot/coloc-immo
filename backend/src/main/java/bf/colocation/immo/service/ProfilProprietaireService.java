package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.ProfilProprietaireDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.ProfilProprietaire}.
 */
public interface ProfilProprietaireService {
    /**
     * Save a profilProprietaire.
     *
     * @param profilProprietaireDTO the entity to save.
     * @return the persisted entity.
     */
    ProfilProprietaireDTO save(ProfilProprietaireDTO profilProprietaireDTO);

    /**
     * Updates a profilProprietaire.
     *
     * @param profilProprietaireDTO the entity to update.
     * @return the persisted entity.
     */
    ProfilProprietaireDTO update(ProfilProprietaireDTO profilProprietaireDTO);

    /**
     * Partially updates a profilProprietaire.
     *
     * @param profilProprietaireDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ProfilProprietaireDTO> partialUpdate(ProfilProprietaireDTO profilProprietaireDTO);

    /**
     * Get all the profilProprietaires.
     *
     * @return the list of entities.
     */
    List<ProfilProprietaireDTO> findAll();

    /**
     * Get all the profilProprietaires with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ProfilProprietaireDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" profilProprietaire.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProfilProprietaireDTO> findOne(Long id);

    /**
     * Delete the "id" profilProprietaire.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
