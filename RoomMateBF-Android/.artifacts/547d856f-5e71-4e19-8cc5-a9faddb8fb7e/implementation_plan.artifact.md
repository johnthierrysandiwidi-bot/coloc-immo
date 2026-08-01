# Fix compilation errors in RoomMateBF

Several compilation errors have been identified across repositories and UI screens. These range from missing imports and syntax errors to API signature mismatches.

## Proposed Changes

### Data Layer

#### [MODIFY] [AlerteRepository.kt](file:///C:/laragon/www/colocation-immo-complet/RoomMateBF-Android/app/src/main/java/bf/colocation/immo/data/repository/AlerteRepository.kt)
- Remove unused UI-related imports (`Intent`, `Uri`, `clickable`, `Icons`, `Phone`, `LocalContext`).
- Investigate why `api.alertes()` and other methods are reported as unresolved or returning `String`. I will try to explicitly specify the return type or use `apiService` instead of `api` if there's shadowing.

#### [MODIFY] [PublicationRepository.kt](file:///C:/laragon/www/colocation-immo-complet/RoomMateBF-Android/app/src/main/java/bf/colocation/immo/data/repository/PublicationRepository.kt)
- Fix calls to `api.mesBiens()` and `api.mesAnnonces()` by removing the unnecessary parameters (`proprietaireId`, `auteurId`) to match the `ApiService` interface.
- Investigate the `actual 'kotlin.String'` error if it persists.

#### [MODIFY] [ReferentielRepository.kt](file:///C:/laragon/www/colocation-immo-complet/RoomMateBF-Android/app/src/main/java/bf/colocation/immo/data/repository/ReferentielRepository.kt)
- Investigate why `api.localites()` etc. are reported as unresolved.

### UI Layer

#### [MODIFY] [AccueilScreen.kt](file:///C:/laragon/www/colocation-immo-complet/RoomMateBF-Android/app/src/main/java/bf/colocation/immo/ui/accueil/AccueilScreen.kt)
- Add missing imports for `LocalContext`, `clickable`, `Intent`, `Uri`, `Icons.Default.Phone`, and `startActivity` (extension on context).

#### [MODIFY] [AnnonceDetailScreen.kt](file:///C:/laragon/www/colocation-immo-complet/RoomMateBF-Android/app/src/main/java/bf/colocation/immo/ui/annonces/AnnonceDetailScreen.kt)
- Add missing closing brace `}` for `BoutonLocalisation` composable.
- Ensure `BoutonsContact` is properly defined as a top-level private function (or at least not nested incorrectly).

#### [MODIFY] [ServicesScreen.kt](file:///C:/laragon/www/colocation-immo-complet/RoomMateBF-Android/app/src/main/java/bf/colocation/immo/ui/navigation/ServicesScreen.kt)
- Investigate why `Routes.PAIEMENTS` and `Routes.ALERTES` are unresolved despite being present in `Routes.kt`. I will try adding an explicit import or qualifying the reference further.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify all compilation errors are resolved.

### Manual Verification
- N/A (Compilation fixes only).
