package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.ImageAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Image;
import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.repository.ImageRepository;
import bf.colocation.immo.service.ImageService;
import bf.colocation.immo.service.dto.ImageDTO;
import bf.colocation.immo.service.mapper.ImageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ImageResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
// Les listes et les accès par identifiant sont désormais restreints au
// titulaire des données. Ces tests vérifient la mécanique CRUD, non les
// autorisations : ils s'exécutent donc en administrateur, rôle qui conserve
// un accès complet par conception. Le cloisonnement lui-même est à couvrir
// par des tests dédiés (voir AutorisationServiceIT à écrire).
class ImageResourceIT {

    private static final String DEFAULT_NOM = "AAAAAAAAAA";
    private static final String UPDATED_NOM = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final Integer DEFAULT_ORDRE = 0;
    private static final Integer UPDATED_ORDRE = 1;

    private static final Boolean DEFAULT_PRINCIPALE = false;
    private static final Boolean UPDATED_PRINCIPALE = true;

    private static final String ENTITY_API_URL = "/api/images";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ImageRepository imageRepository;

    @Mock
    private ImageRepository imageRepositoryMock;

    @Autowired
    private ImageMapper imageMapper;

    @Mock
    private ImageService imageServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restImageMockMvc;

    private Image image;

    private Image insertedImage;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Image createEntity(EntityManager em) {
        Image image = new Image().nom(DEFAULT_NOM).url(DEFAULT_URL).ordre(DEFAULT_ORDRE).principale(DEFAULT_PRINCIPALE);
        // Add required entity
        Immobilier immobilier;
        if (TestUtil.findAll(em, Immobilier.class).isEmpty()) {
            immobilier = ImmobilierResourceIT.createEntity(em);
            em.persist(immobilier);
            em.flush();
        } else {
            immobilier = TestUtil.findAll(em, Immobilier.class).get(0);
        }
        image.setImmobilier(immobilier);
        return image;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Image createUpdatedEntity(EntityManager em) {
        Image updatedImage = new Image().nom(UPDATED_NOM).url(UPDATED_URL).ordre(UPDATED_ORDRE).principale(UPDATED_PRINCIPALE);
        // Add required entity
        Immobilier immobilier;
        if (TestUtil.findAll(em, Immobilier.class).isEmpty()) {
            immobilier = ImmobilierResourceIT.createUpdatedEntity(em);
            em.persist(immobilier);
            em.flush();
        } else {
            immobilier = TestUtil.findAll(em, Immobilier.class).get(0);
        }
        updatedImage.setImmobilier(immobilier);
        return updatedImage;
    }

    @BeforeEach
    void initTest() {
        image = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedImage != null) {
            imageRepository.delete(insertedImage);
            insertedImage = null;
        }
    }

    @Test
    @Transactional
    void createImage() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Image
        ImageDTO imageDTO = imageMapper.toDto(image);
        var returnedImageDTO = om.readValue(
            restImageMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(imageDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ImageDTO.class
        );

        // Validate the Image in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedImage = imageMapper.toEntity(returnedImageDTO);
        assertImageUpdatableFieldsEquals(returnedImage, getPersistedImage(returnedImage));

        insertedImage = returnedImage;
    }

