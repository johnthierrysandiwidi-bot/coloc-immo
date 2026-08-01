package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.VueAnnonce;
import bf.colocation.immo.repository.VueAnnonceRepository;
import bf.colocation.immo.service.VueAnnonceService;
import bf.colocation.immo.service.dto.VueAnnonceDTO;
import bf.colocation.immo.service.mapper.VueAnnonceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.VueAnnonce}.
 */
@Service
@Transactional
public class VueAnnonceServiceImpl implements VueAnnonceService {

    private static final Logger LOG = LoggerFactory.getLogger(VueAnnonceServiceImpl.class);

    private final VueAnnonceRepository vueAnnonceRepository;

    private final VueAnnonceMapper vueAnnonceMapper;

    public VueAnnonceServiceImpl(VueAnnonceRepository vueAnnonceRepository, VueAnnonceMapper vueAnnonceMapper) {
        this.vueAnnonceRepository = vueAnnonceRepository;
        this.vueAnnonceMapper = vueAnnonceMapper;
    }

    @Override
    public VueAnnonceDTO save(VueAnnonceDTO vueAnnonceDTO) {
        LOG.debug("Request to save VueAnnonce : {}", vueAnnonceDTO);
        VueAnnonce vueAnnonce = vueAnnonceMapper.toEntity(vueAnnonceDTO);
        vueAnnonce = vueAnnonceRepository.save(vueAnnonce);
        return vueAnnonceMapper.toDto(vueAnnonce);
    }

    @Override
    public VueAnnonceDTO update(VueAnnonceDTO vueAnnonceDTO) {
        LOG.debug("Request to update VueAnnonce : {}", vueAnnonceDTO);
        VueAnnonce vueAnnonce = vueAnnonceMapper.toEntity(vueAnnonceDTO);
        vueAnnonce = vueAnnonceRepository.save(vueAnnonce);
        return vueAnnonceMapper.toDto(vueAnnonce);
    }

    @Override
    public Optional<VueAnnonceDTO> partialUpdate(VueAnnonceDTO vueAnnonceDTO) {
        LOG.debug("Request to partially update VueAnnonce : {}", vueAnnonceDTO);

        return vueAnnonceRepository
            .findById(vueAnnonceDTO.getId())
            .map(existingVueAnnonce -> {
                vueAnnonceMapper.partialUpdate(existingVueAnnonce, vueAnnonceDTO);

                return existingVueAnnonce;
            })
            .map(vueAnnonceRepository::save)
            .map(vueAnnonceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VueAnnonceDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all VueAnnonces");
        return vueAnnonceRepository.findAll(pageable).map(vueAnnonceMapper::toDto);
    }

    public Page<VueAnnonceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return vueAnnonceRepository.findAllWithEagerRelationships(pageable).map(vueAnnonceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VueAnnonceDTO> findOne(Long id) {
        LOG.debug("Request to get VueAnnonce : {}", id);
        return vueAnnonceRepository.findOneWithEagerRelationships(id).map(vueAnnonceMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete VueAnnonce : {}", id);
        vueAnnonceRepository.deleteById(id);
    }
}
