# Lancer les tests du backend

## Pourquoi les tests échouaient « avant »

Les erreurs du type `cannot find symbol : method toEntity(...)` ne venaient **pas**
des tests eux-mêmes. Elles apparaissaient quand la compilation du code principal
(`src/main`) échouait : dans ce cas, MapStruct ne génère pas les classes de mappers,
et tous les tests qui s'en servent tombent en cascade.

Autrement dit : dès que `src/main` compile, ces erreurs disparaissent d'elles-mêmes.
La cause profonde était l'erreur d'imports dans `UserRepository`, désormais corrigée.

## Les commandes

### Juste lancer l'application (pas de tests)
```
mvnw
```
`mvnw` seul démarre le serveur sans compiler ni exécuter les tests. C'est la commande
à utiliser au quotidien.

### Compiler sans tester
```
mvnw clean compile
```
Vérifie que le code principal compile. Ne touche pas aux tests.

### Produire le .jar sans tests
```
mvnw clean package -DskipTests
```

### Lancer les tests
```
mvnw test
```
Compile et exécute l'ensemble des tests d'intégration. Nécessite Docker : les tests
utilisent Testcontainers, qui démarre une vraie base MySQL le temps du test.

### Lancer un seul test
```
mvnw test -Dtest=CloisonnementIT
```

## Le test de cloisonnement

`CloisonnementIT` est le test le plus important du projet : il démontre que la
correction de sécurité fonctionne. Il monte deux utilisateurs, Alice et Bob, chacun
avec son favori, et vérifie quatre choses :

1. La liste d'Alice ne contient que ses favoris, jamais ceux de Bob.
2. Alice ne peut pas accéder au favori de Bob par son identifiant (403 refusé).
3. Alice accède bien à son propre favori (200).
4. Sans authentification, la ressource est fermée (401).

Le favori sert de cas représentatif : les seize ressources sensibles partagent le
même service d'autorisation, donc la même protection.

## Note sur les tests générés

Les tests d'intégration générés automatiquement par JHipster vérifient surtout les
opérations CRUD, sous une identité administrateur (`@WithMockUser` ADMIN). Ils ne
couvrent pas le cloisonnement entre utilisateurs — c'est précisément le rôle de
`CloisonnementIT`, écrit spécialement pour cela.
