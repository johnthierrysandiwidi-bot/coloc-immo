package bf.colocation.immo.service.mapper;

import bf.colocation.immo.domain.DetailColocation;
import bf.colocation.immo.domain.Equipement;
import bf.colocation.immo.service.dto.DetailColocationDTO;
import bf.colocation.immo.service.dto.EquipementDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Equipement} and its DTO {@link EquipementDTO}.
 */
@Mapper(componentModel = "spring")
public interface EquipementMapper extends EntityMapper<EquipementDTO, Equipement> {
    @Mapping(target = "colocationses", source = "colocationses", qualifiedByName = "detailColocationIdSet")
    EquipementDTO toDto(Equipement s);

    @Mapping(target = "colocationses", ignore = true)
    @Mapping(target = "removeColocations", ignore = true)
    Equipement toEntity(EquipementDTO equipementDTO);

    @Named("detailColocationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DetailColocationDTO toDtoDetailColocationId(DetailColocation detailColocation);

    @Named("detailColocationIdSet")
    default Set<DetailColocationDTO> toDtoDetailColocationIdSet(Set<DetailColocation> detailColocation) {
        return detailColocation.stream().map(this::toDtoDetailColocationId).collect(Collectors.toSet());
    }
}
