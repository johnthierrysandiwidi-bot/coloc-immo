package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.TypeDocument;
import bf.colocation.immo.service.dto.TypeDocumentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TypeDocument} and its DTO {@link TypeDocumentDTO}.
 */
@Mapper(componentModel = "spring")
public interface TypeDocumentMapper extends EntityMapper<TypeDocumentDTO, TypeDocument> {}
