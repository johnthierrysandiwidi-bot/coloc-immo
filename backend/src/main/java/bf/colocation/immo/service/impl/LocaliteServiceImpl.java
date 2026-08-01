package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.repository.LocaliteRepository;
import bf.colocation.immo.service.LocaliteService;
import bf.colocation.immo.service.dto.LocaliteDTO;
import bf.colocation.immo.service.mapper.LocaliteMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Localite}.
 */
@Service
@Transactional
public class LocaliteServiceImpl implements LocaliteService {

    private static final Logger LOG = LoggerFactory.getLogger(LocaliteServiceImpl.class);

    private final LocaliteRepository localiteRepository;

    private final LocaliteMapper localiteMapper;

    public LocaliteServiceImpl(LocaliteRepository localiteRepository, LocaliteMapper localiteMapper) {
        this.localiteRepository = localiteRepository;
        this.localiteMapper = localiteMapper;
    }

    @Override
    public LocaliteDTO save(LocaliteDTO localiteDTO) {
        LOG.debug("Request to save Localite : {}", localiteDTO);
        Localite localite = localiteMapper.toEntity(localiteDTO);
        localite = localiteRepository.save(localite);
        return localiteMapper.toDto(localite);
    }

    @Override
    public LocaliteDTO update(LocaliteDTO localiteDTO) {
        LOG.debug("Request to update Localite : {}", localiteDTO);
        Localite localite = localiteMapper.toEntity(localiteDTO);
        localite = localiteRepository.save(localite);
        return localiteMapper.toDto(localite);
    }

    @Override
    public Optional<LocaliteDTO> partialUpdate(LocaliteDTO localiteDTO) {
        LOG.debug("Request to partially update Localite : {}", localiteDTO);

        return localiteRepository
            .findById(localiteDTO.getId())
            .map(existingLocalite -> {
                localiteMapper.partialUpdate(existingLocalite, localiteDTO);

                return existingLocalite;
            })
            .map(localiteRepository::save)
            .map(localiteMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocaliteDTO> findAll() {
        LOG.debug("Request to get all Localites");
        return localiteRepository.findAll().stream().map(localiteMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocaliteDTO> findOne(Long id) {
        LOG.debug("Request to get Localite : {}", id);
        return localiteRepository.findById(id).map(localiteMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Localite : {}", id);
        localiteRepository.deleteById(id);
    }
}
