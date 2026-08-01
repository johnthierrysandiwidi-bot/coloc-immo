package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.RendezVousDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.RendezVous}.
 */
public interface RendezVousService {
    /**
     * Save a rendezVous.
     *
     * @param rendezVousDTO the entity to save.
     * @return the persisted entity.
     */
    RendezVousDTO save(RendezVousDTO rendezVousDTO);

    /**
     * Updates a rendezVous.
     *
     * @param rendezVousDTO the entity to update.
     * @return the persisted entity.
     */
    RendezVousDTO update(RendezVousDTO rendezVousDTO);

    /**
     * Partially updates a rendezVous.
     *
     * @param rendezVousDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<RendezVousDTO> partialUpdate(RendezVousDTO rendezVousDTO);

    /**
     * Get all the rendezVouses with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<RendezVousDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" rendezVous.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    /**
     * Liste paginée des rendez-vous visibles par l'utilisateur donné (ceux qu'il a demandés
     * ou portant sur une de ses annonces). Sécurise la liste contre l'accès aux données d'autrui.
     */
    Page<RendezVousDTO> findVisiblesPar(Long utilisateurId, Pageable pageable);

    Optional<RendezVousDTO> findOne(Long id);

    /**
     * Delete the "id" rendezVous.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
