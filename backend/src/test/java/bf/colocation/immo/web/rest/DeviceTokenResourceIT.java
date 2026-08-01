package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import static bf.colocation.immo.domain.DeviceTokenAsserts.*;
import static bf.colocation.immo.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.DeviceToken;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.repository.DeviceTokenRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.service.DeviceTokenService;
import bf.colocation.immo.service.dto.DeviceTokenDTO;
import bf.colocation.immo.service.mapper.DeviceTokenMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
 * Integration tests for the {@link DeviceTokenResource} REST controller.
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
class DeviceTokenResourceIT {

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    private static final String DEFAULT_PLATEFORME = "AAAAAAAAAA";
    private static final String UPDATED_PLATEFORME = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE_CREATION = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE_CREATION = Instant.ofEpochMilli(1702714037224L);

    private static final String ENTITY_API_URL = "/api/device-tokens";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private DeviceTokenRepository deviceTokenRepositoryMock;

    @Autowired
    private DeviceTokenMapper deviceTokenMapper;

    @Mock
    private DeviceTokenService deviceTokenServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDeviceTokenMockMvc;

    private DeviceToken deviceToken;

    private DeviceToken insertedDeviceToken;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeviceToken createEntity(EntityManager em) {
        DeviceToken deviceToken = new DeviceToken().token(DEFAULT_TOKEN).plateforme(DEFAULT_PLATEFORME).dateCreation(DEFAULT_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        deviceToken.setUtilisateur(user);
        return deviceToken;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DeviceToken createUpdatedEntity(EntityManager em) {
        DeviceToken updatedDeviceToken = new DeviceToken()
            .token(UPDATED_TOKEN)
            .plateforme(UPDATED_PLATEFORME)
            .dateCreation(UPDATED_DATE_CREATION);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedDeviceToken.setUtilisateur(user);
        return updatedDeviceToken;
    }

    @BeforeEach
    void initTest() {
        deviceToken = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedDeviceToken != null) {
            deviceTokenRepository.delete(insertedDeviceToken);
            insertedDeviceToken = null;
        }
    }

    @Test
    @Transactional
    void createDeviceToken() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DeviceToken
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);
        var returnedDeviceTokenDTO = om.readValue(
            restDeviceTokenMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceTokenDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DeviceTokenDTO.class
        );

        // Validate the DeviceToken in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDeviceToken = deviceTokenMapper.toEntity(returnedDeviceTokenDTO);
        assertDeviceTokenUpdatableFieldsEquals(returnedDeviceToken, getPersistedDeviceToken(returnedDeviceToken));

