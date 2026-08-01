package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.VueAnnonce;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.dto.UserDTO;
import bf.colocation.immo.service.dto.VueAnnonceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link VueAnnonce} and its DTO {@link VueAnnonceDTO}.
 */
@Mapper(componentModel = "spring")
public interface VueAnnonceMapper extends EntityMapper<VueAnnonceDTO, VueAnnonce> {
    @Mapping(target = "annonce", source = "annonce", qualifiedByName = "annonceTitre")
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "userLogin")
    VueAnnonceDTO toDto(VueAnnonce s);

    @Named("annonceTitre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titre", source = "titre")
    AnnonceDTO toDtoAnnonceTitre(Annonce annonce);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
