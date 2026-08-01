package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.DeviceToken;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.DeviceTokenDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DeviceToken} and its DTO {@link DeviceTokenDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeviceTokenMapper extends EntityMapper<DeviceTokenDTO, DeviceToken> {
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "userLogin")
    DeviceTokenDTO toDto(DeviceToken s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
