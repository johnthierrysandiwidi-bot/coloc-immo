package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Document;
import bf.colocation.immo.domain.TypeDocument;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.DocumentDTO;
import bf.colocation.immo.service.dto.TypeDocumentDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Document} and its DTO {@link DocumentDTO}.
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper extends EntityMapper<DocumentDTO, Document> {
    @Mapping(target = "typeDocument", source = "typeDocument", qualifiedByName = "typeDocumentNom")
    @Mapping(target = "demarcheur", source = "demarcheur", qualifiedByName = "userLogin")
    @Mapping(target = "traitePar", source = "traitePar", qualifiedByName = "userLogin")
    DocumentDTO toDto(Document s);

    @Named("typeDocumentNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    TypeDocumentDTO toDtoTypeDocumentNom(TypeDocument typeDocument);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
