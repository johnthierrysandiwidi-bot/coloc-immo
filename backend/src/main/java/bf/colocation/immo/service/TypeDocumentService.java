package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.TypeDocumentDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.TypeDocument}.
 */
public interface TypeDocumentService {
    /**
     * Save a typeDocument.
     *
     * @param typeDocumentDTO the entity to save.
     * @return the persisted entity.
     */
    TypeDocumentDTO save(TypeDocumentDTO typeDocumentDTO);

    /**
     * Updates a typeDocument.
     *
     * @param typeDocumentDTO the entity to update.
     * @return the persisted entity.
     */
    TypeDocumentDTO update(TypeDocumentDTO typeDocumentDTO);

    /**
     * Partially updates a typeDocument.
     *
     * @param typeDocumentDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TypeDocumentDTO> partialUpdate(TypeDocumentDTO typeDocumentDTO);

    /**
     * Get all the typeDocuments.
     *
     * @return the list of entities.
     */
    List<TypeDocumentDTO> findAll();

    /**
     * Get the "id" typeDocument.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TypeDocumentDTO> findOne(Long id);

    /**
     * Delete the "id" typeDocument.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
