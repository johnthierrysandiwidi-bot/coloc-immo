package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.service.RendezVousService;
import bf.colocation.immo.service.dto.RendezVousDTO;
import bf.colocation.immo.service.mapper.RendezVousMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.RendezVous}.
 */
@Service
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    private static final Logger LOG = LoggerFactory.getLogger(RendezVousServiceImpl.class);

    private final RendezVousRepository rendezVousRepository;

    private final RendezVousMapper rendezVousMapper;

    public RendezVousServiceImpl(RendezVousRepository rendezVousRepository, RendezVousMapper rendezVousMapper) {
        this.rendezVousRepository = rendezVousRepository;
        this.rendezVousMapper = rendezVousMapper;
    }

    @Override
    public RendezVousDTO save(RendezVousDTO rendezVousDTO) {
        LOG.debug("Request to save RendezVous : {}", rendezVousDTO);
        RendezVous rendezVous = rendezVousMapper.toEntity(rendezVousDTO);
        rendezVous = rendezVousRepository.save(rendezVous);
        return rendezVousMapper.toDto(rendezVous);
    }

    @Override
    public RendezVousDTO update(RendezVousDTO rendezVousDTO) {
        LOG.debug("Request to update RendezVous : {}", rendezVousDTO);
        RendezVous rendezVous = rendezVousMapper.toEntity(rendezVousDTO);
        rendezVous = rendezVousRepository.save(rendezVous);
        return rendezVousMapper.toDto(rendezVous);
    }

    @Override
    public Optional<RendezVousDTO> partialUpdate(RendezVousDTO rendezVousDTO) {
        LOG.debug("Request to partially update RendezVous : {}", rendezVousDTO);

        return rendezVousRepository
            .findById(rendezVousDTO.getId())
            .map(existingRendezVous -> {
                rendezVousMapper.partialUpdate(existingRendezVous, rendezVousDTO);

                return existingRendezVous;
            })
            .map(rendezVousRepository::save)
            .map(rendezVousMapper::toDto);
    }

    public Page<RendezVousDTO> findAllWithEagerRelationships(Pageable pageable) {
        return rendezVousRepository.findAllWithEagerRelationships(pageable).map(rendezVousMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RendezVousDTO> findVisiblesPar(Long utilisateurId, Pageable pageable) {
        LOG.debug("Request to get RendezVouses visibles par utilisateur : {}", utilisateurId);
        return rendezVousRepository.findVisiblesParUtilisateur(utilisateurId, pageable).map(rendezVousMapper::toDto);
    }

    @Override
    public Optional<RendezVousDTO> findOne(Long id) {
        LOG.debug("Request to get RendezVous : {}", id);
        return rendezVousRepository.findOneWithEagerRelationships(id).map(rendezVousMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete RendezVous : {}", id);
        rendezVousRepository.deleteById(id);
    }
}
