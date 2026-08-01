package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.TypeImmobilier;
import bf.colocation.immo.service.dto.TypeImmobilierDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TypeImmobilier} and its DTO {@link TypeImmobilierDTO}.
 */
@Mapper(componentModel = "spring")
public interface TypeImmobilierMapper extends EntityMapper<TypeImmobilierDTO, TypeImmobilier> {}
