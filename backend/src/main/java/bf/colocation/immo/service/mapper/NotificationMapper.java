package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Notification;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.NotificationDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Notification} and its DTO {@link NotificationDTO}.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {
    @Mapping(target = "destinataire", source = "destinataire", qualifiedByName = "userLogin")
    NotificationDTO toDto(Notification s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
