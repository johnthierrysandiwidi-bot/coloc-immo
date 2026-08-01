package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.ProfilDemarcheur;
import bf.colocation.immo.repository.ProfilDemarcheurRepository;
import bf.colocation.immo.service.ProfilDemarcheurService;
import bf.colocation.immo.service.dto.ProfilDemarcheurDTO;
import bf.colocation.immo.service.mapper.ProfilDemarcheurMapper;
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
 * Service Implementation for managing {@link bf.colocation.immo.domain.ProfilDemarcheur}.
 */
@Service
@Transactional
public class ProfilDemarcheurServiceImpl implements ProfilDemarcheurService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilDemarcheurServiceImpl.class);

    private final ProfilDemarcheurRepository profilDemarcheurRepository;

    private final ProfilDemarcheurMapper profilDemarcheurMapper;

    public ProfilDemarcheurServiceImpl(
        ProfilDemarcheurRepository profilDemarcheurRepository,
        ProfilDemarcheurMapper profilDemarcheurMapper
    ) {
        this.profilDemarcheurRepository = profilDemarcheurRepository;
        this.profilDemarcheurMapper = profilDemarcheurMapper;
    }

    @Override
    public ProfilDemarcheurDTO save(ProfilDemarcheurDTO profilDemarcheurDTO) {
        LOG.debug("Request to save ProfilDemarcheur : {}", profilDemarcheurDTO);
        ProfilDemarcheur profilDemarcheur = profilDemarcheurMapper.toEntity(profilDemarcheurDTO);
        profilDemarcheur = profilDemarcheurRepository.save(profilDemarcheur);
        return profilDemarcheurMapper.toDto(profilDemarcheur);
    }

    @Override
    public ProfilDemarcheurDTO update(ProfilDemarcheurDTO profilDemarcheurDTO) {
        LOG.debug("Request to update ProfilDemarcheur : {}", profilDemarcheurDTO);
        ProfilDemarcheur profilDemarcheur = profilDemarcheurMapper.toEntity(profilDemarcheurDTO);
        profilDemarcheur = profilDemarcheurRepository.save(profilDemarcheur);
        return profilDemarcheurMapper.toDto(profilDemarcheur);
    }

    @Override
    public Optional<ProfilDemarcheurDTO> partialUpdate(ProfilDemarcheurDTO profilDemarcheurDTO) {
        LOG.debug("Request to partially update ProfilDemarcheur : {}", profilDemarcheurDTO);

        return profilDemarcheurRepository
            .findById(profilDemarcheurDTO.getId())
            .map(existingProfilDemarcheur -> {
                profilDemarcheurMapper.partialUpdate(existingProfilDemarcheur, profilDemarcheurDTO);

                return existingProfilDemarcheur;
            })
            .map(profilDemarcheurRepository::save)
            .map(profilDemarcheurMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfilDemarcheurDTO> findAll() {
        LOG.debug("Request to get all ProfilDemarcheurs");
        return profilDemarcheurRepository
            .findAll()
            .stream()
            .map(profilDemarcheurMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<ProfilDemarcheurDTO> findAllWithEagerRelationships(Pageable pageable) {
        return profilDemarcheurRepository.findAllWithEagerRelationships(pageable).map(profilDemarcheurMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfilDemarcheurDTO> findOne(Long id) {
        LOG.debug("Request to get ProfilDemarcheur : {}", id);
        return profilDemarcheurRepository.findOneWithEagerRelationships(id).map(profilDemarcheurMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete ProfilDemarcheur : {}", id);
        profilDemarcheurRepository.deleteById(id);
    }
}
