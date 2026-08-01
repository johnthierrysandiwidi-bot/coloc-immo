package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Favori;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.dto.FavoriDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Favori} and its DTO {@link FavoriDTO}.
 */
@Mapper(componentModel = "spring", uses = { AnnonceMapper.class })
public interface FavoriMapper extends EntityMapper<FavoriDTO, Favori> {
    // Annonce complète : la page « Mes favoris » affichait un titre sans prix ni photo.
    @Mapping(target = "annonce", source = "annonce")
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "favoriUtilisateurLogin")
    FavoriDTO toDto(Favori s);


    @Named("favoriUtilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