        insertedDeviceToken = returnedDeviceToken;
    }

    @Test
    @Transactional
    void createDeviceTokenWithExistingId() throws Exception {
        // Create the DeviceToken with an existing ID
        deviceToken.setId(1L);
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeviceTokenMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceTokenDTO)))
            .andExpect(status().isBadRequest());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTokenIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deviceToken.setToken(null);

        // Create the DeviceToken, which fails.
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        restDeviceTokenMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceTokenDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPlateformeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deviceToken.setPlateforme(null);

        // Create the DeviceToken, which fails.
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        restDeviceTokenMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceTokenDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDeviceTokens() throws Exception {
        // Initialize the database
        insertedDeviceToken = deviceTokenRepository.saveAndFlush(deviceToken);

        // Get all the deviceTokenList
        restDeviceTokenMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deviceToken.getId().intValue())))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)))
            .andExpect(jsonPath("$.[*].plateforme").value(hasItem(DEFAULT_PLATEFORME)))
            .andExpect(jsonPath("$.[*].dateCreation").value(hasItem(DEFAULT_DATE_CREATION.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDeviceTokensWithEagerRelationshipsIsEnabled() throws Exception {
        when(deviceTokenServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDeviceTokenMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(deviceTokenServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDeviceTokensWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(deviceTokenServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDeviceTokenMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(deviceTokenRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getDeviceToken() throws Exception {
        // Initialize the database
        insertedDeviceToken = deviceTokenRepository.saveAndFlush(deviceToken);

        // Get the deviceToken
        restDeviceTokenMockMvc
            .perform(get(ENTITY_API_URL_ID, deviceToken.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(deviceToken.getId().intValue()))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN))
            .andExpect(jsonPath("$.plateforme").value(DEFAULT_PLATEFORME))
            .andExpect(jsonPath("$.dateCreation").value(DEFAULT_DATE_CREATION.toString()));
    }

    @Test
    @Transactional
    void getNonExistingDeviceToken() throws Exception {
        // Get the deviceToken
        restDeviceTokenMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDeviceToken() throws Exception {
        // Initialize the database
        insertedDeviceToken = deviceTokenRepository.saveAndFlush(deviceToken);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deviceToken
        DeviceToken updatedDeviceToken = deviceTokenRepository.findById(deviceToken.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDeviceToken are not directly saved in db
        em.detach(updatedDeviceToken);
        updatedDeviceToken.token(UPDATED_TOKEN).plateforme(UPDATED_PLATEFORME).dateCreation(UPDATED_DATE_CREATION);
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(updatedDeviceToken);

        restDeviceTokenMockMvc
            .perform(
                put(ENTITY_API_URL_ID, deviceTokenDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(deviceTokenDTO))
            )
            .andExpect(status().isOk());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDeviceTokenToMatchAllProperties(updatedDeviceToken);
    }

    @Test
    @Transactional
    void putNonExistingDeviceToken() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deviceToken.setId(longCount.incrementAndGet());

        // Create the DeviceToken
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDeviceTokenMockMvc
            .perform(
                put(ENTITY_API_URL_ID, deviceTokenDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(deviceTokenDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDeviceToken() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deviceToken.setId(longCount.incrementAndGet());

        // Create the DeviceToken
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceTokenMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(deviceTokenDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDeviceToken() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deviceToken.setId(longCount.incrementAndGet());

        // Create the DeviceToken
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceTokenMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceTokenDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDeviceTokenWithPatch() throws Exception {
        // Initialize the database
        insertedDeviceToken = deviceTokenRepository.saveAndFlush(deviceToken);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deviceToken using partial update
        DeviceToken partialUpdatedDeviceToken = new DeviceToken();
        partialUpdatedDeviceToken.setId(deviceToken.getId());

        partialUpdatedDeviceToken.token(UPDATED_TOKEN).plateforme(UPDATED_PLATEFORME).dateCreation(UPDATED_DATE_CREATION);

        restDeviceTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDeviceToken.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDeviceToken))
            )
            .andExpect(status().isOk());

        // Validate the DeviceToken in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeviceTokenUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDeviceToken, deviceToken),
            getPersistedDeviceToken(deviceToken)
        );
    }

    @Test
    @Transactional
    void fullUpdateDeviceTokenWithPatch() throws Exception {
        // Initialize the database
        insertedDeviceToken = deviceTokenRepository.saveAndFlush(deviceToken);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deviceToken using partial update
        DeviceToken partialUpdatedDeviceToken = new DeviceToken();
        partialUpdatedDeviceToken.setId(deviceToken.getId());

        partialUpdatedDeviceToken.token(UPDATED_TOKEN).plateforme(UPDATED_PLATEFORME).dateCreation(UPDATED_DATE_CREATION);

        restDeviceTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDeviceToken.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDeviceToken))
            )
            .andExpect(status().isOk());

        // Validate the DeviceToken in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeviceTokenUpdatableFieldsEquals(partialUpdatedDeviceToken, getPersistedDeviceToken(partialUpdatedDeviceToken));
    }

    @Test
    @Transactional
    void patchNonExistingDeviceToken() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deviceToken.setId(longCount.incrementAndGet());

        // Create the DeviceToken
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDeviceTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, deviceTokenDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(deviceTokenDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDeviceToken() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deviceToken.setId(longCount.incrementAndGet());

        // Create the DeviceToken
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(deviceTokenDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDeviceToken() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deviceToken.setId(longCount.incrementAndGet());

        // Create the DeviceToken
        DeviceTokenDTO deviceTokenDTO = deviceTokenMapper.toDto(deviceToken);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceTokenMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(deviceTokenDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DeviceToken in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDeviceToken() throws Exception {
        // Initialize the database
        insertedDeviceToken = deviceTokenRepository.saveAndFlush(deviceToken);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the deviceToken
        restDeviceTokenMockMvc
            .perform(delete(ENTITY_API_URL_ID, deviceToken.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return deviceTokenRepository.count();
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

    protected DeviceToken getPersistedDeviceToken(DeviceToken deviceToken) {
        return deviceTokenRepository.findById(deviceToken.getId()).orElseThrow();
    }

    protected void assertPersistedDeviceTokenToMatchAllProperties(DeviceToken expectedDeviceToken) {
        assertDeviceTokenAllPropertiesEquals(expectedDeviceToken, getPersistedDeviceToken(expectedDeviceToken));
    }

    protected void assertPersistedDeviceTokenToMatchUpdatableProperties(DeviceToken expectedDeviceToken) {
        assertDeviceTokenAllUpdatablePropertiesEquals(expectedDeviceToken, getPersistedDeviceToken(expectedDeviceToken));
    }
}
