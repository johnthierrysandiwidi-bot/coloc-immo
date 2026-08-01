package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.dto.ImmobilierDTO;
import bf.colocation.immo.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Annonce} and its DTO {@link AnnonceDTO}.
 */
@Mapper(componentModel = "spring", uses = { ImmobilierMapper.class })
public interface AnnonceMapper extends EntityMapper<AnnonceDTO, Annonce> {
    // Le bien est mappé ENTIÈREMENT : sans cela le front n'a ni adresse, ni quartier,
    // ni surface, ni nombre de chambres — d'où les « —, — » affichés sur les cartes.
    @Mapping(target = "immobilier", source = "immobilier")
    @Mapping(target = "auteur", source = "auteur", qualifiedByName = "auteurLogin")
    AnnonceDTO toDto(Annonce s);


    @Named("auteurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
