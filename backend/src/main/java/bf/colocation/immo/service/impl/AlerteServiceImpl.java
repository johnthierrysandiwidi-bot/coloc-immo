package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Alerte;
import bf.colocation.immo.repository.AlerteRepository;
import bf.colocation.immo.service.AlerteService;
import bf.colocation.immo.service.dto.AlerteDTO;
import bf.colocation.immo.service.mapper.AlerteMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Alerte}.
 */
@Service
@Transactional
public class AlerteServiceImpl implements AlerteService {

    private static final Logger LOG = LoggerFactory.getLogger(AlerteServiceImpl.class);

    private final AlerteRepository alerteRepository;

    private final AlerteMapper alerteMapper;

    public AlerteServiceImpl(AlerteRepository alerteRepository, AlerteMapper alerteMapper) {
        this.alerteRepository = alerteRepository;
        this.alerteMapper = alerteMapper;
    }

    @Override
    public AlerteDTO save(AlerteDTO alerteDTO) {
        LOG.debug("Request to save Alerte : {}", alerteDTO);
        Alerte alerte = alerteMapper.toEntity(alerteDTO);
        alerte = alerteRepository.save(alerte);
        return alerteMapper.toDto(alerte);
    }

    @Override
    public AlerteDTO update(AlerteDTO alerteDTO) {
        LOG.debug("Request to update Alerte : {}", alerteDTO);
        Alerte alerte = alerteMapper.toEntity(alerteDTO);
        alerte = alerteRepository.save(alerte);
        return alerteMapper.toDto(alerte);
    }

    @Override
    public Optional<AlerteDTO> partialUpdate(AlerteDTO alerteDTO) {
        LOG.debug("Request to partially update Alerte : {}", alerteDTO);

        return alerteRepository
            .findById(alerteDTO.getId())
            .map(existingAlerte -> {
                alerteMapper.partialUpdate(existingAlerte, alerteDTO);

                return existingAlerte;
            })
            .map(alerteRepository::save)
            .map(alerteMapper::toDto);
    }

    public Page<AlerteDTO> findAllWithEagerRelationships(Pageable pageable) {
        return alerteRepository.findAllWithEagerRelationships(pageable).map(alerteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlerteDTO> findOne(Long id) {
        LOG.debug("Request to get Alerte : {}", id);
        return alerteRepository.findOneWithEagerRelationships(id).map(alerteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Alerte : {}", id);
        alerteRepository.deleteById(id);
    }
}