    @Test
    @Transactional
    void createImageWithExistingId() throws Exception {
        // Create the Image with an existing ID
        image.setId(1L);
        ImageDTO imageDTO = imageMapper.toDto(image);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restImageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(imageDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUrlIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        image.setUrl(null);

        // Create the Image, which fails.
        ImageDTO imageDTO = imageMapper.toDto(image);

        restImageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(imageDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllImages() throws Exception {
        // Initialize the database
        insertedImage = imageRepository.saveAndFlush(image);

        // Get all the imageList
        restImageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(image.getId().intValue())))
            .andExpect(jsonPath("$.[*].nom").value(hasItem(DEFAULT_NOM)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)))
            .andExpect(jsonPath("$.[*].ordre").value(hasItem(DEFAULT_ORDRE)))
            .andExpect(jsonPath("$.[*].principale").value(hasItem(DEFAULT_PRINCIPALE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllImagesWithEagerRelationshipsIsEnabled() throws Exception {
        when(imageServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restImageMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(imageServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllImagesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(imageServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restImageMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(imageRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getImage() throws Exception {
        // Initialize the database
        insertedImage = imageRepository.saveAndFlush(image);

        // Get the image
        restImageMockMvc
            .perform(get(ENTITY_API_URL_ID, image.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(image.getId().intValue()))
            .andExpect(jsonPath("$.nom").value(DEFAULT_NOM))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL))
            .andExpect(jsonPath("$.ordre").value(DEFAULT_ORDRE))
            .andExpect(jsonPath("$.principale").value(DEFAULT_PRINCIPALE));
    }

    @Test
    @Transactional
    void getNonExistingImage() throws Exception {
        // Get the image
        restImageMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingImage() throws Exception {
        // Initialize the database
        insertedImage = imageRepository.saveAndFlush(image);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the image
        Image updatedImage = imageRepository.findById(image.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedImage are not directly saved in db
        em.detach(updatedImage);
        updatedImage.nom(UPDATED_NOM).url(UPDATED_URL).ordre(UPDATED_ORDRE).principale(UPDATED_PRINCIPALE);
        ImageDTO imageDTO = imageMapper.toDto(updatedImage);

        restImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, imageDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(imageDTO))
            )
            .andExpect(status().isOk());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedImageToMatchAllProperties(updatedImage);
    }

    @Test
    @Transactional
    void putNonExistingImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        image.setId(longCount.incrementAndGet());

        // Create the Image
        ImageDTO imageDTO = imageMapper.toDto(image);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, imageDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(imageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        image.setId(longCount.incrementAndGet());

        // Create the Image
        ImageDTO imageDTO = imageMapper.toDto(image);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(imageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        image.setId(longCount.incrementAndGet());

        // Create the Image
        ImageDTO imageDTO = imageMapper.toDto(image);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImageMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(imageDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateImageWithPatch() throws Exception {
        // Initialize the database
        insertedImage = imageRepository.saveAndFlush(image);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the image using partial update
        Image partialUpdatedImage = new Image();
        partialUpdatedImage.setId(image.getId());

        partialUpdatedImage.nom(UPDATED_NOM).ordre(UPDATED_ORDRE).principale(UPDATED_PRINCIPALE);

        restImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedImage.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedImage))
            )
            .andExpect(status().isOk());

        // Validate the Image in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertImageUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedImage, image), getPersistedImage(image));
    }

    @Test
    @Transactional
    void fullUpdateImageWithPatch() throws Exception {
        // Initialize the database
        insertedImage = imageRepository.saveAndFlush(image);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the image using partial update
        Image partialUpdatedImage = new Image();
        partialUpdatedImage.setId(image.getId());

        partialUpdatedImage.nom(UPDATED_NOM).url(UPDATED_URL).ordre(UPDATED_ORDRE).principale(UPDATED_PRINCIPALE);

        restImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedImage.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedImage))
            )
            .andExpect(status().isOk());

        // Validate the Image in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertImageUpdatableFieldsEquals(partialUpdatedImage, getPersistedImage(partialUpdatedImage));
    }

    @Test
    @Transactional
    void patchNonExistingImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        image.setId(longCount.incrementAndGet());

        // Create the Image
        ImageDTO imageDTO = imageMapper.toDto(image);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, imageDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(imageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        image.setId(longCount.incrementAndGet());

        // Create the Image
        ImageDTO imageDTO = imageMapper.toDto(image);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(imageDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamImage() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        image.setId(longCount.incrementAndGet());

        // Create the Image
        ImageDTO imageDTO = imageMapper.toDto(image);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restImageMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(imageDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Image in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteImage() throws Exception {
        // Initialize the database
        insertedImage = imageRepository.saveAndFlush(image);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the image
        restImageMockMvc
            .perform(delete(ENTITY_API_URL_ID, image.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return imageRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Image getPersistedImage(Image image) {
        return imageRepository.findById(image.getId()).orElseThrow();
    }

    protected void assertPersistedImageToMatchAllProperties(Image expectedImage) {
        assertImageAllPropertiesEquals(expectedImage, getPersistedImage(expectedImage));
    }

    protected void assertPersistedImageToMatchUpdatableProperties(Image expectedImage) {
        assertImageAllUpdatablePropertiesEquals(expectedImage, getPersistedImage(expectedImage));
    }
}
