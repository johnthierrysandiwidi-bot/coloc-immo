package bf.colocation.immo.data.remote

import bf.colocation.immo.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/** Endpoints du backend Spring Boot (JHipster) consommés par l'app. */
interface ApiService {

    // ---- Authentification ----
    @POST("api/authenticate")
    suspend fun login(@Body body: LoginRequest): JwtTokenDto

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): JwtTokenDto

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @GET("api/account")
    suspend fun account(): AccountDto

    // ---- Annonces ----
    @GET("api/annonces")
    suspend fun annonces(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "datePublication,desc",
        @Query("titre.contains") titre: String? = null,
        @Query("type.equals") type: String? = null,
        @Query("statut.equals") statut: String? = "PUBLIEE"
    ): Response<List<AnnonceDto>>

    @GET("api/annonces/{id}")
    suspend fun annonce(@Path("id") id: Long): AnnonceDto

    // ---- Favoris ----
    @GET("api/favoris")
    suspend fun favoris(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "dateAjout,desc"
    ): Response<List<FavoriDto>>

    @POST("api/favoris")
    suspend fun ajouterFavori(@Body body: FavoriRequest): FavoriDto

    @DELETE("api/favoris/{id}")
    suspend fun supprimerFavori(@Path("id") id: Long): Response<Unit>

    // ---- Notifications ----
    @GET("api/notifications")
    suspend fun notifications(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "dateCreation,desc"
    ): Response<List<NotificationDto>>

    // ---- Rendez-vous / workflow ----
    @POST("api/rendez-vous/demander")
    suspend fun demanderVisite(@Body body: DemandeVisiteRequest): RendezVousDto

    @GET("api/rendez-vous")
    suspend fun mesRendezVous(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "dateHeure,desc"
    ): Response<List<RendezVousDto>>

    /** Clôture d'une visite réalisée : confirmée par le locataire, elle libère les frais. */
    @PATCH("api/rendez-vous/{id}/terminer")
    suspend fun terminerRendezVous(@Path("id") id: Long): RendezVousDto

    /** Dépose un avis sur le démarcheur, à l'issue d'une visite effectuée. */
    @POST("api/rendez-vous/{id}/avis")
    suspend fun deposerAvis(@Path("id") id: Long, @Body body: AvisRequest): AvisDto

    @PATCH("api/rendez-vous/{id}/annuler")
    suspend fun annulerRendezVous(@Path("id") id: Long, @Body body: MotifRequest): RendezVousDto

    // ---- Paiement des frais de visite ----

    /** Montant des frais (RG23). Le backend renvoie un nombre brut, pas un objet. */
    @GET("api/paiements/frais-de-visite")
    suspend fun fraisDeVisite(): Double

    /** Crée le paiement du rendez-vous, ou retourne celui qui existe déjà. */
    @POST("api/paiements/rendez-vous/{rendezVousId}/initier")
    suspend fun initierPaiement(@Path("rendezVousId") rendezVousId: Long): PaiementDto

    /** Règlement via la passerelle simulée ; « moyen » est un paramètre de requête. */
    @POST("api/paiements/{id}/simuler-reglement")
    suspend fun simulerReglement(
        @Path("id") id: Long,
        @Query("moyen") moyen: String
    ): PaiementDto

    /** Paiement associé à un rendez-vous. Renvoie 204 (corps vide) s'il n'y en a pas. */
    @GET("api/paiements/rendez-vous/{rendezVousId}")
    suspend fun paiementDuRendezVous(@Path("rendezVousId") rendezVousId: Long): Response<PaiementDto>

    // ---- Publication d'annonce ----

    /** Mes annonces, tous statuts confondus. */
    @GET("api/annonces")
    suspend fun mesAnnonces(
        @Query("auteurId.equals") auteurId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sort") sort: String = "id,desc"
    ): Response<List<AnnonceDto>>

    @POST("api/immobiliers")
    suspend fun creerImmobilier(@Body body: ImmobilierRequest): ImmobilierDto

    @GET("api/immobiliers")
    suspend fun mesBiens(
        @Query("proprietaireId.equals") proprietaireId: Long,
        @Query("size") size: Int = 100
    ): Response<List<ImmobilierDto>>

    @POST("api/annonces")
    suspend fun creerAnnonce(@Body body: AnnonceRequest): AnnonceDto

    @PATCH("api/annonces/{id}/publier")
    suspend fun publierAnnonce(@Path("id") id: Long): AnnonceDto

    // ---- Référentiel (villes, quartiers, types de bien) ----

    @GET("api/localites")
    suspend fun localites(@Query("size") size: Int = 200): Response<List<LocaliteDto>>

    @GET("api/quartiers")
    suspend fun quartiers(@Query("size") size: Int = 500): Response<List<QuartierDto>>

    @GET("api/type-immobiliers")
    suspend fun typesImmobilier(@Query("size") size: Int = 100): Response<List<TypeImmobilierDto>>

    // ---- Alertes de recherche ----

    @GET("api/alertes")
    suspend fun alertes(
        @Query("size") size: Int = 100,
        @Query("sort") sort: String = "id,desc"
    ): Response<List<AlerteDto>>

    @POST("api/alertes")
    suspend fun creerAlerte(@Body body: AlerteRequest): AlerteDto

    @DELETE("api/alertes/{id}")
    suspend fun supprimerAlerte(@Path("id") id: Long): Response<Unit>

    // ---- Push ----
    @POST("api/device-tokens")
    suspend fun enregistrerDevice(@Body body: DeviceTokenRequest): Response<Unit>

    // ---- Messagerie ----
    @GET("api/conversations")
    suspend fun conversations(): List<ConversationDto>

    @POST("api/conversations/pour-annonce/{annonceId}")
    suspend fun ouvrirConversation(@Path("annonceId") annonceId: Long): ConversationDto

    @GET("api/conversations/{id}/messages")
    suspend fun messages(@Path("id") id: Long): List<MessageDto>

    @POST("api/conversations/{id}/messages")
    suspend fun envoyerMessage(@Path("id") id: Long, @Body body: EnvoiMessageRequest): MessageDto

    @GET("api/messages/non-lus")
    suspend fun messagesNonLus(): Map<String, Long>

}
