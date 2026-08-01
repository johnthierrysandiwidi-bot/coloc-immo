package bf.colocation.immo.service.impl;

import bf.colocation.immo.domain.Image;
import bf.colocation.immo.repository.ImageRepository;
import bf.colocation.immo.service.ImageService;
import bf.colocation.immo.service.dto.ImageDTO;
import bf.colocation.immo.service.mapper.ImageMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link bf.colocation.immo.domain.Image}.
 */
@Service
@Transactional
public class ImageServiceImpl implements ImageService {

    private static final Logger LOG = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageRepository imageRepository;

    private final ImageMapper imageMapper;

    public ImageServiceImpl(ImageRepository imageRepository, ImageMapper imageMapper) {
        this.imageRepository = imageRepository;
        this.imageMapper = imageMapper;
    }

    @Override
    public ImageDTO save(ImageDTO imageDTO) {
        LOG.debug("Request to save Image : {}", imageDTO);
        Image image = imageMapper.toEntity(imageDTO);
        image = imageRepository.save(image);
        return imageMapper.toDto(image);
    }

    @Override
    public ImageDTO update(ImageDTO imageDTO) {
        LOG.debug("Request to update Image : {}", imageDTO);
        Image image = imageMapper.toEntity(imageDTO);
        image = imageRepository.save(image);
        return imageMapper.toDto(image);
    }

    @Override
    public Optional<ImageDTO> partialUpdate(ImageDTO imageDTO) {
        LOG.debug("Request to partially update Image : {}", imageDTO);

        return imageRepository
            .findById(imageDTO.getId())
            .map(existingImage -> {
                imageMapper.partialUpdate(existingImage, imageDTO);

                return existingImage;
            })
            .map(imageRepository::save)
            .map(imageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImageDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Images");
        return imageRepository.findAll(pageable).map(imageMapper::toDto);
    }

    public Page<ImageDTO> findAllWithEagerRelationships(Pageable pageable) {
        return imageRepository.findAllWithEagerRelationships(pageable).map(imageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImageDTO> findOne(Long id) {
        LOG.debug("Request to get Image : {}", id);
        return imageRepository.findOneWithEagerRelationships(id).map(imageMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Image : {}", id);
        imageRepository.deleteById(id);
    }
}
