package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.repository.ImmobilierRepository;
import bf.colocation.immo.service.ImmobilierService;
import bf.colocation.immo.service.dto.ImmobilierDTO;
import bf.colocation.immo.service.mapper.ImmobilierMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Immobilier}.
 */
@Service
@Transactional
public class ImmobilierServiceImpl implements ImmobilierService {

    private static final Logger LOG = LoggerFactory.getLogger(ImmobilierServiceImpl.class);

    private final ImmobilierRepository immobilierRepository;

    private final ImmobilierMapper immobilierMapper;

    public ImmobilierServiceImpl(ImmobilierRepository immobilierRepository, ImmobilierMapper immobilierMapper) {
        this.immobilierRepository = immobilierRepository;
        this.immobilierMapper = immobilierMapper;
    }

    @Override
    public ImmobilierDTO save(ImmobilierDTO immobilierDTO) {
        LOG.debug("Request to save Immobilier : {}", immobilierDTO);
        Immobilier immobilier = immobilierMapper.toEntity(immobilierDTO);
        immobilier = immobilierRepository.save(immobilier);
        return immobilierMapper.toDto(immobilier);
    }

    @Override
    public ImmobilierDTO update(ImmobilierDTO immobilierDTO) {
        LOG.debug("Request to update Immobilier : {}", immobilierDTO);
        Immobilier immobilier = immobilierMapper.toEntity(immobilierDTO);
        immobilier = immobilierRepository.save(immobilier);
        return immobilierMapper.toDto(immobilier);
    }

    @Override
    public Optional<ImmobilierDTO> partialUpdate(ImmobilierDTO immobilierDTO) {
        LOG.debug("Request to partially update Immobilier : {}", immobilierDTO);

        return immobilierRepository
            .findById(immobilierDTO.getId())
            .map(existingImmobilier -> {
                immobilierMapper.partialUpdate(existingImmobilier, immobilierDTO);

                return existingImmobilier;
            })
            .map(immobilierRepository::save)
            .map(immobilierMapper::toDto);
    }

    public Page<ImmobilierDTO> findAllWithEagerRelationships(Pageable pageable) {
        return immobilierRepository.findAllWithEagerRelationships(pageable).map(immobilierMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImmobilierDTO> findOne(Long id) {
        LOG.debug("Request to get Immobilier : {}", id);
        return immobilierRepository.findOneWithEagerRelationships(id).map(immobilierMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Immobilier : {}", id);
        immobilierRepository.deleteById(id);
    }
}
