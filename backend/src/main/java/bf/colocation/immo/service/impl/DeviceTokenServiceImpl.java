package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.DeviceToken;
import bf.colocation.immo.repository.DeviceTokenRepository;
import bf.colocation.immo.service.DeviceTokenService;
import bf.colocation.immo.service.dto.DeviceTokenDTO;
import bf.colocation.immo.service.mapper.DeviceTokenMapper;
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
 * Service Implementation for managing {@link bf.colocation.immo.domain.DeviceToken}.
 */
@Service
@Transactional
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceTokenServiceImpl.class);

    private final DeviceTokenRepository deviceTokenRepository;

    private final DeviceTokenMapper deviceTokenMapper;

    public DeviceTokenServiceImpl(DeviceTokenRepository deviceTokenRepository, DeviceTokenMapper deviceTokenMapper) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.deviceTokenMapper = deviceTokenMapper;
    }

    @Override
    public DeviceTokenDTO save(DeviceTokenDTO deviceTokenDTO) {
        LOG.debug("Request to save DeviceToken : {}", deviceTokenDTO);
        DeviceToken deviceToken = deviceTokenMapper.toEntity(deviceTokenDTO);
        deviceToken = deviceTokenRepository.save(deviceToken);
        return deviceTokenMapper.toDto(deviceToken);
    }

    @Override
    public DeviceTokenDTO update(DeviceTokenDTO deviceTokenDTO) {
        LOG.debug("Request to update DeviceToken : {}", deviceTokenDTO);
        DeviceToken deviceToken = deviceTokenMapper.toEntity(deviceTokenDTO);
        deviceToken = deviceTokenRepository.save(deviceToken);
        return deviceTokenMapper.toDto(deviceToken);
    }

    @Override
    public Optional<DeviceTokenDTO> partialUpdate(DeviceTokenDTO deviceTokenDTO) {
        LOG.debug("Request to partially update DeviceToken : {}", deviceTokenDTO);

        return deviceTokenRepository
            .findById(deviceTokenDTO.getId())
            .map(existingDeviceToken -> {
                deviceTokenMapper.partialUpdate(existingDeviceToken, deviceTokenDTO);

                return existingDeviceToken;
            })
            .map(deviceTokenRepository::save)
            .map(deviceTokenMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTokenDTO> findAll() {
        LOG.debug("Request to get all DeviceTokens");
        return deviceTokenRepository.findAll().stream().map(deviceTokenMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<DeviceTokenDTO> findAllWithEagerRelationships(Pageable pageable) {
        return deviceTokenRepository.findAllWithEagerRelationships(pageable).map(deviceTokenMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeviceTokenDTO> findOne(Long id) {
        LOG.debug("Request to get DeviceToken : {}", id);
        return deviceTokenRepository.findOneWithEagerRelationships(id).map(deviceTokenMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete DeviceToken : {}", id);
        deviceTokenRepository.deleteById(id);
    }
}
