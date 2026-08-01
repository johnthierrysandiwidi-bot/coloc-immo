package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.AlerteNotifiee;
import bf.colocation.immo.repository.AlerteNotifieeRepository;
import bf.colocation.immo.service.AlerteNotifieeService;
import bf.colocation.immo.service.dto.AlerteNotifieeDTO;
import bf.colocation.immo.service.mapper.AlerteNotifieeMapper;
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
 * Service Implementation for managing {@link bf.colocation.immo.domain.AlerteNotifiee}.
 */
@Service
@Transactional
public class AlerteNotifieeServiceImpl implements AlerteNotifieeService {

    private static final Logger LOG = LoggerFactory.getLogger(AlerteNotifieeServiceImpl.class);

    private final AlerteNotifieeRepository alerteNotifieeRepository;

    private final AlerteNotifieeMapper alerteNotifieeMapper;

    public AlerteNotifieeServiceImpl(AlerteNotifieeRepository alerteNotifieeRepository, AlerteNotifieeMapper alerteNotifieeMapper) {
        this.alerteNotifieeRepository = alerteNotifieeRepository;
        this.alerteNotifieeMapper = alerteNotifieeMapper;
    }

    @Override
    public AlerteNotifieeDTO save(AlerteNotifieeDTO alerteNotifieeDTO) {
        LOG.debug("Request to save AlerteNotifiee : {}", alerteNotifieeDTO);
        AlerteNotifiee alerteNotifiee = alerteNotifieeMapper.toEntity(alerteNotifieeDTO);
        alerteNotifiee = alerteNotifieeRepository.save(alerteNotifiee);
        return alerteNotifieeMapper.toDto(alerteNotifiee);
    }

    @Override
    public AlerteNotifieeDTO update(AlerteNotifieeDTO alerteNotifieeDTO) {
        LOG.debug("Request to update AlerteNotifiee : {}", alerteNotifieeDTO);
        AlerteNotifiee alerteNotifiee = alerteNotifieeMapper.toEntity(alerteNotifieeDTO);
        alerteNotifiee = alerteNotifieeRepository.save(alerteNotifiee);
        return alerteNotifieeMapper.toDto(alerteNotifiee);
    }

    @Override
    public Optional<AlerteNotifieeDTO> partialUpdate(AlerteNotifieeDTO alerteNotifieeDTO) {
        LOG.debug("Request to partially update AlerteNotifiee : {}", alerteNotifieeDTO);

        return alerteNotifieeRepository
            .findById(alerteNotifieeDTO.getId())
            .map(existingAlerteNotifiee -> {
                alerteNotifieeMapper.partialUpdate(existingAlerteNotifiee, alerteNotifieeDTO);

                return existingAlerteNotifiee;
            })
            .map(alerteNotifieeRepository::save)
            .map(alerteNotifieeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteNotifieeDTO> findAll() {
        LOG.debug("Request to get all AlerteNotifiees");
        return alerteNotifieeRepository
            .findAll()
            .stream()
            .map(alerteNotifieeMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<AlerteNotifieeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return alerteNotifieeRepository.findAllWithEagerRelationships(pageable).map(alerteNotifieeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlerteNotifieeDTO> findOne(Long id) {
        LOG.debug("Request to get AlerteNotifiee : {}", id);
        return alerteNotifieeRepository.findOneWithEagerRelationships(id).map(alerteNotifieeMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete AlerteNotifiee : {}", id);
        alerteNotifieeRepository.deleteById(id);
    }
}
