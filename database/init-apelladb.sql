﻿
INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (1, '15jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', 'Σωτήριος', 'Άγγελος',
	'Λένης', 'Sotirios', 'Angelos', 
	'Lenis', 'anglen@ebs.gr', '6951666072', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'anglen', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (1, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 1);

INSERT INTO administrator (id) 
VALUES (1);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (2, '25jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΚΩΝΣΤΑΝΤΙΝΟΣ',
	'ΓΡΑΜΜΑΤΙΚΟΣ', '-', 'KONSTANTINOS', 
	'GRAMMATIKOS', 'kgram@admin.grnet.gr', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'kgram', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (2, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 2);

INSERT INTO administrator (id) 
VALUES (2);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (3, '35jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΜΑΝΟΣ',
	'ΒΟΜΒΟΛΑΚΗΣ', '-', 'MANOS', 
	'VOMVOLAKIS', 'manosvomvolakis@gmail.com', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'manosv', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (3, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 3);

INSERT INTO administrator (id) 
VALUES (3);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (4, '45jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΝΤΟΡΑ',
	'ΜΟΡΦΗ', '-', 'NTORA', 
	'MORFI', 'nmorfi@grnet.gr', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'nmorfi', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (4, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 4);

INSERT INTO administrator (id) 
VALUES (4);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (5, '55jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΣΤΑΥΡΟΣ',
	'ΧΑΤΖΗΒΕΗΣ', '-', 'STAVROS', 
	'CHATZIVEIS', 'schatziveis@grnet.gr', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'schatz', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (5, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 5);

INSERT INTO administrator (id) 
VALUES (5);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (6, '65jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΣΟΛΩΝ',
	'ΖΑΝΝΟΣ', '-', 'SΟΛΟΝ', 
	'ZANNOS', 's.zannos@gmail.com', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'szannos', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (6, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 6);

INSERT INTO administrator (id) 
VALUES (6);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (7, '75jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΒΙΚΥ',
	'ΚΩΝΣΤΑΝΤΙΝΟΠΟΥΛΟΥ', '-', 'VICKY', 
	'KONSTANTINOPOULOU', 'vickyc90@gmail.com', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'vickyk', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (7, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 7);

INSERT INTO administrator (id) 
VALUES (7);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (8, '85jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΚΑΤΕΡΙΝΑ',
	'ΖΕΡΒΟΝΙΚΟΛΑΚΗ', '-', 'KATERINA', 
	'ZERVONIKOLAKI', 'zervkat@gmail.com', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'zervkat', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (8, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 8);

INSERT INTO administrator (id) 
VALUES (8);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, version)
VALUES (9, '85jbeD4HO6xtI26631uELZ3jCDg=', 'USERNAME', '-', 'ΓΙΩΡΓΟΣ',
	'ΨΑΡΡΟΣ', '-', 'GIORGOS', 
	'PSARROS', 'psarros.georgios@gmail.com', '6900000000', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'gpsarros', NULL, 1);

INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) 
VALUES (9, 'ADMINISTRATOR', 'ACTIVE', '2012-09-05 13:29:18.221', NULL, 0, 9);

INSERT INTO administrator (id) 
VALUES (9);

INSERT INTO users(
	id, authtoken, authenticationtype, basicinfo_fathername, basicinfo_firstname, 
	basicinfo_lastname, basicinfolatin_fathername, basicinfolatin_firstname, 
	basicinfolatin_lastname, contactinfo_email, contactinfo_mobile, 
	contactinfo_phone, creationdate, identification, password, passwordsalt, 
	permanentauthtoken, shibbolethinfo_affiliation, shibbolethinfo_givenname, 
	shibbolethinfo_remoteuser, shibbolethinfo_schachomeorganization, 
	shibbolethinfo_sn, status, statusdate, username, verificationnumber, 
	version)
VALUES (10, '95jbeD4HO6xtI26631uELZ3jCEg=', 'USERNAME', '-', 'Χρήστης',
	'Υπουργείου', '-', 'User', 
	'Ministry', 'lenis.angelos@gmail.com', '6951666072', 
	NULL, '2012-07-04 15:49:29.109', '', 'hNOCXEZfAxgjvRQMGlk7tuXfMk0=', 'QLYo0saNqlODWg==',
	NULL, NULL, NULL, 
	NULL, NULL, 
	NULL, 'ACTIVE', '2012-07-04 15:49:29.109', 'ministry', NULL, 
	1);
INSERT INTO roles (id, discriminator, status, statusdate, statusenddate, version, user_id) VALUES (10, 'MINISTRY_MANAGER', 'ACTIVE', '2012-11-06 13:23:12.182', NULL, 6, 10);
INSERT INTO ministrymanager (ministry, id) VALUES ('Παιδείας', 10);

INSERT INTO rank (id, category, name, version) VALUES (1, 'PROFESSOR', 'Καθηγητής', 0);
INSERT INTO rank (id, category, name, version) VALUES (2, 'PROFESSOR', 'Αναπληρωτής Καθηγητής', 0);
INSERT INTO rank (id, category, name, version) VALUES (3, 'RESEARCHER', 'Διευθυντής Ερευνών', 0);
INSERT INTO rank (id, category, name, version) VALUES (4, 'RESEARCHER', 'Κύριος Ερευνητής', 0);

INSERT INTO sector(id, area, category, version) VALUES (1,'ΛΟΓΙΚΗ', 'ΓΕΝΙΚΗ ΛΟΓΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (2,'ΛΟΓΙΚΗ', 'ΑΛΓΕΒΡΑ ΤΟΥ BOOLE', 0);
INSERT INTO sector(id, area, category, version) VALUES (3,'ΛΟΓΙΚΗ', 'MΑΘΗΜΑΤΙΚΗ ΛΟΓΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (4,'ΛΟΓΙΚΗ', 'ΕΠΑΓΩΓΙΚΗ ΛΟΓΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (5,'ΛΟΓΙΚΗ', 'ΑΛΛO', 0);
INSERT INTO sector(id, area, category, version) VALUES (6,'ΜΑΘΗΜΑΤΙΚΑ', 'ΑΛΓΕΒΡΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (7,'ΜΑΘΗΜΑΤΙΚΑ', 'ΑΝΑΛΥΣΗ KAI ΣΥΝΑΡΤΗΣΙΑΚΗ ΑΝΑΛΥΣΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (8,'ΜΑΘΗΜΑΤΙΚΑ', 'ΓΕΩΜΕΤΡΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (9,'ΜΑΘΗΜΑΤΙΚΑ', 'ΘΕΩΡΙΑ ΑΡΙΘΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (10,'ΜΑΘΗΜΑΤΙΚΑ', 'AΡΙΘΜΗΤΙΚΗ ΑΝΑΛΥΣΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (11,'ΜΑΘΗΜΑΤΙΚΑ', 'ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΕΡΕΥΝΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (12,'ΜΑΘΗΜΑΤΙΚΑ', 'ΠΙΘΑΝΟΤΗΤΕΣ KAI ΣΤΑΤΙΣΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (13,'ΜΑΘΗΜΑΤΙΚΑ', 'ΤΟΠΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (14,'ΜΑΘΗΜΑΤΙΚΑ', 'ΑΛΛΕΣ ΜΑΘΗΜΑΤΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (15,'ΠΛΗΡΟΦΟΡΙΚΗ', 'ΛΟΓΙΣΜΙΚΟ', 0);
INSERT INTO sector(id, area, category, version) VALUES (16,'ΠΛΗΡΟΦΟΡΙΚΗ', 'ΥΛΙΚΟ', 0);
INSERT INTO sector(id, area, category, version) VALUES (17,'ΠΛΗΡΟΦΟΡΙΚΗ', 'ΣΥΣΤΗΜΑΤΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (18,'ΠΛΗΡΟΦΟΡΙΚΗ', 'ΤΕΧΝΗΤΗ ΝΟΗΜΟΣΥΝΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (19,'ΠΛΗΡΟΦΟΡΙΚΗ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΠΛΗΡΟΦΟΡΙΚΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (20,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΚΟΣΜΟΛΟΓΙΑ ΚΑΙ ΚΟΣΜΟΓΕΝΕΣΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (21,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'MEΣΟΠΛΑΝΗΤΙΚΟΣ ΧΩΡΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (22,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΟΠΤΙΚΗ ΑΣΤΡΟΝΟΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (23,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΠΛΑΝΗΤΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (24,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΡΑΔΙΟ ΑΣΤΡΟΝΟΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (25,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΗΛΙΑΚΟ ΣΥΣΤΗΜΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (26,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΑΣΤΡΟΜΕΤΡΙΑ, ΟΥΡΑΝΙΑ ΜΗΧΑΝΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (27,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΑΣΤΡΟΝΟΜΙΑ ΠΕΡΑΝ ΤΟΥ ΓΑΛΑΞΙΑ ΜΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (28,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΓΑΛΑΞΙΑΚΗ ΔΥΝΑΜΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (29,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΣΧΗΜΑΤΙΣΜΟΣ ΚΑΙ ΕΞΕΛΙΞΗ ΑΣΤΕΡΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (30,'ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ', 'ΑΛΛΕΣ ΑΣΤΡΟΝΟΜΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (31,'ΦΥΣΙΚΗ', 'AΚΟΥΣΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (32,'ΦΥΣΙΚΗ', 'HΛΕΚΤΡΟΜΑΓΝΗΤΙΣΜΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (33,'ΦΥΣΙΚΗ', 'ΗΛΕΚΤΡΟΝΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (34,'ΦΥΣΙΚΗ', 'ΦΥΣΙΚΗ ΤΩΝ  ΡΕΥΣΤΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (35,'ΦΥΣΙΚΗ', 'ΜΗΧΑΝΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (36,'ΦΥΣΙΚΗ', 'ΜΟΡΙΑΚΗ ΦΥΣΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (37,'ΦΥΣΙΚΗ', 'ΠΥΡΗΝΙΚΗ ΦΥΣΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (38,'ΦΥΣΙΚΗ', 'ΠΥΡΗΝΙΚΗ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (39,'ΦΥΣΙΚΗ', 'ΟΠΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (40,'ΦΥΣΙΚΗ', 'ΦΥΣΙΚΟΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (41,'ΦΥΣΙΚΗ', 'ΦΥΣΙΚΗ ΣΤΕΡΕΑΣ ΚΑΤΑΣΤΑΣΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (42,'ΦΥΣΙΚΗ', 'ΘΕΩΡΗΤΙΚΗ ΦΥΣΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (43,'ΦΥΣΙΚΗ', 'ΘΕΡΜΟΔΥΝΑΜΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (44,'ΦΥΣΙΚΗ', 'METΡΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (45,'ΦΥΣΙΚΗ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΦΥΣΙΚΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (46,'ΧΗΜΕΙΑ', 'ΑΝΑΛΥΤΙΚΗ ΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (47,'ΧΗΜΕΙΑ', 'ΑΝΟΡΓΑΝΗ ΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (48,'ΧΗΜΕΙΑ', 'ΒΙΟΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (49,'ΧΗΜΕΙΑ', 'ΧΗΜΕΙΑ ΜΑΚΡΟΜΟΡΙΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (50,'ΧΗΜΕΙΑ', 'ΟΡΓΑΝΙΚΗ ΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (51,'ΧΗΜΕΙΑ', 'ΠΥΡΗΝΙΚΗ ΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (52,'ΧΗΜΕΙΑ', 'ΚΛΙΝΙΚΗ ΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (53,'ΧΗΜΕΙΑ', 'XHMEIA ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (54,'ΧΗΜΕΙΑ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΧΗΜΕΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (55,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΦΥΣΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (56,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΒΙΟΜΑΘΗΜΑΤΙΚΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (57,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΒΙΟΕΠΙΣΤΗΜΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (58,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΒΙΟΛΟΓΙΑ ΚΥΤΤΑΡΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (59,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΓΕΝΕΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (60,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΦΥΣΙΟΛΟΓΙΑ ΤΟΥ ΑΝΘΡΩΠΟΥ', 0);
INSERT INTO sector(id, area, category, version) VALUES (61,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΑΝΟΣΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (62,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΜΙΚΡΟΒΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (63,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΜΟΡΙΑΚΗ ΒΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (64,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΡΑΔΙΟΒΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (65,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (66,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΘΑΛΑΣΣΙΑ ΒΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (67,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΒΙΟΛΟΓΙΑ ΦΥΤΩΝ (ΒΟΤΑΝΙΚΗ)', 0);
INSERT INTO sector(id, area, category, version) VALUES (68,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΒΙΟΛΟΓΙΑ ΤΩΝ ΖΩΩΝ (ΖΩΟΛΟΓΙΑ)', 0);
INSERT INTO sector(id, area, category, version) VALUES (69,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΕΝΤΟΜΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (70,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΑΘΛΗΤΙΚΗ ΕΠΙΣΤΗΜΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (71,'ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΕΠΙΣΤΗΜΩΝ ΖΩΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (72,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΑΤΜΟΣΦΑΙΡΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (73,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΚΛΙΜΑΤΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (74,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΓΕΩΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (75,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΓΕΩΔΑΙΣΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (76,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΓΕΩΓΡΑΦΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (77,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΓΕΝΙΚΗ ΓΕΩΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (78,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΠΑΛΑΙΟΝΤΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (79,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΟΡΥΚΤΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (80,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΤΕΚΤΟΝΙΚΗ ΓΕΩΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (81,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΓΕΩΘΕΡΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (82,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΓΕΩΦΥΣΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (83,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΥΔΡΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (84,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΜΕΤΕΩΡΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (85,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΘΑΛΑΣΣΙΑ ΓΕΩΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (86,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΩΚΕΑΝΟΓΡΑΦΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (87,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΟΙΚΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (88,'ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ', 'ΑΛΛΕΣ ΕΠΙΣΤΗΜΕΣ ΤΗΣ  ΓΗΣ ΚΑΙ ΤΟΥ ΔΙΑΣΤΗΜΑΤΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (89,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΕΔΑΦΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (90,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΓΕΩΡΓΙΚΗ ΧΗΜΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (91,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΓΕΩΡΓΙΚΗ ΜΗΧΑΝΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (92,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΦΥΤΙΚΗ ΠΑΡΑΓΩΓΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (93,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΦΥΤΟΠΑΘΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (94,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'KTHNIATΡΙΚΗ ΕΠΙΣΤΗΜΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (95,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΖΩΟΤΕΧΝΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (96,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΙΧΘΥΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (97,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΑΓΡΟΤΙΚΗ ΟΙΚΟΝΟΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (98,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΑΣΟΠΟΝΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (99,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΙΑΧΕΙΡΙΣΗ ΟΡΕΙΝΩΝ ΥΔΑΤΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (100,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΛΙΒΑΔΟΠΟΝΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (101,'ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΑΛΛΕΣ ΓΕΩΠΟΝΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (102,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΚΛΙΝΙΚΗ ΙΑΤΡΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (103,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΕΠΙΔΗΜΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (104,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΕΡΓΑΣΙΑΚΗ ΙΑΤΡΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (105,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΠΥΡΗΝΙΚΗ ΙΑΤΡΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (106,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΕΣΩΤΕΡΙΚΗ (ΜΗ ΧΕΙΡΟΥΡΓΙΚΗ) ΙΑΤΡΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (107,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΙΑΙΤΗΤΙΚΗ (ΔΙΑΙΤΟΛΟΓΙΑ)', 0);
INSERT INTO sector(id, area, category, version) VALUES (108,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΠΑΘΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (109,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΥΝΑΜΙΚΗ ΤΩΝ ΦΑΡΜΑΚΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (110,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΦΑΡΜAΚΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (111,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΠΡΟΛΗΠΤΙΚΗ ΙΑΤΡΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (112,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΨΥΧΙΑΤΡΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (113,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΗΜΟΣΙΑ ΥΓΕΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (114,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΧΕΙΡΟΥΡΓΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (115,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΤΟΞΙΚΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (116,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΟΔΟΝΤΙΑΤΡΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (117,'ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΑΛΛΕΣ ΙΑΤΡΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (118,'ΤΕΧΝΟΛΟΓΙΑ', 'ΑΕΡΟΝΑΥΤΙΚΗ ΜΗΧΑΝΟΛΟΓΙΑ KAI ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (119,'ΤΕΧΝΟΛΟΓΙΑ', 'ΒΙΟΧΗΜΙΚΗ ΤΕΧΝΟΛΟΓΙΑ-ΒΙΟΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (120,'ΤΕΧΝΟΛΟΓΙΑ', 'ΧΗΜΙΚΗ ΜΗΧΑΝΙΚΗ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (121,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ KΑΥΣΙΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (122,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΚΑΤΑΣΚΕΥΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (123,'ΤΕΧΝΟΛΟΓΙΑ', 'ΗΛΕΚΤΡΙΚΗ ΤΕΧΝΟΛΟΓΙΑ ΚΑΙ  ΜΗΧΑΝΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (124,'ΤΕΧΝΟΛΟΓΙΑ', 'ΗΛΕΚΤΡΟΝΙΚΗ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (125,'ΤΕΧΝΟΛΟΓΙΑ', 'TEΧΝΟΛΟΓΙΑ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (126,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΚΑΙ ΜΗΧΑΝΙΚΗ  ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (127,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΤΡΟΦΙΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (128,'ΤΕΧΝΟΛΟΓΙΑ', 'ΒΙΟΜΗΧΑΝΙΚΗ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (129,'ΤΕΧΝΟΛΟΓΙΑ', 'TΕΧΝΟΛΟΓΙΑ ΟΡΓΑΝΩΝ ΜΕΤΡΗΣHΣ ΚΑΙ ΕΛΕΓΧΟΥ', 0);
INSERT INTO sector(id, area, category, version) VALUES (130,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΥΛΙΚΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (131,'ΤΕΧΝΟΛΟΓΙΑ', 'ΜΗΧΑΝΟΛΟΓΙΚΗ ΜΗΧΑΝΙΚΗ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (132,'ΤΕΧΝΟΛΟΓΙΑ', 'ΙΑΤΡΙΚΗ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (133,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΜΕΤΑΛΛΟΥΡΓΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (134,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΜΕΤΑΛΛΙΚΩΝ ΠΡΟΙΟΝΤΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (135,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΟΡΥΧΕΙΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (136,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΟΧΗΜΑΤΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (137,'ΤΕΧΝΟΛΟΓΙΑ', 'ΝΑΥΤΙΚΗ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (138,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΣΙΔΗΡΟΔΡΟΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (139,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΜΕΤΑΦΟΡΙΚΩΝ ΜΕΣΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (140,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΚΛΩΣΤΟΥΦΑΝΤΟΥΡΓΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (141,'ΤΕΧΝΟΛΟΓΙΑ', 'ΤΕΧΝΟΛΟΓΙΑ ΔΙΑΣΤΗΜΑΤΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (142,'ΤΕΧΝΟΛΟΓΙΑ', 'ΕΝΕΡΓΕΙΑΚΗ ΤΕΧΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (143,'ΤΕΧΝΟΛΟΓΙΑ', 'ΧΩΡΟΤΑΞΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (144,'ΤΕΧΝΟΛΟΓΙΑ', 'ΠΟΛΕΟΔΟΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (145,'ΤΕΧΝΟΛΟΓΙΑ', 'ΑΡΧΙΤΕΚΤΟΝΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (146,'ΤΕΧΝΟΛΟΓΙΑ', 'ΑΛΛΕΣ ΤΕΧΝΟΛΟΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (147,'ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)', 'ΠΟΛΙΤΙΣΤΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (148,'ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)', 'ΚΟΙΝΩΝΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (149,'ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)', 'ΕΘΝΟΓΡΑΦΙΑ ΚΑΙ ΕΘΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (150,'ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (151,'ΔΗΜΟΓΡΑΦΙΑ', 'ΘΕΩΡΙΑ ΚΑΙ ΜΕΘΟΔΟΛΟΓΙΑ ΕΡΕΥΝΑΣ ΚΑΙ ΑΝΑΛΥΣΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (152,'ΔΗΜΟΓΡΑΦΙΑ', 'ΘΝΗΣΙΜΟΤΗΤΑ ΚΑΙ ΓΟΝΙΜΟΤΗΤΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (153,'ΔΗΜΟΓΡΑΦΙΑ', 'ΙΣΤΟΡΙΚΗ ΔΗΜΟΓΡΑΦΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (154,'ΔΗΜΟΓΡΑΦΙΑ', 'ΜΕΓΕΘΟΣ ΠΛΗΘΥΣΜΟΥ ΚΑΙ ΔΗΜΟΓΡΑΦΙΚΗ ΕΞΕΛΙΞΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (155,'ΔΗΜΟΓΡΑΦΙΑ', 'ΓΕΩΓΡΑΦΙΚΗ ΔΗΜΟΓΡΑΦΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (156,'ΔΗΜΟΓΡΑΦΙΑ', 'XAΡΑΚΤΗΡΙΣΤΙΚΑ ΠΛΗΘΥΣΜΟΥ', 0);
INSERT INTO sector(id, area, category, version) VALUES (157,'ΔΗΜΟΓΡΑΦΙΑ', 'ΑΛΛΕΣ ΔΗΜΟΓΡΑΦΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (158,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΗΜΟΣΙΟΝΟΜΙΚΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (159,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΟΙΚΟΝΟΜΕΤΡΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (160,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΟΙΚΟΝΟΜΙΚΗ ΔΡΑΣΤΗΡΙΟΤΗΤΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (161,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΟΙΚΟΝΟΜΙΚΑ ΤΕΧΝΟΛΟΓΙΚΩΝ ΑΛΛΑΓΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (162,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΟΙΚΟΝΟΜΙΚΗ ΘΕΩΡΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (163,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΓΕΝΙΚΗ ΟΙΚΟΝΟΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (164,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΒΙΟΜΗΧΑΝΙΚΗ ΟΡΓΑΝΩΣΗ ΚΑΙ ΔΗΜΟΣΙΑ ΠΟΛΙΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (165,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΙΕΘΝΗΣ ΟΙΚΟΝΟΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (166,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΟΡΓΑΝΩΣΗ ΚΑΙ ΔΙΟΙΚΗΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (167,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΟΙΚΟΝΟΜΙΑ ΤΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (168,'ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (169,'ΙΣΤΟΡΙΑ', 'ΓΕΝΙΚΗ ΙΣΤΟΡΙΑ, ΘΕΩΡΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (170,'ΙΣΤΟΡΙΑ', 'ΙΣΤΟΡΙΑ ΤΩΝ ΧΩΡΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (171,'ΙΣΤΟΡΙΑ', 'ΕΙΔΙΚΕΣ ΙΣΤΟΡΙΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (172,'ΙΣΤΟΡΙΑ', 'ΑΡΧΑΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (173,'ΙΣΤΟΡΙΑ', 'ΑΛΛΕΣ ΙΣΤΟΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (174,'ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΓΕΝΙΚΗ ΘΕΩΡΙΑ ΚΑΙ ΜΕΘΟΔΟΙ', 0);
INSERT INTO sector(id, area, category, version) VALUES (175,'ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΕΘΝΙΚΟ ΔΙΚΑΙΟ', 0);
INSERT INTO sector(id, area, category, version) VALUES (176,'ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΙΚΟΝΟΜΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (177,'ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΔΙΕΘΝΕΣ ΔΙΚΑΙΟ', 0);
INSERT INTO sector(id, area, category, version) VALUES (178,'ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'AΛΛΕΣ ΝΟΜΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (179,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΕΦΑΡΜΟΣΜΕΝΗ ΓΛΩΣΣΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (180,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΔΙΑΧΡΟΝΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (181,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΘΕΩΡΗΤΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (182,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΓΕΩΓΡΑΦΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (183,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΣΥΓΧΡΟΝΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (184,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΛΕΞΙΚΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (185,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΚΟΙΝΩΝΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (186,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΑΡΧΑΙΕΣ ΓΛΩΣΣΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (187,'ΓΛΩΣΣΟΛΟΓΙΑ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΓΛΩΣΣΟΛΟΓΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (188,'ΠΑΙΔΑΓΩΓΙΚΗ', 'ΘΕΩΡΙΑ ΚΑΙ ΜΕΘΟΔΟΙ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (189,'ΠΑΙΔΑΓΩΓΙΚΗ', 'ΟΡΓΑΝΩΣΗ ΚΑΙ ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (190,'ΠΑΙΔΑΓΩΓΙΚΗ', 'ΑΛΛΕΣ ΠΑΙΔΑΓΩΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (191,'ΠΟΛΙΤΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΠΟΛΙΤΙΚΗ ΕΠΙΣΤΗΜΗ, ΓΕΝΙΚΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (192,'ΗΘΙΚΗ', 'ΚΛΑΣΣΙΚΗ ΗΘΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (193,'ΗΘΙΚΗ', 'ΗΘΙΚΗ ΤΩΝ ΑΤΟΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (194,'ΗΘΙΚΗ', 'ΗΘΙΚΗ ΤΩΝ ΚΟΙΝΩΝΙΚΩΝ ΟΜΑΔΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (195,'ΗΘΙΚΗ', 'ΗΘΙΚΗ ΤΩΝ ΕΠΙΣΤΗΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (196,'ΗΘΙΚΗ', 'ΗΘΙΚΗ ΤΗΣ ΙΑΤΡΙΚΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (197,'ΗΘΙΚΗ', 'AΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΣΧΕΤΙΚΕΣ MΕ ΤΗΝ ΗΘΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (198,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΓΝΩΣΗΣ ΚΑΙ ΕΠΙΣΤΗΜΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (199,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (200,'ΦΙΛΟΣΟΦΙΑ', 'ΓΕΝΙΚΗ ΦΙΛΟΣΟΦΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (201,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΚΑ ΣΥΣΤΗΜΑΤΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (202,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΕΠΙΣΤΗΜΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (203,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΦΥΣΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (204,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΚΟΙΝΩΝΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (205,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΚΑ ΣΥΣΤΗΜΑΤΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (206,'ΦΙΛΟΣΟΦΙΑ', 'ΙΣΤΟΡΙΑ ΤΗΣ ΦΙΛΟΣΟΦΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (207,'ΦΙΛΟΣΟΦΙΑ', 'ΦΑΙΝΟΜΕΝΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (208,'ΦΙΛΟΣΟΦΙΑ', 'ΦΙΛΟΣΟΦΙΚΗ ΛΟΓΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (209,'ΦΙΛΟΣΟΦΙΑ', 'ΑΙΣΘΗΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (210,'ΦΙΛΟΣΟΦΙΑ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΦΙΛΟΣΟΦΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (211,'ΘΕΟΛΟΓΙΑ', 'ΓΕΝΙΚΗ ΘΕΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (212,'ΘΕΟΛΟΓΙΑ', 'ΣΥΣΤΗΜΑΤΙΚΗ ΘΕΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (213,'ΘΕΟΛΟΓΙΑ', 'ΒΙΒΛΟΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (214,'ΘΕΟΛΟΓΙΑ', 'ΑΛΛΕΣ ΘΕΟΛΟΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (215,'ΠΟΛΙΤΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΠΟΛΙΤIΚH EΠΙΣΤΗΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (216,'ΠΟΛΙΤΙΚΕΣ ΕΠΙΣΤΗΜΕΣ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΩΝ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0);
INSERT INTO sector(id, area, category, version) VALUES (217,'ΨΥΧΟΛΟΓΙΑ', 'ΓΕΝΙΚΗ ΨΥΧΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (218,'ΨΥΧΟΛΟΓΙΑ', 'ΨΥΧΟΛΟΓΙΑ ΕΦΗΒΟΥ ΚΑΙ ΠΑΙΔΙΟΥ', 0);
INSERT INTO sector(id, area, category, version) VALUES (219,'ΨΥΧΟΛΟΓΙΑ', 'ΚΛΙΝΙΚΗ ΨΥΧΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (220,'ΨΥΧΟΛΟΓΙΑ', 'ΠΕΙΡΑΜΑΤΙΚΗ ΨΥΧΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (221,'ΨΥΧΟΛΟΓΙΑ', 'ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΨΥΧΟΛΟΓΙΑΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (222,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'ΦΙΛΟΛΟΓΙΑ : ΘΕΩΡΙΑ, ΑΝΑΛΥΣΗ ΚΑΙ ΚΡΙΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (223,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'ΣΥΝΤΗΡΗΣΗ ΚΑΙ ΑΝΑΣΤΗΛΩΣΗ ΕΡΓΩΝ ΤΕΧΝΗΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (224,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'KAΛΕΣ ΤΕΧΝΕΣ : ΘΕΩΡΙΑ, ΑΝΑΛΥΣΗ ΚΑΙ ΚΡΙΤΙΚΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (225,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'ΚΙΝΗΜΑΤΟΓΡΑΦΙΑ, VIDEO', 0);
INSERT INTO sector(id, area, category, version) VALUES (226,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'BYZANTINH ΦΙΛΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (227,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'ΙΣΤΟΡΙΑ ΤΟΥ ΘΕΑΤΡΟΥ', 0);
INSERT INTO sector(id, area, category, version) VALUES (228,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'ΝΕΟΕΛΛΗΝΙΚΗ ΦΙΛΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (229,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'ΘΕΑΤΡΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (230,'ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ', 'AΛΛΕΣ ΚΑΛΛΙΤΕΧΝΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);
INSERT INTO sector(id, area, category, version) VALUES (231,'ΚΟΙΝΩΝΙΟΛΟΓΙΑ', 'ΓΕΝΙΚH KOINΩΝΙΟΛΟΓΙΑ', 0);
INSERT INTO sector(id, area, category, version) VALUES (232,'ΚΟΙΝΩΝΙΟΛΟΓΙΑ', 'ΚΟΙΝΩΝΙΚΗ ΑΛΛΑΓΗ ΚΑΙ ΑΝΑΠΤΥΞΗ', 0);
INSERT INTO sector(id, area, category, version) VALUES (233,'ΚΟΙΝΩΝΙΟΛΟΓΙΑ', 'ΑΛΛΕΣ ΚΟΙΝΩΝΙΟΛΟΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ', 0);

INSERT INTO institution (id, category, name, authenticationtype) VALUES (1, 'INSTITUTION', 'ΑΛΕΞΑΝΔΡΕΙΟ ΤΕΙ ΘΕΣΣΑΛΟΝΙΚΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (6, 'INSTITUTION', 'ΑΝΩΤΑΤΗ ΣΧΟΛΗ ΚΑΛΩΝ ΤΕΧΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (8, 'INSTITUTION', 'ΑΡΙΣΤΟΤΕΛΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΘΕΣ/ΝΙΚΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (9, 'INSTITUTION', 'ΑΣΠΑΙΤΕ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (10, 'INSTITUTION', 'ΓΕΩΠΟΝΙΚΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΘΗΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (11, 'INSTITUTION', 'ΔΗΜΟΚΡΙΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΘΡΑΚΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (45, 'INSTITUTION', 'ΔΙΕΘΝΕΣ ΠΑΝΕΠΙΣΤΗΜΙΟ ΤΗΣ ΕΛΛΑΔΟΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (12, 'INSTITUTION', 'ΕΘΝΙΚΟ & ΚΑΠΟΔΙΣΤΡΙΑΚΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΘΗΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (13, 'INSTITUTION', 'ΕΘΝΙΚΟ ΜΕΤΣΟΒΙΟ ΠΟΛΥΤΕΧΝΕΙΟ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (44, 'INSTITUTION', 'ΕΛΛΗΝΙΚΟ ΑΝΟΙΚΤΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (14, 'INSTITUTION', 'ΙΟΝΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (15, 'INSTITUTION', 'ΟΙΚΟΝΟΜΙΚΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΘΗΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (16, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΙΓΑΙΟΥ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (18, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΔΥΤΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (19, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΘΕΣΣΑΛΙΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (20, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΙΩΑΝΝΙΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (21, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΚΡΗΤΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (22, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΜΑΚΕΔΟΝΙΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (23, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΠΑΤΡΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (24, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΠΕΙΡΑΙΩΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (25, 'INSTITUTION', 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΠΕΛΟΠΟΝΝΗΣΟΥ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (27, 'INSTITUTION', 'ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΚΟΙΝΩΝΙΚΩΝ & ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (28, 'INSTITUTION', 'ΠΟΛΥΤΕΧΝΕΙΟ ΚΡΗΤΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (29, 'INSTITUTION', 'ΤΕΙ ΑΘΗΝΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (33, 'INSTITUTION', 'ΤΕΙ ΑΝΑΤΟΛΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ ΚΑΙ ΘΡΑΚΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (39, 'INSTITUTION', 'ΤΕΙ ΔΥΤΙΚΗΣ ΕΛΛΑΔΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (30, 'INSTITUTION', 'ΤΕΙ ΔΥΤΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (31, 'INSTITUTION', 'ΤΕΙ ΗΠΕΙΡΟΥ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (37, 'INSTITUTION', 'ΤΕΙ ΘΕΣΣΑΛΙΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (32, 'INSTITUTION', 'ΤΕΙ ΙΟΝΙΩΝ ΝΗΣΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (41, 'INSTITUTION', 'ΤΕΙ ΚΕΝΤΡΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (35, 'INSTITUTION', 'ΤΕΙ ΚΡΗΤΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (40, 'INSTITUTION', 'ΤΕΙ ΠΕΙΡΑΙΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (34, 'INSTITUTION', 'ΤΕΙ ΠΕΛΟΠΟΝΝΗΣΟΥ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (36, 'INSTITUTION', 'ΤΕΙ ΣΤΕΡΕΑΣ ΕΛΛΑΔΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (43, 'INSTITUTION', 'ΧΑΡΟΚΟΠΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (101, 'RESEARCH_CENTER', 'ΑΚΑΔΗΜΙΑ ΑΘΗΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (102, 'RESEARCH_CENTER', 'ΔΙΕΘΝΕΣ ΚΕΝΤΡΟ ΔΗΜΟΣΙΑΣ ΔΙΟΙΚΗΣΗΣ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (103, 'RESEARCH_CENTER', 'ΕΘΝΙΚΟ ΑΣΤΕΡΟΣΚΟΠΕΙΟ ΑΘΗΝΩΝ - ΕΑΑ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (104, 'RESEARCH_CENTER', 'ΕΘΝΙΚΟ ΚΕΝΤΡΟ ΕΡΕΥΝΑΣ ΦΥΣΙΚΩΝ ΕΠΙΣΤΗΜΩΝ "ΔΗΜΟΚΡΙΤΟΣ"', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (105, 'RESEARCH_CENTER', 'ΕΘΝΙΚΟ ΚΕΝΤΡΟ ΚΟΙΝΩΝΙΚΩΝ ΕΡΕΥΝΩΝ - ΕΚΚΕ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (106, 'RESEARCH_CENTER', 'ΕΛΛΗΝΙΚΟ ΚΕΝΤΡΟ ΘΑΛΑΣΣΙΩΝ ΕΡΕΥΝΩΝ - ΕΛΚΕΘΕ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (107, 'RESEARCH_CENTER', 'ΙΝΣΤΙΤΟΥΤΟ ΤΕΧΝΙΚΗΣ ΣΕΙΣΜΟΛΟΓΙΑΣ & ΑΝΤΙΣΕΙΣΜΙΚΩΝ ΚΑΤΑΣΚΕΥΩΝ – ΙΤΣΑΚ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (108, 'RESEARCH_CENTER', 'ΓΕΩΡΓΙΚΗ ΚΑΙ ΒΙΟΤΕΧΝΙΚΗ ΣΧΟΛΗ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (109, 'RESEARCH_CENTER', 'Ε.Π.Ι. " ΝΕΥΡΟΧΕΙΡΟΥΡΓΙΚΟ "  - ΠΑΝΕΠΙΣΤΗΜΙΟ ΙΩΑΝΝΙΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (110, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΑΣΤΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΑΝΘΡΩΠΙΝΟΥ ΔΥΝΑΜΙΚΟΥ - ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (111, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΔΙΕΘΝΩΝ ΣΧΕΣΕΩΝ  -  ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (112, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΔΙΚΟΝΟΜΙΚΩΝ ΜΕΛΕΤΩΝ  - ΕΚΠΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (113, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΕΠΙΤΑΧΥΝΤΙΚΩΝ ΣΥΣΤΗΜΑΤΩΝ ΚΑΙ ΕΦΑΡΜΟΓΩΝ ΕΚΠΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (114, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΕΦΗΡΜΟΣΜΕΝΗΣ ΕΠΙΚΟΙΝΩΝΙΑΣ - ΕΚΠΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (115, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΜΕΛΕΤΗΣ ΚΑΙ ΑΝΤΙΜΕΤΩΠΙΣΗΣ ΓΕΝΕΤΙΚΩΝ ΚΑΚΟΗΘΩΝ ΝΟΣΗΜΑΤΩΝ ΤΗΣ ΠΑΙΔΙΚΗΣ ΗΛΙΚΙΑΣ-ΕΚΠΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (116, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΠΑΝΕΠΙΣΤΗΜΙΟ ΜΑΚΕΔΟΝΙΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (117, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΠΕΡΙΦΕΡΕΙΑΚΗΣ ΑΝΑΠΤΥΞΗΣ  -  ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (118, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΣΤΑΤΙΣΤΙΚΗΣ ΤΕΚΜΗΡΙΩΣΗΣ, ΑΝΑΛΥΣΗΣ & ΕΡΕΥΝΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (119, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΣΥΝΤΑΓΜΑΤΙΚΩΝ ΕΡΕΥΝΩΝ  (Ι.Σ.Ε) -  ΕΚΠΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (120, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΣΥΣΤΗΜΑΤΩΝ ΕΠΙΚΟΙΝΩΝΙΑΣ ΚΑΙ ΥΠΟΛΟΓΙΣΤΩΝ (Ε.Μ.Π.)', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (121, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ ΚΡΗΤΗΣ - ΠΟΛΥΤΕΧΝΕΙΟ ΚΡΗΤΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (122, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΦΥΣΙΚΗΣ ΠΛΑΣΜΑΤΟΣ - ΠΑΝΕΠΙΣΤΗΜΙΟ ΚΡΗΤΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (123, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΦΥΣΙΚΗΣ ΤΟΥ ΣΤΕΡΕΟΥ ΦΛΟΙΟΥ ΤΗΣ ΓΗΣ - ΕΚΠΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (124, 'RESEARCH_CENTER', 'Ε.Π.Ι. ΨΥΧΙΚΗΣ ΥΓΙΕΙΝΗΣ - ΕΚΠΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (125, 'RESEARCH_CENTER', 'ΕΘΝΙΚΟ KΕΝΤΡΟ ΔΙΑΒΗΤΗ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (126, 'RESEARCH_CENTER', 'ΕΘΝΙΚΟ ΙΔΡΥΜΑ ΕΡΕΥΝΩΝ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (127, 'RESEARCH_CENTER', 'ΕΘΝΙΚΟ ΚΕΝΤΡΟ ΕΡΕΥΝΑΣ & ΤΕΧΝΟΛΟΓΙΚΗΣ ΑΝΑΠΤΥΞΗΣ – ΕΚΕΤΑ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (128, 'RESEARCH_CENTER', 'ΕΛΛΗΝΙΚΟ ΙΝΣΤΙΤΟΥΤΟ ΠΑΣΤΕΡ - ΕΙΠ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (129, 'RESEARCH_CENTER', 'ΕΛΛΗΝΙΚΟ ΙΝΣΤΙΤΟΥΤΟ ΥΓΙΕΙΝΗΣ & ΑΣΦΑΛΕΙΑΣ ΤΗΣ ΕΡΓΑΣΙΑΣ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (130, 'RESEARCH_CENTER', 'ΕΛΛΗΝΙΚΟΣ ΟΡΓΑΝΙΣΜΟΣ ΤΥΠΟΠΟΙΗΣΗΣ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (131, 'RESEARCH_CENTER', 'ΕΡΕΥΝΗΤΙΚΟ ΑΚΑΔΗΜΑΙΚΟ ΙΝΣΤΙΤΟΥΤΟ ΤΕΧΝΟΛΟΓΙΑΣ ΥΠΟΛΟΓΙΣΤΩΝ (E.A.I.T.Y)  ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (132, 'RESEARCH_CENTER', 'ΕΡΕΥΝΗΤΙΚΟ ΚΕΝΤΡΟ ΒΙΟΙΑΤΡΙΚΩΝ ΕΠΙΣΤΗΜΩΝ - ΕΚEΒΕ "ΑΛΕΞΑΝΔΡΟΣ ΦΛΕΜΙΓΚ"', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (133, 'RESEARCH_CENTER', 'ΕΡΕΥΝΗΤΙΚΟ ΚΕΝΤΡΟ ΚΑΙΝΟΤΟΜΙΑΣ ΣΤΙΣ ΤΕΧΝΟΛΟΓΙΕΣ ΤΗΣ ΠΛΗΡΟΦΟΡΙΑΣ, ΤΩΝ ΕΠΙΚΟΙΝΩΝΙΩΝ & ΤΗΣ ΓΝΩΣΗΣ - "ΑΘΗΝΑ" ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (134, 'RESEARCH_CENTER', 'ΙΔΡΥΜΑ ΕΥΓΕΝΙΔΟΥ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (135, 'RESEARCH_CENTER', 'ΙΔΡΥΜΑ ΙΑΤΡΟΒΙΟΛΟΓΙΚΗΣ ΕΡΕΥΝΑΣ ΑΚΑΔΗΜΙΑΣ ΑΘΗΝΩΝ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (136, 'RESEARCH_CENTER', 'ΙΔΡΥΜΑ ΜΕΙΖΟΝΟΣ ΕΛΛΗΝΙΣΜΟΥ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (137, 'RESEARCH_CENTER', 'ΙΔΡΥΜΑ ΤΕΧΝΟΛΟΓΙΑΣ & ΕΡΕΥΝΑΣ - ΙΤΕ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (138, 'RESEARCH_CENTER', 'ΙΝΣΤΙΤΟΥΤΟ  ΕΡΕΥΝΑΣ ΜΟΥΣΙΚΗΣ ΚΑΙ ΑΚΟΥΣΤΙΚΗΣ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (139, 'RESEARCH_CENTER', 'ΙΝΣΤΙΤΟΥΤΟ ΓΕΩΛΟΓΙΚΩΝ & ΜΕΤΑΛΛΕΥΤΙΚΩΝ ΕΡΕΥΝΩΝ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (140, 'RESEARCH_CENTER', 'ΙΝΣΤΙΤΟΥΤΟ ΕΛΙΑΣ ΚΑΙ ΥΠΟΤΡΟΠΙΚΩΝ ΦΥΤΩΝ ΧΑΝΙΩΝ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (141, 'RESEARCH_CENTER', 'ΚΕΝΤΡΟ ΑΝΑΝΕΩΣΙΜΩΝ ΠΗΓΩΝ ΚΑΙ ΕΞΟΙΚΟΝΟΜΗΣΗΣ ΕΝΕΡΓΕΙΑΣ - ΚΑΠΕ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (142, 'RESEARCH_CENTER', 'ΚΕΝΤΡΟ ΕΚΠΑΙΔΕΥΤΙΚΗΣ ΕΡΕΥΝΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (143, 'RESEARCH_CENTER', 'ΚΕΝΤΡΟ ΕΛΛΗΝΙΚΗΣ ΓΛΩΣΣΑΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (144, 'RESEARCH_CENTER', 'ΚΕΝΤΡΟ ΕΡΕΥΝΑΣ ΓΙΑ ΘΕΜΑΤΑ ΙΣΟΤΗΤΑΣ ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (145, 'RESEARCH_CENTER', 'ΚΕΝΤΡΟ ΕΡΕΥΝΑΣ ΤΕΧΝΟΛΟΓΙΑΣ ΚΑΙ ΑΝΑΠΤΥΞΗΣ ΘΕΣΣΑΛΙΑΣ', 'SHIBBOLETH');
--
INSERT INTO institution (id, category, name, authenticationtype) VALUES (146, 'RESEARCH_CENTER', 'ΕΘΝΙΚΟ ΚΕΝΤΡΟ ΤΕΚΜΗΡΙΩΣΗΣ (ΕΚΤ)', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (147, 'RESEARCH_CENTER', 'ΕΥΡΩΠΑΪΚΟ ΚΕΝΤΡΟ ΓΙΑ ΤΗΝ ΑΝΑΠΤΥΞΗ ΤΗΣ ΕΠΑΓΓΕΛΜΑΤΙΚΗΣ ΚΑΤΑΡΤΙΣΗΣ', 'SHIBBOLETH');
INSERT INTO institution (id, category, name, authenticationtype) VALUES (148, 'RESEARCH_CENTER', 'ΙΝΣΤΙΤΟΥΤΟ ΤΕΧΝΟΛΟΓΙΑΣ ΥΠΟΛΟΓΙΣΤΩΝ & ΕΚΔΟΣΕΩΝ (ΙΤΥΕ "ΔΙΟΦΑΝΤΟΣ")', 'SHIBBOLETH');

UPDATE institution set schachomeorganization='teithe.gr' where id=1;
UPDATE institution set schachomeorganization='asfa.gr' where id=6;
UPDATE institution set schachomeorganization='auth.gr' where id=8;
UPDATE institution set schachomeorganization='aspete.gr' where id=9;
UPDATE institution set schachomeorganization='aua.gr' where id=10;
UPDATE institution set schachomeorganization='duth.gr' where id=11;
UPDATE institution set schachomeorganization='ihu.gr' where id=45;
UPDATE institution set schachomeorganization='uoa.gr' where id=12;
UPDATE institution set schachomeorganization='ntua.gr' where id=13;
UPDATE institution set schachomeorganization='eap.gr' where id=44;
UPDATE institution set schachomeorganization='ionio.gr' where id=14;
UPDATE institution set schachomeorganization='aueb.gr' where id=15;
UPDATE institution set schachomeorganization='aegean.gr' where id=16;
UPDATE institution set schachomeorganization='uowm.gr' where id=18;
UPDATE institution set schachomeorganization='uth.gr' where id=19;
UPDATE institution set schachomeorganization='uoi.gr' where id=20;
UPDATE institution set schachomeorganization='uoc.gr' where id=21;
UPDATE institution set schachomeorganization='uom.gr' where id=22;
UPDATE institution set schachomeorganization='upatras.gr' where id=23;
UPDATE institution set schachomeorganization='unipi.gr' where id=24;
UPDATE institution set schachomeorganization='uop.gr' where id=25;
UPDATE institution set schachomeorganization='panteion.gr' where id=27;
UPDATE institution set schachomeorganization='tuc.gr' where id=28;
UPDATE institution set schachomeorganization='teiath.gr' where id=29;
UPDATE institution set schachomeorganization='teikav.edu.gr' where id=33;
UPDATE institution set schachomeorganization='teipat.gr' where id=39;
UPDATE institution set schachomeorganization='teikoz.gr' where id=30;
UPDATE institution set schachomeorganization='teiep.gr' where id=31;
UPDATE institution set schachomeorganization='teilar.gr' where id=37;
UPDATE institution set schachomeorganization='teiion.gr' where id=32;
UPDATE institution set schachomeorganization='teiser.gr' where id=41;
UPDATE institution set schachomeorganization='teicrete.gr' where id=35;
UPDATE institution set schachomeorganization='teipir.gr' where id=40;
UPDATE institution set schachomeorganization='teikal.gr' where id=34;
UPDATE institution set schachomeorganization='teilam.gr' where id=36;
UPDATE institution set schachomeorganization='hua.gr' where id=43;
---
UPDATE institution set schachomeorganization='bioacademy.gr' where id=135;
UPDATE institution set schachomeorganization='forth.gr' where id=137;
UPDATE institution set schachomeorganization='athena-innovation.gr' where id=133;
UPDATE institution set schachomeorganization='academyofathens.gr' where id=101;
UPDATE institution set schachomeorganization='fleming.gr' where id=132;
UPDATE institution set schachomeorganization='demokritos.gr' where id=104;
UPDATE institution set schachomeorganization='noa.gr' where id=103;
UPDATE institution set schachomeorganization='certh.gr' where id=127;
UPDATE institution set schachomeorganization='ekt.gr' where id=146;
UPDATE institution set schachomeorganization='hcmr.gr' where id=106;
UPDATE institution set schachomeorganization='cedefop.europa.eu' where id=147;
UPDATE institution set schachomeorganization='cti.gr' where id=148;


INSERT INTO school (id, name, institution_id) VALUES (1, '-', 1);
INSERT INTO school (id, name, institution_id) VALUES (2, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 1);
INSERT INTO school (id, name, institution_id) VALUES (3, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 1);
INSERT INTO school (id, name, institution_id) VALUES (4, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 1);
INSERT INTO school (id, name, institution_id) VALUES (5, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 1);
INSERT INTO school (id, name, institution_id) VALUES (6, 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 6);
INSERT INTO school (id, name, institution_id) VALUES (7, 'ΓΕΩΠΟΝΙΑΣ, ΔΑΣΟΛΟΓΙΑΣ ΚΑΙ ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 8);
INSERT INTO school (id, name, institution_id) VALUES (8, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 8);
INSERT INTO school (id, name, institution_id) VALUES (9, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 8);
INSERT INTO school (id, name, institution_id) VALUES (10, 'ΘΕΟΛΟΓΙΚΗ', 8);
INSERT INTO school (id, name, institution_id) VALUES (11, 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 8);
INSERT INTO school (id, name, institution_id) VALUES (12, 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 8);
INSERT INTO school (id, name, institution_id) VALUES (13, 'ΝΟΜΙΚΗ', 8);
INSERT INTO school (id, name, institution_id) VALUES (14, 'ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 8);
INSERT INTO school (id, name, institution_id) VALUES (15, 'ΠΑΙΔΑΓΩΓΙΚΗ', 8);
INSERT INTO school (id, name, institution_id) VALUES (16, 'ΠΟΛΥΤΕΧΝΙΚΗ', 8);
INSERT INTO school (id, name, institution_id) VALUES (17, 'ΦΙΛΟΣΟΦΙΚΗ', 8);
INSERT INTO school (id, name, institution_id) VALUES (18, 'ΠΑΙΔΑΓΩΓΙΚΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', 9);
INSERT INTO school (id, name, institution_id) VALUES (19, 'ΑΓΡΟΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ ΥΠΟΔΟΜΩΝ ΚΑΙ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 10);
INSERT INTO school (id, name, institution_id) VALUES (20, 'ΤΡΟΦΙΜΩΝ ΒΙΟΤΕΧΝΟΛΟΓΙΑΣ ΚΑΙ ΑΝΑΠΤΥΞΗΣ', 10);
INSERT INTO school (id, name, institution_id) VALUES (21, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 11);
INSERT INTO school (id, name, institution_id) VALUES (22, 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 11);
INSERT INTO school (id, name, institution_id) VALUES (23, 'ΕΠΙΣΤΗΜΩΝ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΔΑΣΟΛΟΓΙΑΣ', 11);
INSERT INTO school (id, name, institution_id) VALUES (24, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 11);
INSERT INTO school (id, name, institution_id) VALUES (25, 'ΚΛΑΣΣΙΚΩΝ ΚΑΙ ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΣΠΟΥΔΩΝ', 11);
INSERT INTO school (id, name, institution_id) VALUES (26, 'ΚΟΙΝΩΝΙΚΩΝ ΠΟΛΙΤΙΚΩΝ ΚΑΙ ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 11);
INSERT INTO school (id, name, institution_id) VALUES (27, 'ΝΟΜΙΚΗ', 11);
INSERT INTO school (id, name, institution_id) VALUES (28, 'ΠΟΛΥΤΕΧΝΙΚΗ', 11);
INSERT INTO school (id, name, institution_id) VALUES (29, 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 45);
INSERT INTO school (id, name, institution_id) VALUES (30, 'ΕΠΙΣΤΗΜΩΝ ΤΕΧΝΟΛΟΓΙΑΣ', 45);
INSERT INTO school (id, name, institution_id) VALUES (31, 'ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 45);
INSERT INTO school (id, name, institution_id) VALUES (32, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 12);
INSERT INTO school (id, name, institution_id) VALUES (33, 'ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΑΓΩΓΗΣ', 12);
INSERT INTO school (id, name, institution_id) VALUES (34, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 12);
INSERT INTO school (id, name, institution_id) VALUES (35, 'ΘΕΟΛΟΓΙΚΗ', 12);
INSERT INTO school (id, name, institution_id) VALUES (36, 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 12);
INSERT INTO school (id, name, institution_id) VALUES (37, 'ΝΟΜΙΚΗ', 12);
INSERT INTO school (id, name, institution_id) VALUES (38, 'ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 12);
INSERT INTO school (id, name, institution_id) VALUES (39, 'ΦΙΛΟΣΟΦΙΚΗ', 12);
INSERT INTO school (id, name, institution_id) VALUES (40, 'ΑΓΡΟΝΟΜΩΝ ΚΑΙ ΤΟΠΟΓΡΑΦΩΝ ΜΗΧΑΝΙΚΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (41, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (42, 'ΕΦΑΡΜΟΣΜΕΝΩΝ ΜΑΘΗΜΑΤΙΚΩΝ ΚΑΙ ΦΥΣΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (43, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (44, 'ΜΗΧΑΝΙΚΩΝ ΜΕΤΑΛΛΕΙΩΝ ΜΕΤΑΛΛΟΥΡΓΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (45, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (46, 'ΝΑΥΠΗΓΩΝ ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (47, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (48, 'ΧΗΜΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 13);
INSERT INTO school (id, name, institution_id) VALUES (49, 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΣΠΟΥΔΩΝ', 44);
INSERT INTO school (id, name, institution_id) VALUES (50, 'ΕΦΑΡΜΟΣΜΕΝΩΝ ΤΕΧΝΩΝ', 44);
INSERT INTO school (id, name, institution_id) VALUES (51, 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', 44);
INSERT INTO school (id, name, institution_id) VALUES (52, 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 44);
INSERT INTO school (id, name, institution_id) VALUES (53, 'ΕΠΙΣΤΗΜΗΣ ΤΗΣ ΠΛΗΡΟΦΟΡΙΑΣ ΚΑΙ ΠΛΗΡΟΦΟΡΙΚΗΣ', 14);
INSERT INTO school (id, name, institution_id) VALUES (54, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΜΕΤΑΦΡΑΣΗΣ-ΔΙΕΡΜΗΝΕΙΑΣ', 14);
INSERT INTO school (id, name, institution_id) VALUES (55, 'ΜΟΥΣΙΚΗΣ ΚΑΙ ΟΠΤΙΚΟΑΚΟΥΣΤΙΚΩΝ ΤΕΧΝΩΝ', 14);
INSERT INTO school (id, name, institution_id) VALUES (56, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 15);
INSERT INTO school (id, name, institution_id) VALUES (57, 'ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΗΣ ΠΛΗΡΟΦΟΡΙΑΣ', 15);
INSERT INTO school (id, name, institution_id) VALUES (58, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 15);
INSERT INTO school (id, name, institution_id) VALUES (59, 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 16);
INSERT INTO school (id, name, institution_id) VALUES (60, 'ΕΠΙΣΤΗΜΩΝ ΔΙΟΙΚΗΣΗΣ', 16);
INSERT INTO school (id, name, institution_id) VALUES (61, 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 16);
INSERT INTO school (id, name, institution_id) VALUES (62, 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 16);
INSERT INTO school (id, name, institution_id) VALUES (63, 'ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 16);
INSERT INTO school (id, name, institution_id) VALUES (64, 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 18);
INSERT INTO school (id, name, institution_id) VALUES (65, 'ΠΑΙΔΑΓΩΓΙΚΗ', 18);
INSERT INTO school (id, name, institution_id) VALUES (66, 'ΠΟΛΥΤΕΧΝΙΚΗ', 18);
INSERT INTO school (id, name, institution_id) VALUES (67, 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 19);
INSERT INTO school (id, name, institution_id) VALUES (68, 'ΓΕΩΠΟΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 19);
INSERT INTO school (id, name, institution_id) VALUES (69, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 19);
INSERT INTO school (id, name, institution_id) VALUES (70, 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 19);
INSERT INTO school (id, name, institution_id) VALUES (71, 'ΠΟΛΥΤΕΧΝΙΚΗ', 19);
INSERT INTO school (id, name, institution_id) VALUES (72, 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 20);
INSERT INTO school (id, name, institution_id) VALUES (73, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 20);
INSERT INTO school (id, name, institution_id) VALUES (74, 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 20);
INSERT INTO school (id, name, institution_id) VALUES (75, 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 20);
INSERT INTO school (id, name, institution_id) VALUES (76, 'ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 20);
INSERT INTO school (id, name, institution_id) VALUES (77, 'ΦΙΛΟΣΟΦΙΚΗ', 20);
INSERT INTO school (id, name, institution_id) VALUES (78, 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 21);
INSERT INTO school (id, name, institution_id) VALUES (79, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 21);
INSERT INTO school (id, name, institution_id) VALUES (80, 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 21);
INSERT INTO school (id, name, institution_id) VALUES (81, 'ΚΟΙΝΩΝΙΚΩΝ, ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 21);
INSERT INTO school (id, name, institution_id) VALUES (82, 'ΦΙΛΟΣΟΦΙΚΗ', 21);
INSERT INTO school (id, name, institution_id) VALUES (83, 'ΕΠΙΣΤΗΜΩΝ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 22);
INSERT INTO school (id, name, institution_id) VALUES (84, 'ΕΠΙΣΤΗΜΩΝ ΠΛΗΡΟΦΟΡΙΑΣ', 22);
INSERT INTO school (id, name, institution_id) VALUES (85, 'ΚΟΙΝΩΝΙΚΩΝ, ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΩΝ', 22);
INSERT INTO school (id, name, institution_id) VALUES (86, 'ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΠΕΡΙΦΕΡΕΙΑΚΩΝ ΣΠΟΥΔΩΝ', 22);
INSERT INTO school (id, name, institution_id) VALUES (87, 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 23);
INSERT INTO school (id, name, institution_id) VALUES (88, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 23);
INSERT INTO school (id, name, institution_id) VALUES (89, 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 23);
INSERT INTO school (id, name, institution_id) VALUES (90, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 23);
INSERT INTO school (id, name, institution_id) VALUES (91, 'ΠΟΛΥΤΕΧΝΙΚΗ', 23);
INSERT INTO school (id, name, institution_id) VALUES (92, 'ΝΑΥΤΙΛΙΑΣ ΚΑΙ ΒΙΟΜΗΧΑΝΙΑΣ', 24);
INSERT INTO school (id, name, institution_id) VALUES (93, 'ΟΙΚΟΝΟΜΙΚΩΝ, ΕΠΙΧΕΙΡΗΜΑΤΙΚΩΝ ΚΑΙ ΔΙΕΘΝΩΝ ΣΠΟΥΔΩΝ', 24);
INSERT INTO school (id, name, institution_id) VALUES (94, 'ΤΕΧΝΟΛΟΓΙΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΩΝ', 24);
INSERT INTO school (id, name, institution_id) VALUES (95, 'ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΣΤΑΤΙΣΤΙΚΗΣ', 24);
INSERT INTO school (id, name, institution_id) VALUES (96, 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΠΟΛΙΤΙΣΜΙΚΩΝ ΣΠΟΥΔΩΝ', 25);
INSERT INTO school (id, name, institution_id) VALUES (97, 'ΕΠΙΣΤΗΜΩΝ ΑΝΘΡΩΠΙΝΗΣ ΚΙΝΗΣΗΣ ΚΑΙ ΠΟΙΟΤΗΤΑΣ ΤΗΣ ΖΩΗΣ', 25);
INSERT INTO school (id, name, institution_id) VALUES (98, 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 25);
INSERT INTO school (id, name, institution_id) VALUES (99, 'ΚΟΙΝΩΝΙΚΩΝ ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 25);
INSERT INTO school (id, name, institution_id) VALUES (100, 'ΟΙΚΟΝΟΜΙΑΣ, ΔΙΟΙΚΗΣΗΣ ΚΑΙ ΠΛΗΡΟΦΟΡΙΚΗΣ', 25);
INSERT INTO school (id, name, institution_id) VALUES (101, 'ΔΙΕΘΝΩΝ ΣΠΟΥΔΩΝ ΕΠΙΚΟΙΝΩΝΙΑΣ ΚΑΙ ΠΟΛΙΤΙΣΜΟΥ', 27);
INSERT INTO school (id, name, institution_id) VALUES (102, 'ΕΠΙΣΤΗΜΩΝ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΔΗΜΟΣΙΑΣ ΔΙΟΙΚΗΣΗΣ', 27);
INSERT INTO school (id, name, institution_id) VALUES (103, 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΨΥΧΟΛΟΓΙΑΣ', 27);
INSERT INTO school (id, name, institution_id) VALUES (104, 'ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 27);
INSERT INTO school (id, name, institution_id) VALUES (105, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 28);
INSERT INTO school (id, name, institution_id) VALUES (106, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', 28);
INSERT INTO school (id, name, institution_id) VALUES (107, 'ΜΗΧΑΝΙΚΩΝ ΟΡΥΚΤΩΝ ΠΟΡΩΝ', 28);
INSERT INTO school (id, name, institution_id) VALUES (108, 'ΜΗΧΑΝΙΚΩΝ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ', 28);
INSERT INTO school (id, name, institution_id) VALUES (109, 'ΜΗΧΑΝΙΚΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 28);
INSERT INTO school (id, name, institution_id) VALUES (110, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 29);
INSERT INTO school (id, name, institution_id) VALUES (111, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 29);
INSERT INTO school (id, name, institution_id) VALUES (112, 'ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 29);
INSERT INTO school (id, name, institution_id) VALUES (113, 'ΤΕΧΝΟΛ. ΤΡΟΦΙΜΩΝ & ΔΙΑΤΡΟΦΗΣ', 29);
INSERT INTO school (id, name, institution_id) VALUES (114, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 29);
INSERT INTO school (id, name, institution_id) VALUES (115, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 33);
INSERT INTO school (id, name, institution_id) VALUES (116, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 33);
INSERT INTO school (id, name, institution_id) VALUES (117, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 33);
INSERT INTO school (id, name, institution_id) VALUES (118, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 33);
INSERT INTO school (id, name, institution_id) VALUES (119, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 33);
INSERT INTO school (id, name, institution_id) VALUES (120, '-', 39);
INSERT INTO school (id, name, institution_id) VALUES (121, '-', 39);
INSERT INTO school (id, name, institution_id) VALUES (122, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 39);
INSERT INTO school (id, name, institution_id) VALUES (123, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 39);
INSERT INTO school (id, name, institution_id) VALUES (124, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 39);
INSERT INTO school (id, name, institution_id) VALUES (125, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 39);
INSERT INTO school (id, name, institution_id) VALUES (126, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 39);
INSERT INTO school (id, name, institution_id) VALUES (127, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 39);
INSERT INTO school (id, name, institution_id) VALUES (128, '-', 30);
INSERT INTO school (id, name, institution_id) VALUES (129, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 30);
INSERT INTO school (id, name, institution_id) VALUES (130, 'ΔΙΟΙΚΗΣΗΣ ΟΙΚΟΝΟΜΙΑΣ', 30);
INSERT INTO school (id, name, institution_id) VALUES (131, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 30);
INSERT INTO school (id, name, institution_id) VALUES (132, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 30);
INSERT INTO school (id, name, institution_id) VALUES (133, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 30);
INSERT INTO school (id, name, institution_id) VALUES (134, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 31);
INSERT INTO school (id, name, institution_id) VALUES (135, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 31);
INSERT INTO school (id, name, institution_id) VALUES (136, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 31);
INSERT INTO school (id, name, institution_id) VALUES (137, 'ΤΕΧΝΟΛΟΓΙΑΣ ΗΧΟΥ ΚΑΙ ΜΟΥΣΙΚΩΝ ΟΡΓΑΝΩΝ', 31);
INSERT INTO school (id, name, institution_id) VALUES (138, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 31);
INSERT INTO school (id, name, institution_id) VALUES (139, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 37);
INSERT INTO school (id, name, institution_id) VALUES (140, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 37);
INSERT INTO school (id, name, institution_id) VALUES (141, 'ΠΑΡΑΡΤΗΜΑ ΚΑΡΔΙΤΣΑΣ', 37);
INSERT INTO school (id, name, institution_id) VALUES (142, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 37);
INSERT INTO school (id, name, institution_id) VALUES (143, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 37);
INSERT INTO school (id, name, institution_id) VALUES (144, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 37);
INSERT INTO school (id, name, institution_id) VALUES (145, '-', 32);
INSERT INTO school (id, name, institution_id) VALUES (146, 'ΔΙΟΙΚΗΣΗΣ ΚΑΙ ΟΙΚΟΝΟΜΙΑΣ', 32);
INSERT INTO school (id, name, institution_id) VALUES (147, 'ΜΟΥΣΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', 32);
INSERT INTO school (id, name, institution_id) VALUES (148, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 32);
INSERT INTO school (id, name, institution_id) VALUES (149, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 32);
INSERT INTO school (id, name, institution_id) VALUES (150, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 41);
INSERT INTO school (id, name, institution_id) VALUES (151, 'ΣΧΟΛΗ ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ & ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 41);
INSERT INTO school (id, name, institution_id) VALUES (152, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 41);
INSERT INTO school (id, name, institution_id) VALUES (153, '-', 35);
INSERT INTO school (id, name, institution_id) VALUES (154, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 35);
INSERT INTO school (id, name, institution_id) VALUES (155, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 35);
INSERT INTO school (id, name, institution_id) VALUES (156, 'ΕΦΑΡΜΟΣΜΕΝΩΝ ΕΠΙΣΤΗΜΩΝ', 35);
INSERT INTO school (id, name, institution_id) VALUES (157, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 35);
INSERT INTO school (id, name, institution_id) VALUES (158, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 35);
INSERT INTO school (id, name, institution_id) VALUES (159, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 40);
INSERT INTO school (id, name, institution_id) VALUES (160, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 40);
INSERT INTO school (id, name, institution_id) VALUES (161, '-', 34);
INSERT INTO school (id, name, institution_id) VALUES (162, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 34);
INSERT INTO school (id, name, institution_id) VALUES (163, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 34);
INSERT INTO school (id, name, institution_id) VALUES (164, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 34);
INSERT INTO school (id, name, institution_id) VALUES (165, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 34);
INSERT INTO school (id, name, institution_id) VALUES (166, '-', 36);
INSERT INTO school (id, name, institution_id) VALUES (167, 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 36);
INSERT INTO school (id, name, institution_id) VALUES (168, 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 36);
INSERT INTO school (id, name, institution_id) VALUES (169, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 36);
INSERT INTO school (id, name, institution_id) VALUES (170, 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 36);
INSERT INTO school (id, name, institution_id) VALUES (171, 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ ΚΑΙ ΑΓΩΓΗΣ', 43);
INSERT INTO school (id, name, institution_id) VALUES (172, 'ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΓΕΩΓΡΑΦΙΑΣ ΚΑΙ ΕΦΑΡΜΟΣΜΕΝΩΝ ΟΙΚΟΝΟΜΙΚΩΝ', 43);
INSERT INTO school (id, name, institution_id) VALUES (173, 'ΨΗΦΙΑΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', 43);
INSERT INTO school (id, name, institution_id) VALUES (1174, '-', 101);
INSERT INTO school (id, name, institution_id) VALUES (1175, '-', 102);
INSERT INTO school (id, name, institution_id) VALUES (1176, '-', 103);
INSERT INTO school (id, name, institution_id) VALUES (1177, '-', 104);
INSERT INTO school (id, name, institution_id) VALUES (1178, '-', 105);
INSERT INTO school (id, name, institution_id) VALUES (1179, '-', 106);
INSERT INTO school (id, name, institution_id) VALUES (1180, '-', 107);
INSERT INTO school (id, name, institution_id) VALUES (1181, '-', 108);
INSERT INTO school (id, name, institution_id) VALUES (1182, '-', 109);
INSERT INTO school (id, name, institution_id) VALUES (1183, '-', 110);
INSERT INTO school (id, name, institution_id) VALUES (1184, '-', 111);
INSERT INTO school (id, name, institution_id) VALUES (1185, '-', 112);
INSERT INTO school (id, name, institution_id) VALUES (1186, '-', 113);
INSERT INTO school (id, name, institution_id) VALUES (1187, '-', 114);
INSERT INTO school (id, name, institution_id) VALUES (1188, '-', 115);
INSERT INTO school (id, name, institution_id) VALUES (1189, '-', 116);
INSERT INTO school (id, name, institution_id) VALUES (1190, '-', 117);
INSERT INTO school (id, name, institution_id) VALUES (1191, '-', 118);
INSERT INTO school (id, name, institution_id) VALUES (1192, '-', 119);
INSERT INTO school (id, name, institution_id) VALUES (1193, '-', 120);
INSERT INTO school (id, name, institution_id) VALUES (1194, '-', 121);
INSERT INTO school (id, name, institution_id) VALUES (1195, '-', 122);
INSERT INTO school (id, name, institution_id) VALUES (1196, '-', 123);
INSERT INTO school (id, name, institution_id) VALUES (1197, '-', 124);
INSERT INTO school (id, name, institution_id) VALUES (1198, '-', 125);
INSERT INTO school (id, name, institution_id) VALUES (1199, '-', 126);
INSERT INTO school (id, name, institution_id) VALUES (1200, '-', 127);
INSERT INTO school (id, name, institution_id) VALUES (1201, '-', 128);
INSERT INTO school (id, name, institution_id) VALUES (1202, '-', 129);
INSERT INTO school (id, name, institution_id) VALUES (1203, '-', 130);
INSERT INTO school (id, name, institution_id) VALUES (1204, '-', 131);
INSERT INTO school (id, name, institution_id) VALUES (1205, '-', 132);
INSERT INTO school (id, name, institution_id) VALUES (1206, '-', 133);
INSERT INTO school (id, name, institution_id) VALUES (1207, '-', 134);
INSERT INTO school (id, name, institution_id) VALUES (1208, '-', 135);
INSERT INTO school (id, name, institution_id) VALUES (1209, '-', 136);
INSERT INTO school (id, name, institution_id) VALUES (1210, '-', 137);
INSERT INTO school (id, name, institution_id) VALUES (1211, '-', 138);
INSERT INTO school (id, name, institution_id) VALUES (1212, '-', 139);
INSERT INTO school (id, name, institution_id) VALUES (1213, '-', 140);
INSERT INTO school (id, name, institution_id) VALUES (1214, '-', 141);
INSERT INTO school (id, name, institution_id) VALUES (1215, '-', 142);
INSERT INTO school (id, name, institution_id) VALUES (1216, '-', 143);
INSERT INTO school (id, name, institution_id) VALUES (1217, '-', 144);
INSERT INTO school (id, name, institution_id) VALUES (1218, '-', 145);
--
INSERT INTO school (id, name, institution_id) VALUES (1219, '-', 146);
INSERT INTO school (id, name, institution_id) VALUES (1220, '-', 147);
INSERT INTO school (id, name, institution_id) VALUES (1221, '-', 148);

INSERT INTO department (id, name, school_id) VALUES (97, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 26);
INSERT INTO department (id, name, school_id) VALUES (98, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 100);
INSERT INTO department (id, name, school_id) VALUES (99, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 70);
INSERT INTO department (id, name, school_id) VALUES (101, 'ΘΕΟΛΟΓΙΑΣ', 35);
INSERT INTO department (id, name, school_id) VALUES (102, 'ΦΙΛΟΣΟΦΙΑΣ', 87);
INSERT INTO department (id, name, school_id) VALUES (103, 'ΘΕΟΛΟΓΙΑΣ', 10);
INSERT INTO department (id, name, school_id) VALUES (104, 'ΙΣΤΟΡΙΑΣ, ΑΡΧΑΙΟΛΟΓΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΠΟΛΙΤΙΣΜΙΚΩΝ ΑΓΑΘΩΝ', 96);
INSERT INTO department (id, name, school_id) VALUES (105, 'ΚΟΙΝΩΝΙΚΗΣ ΘΕΟΛΟΓΙΑΣ', 35);
INSERT INTO department (id, name, school_id) VALUES (106, 'ΕΛΛΗΝΙΚΗΣ ΦΙΛΟΛΟΓΙΑΣ', 25);
INSERT INTO department (id, name, school_id) VALUES (107, 'ΠΟΙΜΑΝΤΙΚΗΣ ΚΑΙ  ΚΟΙΝΩΝΙΚΗΣ ΘΕΟΛΟΓΙΑΣ', 10);
INSERT INTO department (id, name, school_id) VALUES (108, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΕΘΝΟΛΟΓΙΑΣ', 25);
INSERT INTO department (id, name, school_id) VALUES (109, 'ΦΙΛΟΛΟΓΙΑΣ', 39);
INSERT INTO department (id, name, school_id) VALUES (110, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 39);
INSERT INTO department (id, name, school_id) VALUES (111, 'ΦΙΛΟΛΟΓΙΑΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (112, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (113, 'ΦΙΛΟΛΟΓΙΑΣ', 77);
INSERT INTO department (id, name, school_id) VALUES (114, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 77);
INSERT INTO department (id, name, school_id) VALUES (115, 'ΦΙΛΟΛΟΓΙΑΣ', 82);
INSERT INTO department (id, name, school_id) VALUES (116, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 82);
INSERT INTO department (id, name, school_id) VALUES (117, 'ΝΟΜΙΚΗΣ', 37);
INSERT INTO department (id, name, school_id) VALUES (118, 'ΦΙΛΟΣΟΦΙΑΣ - ΠΑΙΔΑΓΩΓΙΚΗΣ ΚΑΙ ΨΥΧΟΛΟΓΙΑΣ', 39);
INSERT INTO department (id, name, school_id) VALUES (119, 'ΝΟΜΙΚΗΣ', 13);
INSERT INTO department (id, name, school_id) VALUES (120, 'ΦΙΛΟΣΟΦΙΑΣ & ΠΑΙΔΑΓΩΓΙΚΗΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (121, 'ΝΟΜΙΚΗΣ', 27);
INSERT INTO department (id, name, school_id) VALUES (122, 'ΦΙΛΟΣΟΦΙΑΣ ΠΑΙΔΑΓΩΓΙΚΗΣ ΚΑΙ ΨΥΧΟΛΟΓΙΑΣ', 77);
INSERT INTO department (id, name, school_id) VALUES (123, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΔΗΜΟΣΙΑΣ ΔΙΟΙΚΗΣΗΣ', 38);
INSERT INTO department (id, name, school_id) VALUES (124, 'ΔΗΜΟΣΙΑΣ ΔΙΟΙΚΗΣΗΣ', 102);
INSERT INTO department (id, name, school_id) VALUES (125, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΙΣΤΟΡΙΑΣ', 104);
INSERT INTO department (id, name, school_id) VALUES (126, 'ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', 103);
INSERT INTO department (id, name, school_id) VALUES (127, 'ΑΓΓΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 39);
INSERT INTO department (id, name, school_id) VALUES (128, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 33);
INSERT INTO department (id, name, school_id) VALUES (129, 'ΑΓΓΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (130, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 72);
INSERT INTO department (id, name, school_id) VALUES (131, 'ΓΑΛΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 39);
INSERT INTO department (id, name, school_id) VALUES (132, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 78);
INSERT INTO department (id, name, school_id) VALUES (133, 'ΓΑΛΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (134, 'ΕΠΙΣΤΗΜΩΝ ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΕΚΠΑΙΔΕΥΣΗΣ', 15);
INSERT INTO department (id, name, school_id) VALUES (135, 'ΓΕΡΜΑΝΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 39);
INSERT INTO department (id, name, school_id) VALUES (136, 'ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ ΚΑΙ ΤΗΣ ΑΓΩΓΗΣ ΣΤΗΝ ΠΡΟΣΧΟΛΙΚΗ ΗΛΙΚΙΑ', 87);
INSERT INTO department (id, name, school_id) VALUES (137, 'ΓΕΡΜΑΝΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (138, 'ΦΙΛΟΣΟΦΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΣΠΟΥΔΩΝ', 82);
INSERT INTO department (id, name, school_id) VALUES (139, 'ΙΤΑΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (140, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 15);
INSERT INTO department (id, name, school_id) VALUES (141, 'ΠΑΙΔΑΓΩΓΙΚΟ  ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 87);
INSERT INTO department (id, name, school_id) VALUES (142, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ  ΕΚΠΑΙΔΕΥΣΗΣ', 22);
INSERT INTO department (id, name, school_id) VALUES (143, 'ΠΑΙΔΑΓΩΓΙΚΟ  ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 59);
INSERT INTO department (id, name, school_id) VALUES (144, 'ΟΙΚΙΑΚΗΣ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΟΙΚΟΛΟΓΙΑΣ', 172);
INSERT INTO department (id, name, school_id) VALUES (145, 'ΙΣΤΟΡΙΑΣ', 54);
INSERT INTO department (id, name, school_id) VALUES (146, 'ΘΕΑΤΡΙΚΩΝ ΣΠΟΥΔΩΝ', 39);
INSERT INTO department (id, name, school_id) VALUES (147, 'ΔΗΜΟΣΙΟΓΡΑΦΙΑΣ ΚΑΙ ΜΕΣΩΝ ΜΑΖΙΚΗΣ ΕΝΗΜΕΡΩΣΗΣ', 14);
INSERT INTO department (id, name, school_id) VALUES (148, 'ΕΠΙΚΟΙΝΩΝΙΑΣ & ΜΜΕ', 38);
INSERT INTO department (id, name, school_id) VALUES (149, 'ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', 81);
INSERT INTO department (id, name, school_id) VALUES (150, 'ΔΙΕΘΝΩΝ ΚΑΙ ΕΥΡΩΠΑΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ ΣΠΟΥΔΩΝ', 58);
INSERT INTO department (id, name, school_id) VALUES (151, 'ΨΥΧΟΛΟΓΙΑΣ', 81);
INSERT INTO department (id, name, school_id) VALUES (152, 'ΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΠΕΡΙΦΕΡΕΙΑΚΗΣ ΑΝΑΠΤΥΞΗΣ', 102);
INSERT INTO department (id, name, school_id) VALUES (153, 'ΕΠΙΚΟΙΝΩΝΙΑΣ, ΜΕΣΩΝ ΚΑΙ ΠΟΛΙΤΙΣΜΟΥ', 101);
INSERT INTO department (id, name, school_id) VALUES (154, 'ΕΚΠΑΙΔΕΥΣΗΣ ΚΑΙ ΑΓΩΓΗΣ ΣΤΗΝ ΠΡΟΣΧΟΛΙΚΗ ΗΛΙΚΙΑ', 33);
INSERT INTO department (id, name, school_id) VALUES (155, 'ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΤΡΑΠΕΖΙΚΗΣ ΔΙΟΙΚΗΤΙΚΗΣ', 95);
INSERT INTO department (id, name, school_id) VALUES (156, 'ΠΑΙΔΑΓΩΓΙΚΟ ΝΗΠΙΑΓΩΓΩΝ', 72);
INSERT INTO department (id, name, school_id) VALUES (157, 'ΝΑΥΤΙΛΙΑΚΩΝ ΣΠΟΥΔΩΝ', 92);
INSERT INTO department (id, name, school_id) VALUES (158, 'ΠΑΙΔΑΓΩΓΙΚΟ ΠΡΟΣΧΟΛΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 78);
INSERT INTO department (id, name, school_id) VALUES (159, 'ΚΟΙΝΩΝΙΚΗΣ ΠΟΛΙΤΙΚΗΣ', 104);
INSERT INTO department (id, name, school_id) VALUES (160, 'ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ ΣΤΗΝ ΠΡΟΣΧΟΛΙΚΗ ΗΛΙΚΙΑ', 22);
INSERT INTO department (id, name, school_id) VALUES (161, 'ΔΙΕΘΝΩΝ ΚΑΙ ΕΥΡΩΠΑΙΚΩΝ ΣΠΟΥΔΩΝ', 85);
INSERT INTO department (id, name, school_id) VALUES (162, 'ΕΠΙΣΤΗΜΩΝ ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΕΚΠΑΙΔΕΥΤΙΚΟΥ ΣΧΕΔΙΑΣΜΟΥ', 59);
INSERT INTO department (id, name, school_id) VALUES (163, 'ΚΙΝΗΜΑΤΟΓΡΑΦΟΥ', 12);
INSERT INTO department (id, name, school_id) VALUES (164, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 67);
INSERT INTO department (id, name, school_id) VALUES (165, 'ΚΟΙΝΩΝΙΚΗΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ', 103);
INSERT INTO department (id, name, school_id) VALUES (166, 'ΠΑΙΔΑΓΩΓΙΚΟ ΠΡΟΣΧΟΛΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 67);
INSERT INTO department (id, name, school_id) VALUES (167, 'ΚΟΙΝΩΝΙΚΗΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ ΚΑΙ ΙΣΤΟΡΙΑΣ', 62);
INSERT INTO department (id, name, school_id) VALUES (168, 'ΘΕΑΤΡΟΥ', 12);
INSERT INTO department (id, name, school_id) VALUES (169, 'ΘΕΑΤΡΙΚΩΝ ΣΠΟΥΔΩΝ', 87);
INSERT INTO department (id, name, school_id) VALUES (170, 'ΨΥΧΟΛΟΓΙΑΣ', 103);
INSERT INTO department (id, name, school_id) VALUES (171, 'ΨΥΧΟΛΟΓΙΑΣ', 39);
INSERT INTO department (id, name, school_id) VALUES (172, 'ΨΥΧΟΛΟΓΙΑΣ', 17);
INSERT INTO department (id, name, school_id) VALUES (173, 'ΜΕΘΟΔΟΛΟΓΙΑΣ ΙΣΤΟΡΙΑΣ ΚΑΙ ΘΕΩΡΙΑΣ ΤΗΣ ΕΠΙΣΤΗΜΗΣ', 36);
INSERT INTO department (id, name, school_id) VALUES (174, 'ΕΚΠΑΙΔΕΥΤΙΚΗΣ ΚΑΙ ΚΟΙΝΩΝΙΚΗΣ ΠΟΛΙΤΙΚΗΣ', 85);
INSERT INTO department (id, name, school_id) VALUES (175, 'ΦΙΛΟΛΟΓΙΑΣ', 87);
INSERT INTO department (id, name, school_id) VALUES (176, 'ΒΑΛΚΑΝΙΚΩΝ, ΣΛΑΒΙΚΩΝ ΚΑΙ ΑΝΑΤΟΛΙΚΩΝ ΣΠΟΥΔΩΝ', 86);
INSERT INTO department (id, name, school_id) VALUES (177, 'ΙΣΤΟΡΙΑΣ, ΑΡΧΑΙΟΛΟΓΙΑΣ ΚΑΙ ΚΟΙΝΩΝΙΚΗΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ', 67);
INSERT INTO department (id, name, school_id) VALUES (178, 'ΠΑΙΔΑΓΩΓΙΚΟ ΤΜΗΜΑ ΕΙΔΙΚΗΣ ΑΓΩΓΗΣ', 67);
INSERT INTO department (id, name, school_id) VALUES (179, 'ΔΙΕΘΝΩΝ ΕΥΡΩΠΑΙΚΩΝ ΚΑΙ ΠΕΡΙΦΕΡΕΙΑΚΩΝ ΣΠΟΥΔΩΝ', 101);
INSERT INTO department (id, name, school_id) VALUES (180, 'ΝΑΥΤΙΛΙΑΣ ΚΑΙ ΕΠΙΧΕΙΡΗΜΑΤΙΚΩΝ ΥΠΗΡΕΣΙΩΝ', 60);
INSERT INTO department (id, name, school_id) VALUES (181, 'ΜΕΣΟΓΕΙΑΚΩΝ ΣΠΟΥΔΩΝ', 59);
INSERT INTO department (id, name, school_id) VALUES (182, 'ΙΤΑΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ ', 39);
INSERT INTO department (id, name, school_id) VALUES (183, 'ΙΣΠΑΝΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ ', 39);
INSERT INTO department (id, name, school_id) VALUES (185, 'ΠΛΑΣΤΙΚΩΝ ΤΕΧΝΩΝ & ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΤΕΧΝΗΣ', 75);
INSERT INTO department (id, name, school_id) VALUES (186, 'ΓΛΩΣΣΑΣ ΦΙΛΟΛΟΓΙΑΣ ΚΑΙ ΠΟΛΙΤΙΣΜΟΥ ΠΑΡΕΥΞΕΙΝΙΩΝ ΧΩΡΩΝ', 25);
INSERT INTO department (id, name, school_id) VALUES (187, 'ΚΟΙΝΩΝΙΚΗΣ ΚΑΙ ΕΚΠΑΙΔΕΥΤΙΚΗΣ ΠΟΛΙΤΙΚΗΣ', 99);
INSERT INTO department (id, name, school_id) VALUES (188, 'ΤΟΥΡΚΙΚΩΝ ΣΠΟΥΔΩΝ ΚΑΙ ΣΥΓΧΡΟΝΩΝ ΑΣΙΑΤΙΚΩΝ ΣΠΟΥΔΩΝ', 38);
INSERT INTO department (id, name, school_id) VALUES (189, 'ΦΙΛΟΛΟΓΙΑΣ', 96);
INSERT INTO department (id, name, school_id) VALUES (190, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 97);
INSERT INTO department (id, name, school_id) VALUES (192, 'ΣΛΑΒΙΚΩΝ ΣΠΟΥΔΩΝ', 39);
INSERT INTO department (id, name, school_id) VALUES (200, 'ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΥΛΙΚΩΝ', 80);
INSERT INTO department (id, name, school_id) VALUES (201, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 47);
INSERT INTO department (id, name, school_id) VALUES (203, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 16);
INSERT INTO department (id, name, school_id) VALUES (205, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 91);
INSERT INTO department (id, name, school_id) VALUES (207, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 28);
INSERT INTO department (id, name, school_id) VALUES (208, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 71);
INSERT INTO department (id, name, school_id) VALUES (209, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 45);
INSERT INTO department (id, name, school_id) VALUES (210, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 66);
INSERT INTO department (id, name, school_id) VALUES (211, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 16);
INSERT INTO department (id, name, school_id) VALUES (212, 'ΔΑΣΟΛΟΓΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΦΥΣΙΚΩΝ ΠΟΡΩΝ', 23);
INSERT INTO department (id, name, school_id) VALUES (213, 'ΜΗΧΑΝΟΛΟΓΩΝ &  ΑΕΡΟΝΑΥΠΗΓΩΝ ΜΗΧΑΝΙΚΩΝ', 91);
INSERT INTO department (id, name, school_id) VALUES (214, 'ΜΗΧΑΝΙΚΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 109);
INSERT INTO department (id, name, school_id) VALUES (215, 'ΜΗΧΑΝΙΚΩΝ ΗΛΕΚΤΡΟΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ ΚΑΙ ΠΛΗΡΟΦΟΡΙΚΗΣ', 91);
INSERT INTO department (id, name, school_id) VALUES (216, 'ΕΠΙΣΤΗΜΗΣ ΥΠΟΛΟΓΙΣΤΩΝ', 80);
INSERT INTO department (id, name, school_id) VALUES (217, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', 43);
INSERT INTO department (id, name, school_id) VALUES (219, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', 16);
INSERT INTO department (id, name, school_id) VALUES (220, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', 71);
INSERT INTO department (id, name, school_id) VALUES (221, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΥΠΟΛΟΓΙΣΤΩΝ', 91);
INSERT INTO department (id, name, school_id) VALUES (222, 'ΜΗΧΑΝΙΚΩΝ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ', 60);
INSERT INTO department (id, name, school_id) VALUES (223, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ & ΜΗΧΑΝΙΚΩΝ Η/Υ', 28);
INSERT INTO department (id, name, school_id) VALUES (224, 'ΜΗΧΑΝΙΚΩΝ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ', 28);
INSERT INTO department (id, name, school_id) VALUES (225, 'ΑΓΡΟΝΟΜΩΝ ΚΑΙ ΤΟΠΟΓΡΑΦΩΝ ΜΗΧΑΝΙΚΩΝ', 40);
INSERT INTO department (id, name, school_id) VALUES (227, 'ΑΓΡΟΝΟΜΩΝ ΚΑΙ ΤΟΠΟΓΡΑΦΩΝ ΜΗΧΑΝΙΚΩΝ', 16);
INSERT INTO department (id, name, school_id) VALUES (228, 'ΜΗΧΑΝΙΚΩΝ ΧΩΡΟΤΑΞΙΑΣ, ΠΟΛΕΟΔΟΜΙΑΣ ΚΑΙ ΠΕΡΙΦΕΡΕΙΑΚΗΣ ΑΝΑΠΤΥΞΗΣ', 71);
INSERT INTO department (id, name, school_id) VALUES (229, 'ΝΑΥΠΗΓΩΝ ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 46);
INSERT INTO department (id, name, school_id) VALUES (230, 'ΜΗΧΑΝΙΚΩΝ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ', 108);
INSERT INTO department (id, name, school_id) VALUES (231, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 41);
INSERT INTO department (id, name, school_id) VALUES (232, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 91);
INSERT INTO department (id, name, school_id) VALUES (233, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 16);
INSERT INTO department (id, name, school_id) VALUES (234, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 28);
INSERT INTO department (id, name, school_id) VALUES (235, 'ΧΗΜΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 48);
INSERT INTO department (id, name, school_id) VALUES (236, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 71);
INSERT INTO department (id, name, school_id) VALUES (237, 'ΧΗΜΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 16);
INSERT INTO department (id, name, school_id) VALUES (238, 'ΜΗΧΑΝΙΚΩΝ ΣΧΕΔΙΑΣΗΣ ΠΡΟΙΟΝΤΩΝ ΚΑΙ ΣΥΣΤΗΜΑΤΩΝ', 61);
INSERT INTO department (id, name, school_id) VALUES (239, 'ΧΗΜΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 91);
INSERT INTO department (id, name, school_id) VALUES (240, 'ΔΙΟΙΚΗΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', 56);
INSERT INTO department (id, name, school_id) VALUES (241, 'ΜΗΧΑΝΙΚΩΝ ΜΕΤΑΛΛΕΙΩΝ ΜΕΤΑΛΛΟΥΡΓΩΝ', 44);
INSERT INTO department (id, name, school_id) VALUES (242, 'ΜΗΧΑΝΙΚΩΝ ΟΡΥΚΤΩΝ ΠΟΡΩΝ', 107);
INSERT INTO department (id, name, school_id) VALUES (243, 'ΜΑΘΗΜΑΤΙΚΩΝ', 36);
INSERT INTO department (id, name, school_id) VALUES (245, 'ΜΑΘΗΜΑΤΙΚΩΝ', 11);
INSERT INTO department (id, name, school_id) VALUES (246, 'ΕΦΑΡΜΟΣΜΕΝΩΝ ΜΑΘΗΜΑΤΙΚΩΝ ΚΑΙ ΦΥΣΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 42);
INSERT INTO department (id, name, school_id) VALUES (247, 'ΜΑΘΗΜΑΤΙΚΩΝ', 89);
INSERT INTO department (id, name, school_id) VALUES (249, 'ΜΑΘΗΜΑΤΙΚΩΝ', 74);
INSERT INTO department (id, name, school_id) VALUES (250, 'ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΘΑΛΑΣΣΑΣ', 63);
INSERT INTO department (id, name, school_id) VALUES (253, 'ΦΥΣΙΚΗΣ', 36);
INSERT INTO department (id, name, school_id) VALUES (255, 'ΦΥΣΙΚΗΣ', 11);
INSERT INTO department (id, name, school_id) VALUES (257, 'ΦΥΣΙΚΗΣ', 89);
INSERT INTO department (id, name, school_id) VALUES (259, 'ΦΥΣΙΚΗΣ', 74);
INSERT INTO department (id, name, school_id) VALUES (261, 'ΦΥΣΙΚΗΣ', 80);
INSERT INTO department (id, name, school_id) VALUES (262, 'ΨΗΦΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ', 94);
INSERT INTO department (id, name, school_id) VALUES (263, 'ΧΗΜΕΙΑΣ', 36);
INSERT INTO department (id, name, school_id) VALUES (265, 'ΧΗΜΕΙΑΣ', 11);
INSERT INTO department (id, name, school_id) VALUES (267, 'ΧΗΜΕΙΑΣ', 89);
INSERT INTO department (id, name, school_id) VALUES (269, 'ΧΗΜΕΙΑΣ', 74);
INSERT INTO department (id, name, school_id) VALUES (270, 'ΧΗΜΕΙΑΣ', 80);
INSERT INTO department (id, name, school_id) VALUES (271, 'ΔΙΑΧΕΙΡΙΣΗΣ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΦΥΣΙΚΩΝ ΠΟΡΩΝ', 91);
INSERT INTO department (id, name, school_id) VALUES (272, 'ΜΗΧΑΝΙΚΩΝ ΕΠΙΣΤΗΜΗΣ ΥΛΙΚΩΝ', 74);
INSERT INTO department (id, name, school_id) VALUES (273, 'ΓΕΩΠΟΝΙΑΣ', 7);
INSERT INTO department (id, name, school_id) VALUES (274, 'ΓΕΩΠΟΝΙΑΣ ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΑΓΡΟΤΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 68);
INSERT INTO department (id, name, school_id) VALUES (275, 'ΔΑΣΟΛΟΓΙΑΣ ΚΑΙ ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 7);
INSERT INTO department (id, name, school_id) VALUES (276, 'ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 63);
INSERT INTO department (id, name, school_id) VALUES (277, 'ΒΙΟΛΟΓΙΑΣ', 36);
INSERT INTO department (id, name, school_id) VALUES (279, 'ΒΙΟΛΟΓΙΑΣ', 11);
INSERT INTO department (id, name, school_id) VALUES (280, 'ΒΙΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΩΝ', 73);
INSERT INTO department (id, name, school_id) VALUES (281, 'ΒΙΟΛΟΓΙΑΣ', 89);
INSERT INTO department (id, name, school_id) VALUES (282, 'ΒΙΟΛΟΓΙΑΣ', 80);
INSERT INTO department (id, name, school_id) VALUES (283, 'ΓΕΩΛΟΓΙΑΣ ΚΑΙ ΓΕΩΠΕΡΙΒΑΛΛΟΝΤΟΣ', 36);
INSERT INTO department (id, name, school_id) VALUES (284, 'ΒΙΟΧΗΜΕΙΑΣ ΚΑΙ ΒΙΟΤΕΧΝΟΛΟΓΙΑΣ', 69);
INSERT INTO department (id, name, school_id) VALUES (285, 'ΓΕΩΛΟΓΙΑΣ', 11);
INSERT INTO department (id, name, school_id) VALUES (287, 'ΓΕΩΛΟΓΙΑΣ', 89);
INSERT INTO department (id, name, school_id) VALUES (8012, '-', 31);
INSERT INTO department (id, name, school_id) VALUES (288, 'ΕΠΙΣΤΗΜΗΣ ΤΩΝ ΥΛΙΚΩΝ', 89);
INSERT INTO department (id, name, school_id) VALUES (289, 'ΦΑΡΜΑΚΕΥΤΙΚΗΣ', 34);
INSERT INTO department (id, name, school_id) VALUES (290, 'ΜΟΡΙΑΚΗΣ ΒΙΟΛΟΓΙΑΣ ΚΑΙ ΓΕΝΕΤΙΚΗΣ', 24);
INSERT INTO department (id, name, school_id) VALUES (291, 'ΦΑΡΜΑΚΕΥΤΙΚΗΣ', 9);
INSERT INTO department (id, name, school_id) VALUES (293, 'ΦΑΡΜΑΚΕΥΤΙΚΗΣ', 88);
INSERT INTO department (id, name, school_id) VALUES (294, 'ΕΠΙΣΤΗΜΗΣ ΔΙΑΙΤΟΛΟΓΙΑΣ - ΔΙΑΤΡΟΦΗΣ', 171);
INSERT INTO department (id, name, school_id) VALUES (295, 'ΙΑΤΡΙΚΗΣ', 34);
INSERT INTO department (id, name, school_id) VALUES (297, 'ΙΑΤΡΙΚΗΣ', 9);
INSERT INTO department (id, name, school_id) VALUES (299, 'ΙΑΤΡΙΚΗΣ', 88);
INSERT INTO department (id, name, school_id) VALUES (300, 'ΙΑΤΡΙΚΗΣ', 69);
INSERT INTO department (id, name, school_id) VALUES (301, 'ΙΑΤΡΙΚΗΣ', 73);
INSERT INTO department (id, name, school_id) VALUES (302, 'ΙΑΤΡΙΚΗΣ', 24);
INSERT INTO department (id, name, school_id) VALUES (303, 'ΟΔΟΝΤΙΑΤΡΙΚΗΣ', 34);
INSERT INTO department (id, name, school_id) VALUES (304, 'ΙΑΤΡΙΚΗΣ', 79);
INSERT INTO department (id, name, school_id) VALUES (305, 'ΟΔΟΝΤΙΑΤΡΙΚΗΣ', 9);
INSERT INTO department (id, name, school_id) VALUES (306, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 34);
INSERT INTO department (id, name, school_id) VALUES (307, 'ΚΤΗΝΙΑΤΡΙΚΗΣ', 9);
INSERT INTO department (id, name, school_id) VALUES (308, 'ΚΤΗΝΙΑΤΡΙΚΗΣ', 69);
INSERT INTO department (id, name, school_id) VALUES (309, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 38);
INSERT INTO department (id, name, school_id) VALUES (310, 'ΓΕΩΓΡΑΦΙΑΣ', 62);
INSERT INTO department (id, name, school_id) VALUES (311, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 14);
INSERT INTO department (id, name, school_id) VALUES (312, 'ΟΙΚΟΝΟΜΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', 58);
INSERT INTO department (id, name, school_id) VALUES (313, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 56);
INSERT INTO department (id, name, school_id) VALUES (314, 'ΜΑΡΚΕΤΙΝΓΚ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ', 56);
INSERT INTO department (id, name, school_id) VALUES (315, 'ΟΙΚΟΝΟΜΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', 93);
INSERT INTO department (id, name, school_id) VALUES (316, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 93);
INSERT INTO department (id, name, school_id) VALUES (317, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 86);
INSERT INTO department (id, name, school_id) VALUES (318, 'ΣΤΑΤΙΣΤΙΚΗΣ ΚΑΙ ΑΣΦΑΛΙΣΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', 95);
INSERT INTO department (id, name, school_id) VALUES (319, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 90);
INSERT INTO department (id, name, school_id) VALUES (320, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 60);
INSERT INTO department (id, name, school_id) VALUES (321, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 81);
INSERT INTO department (id, name, school_id) VALUES (322, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 83);
INSERT INTO department (id, name, school_id) VALUES (323, 'ΕΠΙΣΤΗΜΗΣ ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 19);
INSERT INTO department (id, name, school_id) VALUES (324, 'ΕΠΙΣΤΗΜΗΣ ΖΩΙΚΗΣ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΥΔΑΤΟΚΑΛΛΙΕΡΓΕΙΩΝ', 19);
INSERT INTO department (id, name, school_id) VALUES (325, 'ΒΙΟΤΕΧΝΟΛΟΓΙΑΣ', 20);
INSERT INTO department (id, name, school_id) VALUES (326, 'ΑΓΡΟΤΙΚΗΣ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΑΝΑΠΤΥΞΗΣ', 20);
INSERT INTO department (id, name, school_id) VALUES (327, 'ΑΞΙΟΠΟΙΗΣΗΣ ΦΥΣΙΚΩΝ ΠΟΡΩΝ ΚΑΙ ΓΕΩΡΓΙΚΗΣ ΜΗΧΑΝΙΚΗΣ', 19);
INSERT INTO department (id, name, school_id) VALUES (328, 'ΕΠΙΣΤΗΜΗΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ ΤΟΥ ΑΝΘΡΩΠΟΥ', 20);
INSERT INTO department (id, name, school_id) VALUES (329, 'ΣΤΑΤΙΣΤΙΚΗΣ', 57);
INSERT INTO department (id, name, school_id) VALUES (330, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 36);
INSERT INTO department (id, name, school_id) VALUES (331, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', 106);
INSERT INTO department (id, name, school_id) VALUES (332, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 71);
INSERT INTO department (id, name, school_id) VALUES (333, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 57);
INSERT INTO department (id, name, school_id) VALUES (334, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 65);
INSERT INTO department (id, name, school_id) VALUES (335, 'ΕΦΑΡΜΟΣΜΕΝΗΣ ΠΛΗΡΟΦΟΡΙΚΗΣ', 84);
INSERT INTO department (id, name, school_id) VALUES (336, 'ΒΙΟΜΗΧΑΝΙΚΗΣ ΔΙΟΙΚΗΣΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', 92);
INSERT INTO department (id, name, school_id) VALUES (337, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 83);
INSERT INTO department (id, name, school_id) VALUES (338, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 11);
INSERT INTO department (id, name, school_id) VALUES (339, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 94);
INSERT INTO department (id, name, school_id) VALUES (340, 'ΜΗΧΑΝΙΚΩΝ ΗΛΕΚΤΡΟΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ ΚΑΙ ΠΛΗΡΟΦΟΡΙΚΗΣ', 74);
INSERT INTO department (id, name, school_id) VALUES (341, 'ΠΑΙΔΑΓΩΓΙΚΟ ΝΗΠΙΑΓΩΓΩΝ', 65);
INSERT INTO department (id, name, school_id) VALUES (342, 'ΑΡΧΕΙΟΝΟΜΙΑΣ, ΒΙΒΛΙΟΘΗΚΟΝΟΜΙΑΣ ΚΑΙ ΜΟΥΣΕΙΟΛΟΓΙΑΣ', 53);
INSERT INTO department (id, name, school_id) VALUES (344, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΑΚΩΝ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ', 61);
INSERT INTO department (id, name, school_id) VALUES (345, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 76);
INSERT INTO department (id, name, school_id) VALUES (346, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ΑΓΡΟΤΙΚΩΝ ΠΡΟΙΟΝΤΩΝ ΚΑΙ ΤΡΟΦΙΜΩΝ', 90);
INSERT INTO department (id, name, school_id) VALUES (347, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 56);
INSERT INTO department (id, name, school_id) VALUES (348, 'ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', 62);
INSERT INTO department (id, name, school_id) VALUES (350, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 67);
INSERT INTO department (id, name, school_id) VALUES (351, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', 81);
INSERT INTO department (id, name, school_id) VALUES (352, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 90);
INSERT INTO department (id, name, school_id) VALUES (353, 'ΑΓΡΟΤΙΚΗΣ ΑΝΑΠΤΥΞΗΣ', 23);
INSERT INTO department (id, name, school_id) VALUES (354, 'ΠΟΛΙΤΙΣΜΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ', 62);
INSERT INTO department (id, name, school_id) VALUES (355, 'ΔΙΕΘΝΩΝ ΚΑΙ ΕΥΡΩΠΑΙΚΩΝ ΣΠΟΥΔΩΝ', 93);
INSERT INTO department (id, name, school_id) VALUES (356, 'ΓΕΩΓΡΑΦΙΑΣ', 172);
INSERT INTO department (id, name, school_id) VALUES (357, 'ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 14);
INSERT INTO department (id, name, school_id) VALUES (360, 'ΓΕΩΠΟΝΙΑΣ ΙΧΘΥΟΛΟΓΙΑΣ ΚΑΙ ΥΔΑΤΙΝΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 68);
INSERT INTO department (id, name, school_id) VALUES (361, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 100);
INSERT INTO department (id, name, school_id) VALUES (362, 'ΘΕΑΤΡΙΚΩΝ ΣΠΟΥΔΩΝ', 98);
INSERT INTO department (id, name, school_id) VALUES (363, 'ΜΗΧΑΝΙΚΩΝ ΧΩΡΟΤΑΞΙΑΣ ΚΑΙ ΑΝΑΠΤΥΞΗΣ', 16);
INSERT INTO department (id, name, school_id) VALUES (366, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 53);
INSERT INTO department (id, name, school_id) VALUES (367, 'ΤΕΧΝΩΝ ΗΧΟΥ ΚΑΙ ΕΙΚΟΝΑΣ', 55);
INSERT INTO department (id, name, school_id) VALUES (368, 'ΔΙΑΧΕΙΡΙΣΗΣ ΠΟΛΙΤΙΣΜΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΝΕΩΝ ΤΕΧΝΟΛΟΓΙΩΝ', 90);
INSERT INTO department (id, name, school_id) VALUES (369, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΜΕ ΕΦΑΡΜΟΓΕΣ ΣΤΗ ΒΙΟΙΑΤΡΙΚΗ', 70);
INSERT INTO department (id, name, school_id) VALUES (370, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 105);
INSERT INTO department (id, name, school_id) VALUES (371, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 66);
INSERT INTO department (id, name, school_id) VALUES (372, 'ΕΠΙΣΤΗΜΗΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 63);
INSERT INTO department (id, name, school_id) VALUES (384, 'ΘΕΩΡΙΑΣ ΚΑΙ ΙΣΤΟΡΙΑΣ ΤΗΣ ΤΕΧΝΗΣ', 6);
INSERT INTO department (id, name, school_id) VALUES (385, 'ΞΕΝΩΝ ΓΛΩΣΣΩΝ, ΜΕΤΑΦΡΑΣΗΣ ΚΑΙ ΔΙΕΡΜΗΝΕΙΑΣ', 54);
INSERT INTO department (id, name, school_id) VALUES (400, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΑΘΛΗΤΙΣΜΟΥ', 97);
INSERT INTO department (id, name, school_id) VALUES (401, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 32);
INSERT INTO department (id, name, school_id) VALUES (402, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ  (ΣΕΡΡΩΝ)', 8);
INSERT INTO department (id, name, school_id) VALUES (8013, '-', 30);
INSERT INTO department (id, name, school_id) VALUES (403, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 8);
INSERT INTO department (id, name, school_id) VALUES (404, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 21);
INSERT INTO department (id, name, school_id) VALUES (405, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 67);
INSERT INTO department (id, name, school_id) VALUES (406, 'ΜΟΥΣΙΚΩΝ ΣΠΟΥΔΩΝ', 12);
INSERT INTO department (id, name, school_id) VALUES (407, 'ΜΟΥΣΙΚΩΝ ΣΠΟΥΔΩΝ', 55);
INSERT INTO department (id, name, school_id) VALUES (408, 'ΜΟΥΣΙΚΩΝ ΣΠΟΥΔΩΝ', 39);
INSERT INTO department (id, name, school_id) VALUES (409, 'ΜΟΥΣΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΗΣ', 85);
INSERT INTO department (id, name, school_id) VALUES (411, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΔΙΕΘΝΩΝ ΣΧΕΣΕΩΝ', 99);
INSERT INTO department (id, name, school_id) VALUES (412, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΜΑΤΙΚΗΣ', 173);
INSERT INTO department (id, name, school_id) VALUES (445, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 160);
INSERT INTO department (id, name, school_id) VALUES (447, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 5);
INSERT INTO department (id, name, school_id) VALUES (451, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε. (ΛΑΡΙΣΑ)', 144);
INSERT INTO department (id, name, school_id) VALUES (453, 'ΠΟΛΙΤΙΚΩΝ ΔΟΜΙΚΩΝ ΕΡΓΩΝ', 158);
INSERT INTO department (id, name, school_id) VALUES (461, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 160);
INSERT INTO department (id, name, school_id) VALUES (465, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 127);
INSERT INTO department (id, name, school_id) VALUES (467, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 144);
INSERT INTO department (id, name, school_id) VALUES (475, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 152);
INSERT INTO department (id, name, school_id) VALUES (476, 'ΜΗΧΑΝΙΚΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 28);
INSERT INTO department (id, name, school_id) VALUES (477, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΜΗΧΑΝΟΛΟΓΙΑΣ', 18);
INSERT INTO department (id, name, school_id) VALUES (479, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 170);
INSERT INTO department (id, name, school_id) VALUES (480, 'ΜΗΧΑΝΙΚΩΝ ΒΙΟΪΑΤΡΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ Τ.Ε.', 114);
INSERT INTO department (id, name, school_id) VALUES (483, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 160);
INSERT INTO department (id, name, school_id) VALUES (487, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 127);
INSERT INTO department (id, name, school_id) VALUES (489, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 144);
INSERT INTO department (id, name, school_id) VALUES (491, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 158);
INSERT INTO department (id, name, school_id) VALUES (493, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 119);
INSERT INTO department (id, name, school_id) VALUES (495, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 133);
INSERT INTO department (id, name, school_id) VALUES (498, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 170);
INSERT INTO department (id, name, school_id) VALUES (499, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 170);
INSERT INTO department (id, name, school_id) VALUES (501, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 114);
INSERT INTO department (id, name, school_id) VALUES (503, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 160);
INSERT INTO department (id, name, school_id) VALUES (505, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 5);
INSERT INTO department (id, name, school_id) VALUES (506, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 170);
INSERT INTO department (id, name, school_id) VALUES (511, 'ΝΑΥΠΗΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 114);
INSERT INTO department (id, name, school_id) VALUES (512, 'ΜΗΧΑΝΙΚΩΝ ΕΝΕΡΓΕΙΑΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ Τ.Ε.', 114);
INSERT INTO department (id, name, school_id) VALUES (515, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 164);
INSERT INTO department (id, name, school_id) VALUES (516, 'ΜΗΧΑΝΟΛΟΓΩΝ ΟΧΗΜΑΤΩΝ Τ.Ε.', 5);
INSERT INTO department (id, name, school_id) VALUES (518, 'ΦΩΤΟΓΡΑΦΙΑΣ ΚΑΙ ΟΠΤΙΚΟΑΚΟΥΣΤΙΚΩΝ', 112);
INSERT INTO department (id, name, school_id) VALUES (519, 'ΕΣΩΤΕΡΙΚΗΣ ΑΡΧΙΤΕΚΤΟΝΙΚΗΣ, ΔΙΑΚΟΣΜΗΣΗΣ ΚΑΙ ΣΧΕΔΙΑΣΜΟΥ ΑΝΤΙΚΕΙΜΕΝΩΝ', 112);
INSERT INTO department (id, name, school_id) VALUES (520, 'ΣΥΝΤΗΡΗΣΗΣ ΑΡΧΑΙΟΤΗΤΩΝ & ‘ΕΡΓΩΝ ΤΕΧΝΗΣ', 112);
INSERT INTO department (id, name, school_id) VALUES (522, 'ΣΧΕΔΙΑΣΜΟΥ & ΤΕΧΝΟΛΟΓΙΑΣ ΞΥΛΟΥ & ΕΠΙΠΛΟΥ Τ.Ε.', 144);
INSERT INTO department (id, name, school_id) VALUES (524, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΜΕΣΩΝ ΜΑΖΙΚΗΣ ΕΝΗΜΕΡΩΣΗΣ', 120);
INSERT INTO department (id, name, school_id) VALUES (526, 'ΕΜΠΟΡΙΑΣ ΚΑΙ ΔΙΑΦΗΜΙΣΗΣ', 153);
INSERT INTO department (id, name, school_id) VALUES (528, 'ΑΡΧΙΤΕΚΤΟΝΙΚΗΣ ΤΟΠΙΟΥ', 117);
INSERT INTO department (id, name, school_id) VALUES (529, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 165);
INSERT INTO department (id, name, school_id) VALUES (532, 'ΜΗΧΑΝΙΚΩΝ ΦΥΣΙΚΩΝ ΠΟΡΩΝ ΚΑΙ ΠΕΡΙΒΑΛΛΟΝΤΟΣ Τ.Ε.', 156);
INSERT INTO department (id, name, school_id) VALUES (544, 'ΜΗΧΑΝΟΛΟΓΙΑ ΚΑΙ ΥΔΑΤΙΝΩΝ ΠΟΡΩΝ', 125);
INSERT INTO department (id, name, school_id) VALUES (545, 'ΜΗΧΑΝΙΚΗΣ ΒΙΟΣΥΣΤΗΜΑΤΩΝ', 142);
INSERT INTO department (id, name, school_id) VALUES (546, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 148);
INSERT INTO department (id, name, school_id) VALUES (547, 'ΤΕΧΝΟΛ. ΑΛΙΕΙΑΣ & ΥΔΑΤΟΚΑΛΛΙΕΡΓΕΙΩΝ', 1);
INSERT INTO department (id, name, school_id) VALUES (549, 'ΔΙΟΙΚΗΣΗΣ ΣΥΣΤΗΜΑΤΩΝ ΕΦΟΔΙΑΣΜΟΥ', 150);
INSERT INTO department (id, name, school_id) VALUES (550, 'ΔΑΣΟΠΟΝΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ  ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 118);
INSERT INTO department (id, name, school_id) VALUES (551, 'ΔΑΣΟΠΟΝΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 169);
INSERT INTO department (id, name, school_id) VALUES (553, 'ΔΑΣΟΠΟΝΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 141);
INSERT INTO department (id, name, school_id) VALUES (555, 'ΤΕΧΝΟΛΟΓΙΑΣ ΑΛΙΕΙΑΣ - ΥΔΑΤΟΚΑΛΛΙΕΡΓΕΙΩΝ', 126);
INSERT INTO department (id, name, school_id) VALUES (557, 'ΤΕΧΝΟΛΟΓΩΝ ΓΕΩΠΟΝΩΝ', 126);
INSERT INTO department (id, name, school_id) VALUES (559, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 159);
INSERT INTO department (id, name, school_id) VALUES (561, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 2);
INSERT INTO department (id, name, school_id) VALUES (563, 'ΛΟΓΙΣΤΙΚΗΣ', 122);
INSERT INTO department (id, name, school_id) VALUES (565, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 139);
INSERT INTO department (id, name, school_id) VALUES (569, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 115);
INSERT INTO department (id, name, school_id) VALUES (575, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 150);
INSERT INTO department (id, name, school_id) VALUES (577, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 167);
INSERT INTO department (id, name, school_id) VALUES (580, 'ΔΙΕΘΝΟΥΣ ΕΜΠΟΡΙΟΥ', 128);
INSERT INTO department (id, name, school_id) VALUES (581, 'ΕΜΠΟΡΙΑΣ & ΔΙΑΦΗΜΙΣΗΣ', 110);
INSERT INTO department (id, name, school_id) VALUES (583, 'ΕΜΠΟΡΙΑΣ & ΔΙΑΦΗΜΙΣΗΣ', 2);
INSERT INTO department (id, name, school_id) VALUES (587, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 115);
INSERT INTO department (id, name, school_id) VALUES (595, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 150);
INSERT INTO department (id, name, school_id) VALUES (596, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ (ΚΟΖΑΝΗ)', 130);
INSERT INTO department (id, name, school_id) VALUES (597, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 167);
INSERT INTO department (id, name, school_id) VALUES (599, 'ΒΙΒΛΙΟΘΗΚΟΝΟΜΙΑΣ ΚΑΙ ΣΥΣΤΗΜΑΤΩΝ ΠΛΗΡΟΦΟΡΗΣΗΣ', 110);
INSERT INTO department (id, name, school_id) VALUES (601, 'ΒΙΒΛΙΟΘΗΚΟΝΟΜΙΑΣ ΚΑΙ ΣΥΣΤΗΜΑΤΩΝ ΠΛΗΡΟΦΟΡΗΣΗΣ', 2);
INSERT INTO department (id, name, school_id) VALUES (607, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 122);
INSERT INTO department (id, name, school_id) VALUES (615, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (616, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', 124);
INSERT INTO department (id, name, school_id) VALUES (617, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', 3);
INSERT INTO department (id, name, school_id) VALUES (618, 'ΛΟΓΟΘΕΡΑΠΕΙΑΣ', 124);
INSERT INTO department (id, name, school_id) VALUES (619, 'ΕΡΓΟΘΕΡΑΠΕΙΑΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (620, 'ΛΟΓΟΘΕΡΑΠΕΙΑΣ', 135);
INSERT INTO department (id, name, school_id) VALUES (8014, '-', 29);
INSERT INTO department (id, name, school_id) VALUES (621, 'ΙΑΤΡΙΚΩΝ ΕΡΓΑΣΤΗΡΙΩΝ', 111);
INSERT INTO department (id, name, school_id) VALUES (623, 'ΙΑΤΡΙΚΩΝ ΕΡΓΑΣΤΗΡΙΩΝ', 3);
INSERT INTO department (id, name, school_id) VALUES (625, 'ΙΑΤΡΙΚΩΝ ΕΡΓΑΣΤΗΡΙΩΝ', 140);
INSERT INTO department (id, name, school_id) VALUES (627, 'ΡΑΔΙΟΛΟΓΙΑΣ - ΑΚΤΙΝΟΛΟΓΙΑΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (628, 'ΔΙΑΤΡΟΦΗΣ & ΔΙΑΙΤΟΛΟΓΙΑΣ', 157);
INSERT INTO department (id, name, school_id) VALUES (629, 'ΟΔΟΝΤΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (635, 'ΟΠΤΙΚΗΣ ΚΑΙ ΟΠΤΟΜΕΤΡΙΑΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (637, 'ΑΙΣΘΗΤΙΚΗΣ ΚΑΙ ΚΟΣΜΗΤΟΛΟΓΙΑΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (639, 'ΑΙΣΘΗΤΙΚΗΣ ΚΑΙ ΚΟΣΜΗΤΟΛΟΓΙΑΣ', 3);
INSERT INTO department (id, name, school_id) VALUES (641, 'ΚΟΙΝΩΝΙΚΗΣ ΕΡΓΑΣΙΑΣ', 124);
INSERT INTO department (id, name, school_id) VALUES (643, 'ΚΟΙΝΩΝΙΚΗΣ ΕΡΓΑΣΙΑΣ', 155);
INSERT INTO department (id, name, school_id) VALUES (644, 'ΜΗΧΑΝΙΚΩΝ ΜΟΥΣΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ ΚΑΙ ΑΚΟΥΣΤΙΚΗΣ Τ.Ε.', 156);
INSERT INTO department (id, name, school_id) VALUES (645, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (647, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 3);
INSERT INTO department (id, name, school_id) VALUES (649, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 124);
INSERT INTO department (id, name, school_id) VALUES (651, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 140);
INSERT INTO department (id, name, school_id) VALUES (652, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 168);
INSERT INTO department (id, name, school_id) VALUES (653, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 155);
INSERT INTO department (id, name, school_id) VALUES (654, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 135);
INSERT INTO department (id, name, school_id) VALUES (655, 'ΜΑΙΕΥΤΙΚΗΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (657, 'ΜΑΙΕΥΤΙΚΗΣ', 3);
INSERT INTO department (id, name, school_id) VALUES (659, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', 168);
INSERT INTO department (id, name, school_id) VALUES (661, 'ΚΟΙΝΩΝΙΚΗΣ ΕΡΓΑΣΙΑΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (662, 'ΜΑΙΕΥΤΙΚΗΣ', 131);
INSERT INTO department (id, name, school_id) VALUES (663, 'ΛΟΓΟΘΕΡΑΠΕΙΑΣ', 161);
INSERT INTO department (id, name, school_id) VALUES (690, 'ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ', 111);
INSERT INTO department (id, name, school_id) VALUES (692, 'ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ', 135);
INSERT INTO department (id, name, school_id) VALUES (694, 'ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ', 3);
INSERT INTO department (id, name, school_id) VALUES (696, 'ΛΑΙΚΗΣ ΚΑΙ ΠΑΡΑΔΟΣΙΑΚΗΣ ΜΟΥΣΙΚΗΣ', 137);
INSERT INTO department (id, name, school_id) VALUES (697, 'ΤΕΧΝΟΛΟΓΙΑΣ ΗΧΟΥ ΚΑΙ ΜΟΥΣΙΚΩΝ ΟΡΓΑΝΩΝ', 147);
INSERT INTO department (id, name, school_id) VALUES (701, 'ΚΛΩΣΤΟΫΦΑΝΤΟΥΡΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 160);
INSERT INTO department (id, name, school_id) VALUES (703, 'ΣΧΕΔΙΑΣΜΟΥ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΕΝΔΥΣΗΣ', 152);
INSERT INTO department (id, name, school_id) VALUES (705, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 170);
INSERT INTO department (id, name, school_id) VALUES (709, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 156);
INSERT INTO department (id, name, school_id) VALUES (710, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 138);
INSERT INTO department (id, name, school_id) VALUES (711, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 114);
INSERT INTO department (id, name, school_id) VALUES (712, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 5);
INSERT INTO department (id, name, school_id) VALUES (713, 'ΜΗΧΑΝΙΚΩΝ ΗΛΕΚΤΡΟΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΙΚΩΝ ΣΥΣΤΗΜΑΤΩΝ Τ.Ε.', 160);
INSERT INTO department (id, name, school_id) VALUES (714, 'ΜΗΧΑΝΙΚΩΝ ΑΥΤΟΜΑΤΙΣΜΟΥ Τ.Ε.', 160);
INSERT INTO department (id, name, school_id) VALUES (716, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 113);
INSERT INTO department (id, name, school_id) VALUES (717, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 4);
INSERT INTO department (id, name, school_id) VALUES (718, 'ΟΙΝΟΛΟΓΙΑΣ & ΤΕΧΝΟΛΟΓΙΑΣ ΠΟΤΩΝ', 113);
INSERT INTO department (id, name, school_id) VALUES (719, 'ΔΙΑΤΡΟΦΗΣ ΚΑΙ ΔΙΑΙΤΟΛΟΓΙΑΣ', 4);
INSERT INTO department (id, name, school_id) VALUES (720, 'ΜΗΧΑΝΙΚΩΝ ΑΥΤΟΜΑΤΙΣΜΟΥ Τ.Ε.', 5);
INSERT INTO department (id, name, school_id) VALUES (722, 'ΜΗΧΑΝΙΚΩΝ ΑΥΤΟΜΑΤΙΣΜΟΥ Τ.Ε.', 170);
INSERT INTO department (id, name, school_id) VALUES (723, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 144);
INSERT INTO department (id, name, school_id) VALUES (724, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 119);
INSERT INTO department (id, name, school_id) VALUES (725, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 158);
INSERT INTO department (id, name, school_id) VALUES (727, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 152);
INSERT INTO department (id, name, school_id) VALUES (730, 'ΨΗΦΙΑΚΩΝ ΜΕΣΩΝ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ Τ.Ε.', 133);
INSERT INTO department (id, name, school_id) VALUES (731, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 162);
INSERT INTO department (id, name, school_id) VALUES (735, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 133);
INSERT INTO department (id, name, school_id) VALUES (736, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.', 127);
INSERT INTO department (id, name, school_id) VALUES (737, 'ΜΗΧΑΝΙΚΩΝ ΤΕΧΝΟΛΟΓΙΑΣ ΑΕΡΟΣΚΑΦΩΝ Τ.Ε.', 170);
INSERT INTO department (id, name, school_id) VALUES (739, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 143);
INSERT INTO department (id, name, school_id) VALUES (740, 'ΔΙΟΙΚΗΣΗΣ ΣΥΣΤΗΜΑΤΩΝ ΕΦΟΔΙΑΣΜΟΥ', 167);
INSERT INTO department (id, name, school_id) VALUES (741, 'ΔΗΜΟΣΙΩΝ ΣΧΕΣΕΩΝ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ', 145);
INSERT INTO department (id, name, school_id) VALUES (742, 'ΟΠΤΙΚΗΣ ΚΑΙ ΟΠΤΟΜΕΤΡΙΑΣ', 120);
INSERT INTO department (id, name, school_id) VALUES (744, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 116);
INSERT INTO department (id, name, school_id) VALUES (745, 'ΕΜΠΟΡΙΑΣ ΚΑΙ ΔΙΑΦΗΜΙΣΗΣ', 166);
INSERT INTO department (id, name, school_id) VALUES (747, 'ΔΙΑΤΡΟΦΗΣ ΚΑΙ ΔΙΑΙΤΟΛΟΓΙΑΣ', 143);
INSERT INTO department (id, name, school_id) VALUES (750, 'ΕΣΩΤΕΡΙΚΗΣ ΑΡΧΙΤΕΚΤΟΝΙΚΗΣ, ΔΙΑΚΟΣΜΗΣΗΣ ΚΑΙ ΣΧΕΔΙΑΣΜΟΥ ΑΝΤΙΚΕΙΜΕΝΩΝ', 151);
INSERT INTO department (id, name, school_id) VALUES (751, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ (ΑΓΙΟΣ ΝΙΚΟΛΑΟΣ)', 154);
INSERT INTO department (id, name, school_id) VALUES (752, 'ΟΙΝΟΛΟΓΙΑΣ & ΤΕΧΝΟΛΟΓΙΑΣ ΠΟΤΩΝ', 118);
INSERT INTO department (id, name, school_id) VALUES (755, 'ΔΙΟΙΚΗΣΗΣ, ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ ΠΟΛΙΤΙΣΤΙΚΩΝ ΚΑΙ ΤΟΥΡΙΣΤΙΚΩΝ ΜΟΝΑΔΩΝ', 167);
INSERT INTO department (id, name, school_id) VALUES (756, 'ΔΙΟΙΚΗΣΗΣ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ ΠΟΛΙΤΙΣΤΙΚΩΝ ΚΑΙ ΤΟΥΡΙΣΤΙΚΩΝ ΜΟΝΑΔΩΝ', 122);
INSERT INTO department (id, name, school_id) VALUES (757, 'ΤΕΧΝΟΛΟΓΩΝ ΓΕΩΠΟΝΩΝ', 136);
INSERT INTO department (id, name, school_id) VALUES (758, 'ΤΕΧΝΟΛΟΓΩΝ ΓΕΩΠΟΝΩΝ', 157);
INSERT INTO department (id, name, school_id) VALUES (759, 'ΤΕΧΝΟΛΟΓΩΝ ΓΕΩΠΟΝΩΝ', 4);
INSERT INTO department (id, name, school_id) VALUES (760, 'ΤΕΧΝΟΛΟΓΩΝ ΓΕΩΠΟΝΩΝ', 164);
INSERT INTO department (id, name, school_id) VALUES (761, 'ΤΕΧΝΟΛΟΓΩΝ ΓΕΩΠΟΝΩΝ', 143);
INSERT INTO department (id, name, school_id) VALUES (762, 'ΤΕΧΝΟΛΟΓΩΝ ΓΕΩΠΟΝΩΝ', 132);
INSERT INTO department (id, name, school_id) VALUES (765, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ (ΜΕΣΟΛΟΓΓΙ)', 122);
INSERT INTO department (id, name, school_id) VALUES (766, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ (ΠΑΤΡΑ)', 122);
INSERT INTO department (id, name, school_id) VALUES (767, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 158);
INSERT INTO department (id, name, school_id) VALUES (768, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε.', 127);
INSERT INTO department (id, name, school_id) VALUES (769, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε. (ΤΡΙΚΑΛΑ)', 144);
INSERT INTO department (id, name, school_id) VALUES (772, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 154);
INSERT INTO department (id, name, school_id) VALUES (773, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 129);
INSERT INTO department (id, name, school_id) VALUES (774, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 122);
INSERT INTO department (id, name, school_id) VALUES (775, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', 134);
INSERT INTO department (id, name, school_id) VALUES (776, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 18);
INSERT INTO department (id, name, school_id) VALUES (8017, 'ΠΑΙΔΑΓΩΓΙΚΟ ΤΜΗΜΑ', 18);
INSERT INTO department (id, name, school_id) VALUES (9996, 'ΕΙΚΑΣΤΙΚΩΝ ΚΑΙ ΕΦΑΡΜΟΣΜΕΝΩΝ ΤΕΧΝΩΝ', 64);
INSERT INTO department (id, name, school_id) VALUES (9997, 'ΕΙΚΑΣΤΙΚΩΝ ΤΕΧΝΩΝ', 6);
INSERT INTO department (id, name, school_id) VALUES (9998, 'ΕΙΚΑΣΤΙΚΩΝ ΚΑΙ ΕΦΑΡΜΟΣΜΕΝΩΝ ΤΕΧΝΩΝ', 12);
INSERT INTO department (id, name, school_id) VALUES (1001, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ', 2);
INSERT INTO department (id, name, school_id) VALUES (1002, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΗΛΕΚΤΡΟΛΟΓΙΑΣ ΚΑΙ ΗΛΕΚΤΡΟΝΙΚΗΣ ', 18);
INSERT INTO department (id, name, school_id) VALUES (1003, 'ΚΟΙΝΩΝΙΚΗΣ ΔΙΟΙΚΗΣΗΣ ΚΑΙ ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ', 26);
INSERT INTO department (id, name, school_id) VALUES (1004, 'ΜΑΘΗΜΑΤΙΚΩΝ ', 61);
INSERT INTO department (id, name, school_id) VALUES (1005, 'ΜΑΘΗΜΑΤΙΚΩΝ ΚΑΙ ΕΦΑΡΜΟΣΜΕΝΩΝ ΜΑΘΗΜΑΤΙΚΩΝ ', 80);
INSERT INTO department (id, name, school_id) VALUES (1006, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε. ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΤΟΠΟΓΡΑΦΙΑΣ ΚΑΙ ΓΕΩΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε. ', 114);
INSERT INTO department (id, name, school_id) VALUES (1007, 'ΓΡΑΦΙΣΤΙΚΗΣ ', 112);
INSERT INTO department (id, name, school_id) VALUES (1008, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ', 110);
INSERT INTO department (id, name, school_id) VALUES (1009, 'ΔΗΜΟΣΙΑΣ ΥΓΕΙΑΣ ΚΑΙ ΚΟΙΝΟΤΙΚΗΣ ΥΓΕΙΑΣ ', 111);
INSERT INTO department (id, name, school_id) VALUES (1010, 'ΜΗΧΑΝΙΚΩΝ ΤΕΧΝΟΛΟΓΙΑΣ ΠΕΤΡΕΛΑΙΟΥ ΚΑΙ ΦΥΣΙΚΟΥ ΑΕΡΙΟΥ Τ.Ε. ΚΑΙ ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε. ', 119);
INSERT INTO department (id, name, school_id) VALUES (1011, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΒΙΟΜΗΧΑΝΙΚΟΥ ΣΧΕΔΙΑΣΜΟΥ Τ.Ε. ', 133);
INSERT INTO department (id, name, school_id) VALUES (1012, 'ΜΗΧΑΝΙΚΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΑΝΤΙΡΡΥΠΑΝΣΗΣ Τ.Ε.  ', 133);
INSERT INTO department (id, name, school_id) VALUES (1013, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ (ΓΡΕΒΕΝΑ) ', 129);
INSERT INTO department (id, name, school_id) VALUES (1014, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ', 134);
INSERT INTO department (id, name, school_id) VALUES (1015, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ', 139);
INSERT INTO department (id, name, school_id) VALUES (1016, 'ΤΕΧΝΟΛΟΓΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ Τ.Ε. ', 149);
INSERT INTO department (id, name, school_id) VALUES (1017, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ', 146);
INSERT INTO department (id, name, school_id) VALUES (1018, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε. ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΤΟΠΟΓΡΑΦΙΑΣ ΚΑΙ ΓΕΩΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε.  ', 152);
INSERT INTO department (id, name, school_id) VALUES (1019, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ Τ.Ε. ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΤΟΠΟΓΡΑΦΙΑΣ ΚΑΙ ΓΕΩΠΛΗΡΟΦΟΡΙΚΗΣ Τ.Ε. ', 152);
INSERT INTO department (id, name, school_id) VALUES (1020, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ (ΗΡΑΚΛΕΙΟ) ', 154);
INSERT INTO department (id, name, school_id) VALUES (1021, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ', 159);
INSERT INTO department (id, name, school_id) VALUES (1022, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ΚΑΙ ΟΡΓΑΝΙΣΜΩΝ ', 162);
INSERT INTO department (id, name, school_id) VALUES (1023, '-', 49);
INSERT INTO department (id, name, school_id) VALUES (1024, '-', 50);
INSERT INTO department (id, name, school_id) VALUES (1025, '-', 51);
INSERT INTO department (id, name, school_id) VALUES (1026, '-', 52);
INSERT INTO department (id, name, school_id) VALUES (20001, 'ΚΕΝΤΡΟ ΕΡΕΥΝΑΣ ΤΗΣ ΒΥΖΑΝΤΙΝΗΣ ΚΑΙ ΜΕΤΑΒΥΖΑΝΤΙΝΗΣ ΤΕΧΝΗΣ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20002, 'ΚΕΝΤΡΟ ΕΡΕΥΝΗΣ ΤΩΝ ΝΕΟΕΛΛΗΝΙΚΩΝ ΔΙΑΛΕΚΤΩΝ ΚΑΙ ΙΔΙΩΜΑΤΩΝ Ι.Λ.Ν.Ε', 1174);
INSERT INTO department (id, name, school_id) VALUES (20003, 'ΚΕΝΤΡΟ ΕΡΕΥΝΩΝ ΑΣΤΡΟΝΟΜΙΑΣ ΚΑΙ ΕΦΗΡΜΟΣΜΕΝΩΝ ΜΑΘΗΜΑΤΙΚΩΝ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20004, 'ΚΕΝΤΡΟ ΕΡΕΥΝΩΝ ΘΕΩΡΗΤΙΚΩΝ ΚΑΙ ΕΦΗΡΜΟΣΜΕΝΩΝ ΜΑΘΗΜΑΤΙΚΩΝ ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20005, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΕΠΙΣΤΗΜΟΝΙΚΩΝ ΟΡΩΝ ΚΑΙ ΝΕΟΛΟΓΙΣΜΩΝ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20006, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΗΣ ΑΡΧΑΙΟΤΗΤΟΣ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20007, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΗΣ ΕΛΛΗΝΙΚΗΣ ΚΑΙ ΛΑΤΙΝΙΚΗΣ ΓΡΑΜΜΑΤΕΙΑΣ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20008, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΗΣ ΕΛΛΗΝΙΚΗΣ ΚΟΙΝΩΝΙΑΣ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20009, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΗΣ ΕΛΛΗΝΙΚΗΣ ΛΑΟΓΡΑΦΙΑΣ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20010, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΗΣ ΕΛΛΗΝΙΚΗΣ ΦΙΛΟΣΟΦΙΑΣ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20011, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΗΣ ΙΣΤΟΡΙΑΣ ΤΟΥ ΕΛΛΗΝΙΚΟΥ ΔΙΚΑΙΟΥ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20012, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΗΣ ΙΣΤΟΡΙΑΣ ΤΟΥ ΝΕΩΤΕΡΟΥ ΕΛΛΗΝΙΣΜΟΥ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20013, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΤΟΥ ΜΕΣΑΙΩΝΙΚΟΥ ΚΑΙ ΝΕΟΥ ΕΛΛΗΝΙΣΜΟΥ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20014, 'ΚΕΝΤΡΟΝ ΕΡΕΥΝΗΣ ΦΥΣΙΚΗΣ ΤΗΣ ΑΤΜΟΣΦΑΙΡΑΣ ΚΑΙ ΚΛΙΜΑΤΟΛΟΓΙΑΣ', 1174);
INSERT INTO department (id, name, school_id) VALUES (20015, 'ΔΙΕΘΝΕΣ ΚΕΝΤΡΟ ΔΗΜΟΣΙΑΣ ΔΙΟΙΚΗΣΗΣ ', 1175);
INSERT INTO department (id, name, school_id) VALUES (20016, 'ΓΕΩΔΥΝΑΜΙΚΟ ΙΝΣΤΙΤΟΥΤΟ ', 1176);
INSERT INTO department (id, name, school_id) VALUES (20017, 'ΙΝΣΤΙΤΟΥΤΟ ΑΣΤΡΟΝΟΜΙΑΣ, ΑΣΤΡΟΦΥΣΙΚΗΣ, ΔΙΑΣΤΗΜΙΚΩΝ ΕΦΑΡΜΟΓΩΝ ΚΑΙ ΤΗΛΕΠΙΣΚΟΠΗΣΗΣ', 1176);
INSERT INTO department (id, name, school_id) VALUES (20018, 'ΙΝΣΤΙΤΟΥΤΟ ΕΡΕΥΝΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΒΙΩΣΙΜΗΣ ΑΝΑΠΤΥΞΗΣ ', 1176);
INSERT INTO department (id, name, school_id) VALUES (20019, 'ΙΝΣΤΙΤΟΥΤΟ ΤΕΧΝΟΛΟΓΙΩΝ ΚΑΙ ΕΡΕΥΝΩΝ ΒΑΘΕΙΑΣ ΘΑΛΑΣΣΗΣ ', 1176);
INSERT INTO department (id, name, school_id) VALUES (20020, 'ΙΝΣΤΙΤΟΥΤΟ ΒΙΟΕΠΙΣΤΗΜΩΝ ΚΑΙ ΕΦΑΡΜΟΓΩΝ ', 1177);
INSERT INTO department (id, name, school_id) VALUES (20021, 'ΙΝΣΤΙΤΟΥΤΟ ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 1177);
INSERT INTO department (id, name, school_id) VALUES (20022, 'ΙΝΣΤΙΤΟΥΤΟ ΠΡΟΗΓΜΕΝΩΝ ΥΛΙΚΩΝ, ΦΥΣΙΚΟΧΗΜΙΚΩΝ ΔΙΕΡΓΑΣΙΩΝ, ΝΑΝΟΤΕΧΝΟΛΟΓΙΑΣ ΚΑΙ ΜΙΚΡΟΣΥΣΤΗΜΑΤΩΝ', 1177);
INSERT INTO department (id, name, school_id) VALUES (20023, 'ΙΝΣΤΙΤΟΥΤΟ ΠΥΡΗΝΙΚΗΣ ΚΑΙ ΣΩΜΑΤΙΔΙΑΚΗΣ ΦΥΣΙΚΗΣ', 1177);
INSERT INTO department (id, name, school_id) VALUES (20024, 'ΙΝΣΤΙΤΟΥΤΟ ΠΥΡΗΝΙΚΩΝ ΚΑΙ ΡΑΔΙΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ, ΕΝΕΡΓΕΙΑΣ ΚΑΙ ΑΣΦΑΛΕΙΑΣ', 1177);
INSERT INTO department (id, name, school_id) VALUES (20025, 'ΙΝΣΤΙΤΟΥΤΟ ΑΣΤΙΚΗΣ ΚΑΙ ΑΓΡΟΤΙΚΗΣ ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', 1178);
INSERT INTO department (id, name, school_id) VALUES (20026, 'ΙΝΣΤΙΤΟΥΤΟ ΚΟΙΝΩΝΙΚΗΣ ΠΟΛΙΤΙΚΗΣ', 1178);
INSERT INTO department (id, name, school_id) VALUES (20027, 'ΙΝΣΤΙΤΟΥΤΟ ΠΟΛΙΤΙΚΗΣ ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', 1178);
INSERT INTO department (id, name, school_id) VALUES (20028, 'ΙΝΣΤΙΤΟΥΤΟ ΕΣΩΤΕΡΙΚΩΝ ΥΔΑΤΩΝ', 1179);
INSERT INTO department (id, name, school_id) VALUES (20029, 'ΙΝΣΤΙΤΟΥΤΟ ΘΑΛΑΣΣΙΑΣ ΒΙΟΛΟΓΙΑΣ ΚΑΙ ΓΕΝΕΤΙΚΗΣ', 1179);
INSERT INTO department (id, name, school_id) VALUES (20030, 'ΙΝΣΤΙΤΟΥΤΟ ΘΑΛΑΣΣΙΩΝ ΒΙΟΛΟΓΙΚΩΝ ΠΟΡΩΝ ', 1179);
INSERT INTO department (id, name, school_id) VALUES (20031, 'ΙΝΣΤΙΤΟΥΤΟ ΥΔΑΤΟΚΑΛΛΙΕΡΓΕΙΩΝ', 1179);
INSERT INTO department (id, name, school_id) VALUES (20032, 'ΙΝΣΤΙΤΟΥΤΟ ΩΚΕΑΝΟΓΡΑΦΙΑΣ ', 1179);
INSERT INTO department (id, name, school_id) VALUES (20033, 'ΙΝΣΤΙΤΟΥΤΟ ΤΕΧΝΙΚΗΣ ΣΕΙΣΜΟΛΟΓΙΑΣ & ΑΝΤΙΣΕΙΣΜΙΚΩΝ ΚΑΤΑΣΚΕΥΩΝ – ΙΤΣΑΚ ', 1180);
INSERT INTO department (id, name, school_id) VALUES (20034, 'ΓΕΩΡΓΙΚΗ ΚΑΙ ΒΙΟΤΕΧΝΙΚΗ ΣΧΟΛΗ ', 1181);
INSERT INTO department (id, name, school_id) VALUES (20035, 'Ε.Π.Ι. " ΝΕΥΡΟΧΕΙΡΟΥΡΓΙΚΟ "  - ΠΑΝΕΠΙΣΤΗΜΙΟ ΙΩΑΝΝΙΝΩΝ', 1182);
INSERT INTO department (id, name, school_id) VALUES (20036, 'Ε.Π.Ι. ΑΣΤΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΑΝΘΡΩΠΙΝΟΥ ΔΥΝΑΜΙΚΟΥ - ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 1183);
INSERT INTO department (id, name, school_id) VALUES (20037, 'Ε.Π.Ι. ΔΙΕΘΝΩΝ ΣΧΕΣΕΩΝ  -  ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 1184);
INSERT INTO department (id, name, school_id) VALUES (20038, 'Ε.Π.Ι. ΔΙΚΟΝΟΜΙΚΩΝ ΜΕΛΕΤΩΝ  - ΕΚΠΑ', 1185);
INSERT INTO department (id, name, school_id) VALUES (20039, 'Ε.Π.Ι. ΕΠΙΤΑΧΥΝΤΙΚΩΝ ΣΥΣΤΗΜΑΤΩΝ ΚΑΙ ΕΦΑΡΜΟΓΩΝ ΕΚΠΑ', 1186);
INSERT INTO department (id, name, school_id) VALUES (20040, 'Ε.Π.Ι. ΕΦΗΡΜΟΣΜΕΝΗΣ ΕΠΙΚΟΙΝΩΝΙΑΣ - ΕΚΠΑ', 1187);
INSERT INTO department (id, name, school_id) VALUES (20041, 'Ε.Π.Ι. ΜΕΛΕΤΗΣ ΚΑΙ ΑΝΤΙΜΕΤΩΠΙΣΗΣ ΓΕΝΕΤΙΚΩΝ ΚΑΚΟΗΘΩΝ ΝΟΣΗΜΑΤΩΝ ΤΗΣ ΠΑΙΔΙΚΗΣ ΗΛΙΚΙΑΣ-ΕΚΠΑ', 1188);
INSERT INTO department (id, name, school_id) VALUES (20042, 'Ε.Π.Ι. ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΠΑΝΕΠΙΣΤΗΜΙΟ ΜΑΚΕΔΟΝΙΑΣ', 1189);
INSERT INTO department (id, name, school_id) VALUES (20043, 'Ε.Π.Ι. ΠΕΡΙΦΕΡΕΙΑΚΗΣ ΑΝΑΠΤΥΞΗΣ  -  ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 1190);
INSERT INTO department (id, name, school_id) VALUES (20044, 'Ε.Π.Ι. ΣΤΑΤΙΣΤΙΚΗΣ ΤΕΚΜΗΡΙΩΣΗΣ, ΑΝΑΛΥΣΗΣ & ΕΡΕΥΝΑΣ', 1191);
INSERT INTO department (id, name, school_id) VALUES (20045, 'Ε.Π.Ι. ΣΥΝΤΑΓΜΑΤΙΚΩΝ ΕΡΕΥΝΩΝ  (Ι.Σ.Ε) -  ΕΚΠΑ', 1192);
INSERT INTO department (id, name, school_id) VALUES (20046, 'Ε.Π.Ι. ΣΥΣΤΗΜΑΤΩΝ ΕΠΙΚΟΙΝΩΝΙΑΣ ΚΑΙ ΥΠΟΛΟΓΙΣΤΩΝ (Ε.Μ.Π.)', 1193);
INSERT INTO department (id, name, school_id) VALUES (20047, 'Ε.Π.Ι. ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ ΚΡΗΤΗΣ - ΠΟΛΥΤΕΧΝΕΙΟ ΚΡΗΤΗΣ', 1194);
INSERT INTO department (id, name, school_id) VALUES (20048, 'Ε.Π.Ι. ΦΥΣΙΚΗΣ ΠΛΑΣΜΑΤΟΣ - ΠΑΝΕΠΙΣΤΗΜΙΟ ΚΡΗΤΗΣ', 1195);
INSERT INTO department (id, name, school_id) VALUES (20049, 'Ε.Π.Ι. ΦΥΣΙΚΗΣ ΤΟΥ ΣΤΕΡΕΟΥ ΦΛΟΙΟΥ ΤΗΣ ΓΗΣ - ΕΚΠΑ', 1196);
INSERT INTO department (id, name, school_id) VALUES (20050, 'Ε.Π.Ι. ΨΥΧΙΚΗΣ ΥΓΙΕΙΝΗΣ - ΕΚΠΑ', 1197);
INSERT INTO department (id, name, school_id) VALUES (20051, 'ΕΘΝΙΚΟ KΕΝΤΡΟ ΔΙΑΒΗΤΗ ', 1198);
INSERT INTO department (id, name, school_id) VALUES (20052, 'ΕΘΝΙΚΟ ΚΕΝΤΡΟ ΤΕΚΜΗΡΙΩΣΗΣ (ΕΚΤ)', 1219);
INSERT INTO department (id, name, school_id) VALUES (20053, 'ΙΝΣΤΙΤΟΥΤΟ ΒΙΟΛΟΓΙΑΣ, ΦΑΡΜΑΚΕΥΤΙΚΗΣ, ΧΗΜΕΙΑΣ ΚΑΙ ΒΙΟΤΕΧΝΟΛΟΓΙΑΣ', 1199);
INSERT INTO department (id, name, school_id) VALUES (20054, 'ΙΝΣΤΙΤΟΥΤΟ ΘΕΩΡΗΤΙΚΗΣ ΚΑΙ ΦΥΣΙΚΗΣ ΧΗΜΕΙΑΣ', 1199);
INSERT INTO department (id, name, school_id) VALUES (20055, 'ΙΝΣΤΙΤΟΥΤΟ ΙΣΤΟΡΙΚΩΝ ΕΡΕΥΝΩΝ', 1199);
INSERT INTO department (id, name, school_id) VALUES (20056, 'ΙΝΣΤΙΤΟΥΤΟ ΒΙΩΣΙΜΗΣ ΚΙΝΗΤΙΚΟΤΗΤΑΣ ΚΑΙ ΔΙΚΤΥΩΝ ΜΕΤΑΦΟΡΩΝ', 1200);
INSERT INTO department (id, name, school_id) VALUES (20057, 'ΙΝΣΤΙΤΟΥΤΟ ΕΦΑΡΜΟΣΜΕΝΩΝ ΒΙΟΕΠΙΣΤΗΜΩΝ', 1200);
INSERT INTO department (id, name, school_id) VALUES (20058, 'ΙΝΣΤΙΤΟΥΤΟ ΤΕΧΝΟΛΟΓΙΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΩΝ', 1200);
INSERT INTO department (id, name, school_id) VALUES (20059, 'ΙΝΣΤΙΤΟΥΤΟ ΧΗΜΙΚΩΝ ΔΙΕΡΓΑΣΙΩΝ ΚΑΙ ΕΝΕΡΓΕΙΑΚΩΝ ΠΟΡΩΝ', 1200);
INSERT INTO department (id, name, school_id) VALUES (20060, 'ΕΛΛΗΝΙΚΟ ΙΝΣΤΙΤΟΥΤΟ ΠΑΣΤΕΡ - ΕΙΠ ', 1201);
INSERT INTO department (id, name, school_id) VALUES (20061, 'ΕΛΛΗΝΙΚΟ ΙΝΣΤΙΤΟΥΤΟ ΥΓΙΕΙΝΗΣ & ΑΣΦΑΛΕΙΑΣ ΤΗΣ ΕΡΓΑΣΙΑΣ ', 1202);
INSERT INTO department (id, name, school_id) VALUES (20062, 'ΕΛΛΗΝΙΚΟΣ ΟΡΓΑΝΙΣΜΟΣ ΤΥΠΟΠΟΙΗΣΗΣ ', 1203);
INSERT INTO department (id, name, school_id) VALUES (20063, 'ΕΡΕΥΝΗΤΙΚΟ ΑΚΑΔΗΜΑΙΚΟ ΙΝΣΤΙΤΟΥΤΟ ΤΕΧΝΟΛΟΓΙΑΣ ΥΠΟΛΟΓΙΣΤΩΝ (E.A.I.T.Y)  ', 1204);
INSERT INTO department (id, name, school_id) VALUES (20064, 'ΙΝΣΤΙΤΟΥΤΟ ΑΝΟΣΟΛΟΓΙΑΣ ', 1205);
INSERT INTO department (id, name, school_id) VALUES (20065, 'ΙΝΣΤΙΤΟΥΤΟ ΚΥΤΤΑΡΙΚΗΣ & ΑΝΑΠΤΥΞΙΑΚΗΣ ΒΙΟΛΟΓΙΑΣ ', 1205);
INSERT INTO department (id, name, school_id) VALUES (20066, 'ΙΝΣΤΙΤΟΥΤΟ ΜΙΚΡΟΒΙΟΛΟΓΙΑΣ-ΙΟΛΟΓΙΑΣ ', 1205);
INSERT INTO department (id, name, school_id) VALUES (20067, 'ΙΝΣΤΙΤΟΥΤΟ ΜΟΡΙΑΚΗΣ ΒΙΟΛΟΓΙΑΣ & ΓΕΝΕΤΙΚΗΣ ', 1205);
INSERT INTO department (id, name, school_id) VALUES (20068, 'ΙΝΣΤΙΤΟΥΤΟ ΜΟΡΙΑΚΗΣ ΟΓΚΟΛΟΓΙΑΣ ', 1205);
INSERT INTO department (id, name, school_id) VALUES (20069, 'ΙΝΣΤΙΤΟΥΤΟ ΒΙΟΜΗΧΑΝΙΚΩΝ ΣΥΣΤΗΜΑΤΩΝ (ΙΝΒΙΣ)', 1206);
INSERT INTO department (id, name, school_id) VALUES (20070, 'ΙΝΣΤΙΤΟΥΤΟ ΕΠΕΞΕΡΓΑΣΙΑΣ ΤΟΥ ΛΟΓΟΥ (ΙΕΛ)', 1206);
INSERT INTO department (id, name, school_id) VALUES (20071, 'ΙΝΣΤΙΤΟΥΤΟ ΠΛΗΡΟΦΟΡΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ ', 1206);
INSERT INTO department (id, name, school_id) VALUES (20072, 'ΜΟΝΑΔΑ ΑΝΑΛΥΣΗΣ ΚΑΙ ΜΟΝΤΕΛΟΠΟΙΗΣΗΣ ΤΗΣ ΠΛΗΡΟΦΟΡΙΑΣ', 1206);
INSERT INTO department (id, name, school_id) VALUES (20073, 'ΜΟΝΑΔΑ ΑΝΑΠΤΥΞΗΣ ΕΛΛΗΝΙΚΩΝ ΤΕΧΝΟΛΟΓΙΚΩΝ ΣΥΝΕΡΓΑΤΙΚΩΝ ΣΧΗΜΑΤΙΣΜΩΝ ', 1206);
INSERT INTO department (id, name, school_id) VALUES (20074, 'ΜΟΝΑΔΑ ΔΙΑΣΤΗΜΙΚΩΝ ΠΡΟΓΡΑΜΜΑΤΩΝ ', 1206);
INSERT INTO department (id, name, school_id) VALUES (20075, 'ΙΔΡΥΜΑ ΕΥΓΕΝΙΔΟΥ', 1207);
INSERT INTO department (id, name, school_id) VALUES (20076, 'ΙΔΡΥΜΑ ΙΑΤΡΟΒΙΟΛΟΓΙΚΗΣ ΕΡΕΥΝΑΣ ΑΚΑΔΗΜΙΑΣ ΑΘΗΝΩΝ', 1208);
INSERT INTO department (id, name, school_id) VALUES (20077, 'ΙΔΡΥΜΑ ΜΕΙΖΟΝΟΣ ΕΛΛΗΝΙΣΜΟΥ', 1209);
INSERT INTO department (id, name, school_id) VALUES (20078, 'IΝΣΤΙΤΟΥΤΟ ΕΠΙΣΤΗΜΩΝ ΧΗΜΙΚΗΣ ΜΗΧΑΝΙΚΗΣ', 1210);
INSERT INTO department (id, name, school_id) VALUES (20079, 'ΙΝΣΤΙΤΟΥΤΟ ΗΛΕΚΤΡΟΝΙΚΗΣ ΔΟΜΗΣ & ΛΕΙΖΕΡ', 1210);
INSERT INTO department (id, name, school_id) VALUES (20080, 'ΙΝΣΤΙΤΟΥΤΟ ΜΕΣΟΓΕΙΑΚΩΝ ΣΠΟΥΔΩΝ', 1210);
INSERT INTO department (id, name, school_id) VALUES (20081, 'ΙΝΣΤΙΤΟΥΤΟ ΜΟΡΙΑΚΗΣ ΒΙΟΛΟΓΙΑΣ & ΒΙΟΤΕΧΝΟΛΟΓΙΑΣ', 1210);
INSERT INTO department (id, name, school_id) VALUES (20082, 'ΙΝΣΤΙΤΟΥΤΟ ΠΛΗΡΟΦΟΡΙΚΗΣ', 1210);
INSERT INTO department (id, name, school_id) VALUES (20083, 'ΙΝΣΤΙΤΟΥΤΟ ΥΠΟΛΟΓΙΣΤΙΚΩΝ ΜΑΘΗΜΑΤΙΚΩΝ', 1210);
INSERT INTO department (id, name, school_id) VALUES (20084, 'ΙΝΣΤΙΤΟΥΤΟ  ΕΡΕΥΝΑΣ ΜΟΥΣΙΚΗΣ ΚΑΙ ΑΚΟΥΣΤΙΚΗΣ ', 1211);
INSERT INTO department (id, name, school_id) VALUES (20085, 'ΙΝΣΤΙΤΟΥΤΟ ΓΕΩΛΟΓΙΚΩΝ & ΜΕΤΑΛΛΕΥΤΙΚΩΝ ΕΡΕΥΝΩΝ ', 1212);
INSERT INTO department (id, name, school_id) VALUES (20086, 'ΙΝΣΤΙΤΟΥΤΟ ΕΛΙΑΣ ΚΑΙ ΥΠΟΤΡΟΠΙΚΩΝ ΦΥΤΩΝ ΧΑΝΙΩΝ ', 1213);
INSERT INTO department (id, name, school_id) VALUES (20087, 'ΚΕΝΤΡΟ ΑΝΑΝΕΩΣΙΜΩΝ ΠΗΓΩΝ ΚΑΙ ΕΞΟΙΚΟΝΟΜΗΣΗΣ ΕΝΕΡΓΕΙΑΣ - ΚΑΠΕ', 1214);
INSERT INTO department (id, name, school_id) VALUES (20088, 'ΚΕΝΤΡΟ ΕΚΠΑΙΔΕΥΤΙΚΗΣ ΕΡΕΥΝΑΣ', 1215);
INSERT INTO department (id, name, school_id) VALUES (20089, 'ΚΕΝΤΡΟ ΕΛΛΗΝΙΚΗΣ ΓΛΩΣΣΑΣ', 1216);
INSERT INTO department (id, name, school_id) VALUES (20090, 'ΚΕΝΤΡΟ ΕΡΕΥΝΑΣ ΓΙΑ ΘΕΜΑΤΑ ΙΣΟΤΗΤΑΣ ', 1217);
INSERT INTO department (id, name, school_id) VALUES (20091, 'ΚΕΝΤΡΟ ΕΡΕΥΝΑΣ ΤΕΧΝΟΛΟΓΙΑΣ ΚΑΙ ΑΝΑΠΤΥΞΗΣ ΘΕΣΣΑΛΙΑΣ', 1218);
---
INSERT INTO department (id, name, school_id) VALUES (20092, 'ΕΥΡΩΠΑΪΚΟ ΚΕΝΤΡΟ ΓΙΑ ΤΗΝ ΑΝΑΠΤΥΞΗ ΤΗΣ ΕΠΑΓΓΕΛΜΑΤΙΚΗΣ ΚΑΤΑΡΤΙΣΗΣ', 1220);
INSERT INTO department (id, name, school_id) VALUES (20093, 'ΕΡΕΥΝΗΤΙΚΗ ΜΟΝΑΔΑ 1', 1221);
INSERT INTO department (id, name, school_id) VALUES (20094, 'ΕΡΕΥΝΗΤΙΚΗ ΜΟΝΑΔΑ 3', 1221);
INSERT INTO department (id, name, school_id) VALUES (20095, 'ΕΡΕΥΝΗΤΙΚΗ ΜΟΝΑΔΑ 6', 1221);
INSERT INTO department (id, name, school_id) VALUES (20096, 'ΕΡΕΥΝΗΤΙΚΗ ΜΟΝΑΔΑ 8', 1221);
INSERT INTO department (id, name, school_id) VALUES (20097, 'ΕΡΕΥΝΗΤΙΚΗ ΜΟΝΑΔΑ 9', 1221);



INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (4, 'Afghanistan', 'AF', 'AFG', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (248, 'Åland Islands', 'AX', 'ALA', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (8, 'Albania', 'AL', 'ALB', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (12, 'Algeria', 'DZ', 'DZA', 'Africa', 'Northern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (16, 'American Samoa', 'AS', 'ASM', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (20, 'Andorra', 'AD', 'AND', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (24, 'Angola', 'AO', 'AGO', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (660, 'Anguilla', 'AI', 'AIA', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (10, 'Antarctica', 'AQ', 'ATA', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (28, 'Antigua and Barbuda', 'AG', 'ATG', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (32, 'Argentina', 'AR', 'ARG', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (51, 'Armenia', 'AM', 'ARM', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (533, 'Aruba', 'AW', 'ABW', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (36, 'Australia', 'AU', 'AUS', 'Oceania', 'Australia,New Zealand');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (40, 'Austria', 'AT', 'AUT', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (31, 'Azerbaijan', 'AZ', 'AZE', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (44, 'Bahamas', 'BS', 'BHS', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (48, 'Bahrain', 'BH', 'BHR', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (50, 'Bangladesh', 'BD', 'BGD', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (52, 'Barbados', 'BB', 'BRB', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (112, 'Belarus', 'BY', 'BLR', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (56, 'Belgium', 'BE', 'BEL', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (84, 'Belize', 'BZ', 'BLZ', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (204, 'Benin', 'BJ', 'BEN', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (60, 'Bermuda', 'BM', 'BMU', 'Americas', 'Northern America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (64, 'Bhutan', 'BT', 'BTN', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (68, 'Bolivia', 'BO', 'BOL', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (535, 'Bonaire', 'BQ', 'BES', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (70, 'Bosnia and Herzegovina', 'BA', 'BIH', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (72, 'Botswana', 'BW', 'BWA', 'Africa', 'Southern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (74, 'Bouvet Island', 'BV', 'BVT', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (76, 'Brazil', 'BR', 'BRA', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (86, 'British Indian Ocean Territory', 'IO', 'IOT', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (96, 'Brunei Darussalam', 'BN', 'BRN', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (100, 'Bulgaria', 'BG', 'BGR', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (854, 'Burkina Faso', 'BF', 'BFA', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (108, 'Burundi', 'BI', 'BDI', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (116, 'Cambodia', 'KH', 'KHM', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (120, 'Cameroon', 'CM', 'CMR', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (124, 'Canada', 'CA', 'CAN', 'Americas', 'Northern America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (132, 'Cape Verde', 'CV', 'CPV', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (136, 'Cayman Islands', 'KY', 'CYM', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (140, 'Central African Republic', 'CF', 'CAF', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (148, 'Chad', 'TD', 'TCD', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (152, 'Chile', 'CL', 'CHL', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (156, 'China', 'CN', 'CHN', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (162, 'Christmas Island', 'CX', 'CXR', 'Oceania', 'Australia,New Zealand');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (166, 'Cocos (Keeling) Islands', 'CC', 'CCK', 'Oceania', 'Australia,New Zealand');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (170, 'Colombia', 'CO', 'COL', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (174, 'Comoros', 'KM', 'COM', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (178, 'Republic of the Congo', 'CG', 'COG', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (180, 'Democratic Republic of the Congo', 'CD', 'COD', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (184, 'Cook Islands', 'CK', 'COK', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (188, 'Costa Rica', 'CR', 'CRI', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (384, 'Côte d''Ivoire', 'CI', 'CIV', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (191, 'Croatia', 'HR', 'HRV', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (192, 'Cuba', 'CU', 'CUB', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (531, 'Curaçao', 'CW', 'CUW', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (196, 'Cyprus', 'CY', 'CYP', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (203, 'Czech Republic', 'CZ', 'CZE', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (208, 'Denmark', 'DK', 'DNK', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (262, 'Djibouti', 'DJ', 'DJI', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (212, 'Dominica', 'DM', 'DMA', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (214, 'Dominican Republic', 'DO', 'DOM', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (218, 'Ecuador', 'EC', 'ECU', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (818, 'Egypt', 'EG', 'EGY', 'Africa', 'Northern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (222, 'El Salvador', 'SV', 'SLV', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (226, 'Equatorial Guinea', 'GQ', 'GNQ', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (232, 'Eritrea', 'ER', 'ERI', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (233, 'Estonia', 'EE', 'EST', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (231, 'Ethiopia', 'ET', 'ETH', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (238, 'Falkland Islands (Malvinas)', 'FK', 'FLK', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (234, 'Faroe Islands', 'FO', 'FRO', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (242, 'Fiji', 'FJ', 'FJI', 'Oceania', 'Melanesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (246, 'Finland', 'FI', 'FIN', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (250, 'France', 'FR', 'FRA', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (254, 'French Guiana', 'GF', 'GUF', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (258, 'French Polynesia', 'PF', 'PYF', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (260, 'French Southern Territories', 'TF', 'ATF', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (266, 'Gabon', 'GA', 'GAB', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (270, 'Gambia', 'GM', 'GMB', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (268, 'Georgia', 'GE', 'GEO', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (276, 'Germany', 'DE', 'DEU', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (288, 'Ghana', 'GH', 'GHA', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (292, 'Gibraltar', 'GI', 'GIB', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (300, 'Greece', 'GR', 'GRC', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (304, 'Greenland', 'GL', 'GRL', 'Americas', 'Northern America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (308, 'Grenada', 'GD', 'GRD', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (312, 'Guadeloupe', 'GP', 'GLP', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (316, 'Guam', 'GU', 'GUM', 'Oceania', 'Micronesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (320, 'Guatemala', 'GT', 'GTM', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (831, 'Guernsey', 'GG', 'GGY', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (324, 'Guinea', 'GN', 'GIN', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (624, 'Guinea-Bissau', 'GW', 'GNB', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (328, 'Guyana', 'GY', 'GUY', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (332, 'Haiti', 'HT', 'HTI', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (334, 'Heard Island and McDonald Islands', 'HM', 'HMD', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (336, 'Vatican City', 'VA', 'VAT', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (340, 'Honduras', 'HN', 'HND', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (344, 'Hong Kong', 'HK', 'HKG', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (348, 'Hungary', 'HU', 'HUN', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (352, 'Iceland', 'IS', 'ISL', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (356, 'India', 'IN', 'IND', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (360, 'Indonesia', 'ID', 'IDN', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (364, 'Iran', 'IR', 'IRN', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (368, 'Iraq', 'IQ', 'IRQ', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (372, 'Ireland', 'IE', 'IRL', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (833, 'Isle of Man', 'IM', 'IMN', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (376, 'Israel', 'IL', 'ISR', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (380, 'Italy', 'IT', 'ITA', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (388, 'Jamaica', 'JM', 'JAM', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (392, 'Japan', 'JP', 'JPN', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (832, 'Jersey', 'JE', 'JEY', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (400, 'Jordan', 'JO', 'JOR', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (398, 'Kazakhstan', 'KZ', 'KAZ', 'Asia', 'Central Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (404, 'Kenya', 'KE', 'KEN', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (296, 'Kiribati', 'KI', 'KIR', 'Oceania', 'Micronesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (414, 'Kuwait', 'KW', 'KWT', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (417, 'Kyrgyzstan', 'KG', 'KGZ', 'Asia', 'Central Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (418, 'Laos', 'LA', 'LAO', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (428, 'Latvia', 'LV', 'LVA', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (422, 'Lebanon', 'LB', 'LBN', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (426, 'Lesotho', 'LS', 'LSO', 'Africa', 'Southern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (430, 'Liberia', 'LR', 'LBR', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (434, 'Libya', 'LY', 'LBY', 'Africa', 'Northern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (438, 'Liechtenstein', 'LI', 'LIE', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (440, 'Lithuania', 'LT', 'LTU', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (442, 'Luxembourg', 'LU', 'LUX', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (446, 'Macao', 'MO', 'MAC', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (807, 'Macedonia', 'MK', 'MKD', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (450, 'Madagascar', 'MG', 'MDG', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (454, 'Malawi', 'MW', 'MWI', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (458, 'Malaysia', 'MY', 'MYS', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (462, 'Maldives', 'MV', 'MDV', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (466, 'Mali', 'ML', 'MLI', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (470, 'Malta', 'MT', 'MLT', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (584, 'Marshall Islands', 'MH', 'MHL', 'Oceania', 'Micronesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (474, 'Martinique', 'MQ', 'MTQ', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (478, 'Mauritania', 'MR', 'MRT', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (480, 'Mauritius', 'MU', 'MUS', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (175, 'Mayotte', 'YT', 'MYT', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (484, 'Mexico', 'MX', 'MEX', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (583, 'Micronesia', 'FM', 'FSM', 'Oceania', 'Micronesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (498, 'Moldova', 'MD', 'MDA', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (492, 'Monaco', 'MC', 'MCO', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (496, 'Mongolia', 'MN', 'MNG', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (499, 'Montenegro', 'ME', 'MNE', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (500, 'Montserrat', 'MS', 'MSR', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (504, 'Morocco', 'MA', 'MAR', 'Africa', 'Northern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (508, 'Mozambique', 'MZ', 'MOZ', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (104, 'Myanmar (Burma)', 'MM', 'MMR', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (516, 'Namibia', 'NA', 'NAM', 'Africa', 'Southern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (520, 'Nauru', 'NR', 'NRU', 'Oceania', 'Micronesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (524, 'Nepal', 'NP', 'NPL', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (528, 'Netherlands', 'NL', 'NLD', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (540, 'New Caledonia', 'NC', 'NCL', 'Oceania', 'Melanesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (554, 'New Zealand', 'NZ', 'NZL', 'Oceania', 'Australia,New Zealand');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (558, 'Nicaragua', 'NI', 'NIC', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (562, 'Niger', 'NE', 'NER', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (566, 'Nigeria', 'NG', 'NGA', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (570, 'Niue', 'NU', 'NIU', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (574, 'Norfolk Island', 'NF', 'NFK', 'Oceania', 'Australia,New Zealand');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (408, 'North Korea', 'KP', 'PRK', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (580, 'Northern Mariana Islands', 'MP', 'MNP', 'Oceania', 'Micronesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (578, 'Norway', 'NO', 'NOR', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (512, 'Oman', 'OM', 'OMN', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (586, 'Pakistan', 'PK', 'PAK', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (585, 'Palau', 'PW', 'PLW', 'Oceania', 'Micronesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (275, 'Palestinian Territory', 'PS', 'PSE', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (591, 'Panama', 'PA', 'PAN', 'Americas', 'Central America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (598, 'Papua New Guinea', 'PG', 'PNG', 'Oceania', 'Melanesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (600, 'Paraguay', 'PY', 'PRY', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (604, 'Peru', 'PE', 'PER', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (608, 'Philippines', 'PH', 'PHL', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (612, 'Pitcairn', 'PN', 'PCN', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (616, 'Poland', 'PL', 'POL', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (620, 'Portugal', 'PT', 'PRT', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (630, 'Puerto Rico', 'PR', 'PRI', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (634, 'Qatar', 'QA', 'QAT', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (638, 'Réunion', 'RE', 'REU', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (642, 'Romania', 'RO', 'ROU', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (643, 'Russian Federation', 'RU', 'RUS', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (646, 'Rwanda', 'RW', 'RWA', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (652, 'Saint Barthélemy', 'BL', 'BLM', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (654, 'Saint Helena', 'SH', 'SHN', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (659, 'Saint Kitts and Nevis', 'KN', 'KNA', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (662, 'Saint Lucia', 'LC', 'LCA', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (663, 'Saint Martin', 'MF', 'MAF', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (666, 'Saint Pierre and Miquelon', 'PM', 'SPM', 'Americas', 'Northern America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (670, 'Saint Vincent and the Grenadines', 'VC', 'VCT', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (882, 'Samoa', 'WS', 'WSM', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (674, 'San Marino', 'SM', 'SMR', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (678, 'São Tomé and Príncipe', 'ST', 'STP', 'Africa', 'Middle Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (682, 'Saudi Arabia', 'SA', 'SAU', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (686, 'Senegal', 'SN', 'SEN', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (688, 'Serbia', 'RS', 'SRB', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (690, 'Seychelles', 'SC', 'SYC', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (694, 'Sierra Leone', 'SL', 'SLE', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (702, 'Singapore', 'SG', 'SGP', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (534, 'Sint Maarten', 'SX', 'SXM', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (703, 'Slovakia', 'SK', 'SVK', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (705, 'Slovenia', 'SI', 'SVN', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (90, 'Solomon Islands', 'SB', 'SLB', 'Oceania', 'Melanesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (706, 'Somalia', 'SO', 'SOM', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (710, 'South Africa', 'ZA', 'ZAF', 'Africa', 'Southern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (239, 'South Georgia', 'GS', 'SGS', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (410, 'South Korea', 'KR', 'KOR', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (728, 'South Sudan', 'SS', 'SSD', NULL, NULL);
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (724, 'Spain', 'ES', 'ESP', 'Europe', 'Southern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (144, 'Sri Lanka', 'LK', 'LKA', 'Asia', 'Southern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (729, 'Sudan', 'SD', 'SDN', 'Africa', 'Northern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (740, 'Suriname', 'SR', 'SUR', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (744, 'Svalbard and Jan Mayen', 'SJ', 'SJM', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (748, 'Swaziland', 'SZ', 'SWZ', 'Africa', 'Southern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (752, 'Sweden', 'SE', 'SWE', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (756, 'Switzerland', 'CH', 'CHE', 'Europe', 'Western Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (760, 'Syrian Arab Republic', 'SY', 'SYR', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (158, 'Taiwan, Province of China', 'TW', 'TWN', 'Asia', 'Eastern Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (762, 'Tajikistan', 'TJ', 'TJK', 'Asia', 'Central Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (834, 'Tanzania', 'TZ', 'TZA', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (764, 'Thailand', 'TH', 'THA', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (626, 'Timor-Leste', 'TL', 'TLS', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (768, 'Togo', 'TG', 'TGO', 'Africa', 'Western Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (772, 'Tokelau', 'TK', 'TKL', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (776, 'Tonga', 'TO', 'TON', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (780, 'Trinidad and Tobago', 'TT', 'TTO', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (788, 'Tunisia', 'TN', 'TUN', 'Africa', 'Northern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (792, 'Turkey', 'TR', 'TUR', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (795, 'Turkmenistan', 'TM', 'TKM', 'Asia', 'Central Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (796, 'Turks and Caicos Islands', 'TC', 'TCA', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (798, 'Tuvalu', 'TV', 'TUV', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (800, 'Uganda', 'UG', 'UGA', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (804, 'Ukraine', 'UA', 'UKR', 'Europe', 'Eastern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (784, 'United Arab Emirates', 'AE', 'ARE', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (826, 'United Kingdom', 'GB', 'GBR', 'Europe', 'Northern Europe');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (840, 'United States', 'US', 'USA', 'Americas', 'Northern America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (581, 'United States Minor Outlying Islands', 'UM', 'UMI', 'Americas', 'Northern America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (858, 'Uruguay', 'UY', 'URY', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (860, 'Uzbekistan', 'UZ', 'UZB', 'Asia', 'Central Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (548, 'Vanuatu', 'VU', 'VUT', 'Oceania', 'Melanesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (862, 'Venezuela', 'VE', 'VEN', 'Americas', 'South America');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (704, 'Viet Nam', 'VN', 'VNM', 'Asia', 'South-Eastern,Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (92, 'Virgin Islands (British)', 'VG', 'VGB', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (850, 'Virgin Islands (U.S.)', 'VI', 'VIR', 'Americas', 'Caribbean');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (876, 'Wallis and Futuna', 'WF', 'WLF', 'Oceania', 'Polynesia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (732, 'Western Sahara', 'EH', 'ESH', 'Africa', 'Northern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (887, 'Yemen', 'YE', 'YEM', 'Asia', 'Western Asia');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (894, 'Zambia', 'ZM', 'ZMB', 'Africa', 'Eastern Africa');
INSERT INTO country (code, name, alpha3, alpha2, region, subregion) VALUES (716, 'Zimbabwe', 'ZW', 'ZWE', 'Africa', 'Eastern Africa');
