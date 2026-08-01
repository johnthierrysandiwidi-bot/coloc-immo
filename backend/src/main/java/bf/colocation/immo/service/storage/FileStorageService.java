package bf.colocation.immo.service.storage;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Stocke images et documents sur le système de fichiers, jamais en base (ENF-08).
 * Nom aléatoire (UUID), type MIME vérifié sur le contenu réel et non sur l'extension.
 */
@Service
public class FileStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageService.class);

    private static final List<String> MIME_IMAGES = List.of("image/jpeg", "image/png", "image/webp");
    private static final List<String> MIME_DOCUMENTS = List.of("application/pdf", "image/jpeg", "image/png");
    private static final long TAILLE_MAX = 5L * 1024 * 1024; // 5 Mo

    private final Tika tika = new Tika();

    @Value("${application.storage.location:./uploads}")
    private String racine;

    private Path racineDir;

    @PostConstruct
    public void init() throws IOException {
        this.racineDir = Paths.get(racine).toAbsolutePath().normalize();
        Files.createDirectories(racineDir.resolve("images"));
        Files.createDirectories(racineDir.resolve("documents"));
        LOG.info("Stockage des fichiers : {}", racineDir);
    }

    public String stockerImage(MultipartFile fichier) {
        return stocker(fichier, "images", MIME_IMAGES);
    }

    public String stockerDocument(MultipartFile fichier) {
        return stocker(fichier, "documents", MIME_DOCUMENTS);
    }

    private String stocker(MultipartFile fichier, String sousDossier, List<String> mimesAutorises) {
        if (fichier == null || fichier.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier vide");
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Fichier supérieur à 5 Mo");
        }

        String mime;
        try (InputStream in = fichier.getInputStream()) {
            mime = tika.detect(in); // détection sur le contenu réel : une extension mensongère ne passe pas
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier illisible", e);
        }
        if (!mimesAutorises.contains(mime.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Type de fichier refusé : " + mime);
        }

        String nom = UUID.randomUUID() + extensionPour(mime);
        Path cible = racineDir.resolve(sousDossier).resolve(nom).normalize();
        if (!cible.startsWith(racineDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chemin invalide");
        }

        try (InputStream in = fichier.getInputStream()) {
            Files.copy(in, cible, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec de l'enregistrement", e);
        }

        return "/api/files/" + sousDossier + "/" + nom;
    }

    public Resource charger(String sousDossier, String nomFichier) {
        Path chemin = racineDir.resolve(sousDossier).resolve(nomFichier).normalize();
        if (!chemin.startsWith(racineDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chemin invalide");
        }
        try {
            Resource resource = new UrlResource(chemin.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
            }
            return resource;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable", e);
        }
    }

    private String extensionPour(String mime) {
        return switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}
