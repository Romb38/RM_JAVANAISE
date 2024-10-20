# Javanaise

Ceci est notre projet de M2 - Génie Informatique pour la matière Système et Application Répartis (SAR)

# Authors

Romain BARBIER
Mathieu ZUSSY

# Installation et lancement

Pour lancer ce projet une fois installé, il vous faut suivre les étapes suivantes :
- Démmarer le serveur IRC
- Lancer le coordinateur (avec la classe CoordMain.java)
- Ensuite exécuter autant de fois le chat que nécessaire (classe IRC2.java)


# IRC et IRC2 quesaco ?

La différence entre ces deux applications de chat réside dans l'implémentation de Javanaise :
- IRC correspond à l'implémentation de Javanaise 1 et est obsolète actuellement sur notre projet, il est ici pour montrer les différence entre Javanaise 1 et 2
- IRC2 correspond à l'implémentation de Javanaise 2 et fonctionnement correctement

# Quelles sont les features présentent

Notre programme finale permet la gestion d'un chache d'objet pour chaque application qui possède un serveur partagé. La gestion de ce cache est transparente pour l'utilisateur grâce à l'utilisation d'un proxy (Javanaise 2). Nous avons également ajouté une gestion de panne client du côté serveur partagé ainsi qu'un flush régulier des ressources non-utilisées.

# De l'aide ?

Lors de la création de ce projet, nous avons eu quelques bugs et pour les résoudres nous nous sommes reposés sur 2 ressources principales :
- https://github.com/Maleusa/Javanaise : Le répertoire d'un groupe de notre classe
- https://github.com/Cieldara/Javanaise : Trouvé par des recherches
