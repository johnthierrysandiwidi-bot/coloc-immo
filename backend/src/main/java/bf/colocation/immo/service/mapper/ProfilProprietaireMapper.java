package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.ProfilProprietaire;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.ProfilProprietaireDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ProfilProprietaire} and its DTO {@link ProfilProprietaireDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProfilProprietaireMapper extends EntityMapper<ProfilProprietaireDTO, ProfilProprietaire> {
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "userLogin")
    ProfilProprietaireDTO toDto(ProfilProprietaire s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
