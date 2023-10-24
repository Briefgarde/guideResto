INSERT INTO TYPES_GASTRONOMIQUES(libelle, description) VALUES ('Cuisine suisse', 'Cuisine classique et plats typiquement suisses');
INSERT INTO TYPES_GASTRONOMIQUES(libelle, description) VALUES ('Restaurant gastronomique', 'Restaurant gastronomique de haut standing');
INSERT INTO TYPES_GASTRONOMIQUES(libelle, description) VALUES ('Pizzeria', 'Pizzas et autres spécialités italiennes');
COMMIT;

INSERT INTO CRITERES_EVALUATION(nom, description) VALUES ('Service', 'Qualité du service');
INSERT INTO CRITERES_EVALUATION(nom, description) VALUES ('Cuisine', 'Qualité de la nourriture');
INSERT INTO CRITERES_EVALUATION(nom, description) VALUES ('Cadre', 'L''ambiance et la décoration sont-elles bonnes ?');
COMMIT;

INSERT INTO VILLES(code_postal, nom_ville) VALUES ('2000', 'Neuchâtel');
COMMIT;

INSERT INTO RESTAURANTS(nom, adresse, description, site_web, fk_type, fk_vill) VALUES ('Fleur-de-Lys', 'Rue du Bassin 10', 'Pizzeria au centre de Neuchâtel', 'http://www.pizzeria-neuchatel.ch', 3, 1);
INSERT INTO RESTAURANTS(nom, adresse, description, site_web, fk_type, fk_vill) VALUES ('La Maison du Prussien', 'Rue des Tunnels 11', 'Restaurant gastronomique renommé de Neuchâtel', 'www.hotel-prussien.ch', 2, 1);
COMMIT;

INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Génial !', 'Toto', 1);
INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Très bon', 'Titi', 1);
INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Un régal !', 'Dupont', 2);
INSERT INTO COMMENTAIRES(date_eval, commentaire, nom_utilisateur, fk_rest) VALUES (sysdate, 'Rien à dire, le top !', 'Dupasquier', 2);
COMMIT;

INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 1, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 1, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 1, 3);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 2, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 2, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (4, 2, 3);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 3, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 3, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 3, 3);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 4, 1);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 4, 2);
INSERT INTO NOTES(note, fk_comm, fk_crit) VALUES (5, 4, 3);
COMMIT;

INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.4', 1);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.5', 1);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('F', sysdate, '1.2.3.6', 1);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.7', 2);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.8', 2);
INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES ('T', sysdate, '1.2.3.9', 2);
COMMIT;