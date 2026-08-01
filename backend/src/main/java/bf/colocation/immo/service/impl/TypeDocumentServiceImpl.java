package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.TypeDocument;
import bf.colocation.immo.repository.TypeDocumentRepository;
import bf.colocation.immo.service.TypeDocumentService;
import bf.colocation.immo.service.dto.TypeDocumentDTO;
import bf.colocation.immo.service.mapper.TypeDocumentMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.TypeDocument}.
 */
@Service
@Transactional
public class TypeDocumentServiceImpl implements TypeDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(TypeDocumentServiceImpl.class);

    private final TypeDocumentRepository typeDocumentRepository;

    private final TypeDocumentMapper typeDocumentMapper;

    public TypeDocumentServiceImpl(TypeDocumentRepository typeDocumentRepository, TypeDocumentMapper typeDocumentMapper) {
        this.typeDocumentRepository = typeDocumentRepository;
        this.typeDocumentMapper = typeDocumentMapper;
    }

    @Override
    public TypeDocumentDTO save(TypeDocumentDTO typeDocumentDTO) {
        LOG.debug("Request to save TypeDocument : {}", typeDocumentDTO);
        TypeDocument typeDocument = typeDocumentMapper.toEntity(typeDocumentDTO);
        typeDocument = typeDocumentRepository.save(typeDocument);
        return typeDocumentMapper.toDto(typeDocument);
    }

    @Override
    public TypeDocumentDTO update(TypeDocumentDTO typeDocumentDTO) {
        LOG.debug("Request to update TypeDocument : {}", typeDocumentDTO);
        TypeDocument typeDocument = typeDocumentMapper.toEntity(typeDocumentDTO);
        typeDocument = typeDocumentRepository.save(typeDocument);
        return typeDocumentMapper.toDto(typeDocument);
    }

    @Override
    public Optional<TypeDocumentDTO> partialUpdate(TypeDocumentDTO typeDocumentDTO) {
        LOG.debug("Request to partially update TypeDocument : {}", typeDocumentDTO);

        return typeDocumentRepository
            .findById(typeDocumentDTO.getId())
            .map(existingTypeDocument -> {
                typeDocumentMapper.partialUpdate(existingTypeDocument, typeDocumentDTO);

                return existingTypeDocument;
            })
            .map(typeDocumentRepository::save)
            .map(typeDocumentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeDocumentDTO> findAll() {
        LOG.debug("Request to get all TypeDocuments");
        return typeDocumentRepository.findAll().stream().map(typeDocumentMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TypeDocumentDTO> findOne(Long id) {
        LOG.debug("Request to get TypeDocument : {}", id);
        return typeDocumentRepository.findById(id).map(typeDocumentMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete TypeDocument : {}", id);
        typeDocumentRepository.deleteById(id);
    }
}
