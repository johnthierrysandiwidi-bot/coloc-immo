package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Image;
import bf.colocation.immo.repository.ImageRepository;
import bf.colocation.immo.service.dto.AnnonceDTO;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rattache leurs photos aux annonces.
 *
 * JHipster ne mappe pas les relations « un-à-plusieurs » inverses : ImmobilierDTO
 * ne porte aucune liste d'images. Sans ce service, les photos existent en base
 * mais ne sortent jamais de l'API, et le front affiche des cartes grises.
 *
 * Le chargement est groupé : une requête pour toute la page, pas une par annonce.
 */
@Service
@Transactional(readOnly = true)
public class PhotoAnnonceService {

    private final ImageRepository imageRepository;

    public PhotoAnnonceService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void enrichir(Collection<AnnonceDTO> annonces) {
        if (annonces == null || annonces.isEmpty()) {
            return;
        }

        Set<Long> idsBiens = annonces
            .stream()
            .filter(a -> a.getImmobilier() != null && a.getImmobilier().getId() != null)
            .map(a -> a.getImmobilier().getId())
            .collect(Collectors.toSet());

        if (idsBiens.isEmpty()) {
            return;
        }

        Map<Long, List<Image>> parBien = imageRepository
            .findByImmobilierIdInOrderByOrdreAsc(idsBiens)
            .stream()
            .filter(i -> i.getImmobilier() != null)
            .collect(Collectors.groupingBy(i -> i.getImmobilier().getId()));

        for (AnnonceDTO annonce : annonces) {
            if (annonce.getImmobilier() == null || annonce.getImmobilier().getId() == null) {
                continue;
            }
            List<Image> images = parBien.getOrDefault(annonce.getImmobilier().getId(), List.of());
            if (images.isEmpty()) {
                continue;
            }

            // La photo principale d'abord ; à défaut, la première dans l'ordre.
            List<Image> triees = images
                .stream()
                .sorted(Comparator.comparing((Image i) -> !Boolean.TRUE.equals(i.getPrincipale())))
                .toList();

            annonce.setPhotos(triees.stream().map(Image::getUrl).filter(u -> u != null).toList());
            annonce.setPhotoUrl(annonce.getPhotos().isEmpty() ? null : annonce.getPhotos().get(0));
        }
    }

    public void enrichir(AnnonceDTO annonce) {
        if (annonce != null) {
            enrichir(List.of(annonce));
        }
    }

    /**
     * Pour les DTO qui embarquent une annonce (favori, rendez-vous) :
     * on extrait les annonces imbriquées et on les enrichit d'un coup.
     */
    public <T> void enrichirImbriquees(Collection<T> porteurs, java.util.function.Function<T, AnnonceDTO> extraire) {
        if (porteurs == null || porteurs.isEmpty()) {
            return;
        }
        List<AnnonceDTO> annonces = porteurs
            .stream()
            .map(extraire)
            .filter(a -> a != null)
            .toList();
        enrichir(annonces);
    }
}
