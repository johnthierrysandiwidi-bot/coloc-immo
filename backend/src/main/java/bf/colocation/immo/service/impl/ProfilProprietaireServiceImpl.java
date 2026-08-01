package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.ProfilProprietaire;
import bf.colocation.immo.repository.ProfilProprietaireRepository;
import bf.colocation.immo.service.ProfilProprietaireService;
import bf.colocation.immo.service.dto.ProfilProprietaireDTO;
import bf.colocation.immo.service.mapper.ProfilProprietaireMapper;
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
 * Service Implementation for managing {@link bf.colocation.immo.domain.ProfilProprietaire}.
 */
@Service
@Transactional
public class ProfilProprietaireServiceImpl implements ProfilProprietaireService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfilProprietaireServiceImpl.class);

    private final ProfilProprietaireRepository profilProprietaireRepository;

    private final ProfilProprietaireMapper profilProprietaireMapper;

    public ProfilProprietaireServiceImpl(
        ProfilProprietaireRepository profilProprietaireRepository,
        ProfilProprietaireMapper profilProprietaireMapper
    ) {
        this.profilProprietaireRepository = profilProprietaireRepository;
        this.profilProprietaireMapper = profilProprietaireMapper;
    }

    @Override
    public ProfilProprietaireDTO save(ProfilProprietaireDTO profilProprietaireDTO) {
        LOG.debug("Request to save ProfilProprietaire : {}", profilProprietaireDTO);
        ProfilProprietaire profilProprietaire = profilProprietaireMapper.toEntity(profilProprietaireDTO);
        profilProprietaire = profilProprietaireRepository.save(profilProprietaire);
        return profilProprietaireMapper.toDto(profilProprietaire);
    }

    @Override
    public ProfilProprietaireDTO update(ProfilProprietaireDTO profilProprietaireDTO) {
        LOG.debug("Request to update ProfilProprietaire : {}", profilProprietaireDTO);
        ProfilProprietaire profilProprietaire = profilProprietaireMapper.toEntity(profilProprietaireDTO);
        profilProprietaire = profilProprietaireRepository.save(profilProprietaire);
        return profilProprietaireMapper.toDto(profilProprietaire);
    }

    @Override
    public Optional<ProfilProprietaireDTO> partialUpdate(ProfilProprietaireDTO profilProprietaireDTO) {
        LOG.debug("Request to partially update ProfilProprietaire : {}", profilProprietaireDTO);

        return profilProprietaireRepository
            .findById(profilProprietaireDTO.getId())
            .map(existingProfilProprietaire -> {
                profilProprietaireMapper.partialUpdate(existingProfilProprietaire, profilProprietaireDTO);

                return existingProfilProprietaire;
            })
            .map(profilProprietaireRepository::save)
            .map(profilProprietaireMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfilProprietaireDTO> findAll() {
        LOG.debug("Request to get all ProfilProprietaires");
        return profilProprietaireRepository
            .findAll()
            .stream()
            .map(profilProprietaireMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<ProfilProprietaireDTO> findAllWithEagerRelationships(Pageable pageable) {
        return profilProprietaireRepository.findAllWithEagerRelationships(pageable).map(profilProprietaireMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfilProprietaireDTO> findOne(Long id) {
        LOG.debug("Request to get ProfilProprietaire : {}", id);
        return profilProprietaireRepository.findOneWithEagerRelationships(id).map(profilProprietaireMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete ProfilProprietaire : {}", id);
        profilProprietaireRepository.deleteById(id);
    }
}
