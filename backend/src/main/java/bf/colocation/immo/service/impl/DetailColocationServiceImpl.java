package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.DetailColocation;
import bf.colocation.immo.repository.DetailColocationRepository;
import bf.colocation.immo.service.DetailColocationService;
import bf.colocation.immo.service.dto.DetailColocationDTO;
import bf.colocation.immo.service.mapper.DetailColocationMapper;
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
 * Service Implementation for managing {@link bf.colocation.immo.domain.DetailColocation}.
 */
@Service
@Transactional
public class DetailColocationServiceImpl implements DetailColocationService {

    private static final Logger LOG = LoggerFactory.getLogger(DetailColocationServiceImpl.class);

    private final DetailColocationRepository detailColocationRepository;

    private final DetailColocationMapper detailColocationMapper;

    public DetailColocationServiceImpl(
        DetailColocationRepository detailColocationRepository,
        DetailColocationMapper detailColocationMapper
    ) {
        this.detailColocationRepository = detailColocationRepository;
        this.detailColocationMapper = detailColocationMapper;
    }

    @Override
    public DetailColocationDTO save(DetailColocationDTO detailColocationDTO) {
        LOG.debug("Request to save DetailColocation : {}", detailColocationDTO);
        DetailColocation detailColocation = detailColocationMapper.toEntity(detailColocationDTO);
        detailColocation = detailColocationRepository.save(detailColocation);
        return detailColocationMapper.toDto(detailColocation);
    }

    @Override
    public DetailColocationDTO update(DetailColocationDTO detailColocationDTO) {
        LOG.debug("Request to update DetailColocation : {}", detailColocationDTO);
        DetailColocation detailColocation = detailColocationMapper.toEntity(detailColocationDTO);
        detailColocation = detailColocationRepository.save(detailColocation);
        return detailColocationMapper.toDto(detailColocation);
    }

    @Override
    public Optional<DetailColocationDTO> partialUpdate(DetailColocationDTO detailColocationDTO) {
        LOG.debug("Request to partially update DetailColocation : {}", detailColocationDTO);

        return detailColocationRepository
            .findById(detailColocationDTO.getId())
            .map(existingDetailColocation -> {
                detailColocationMapper.partialUpdate(existingDetailColocation, detailColocationDTO);

                return existingDetailColocation;
            })
            .map(detailColocationRepository::save)
            .map(detailColocationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetailColocationDTO> findAll() {
        LOG.debug("Request to get all DetailColocations");
        return detailColocationRepository
            .findAll()
            .stream()
            .map(detailColocationMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<DetailColocationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return detailColocationRepository.findAllWithEagerRelationships(pageable).map(detailColocationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DetailColocationDTO> findOne(Long id) {
        LOG.debug("Request to get DetailColocation : {}", id);
        return detailColocationRepository.findOneWithEagerRelationships(id).map(detailColocationMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete DetailColocation : {}", id);
        detailColocationRepository.deleteById(id);
    }
}
