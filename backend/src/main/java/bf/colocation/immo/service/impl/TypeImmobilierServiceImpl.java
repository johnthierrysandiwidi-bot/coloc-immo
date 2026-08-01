package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.TypeImmobilier;
import bf.colocation.immo.repository.TypeImmobilierRepository;
import bf.colocation.immo.service.TypeImmobilierService;
import bf.colocation.immo.service.dto.TypeImmobilierDTO;
import bf.colocation.immo.service.mapper.TypeImmobilierMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.TypeImmobilier}.
 */
@Service
@Transactional
public class TypeImmobilierServiceImpl implements TypeImmobilierService {

    private static final Logger LOG = LoggerFactory.getLogger(TypeImmobilierServiceImpl.class);

    private final TypeImmobilierRepository typeImmobilierRepository;

    private final TypeImmobilierMapper typeImmobilierMapper;

    public TypeImmobilierServiceImpl(TypeImmobilierRepository typeImmobilierRepository, TypeImmobilierMapper typeImmobilierMapper) {
        this.typeImmobilierRepository = typeImmobilierRepository;
        this.typeImmobilierMapper = typeImmobilierMapper;
    }

    @Override
    public TypeImmobilierDTO save(TypeImmobilierDTO typeImmobilierDTO) {
        LOG.debug("Request to save TypeImmobilier : {}", typeImmobilierDTO);
        TypeImmobilier typeImmobilier = typeImmobilierMapper.toEntity(typeImmobilierDTO);
        typeImmobilier = typeImmobilierRepository.save(typeImmobilier);
        return typeImmobilierMapper.toDto(typeImmobilier);
    }

    @Override
    public TypeImmobilierDTO update(TypeImmobilierDTO typeImmobilierDTO) {
        LOG.debug("Request to update TypeImmobilier : {}", typeImmobilierDTO);
        TypeImmobilier typeImmobilier = typeImmobilierMapper.toEntity(typeImmobilierDTO);
        typeImmobilier = typeImmobilierRepository.save(typeImmobilier);
        return typeImmobilierMapper.toDto(typeImmobilier);
    }

    @Override
    public Optional<TypeImmobilierDTO> partialUpdate(TypeImmobilierDTO typeImmobilierDTO) {
        LOG.debug("Request to partially update TypeImmobilier : {}", typeImmobilierDTO);

        return typeImmobilierRepository
            .findById(typeImmobilierDTO.getId())
            .map(existingTypeImmobilier -> {
                typeImmobilierMapper.partialUpdate(existingTypeImmobilier, typeImmobilierDTO);

                return existingTypeImmobilier;
            })
            .map(typeImmobilierRepository::save)
            .map(typeImmobilierMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeImmobilierDTO> findAll() {
        LOG.debug("Request to get all TypeImmobiliers");
        return typeImmobilierRepository
            .findAll()
            .stream()
            .map(typeImmobilierMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TypeImmobilierDTO> findOne(Long id) {
        LOG.debug("Request to get TypeImmobilier : {}", id);
        return typeImmobilierRepository.findById(id).map(typeImmobilierMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete TypeImmobilier : {}", id);
        typeImmobilierRepository.deleteById(id);
    }
}
