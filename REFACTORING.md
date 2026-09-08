# Journal de refactoring

| Classe/méthode   | Problème observé                            | Refactoring appliqué                                                              | Justification                         |
|------------------|---------------------------------------------|-----------------------------------------------------------------------------------|---------------------------------------|
| Shipment Service | Classe faisait beaucoup trop de choses      | Séparé chaque foncitonalité dans une méthode unique                               | Méthode de base faisait beaucoup trop |
| Pricing Service  | Beaucoup trop de paramètres dans la méthode | Séparé la méthode en plus de méthodes plus précises et enlevé paramètres inutiles | Méthode n'était pas très lisible      |
