package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.AnnonceDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.Annonce}.
 */
public interface AnnonceService {
    /**
     * Save a annonce.
     *
     * @param annonceDTO the entity to save.
     * @return the persisted entity.
     */
    AnnonceDTO save(AnnonceDTO annonceDTO);

    /**
     * Updates a annonce.
     *
     * @param annonceDTO the entity to update.
     * @return the persisted entity.
     */
    AnnonceDTO update(AnnonceDTO annonceDTO);

    /**
     * Partially updates a annonce.
     *
     * @param annonceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AnnonceDTO> partialUpdate(AnnonceDTO annonceDTO);

    /**
     * Get all the AnnonceDTO where DetailColocation is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<AnnonceDTO> findAllWhereDetailColocationIsNull();

    /**
     * Get all the annonces with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AnnonceDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" annonce.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AnnonceDTO> findOne(Long id);

    /**
     * Delete the "id" annonce.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
