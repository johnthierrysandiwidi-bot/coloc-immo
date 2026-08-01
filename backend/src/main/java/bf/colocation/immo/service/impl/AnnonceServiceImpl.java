package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.service.AnnonceService;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.mapper.AnnonceMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Annonce}.
 */
@Service
@Transactional
public class AnnonceServiceImpl implements AnnonceService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnonceServiceImpl.class);

    private final AnnonceRepository annonceRepository;

    private final AnnonceMapper annonceMapper;

    public AnnonceServiceImpl(AnnonceRepository annonceRepository, AnnonceMapper annonceMapper) {
        this.annonceRepository = annonceRepository;
        this.annonceMapper = annonceMapper;
    }

    @Override
    public AnnonceDTO save(AnnonceDTO annonceDTO) {
        LOG.debug("Request to save Annonce : {}", annonceDTO);
        Annonce annonce = annonceMapper.toEntity(annonceDTO);
        annonce = annonceRepository.save(annonce);
        return annonceMapper.toDto(annonce);
    }

    @Override
    public AnnonceDTO update(AnnonceDTO annonceDTO) {
        LOG.debug("Request to update Annonce : {}", annonceDTO);
        Annonce annonce = annonceMapper.toEntity(annonceDTO);
        annonce = annonceRepository.save(annonce);
        return annonceMapper.toDto(annonce);
    }

    @Override
    public Optional<AnnonceDTO> partialUpdate(AnnonceDTO annonceDTO) {
        LOG.debug("Request to partially update Annonce : {}", annonceDTO);

        return annonceRepository
            .findById(annonceDTO.getId())
            .map(existingAnnonce -> {
                annonceMapper.partialUpdate(existingAnnonce, annonceDTO);

                return existingAnnonce;
            })
            .map(annonceRepository::save)
            .map(annonceMapper::toDto);
    }

    public Page<AnnonceDTO> findAllWithEagerRelationships(Pageable pageable) {
        return annonceRepository.findAllWithEagerRelationships(pageable).map(annonceMapper::toDto);
    }

    /**
     *  Get all the annonces where DetailColocation is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<AnnonceDTO> findAllWhereDetailColocationIsNull() {
        LOG.debug("Request to get all annonces where DetailColocation is null");
        return StreamSupport.stream(annonceRepository.findAll().spliterator(), false)
            .filter(annonce -> annonce.getDetailColocation() == null)
            .map(annonceMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnnonceDTO> findOne(Long id) {
        LOG.debug("Request to get Annonce : {}", id);
        return annonceRepository.findOneWithEagerRelationships(id).map(annonceMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Annonce : {}", id);
        annonceRepository.deleteById(id);
    }
}
