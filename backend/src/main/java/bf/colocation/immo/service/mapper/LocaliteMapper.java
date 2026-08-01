package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.service.dto.LocaliteDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Localite} and its DTO {@link LocaliteDTO}.
 */
@Mapper(componentModel = "spring")
public interface LocaliteMapper extends EntityMapper<LocaliteDTO, Localite> {}
