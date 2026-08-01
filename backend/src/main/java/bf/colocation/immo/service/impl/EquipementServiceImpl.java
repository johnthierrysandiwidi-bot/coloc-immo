package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Equipement;
import bf.colocation.immo.repository.EquipementRepository;
import bf.colocation.immo.service.EquipementService;
import bf.colocation.immo.service.dto.EquipementDTO;
import bf.colocation.immo.service.mapper.EquipementMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Equipement}.
 */
@Service
@Transactional
public class EquipementServiceImpl implements EquipementService {

    private static final Logger LOG = LoggerFactory.getLogger(EquipementServiceImpl.class);

    private final EquipementRepository equipementRepository;

    private final EquipementMapper equipementMapper;

    public EquipementServiceImpl(EquipementRepository equipementRepository, EquipementMapper equipementMapper) {
        this.equipementRepository = equipementRepository;
        this.equipementMapper = equipementMapper;
    }

    @Override
    public EquipementDTO save(EquipementDTO equipementDTO) {
        LOG.debug("Request to save Equipement : {}", equipementDTO);
        Equipement equipement = equipementMapper.toEntity(equipementDTO);
        equipement = equipementRepository.save(equipement);
        return equipementMapper.toDto(equipement);
    }

    @Override
    public EquipementDTO update(EquipementDTO equipementDTO) {
        LOG.debug("Request to update Equipement : {}", equipementDTO);
        Equipement equipement = equipementMapper.toEntity(equipementDTO);
        equipement = equipementRepository.save(equipement);
        return equipementMapper.toDto(equipement);
    }

    @Override
    public Optional<EquipementDTO> partialUpdate(EquipementDTO equipementDTO) {
        LOG.debug("Request to partially update Equipement : {}", equipementDTO);

        return equipementRepository
            .findById(equipementDTO.getId())
            .map(existingEquipement -> {
                equipementMapper.partialUpdate(existingEquipement, equipementDTO);

                return existingEquipement;
            })
            .map(equipementRepository::save)
            .map(equipementMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipementDTO> findAll() {
        LOG.debug("Request to get all Equipements");
        return equipementRepository.findAll().stream().map(equipementMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquipementDTO> findOne(Long id) {
        LOG.debug("Request to get Equipement : {}", id);
        return equipementRepository.findById(id).map(equipementMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Equipement : {}", id);
        equipementRepository.deleteById(id);
    }
}
