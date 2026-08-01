package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.DetailColocation;
import bf.colocation.immo.domain.Equipement;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.dto.DetailColocationDTO;
import bf.colocation.immo.service.dto.EquipementDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DetailColocation} and its DTO {@link DetailColocationDTO}.
 */
@Mapper(componentModel = "spring")
public interface DetailColocationMapper extends EntityMapper<DetailColocationDTO, DetailColocation> {
    @Mapping(target = "annonce", source = "annonce", qualifiedByName = "annonceTitre")
    @Mapping(target = "equipementses", source = "equipementses", qualifiedByName = "equipementNomSet")
    DetailColocationDTO toDto(DetailColocation s);

    @Mapping(target = "removeEquipements", ignore = true)
    DetailColocation toEntity(DetailColocationDTO detailColocationDTO);

    @Named("annonceTitre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titre", source = "titre")
    AnnonceDTO toDtoAnnonceTitre(Annonce annonce);

    @Named("equipementNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    EquipementDTO toDtoEquipementNom(Equipement equipement);

    @Named("equipementNomSet")
    default Set<EquipementDTO> toDtoEquipementNomSet(Set<Equipement> equipement) {
        return equipement.stream().map(this::toDtoEquipementNom).collect(Collectors.toSet());
    }
}
