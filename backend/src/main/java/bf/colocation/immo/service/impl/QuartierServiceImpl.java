package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Quartier;
import bf.colocation.immo.repository.QuartierRepository;
import bf.colocation.immo.service.QuartierService;
import bf.colocation.immo.service.dto.QuartierDTO;
import bf.colocation.immo.service.mapper.QuartierMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Quartier}.
 */
@Service
@Transactional
public class QuartierServiceImpl implements QuartierService {

    private static final Logger LOG = LoggerFactory.getLogger(QuartierServiceImpl.class);

    private final QuartierRepository quartierRepository;

    private final QuartierMapper quartierMapper;

    public QuartierServiceImpl(QuartierRepository quartierRepository, QuartierMapper quartierMapper) {
        this.quartierRepository = quartierRepository;
        this.quartierMapper = quartierMapper;
    }

    @Override
    public QuartierDTO save(QuartierDTO quartierDTO) {
        LOG.debug("Request to save Quartier : {}", quartierDTO);
        Quartier quartier = quartierMapper.toEntity(quartierDTO);
        quartier = quartierRepository.save(quartier);
        return quartierMapper.toDto(quartier);
    }

    @Override
    public QuartierDTO update(QuartierDTO quartierDTO) {
        LOG.debug("Request to update Quartier : {}", quartierDTO);
        Quartier quartier = quartierMapper.toEntity(quartierDTO);
        quartier = quartierRepository.save(quartier);
        return quartierMapper.toDto(quartier);
    }

    @Override
    public Optional<QuartierDTO> partialUpdate(QuartierDTO quartierDTO) {
        LOG.debug("Request to partially update Quartier : {}", quartierDTO);

        return quartierRepository
            .findById(quartierDTO.getId())
            .map(existingQuartier -> {
                quartierMapper.partialUpdate(existingQuartier, quartierDTO);

                return existingQuartier;
            })
            .map(quartierRepository::save)
            .map(quartierMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuartierDTO> findAll() {
        LOG.debug("Request to get all Quartiers");
        return quartierRepository.findAll().stream().map(quartierMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<QuartierDTO> findAllWithEagerRelationships(Pageable pageable) {
        return quartierRepository.findAllWithEagerRelationships(pageable).map(quartierMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuartierDTO> findOne(Long id) {
        LOG.debug("Request to get Quartier : {}", id);
        return quartierRepository.findOneWithEagerRelationships(id).map(quartierMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Quartier : {}", id);
        quartierRepository.deleteById(id);
    }
}
