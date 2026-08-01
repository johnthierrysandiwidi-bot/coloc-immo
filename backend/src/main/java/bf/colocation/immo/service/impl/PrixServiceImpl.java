package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Prix;
import bf.colocation.immo.repository.PrixRepository;
import bf.colocation.immo.service.PrixService;
import bf.colocation.immo.service.dto.PrixDTO;
import bf.colocation.immo.service.mapper.PrixMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Prix}.
 */
@Service
@Transactional
public class PrixServiceImpl implements PrixService {

    private static final Logger LOG = LoggerFactory.getLogger(PrixServiceImpl.class);

    private final PrixRepository prixRepository;

    private final PrixMapper prixMapper;

    public PrixServiceImpl(PrixRepository prixRepository, PrixMapper prixMapper) {
        this.prixRepository = prixRepository;
        this.prixMapper = prixMapper;
    }

    @Override
    public PrixDTO save(PrixDTO prixDTO) {
        LOG.debug("Request to save Prix : {}", prixDTO);
        Prix prix = prixMapper.toEntity(prixDTO);
        prix = prixRepository.save(prix);
        return prixMapper.toDto(prix);
    }

    @Override
    public PrixDTO update(PrixDTO prixDTO) {
        LOG.debug("Request to update Prix : {}", prixDTO);
        Prix prix = prixMapper.toEntity(prixDTO);
        prix = prixRepository.save(prix);
        return prixMapper.toDto(prix);
    }

    @Override
    public Optional<PrixDTO> partialUpdate(PrixDTO prixDTO) {
        LOG.debug("Request to partially update Prix : {}", prixDTO);

        return prixRepository
            .findById(prixDTO.getId())
            .map(existingPrix -> {
                prixMapper.partialUpdate(existingPrix, prixDTO);

                return existingPrix;
            })
            .map(prixRepository::save)
            .map(prixMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrixDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Prixes");
        return prixRepository.findAll(pageable).map(prixMapper::toDto);
    }

    public Page<PrixDTO> findAllWithEagerRelationships(Pageable pageable) {
        return prixRepository.findAllWithEagerRelationships(pageable).map(prixMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrixDTO> findOne(Long id) {
        LOG.debug("Request to get Prix : {}", id);
        return prixRepository.findOneWithEagerRelationships(id).map(prixMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Prix : {}", id);
        prixRepository.deleteById(id);
    }
}
