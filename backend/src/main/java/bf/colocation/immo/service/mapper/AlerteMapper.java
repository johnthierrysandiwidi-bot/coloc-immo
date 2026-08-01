package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Alerte;
import bf.colocation.immo.domain.Localite;
import bf.colocation.immo.domain.Quartier;
import bf.colocation.immo.domain.TypeImmobilier;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.AlerteDTO;
import bf.colocation.immo.service.dto.LocaliteDTO;
import bf.colocation.immo.service.dto.QuartierDTO;
import bf.colocation.immo.service.dto.TypeImmobilierDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Alerte} and its DTO {@link AlerteDTO}.
 */
@Mapper(componentModel = "spring")
public interface AlerteMapper extends EntityMapper<AlerteDTO, Alerte> {
    @Mapping(target = "titulaire", source = "titulaire", qualifiedByName = "userLogin")
    @Mapping(target = "localite", source = "localite", qualifiedByName = "localiteNom")
    @Mapping(target = "quartier", source = "quartier", qualifiedByName = "quartierNom")
    @Mapping(target = "typeImmobilier", source = "typeImmobilier", qualifiedByName = "typeImmobilierNom")
    AlerteDTO toDto(Alerte s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("localiteNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    LocaliteDTO toDtoLocaliteNom(Localite localite);

    @Named("quartierNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    QuartierDTO toDtoQuartierNom(Quartier quartier);

    @Named("typeImmobilierNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    TypeImmobilierDTO toDtoTypeImmobilierNom(TypeImmobilier typeImmobilier);
}
