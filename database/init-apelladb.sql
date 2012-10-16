--
-- PostgreSQL database dump
--


--
-- TOC entry 2038 (class 0 OID 0)
-- Dependencies: 1585
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: apella
--

SELECT pg_catalog.setval('hibernate_sequence', 143, true);


--
-- TOC entry 2032 (class 0 OID 164396)
-- Dependencies: 1584
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: apella
--

INSERT INTO users VALUES (97, 'im@ebs.gr1345636152525', 'Σωτήριος', 'Άγγελος', 'Λένης', 'Sotirios', 'Angelos', 'Lenis', 'Αθήνα', 'Ελλάδα', '30', 'Μπερόβου', '11363', 'im@ebs.gr', '6951666072', NULL, 'f+2fZggHEYfOe5Rre2sDXxIxnNo=', '2012-07-12 13:36:32.178', 'ACTIVE', '2012-07-12 13:36:32.178', 'im@ebs.gr', NULL, 5, 'REGISTRATION_FORM', NULL);
INSERT INTO users VALUES (100, 'admin@ebs.gr1345636340474', 'Σωτήριος', 'Άγγελος2', 'Λένης2', 'Sotirios2', 'Angelos2', 'Lenis2', 'Χαλάνδρι', 'Ελλάδα', '30', 'Αρτεμισίου', '15234', 'admin@ebs.gr', '69516660000', NULL, 'f+2fZggHEYfOe5Rre2sDXxIxnNo=', '2012-07-04 15:49:29.109', 'ACTIVE', '2012-07-04 15:49:29.109', 'admin@ebs.gr', NULL, 9, 'REGISTRATION_FORM', NULL);

--
-- TOC entry 2030 (class 0 OID 164383)
-- Dependencies: 1582 2032
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: apella
--

INSERT INTO roles VALUES (100, 'ADMINISTRATOR', 'ACTIVE', '2012-07-04 16:07:56.658', 100, 0);
INSERT INTO roles VALUES (109, 'INSTITUTION_MANAGER', 'ACTIVE', '2012-08-20 13:34:15.604', 97, 1);

--
-- TOC entry 2006 (class 0 OID 164215)
-- Dependencies: 1558 2030
-- Data for Name: administrator; Type: TABLE DATA; Schema: public; Owner: apella
--

INSERT INTO administrator VALUES (100);

--
-- TOC entry 2017 (class 0 OID 164282)
-- Dependencies: 1569
-- Data for Name: filebody; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2018 (class 0 OID 164290)
-- Dependencies: 1570 2032 2017
-- Data for Name: fileheader; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2009 (class 0 OID 164230)
-- Dependencies: 1561 2018 2030 2018 2018
-- Data for Name: candidate; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2019 (class 0 OID 164299)
-- Dependencies: 1571
-- Data for Name: institution; Type: TABLE DATA; Schema: public; Owner: apella
--

INSERT INTO institution VALUES (1, 'ΑΛΕΞΑΝΔΡΕΙΟ ΤΕΙ ΘΕΣΣΑΛΟΝΙΚΗΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (44, 'Ελληνικό Ανοιχτό Πανεπιστήμιο', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (2, 'ΑΝΩΤΑΤΗ ΕΚΚΛΗΣΙΑΣΤΙΚΗ ΑΚΑΔΗΜΙΑ ΑΘΗΝΑΣ', 'REGISTRATION_FORM', 2);
INSERT INTO institution VALUES (3, 'ΑΝΩΤΑΤΗ ΕΚΚΛΗΣΙΑΣΤΙΚΗ ΑΚΑΔΗΜΙΑ ΒΕΛΛΑΣ ΙΩΑΝΝΙΝΩΝ', 'REGISTRATION_FORM', 2);
INSERT INTO institution VALUES (4, 'ΑΝΩΤΑΤΗ ΕΚΚΛΗΣΙΑΣΤΙΚΗ ΑΚΑΔΗΜΙΑ ΗΡΑΚΛΕΙΟΥ ΚΡΗΤΗΣ', 'REGISTRATION_FORM', 2);
INSERT INTO institution VALUES (5, 'ΑΝΩΤΑΤΗ ΕΚΚΛΗΣΙΑΣΤΙΚΗ ΑΚΑΔΗΜΙΑ ΘΕΣΣΑΛΟΝΙΚΗΣ', 'REGISTRATION_FORM', 2);
INSERT INTO institution VALUES (6, 'ΑΝΩΤΑΤΗ ΣΧΟΛΗ ΚΑΛΩΝ ΤΕΧΝΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (8, 'ΑΡΙΣΤΟΤΕΛΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΘΕΣ/ΝΙΚΗΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (9, 'ΑΣΠΑΙΤΕ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (10, 'ΓΕΩΠΟΝΙΚΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΘΗΝΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (11, 'ΔΗΜΟΚΡΙΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΘΡΑΚΗΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (12, 'ΕΘΝΙΚΟ & ΚΑΠΟΔΙΣΤΡΙΑΚΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΘΗΝΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (13, 'ΕΘΝΙΚΟ ΜΕΤΣΟΒΙΟ ΠΟΛΥΤΕΧΝΕΙΟ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (14, 'ΙΟΝΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (15, 'ΟΙΚΟΝΟΜΙΚΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΘΗΝΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (16, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΑΙΓΑΙΟΥ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (17, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΔΥΤΙΚΗΣ ΕΛΛΑΔΑΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (18, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΔΥΤΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (19, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΘΕΣΣΑΛΙΑΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (20, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΙΩΑΝΝΙΝΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (21, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΚΡΗΤΗΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (22, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΜΑΚΕΔΟΝΙΑΣ ΟΙΚΟΝΟΜΙΚΩΝ & ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (23, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΠΑΤΡΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (24, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΠΕΙΡΑΙΩΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (25, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΠΕΛΟΠΟΝΝΗΣΟΥ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (26, 'ΠΑΝΕΠΙΣΤΗΜΙΟ ΣΤΕΡΕΑΣ ΕΛΛΑΔΑΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (27, 'ΠΑΝΤΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ ΚΟΙΝΩΝΙΚΩΝ & ΠΟΛΙΤΙΚΩΝ ΣΠΟΥΔΩΝ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (28, 'ΠΟΛΥΤΕΧΝΕΙΟ ΚΡΗΤΗΣ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (29, 'ΤΕΙ ΑΘΗΝΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (31, 'ΤΕΙ ΗΠΕΙΡΟΥ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (30, 'ΤΕΙ ΔΥΤΙΚΗΣ ΜΑΚΕΔΟΝΙΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (32, 'ΤΕΙ ΙΟΝΙΩΝ ΝΗΣΩΝ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (33, 'ΤΕΙ ΚΑΒΑΛΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (34, 'ΤΕΙ ΚΑΛΑΜΑΤΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (35, 'ΤΕΙ ΚΡΗΤΗΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (36, 'ΤΕΙ ΛΑΜΙΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (37, 'ΤΕΙ ΛΑΡΙΣΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (38, 'ΤΕΙ ΜΕΣΟΛΟΓΓΙΟΥ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (39, 'ΤΕΙ ΠΑΤΡΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (40, 'ΤΕΙ ΠΕΙΡΑΙΑ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (41, 'ΤΕΙ ΣΕΡΡΩΝ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (42, 'ΤΕΙ ΧΑΛΚΙΔΑΣ', 'REGISTRATION_FORM', 1);
INSERT INTO institution VALUES (43, 'ΧΑΡΟΚΟΠΕΙΟ ΠΑΝΕΠΙΣΤΗΜΙΟ', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (45, 'Διεθνές Πανεπιστήμιο Ελλάδος', 'REGISTRATION_FORM', 0);
INSERT INTO institution VALUES (46, 'ΕΙΔΙΚΗ ΠΑΙΔΑΓΩΓΙΚΗ ΑΚΑΔΗΜΙΑ ΘΕΣΣΑΛΟΝΙΚΗΣ', 'REGISTRATION_FORM', 0);


--
-- TOC entry 2014 (class 0 OID 164259)
-- Dependencies: 1566 2019
-- Data for Name: department; Type: TABLE DATA; Schema: public; Owner: apella
--

INSERT INTO department VALUES (526, 'ΕΜΠΟΡΙΑΣ ΚΑΙ ΔΙΑΦΗΜΙΣΗΣ', '', 0, 35);
INSERT INTO department VALUES (8000, 'ΚΕΝΤΡΟ ΞΕΝΩΝ ΓΛΩΣΣΩΝ ΚΑΙ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ', '', 0, 1);
INSERT INTO department VALUES (8001, 'ΓΕΝΙΚΟ ΤΜΗΜΑ ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 1);
INSERT INTO department VALUES (8002, 'ΚΕΝΤΡΟ ΞΕΝΩΝ ΓΛΩΣΣΩΝ ΚΑΙ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ', '', 0, 40);
INSERT INTO department VALUES (8003, 'ΓΕΝΙΚΟ ΤΜΗΜΑ ΦΥΣΙΚΗΣ, ΧΗΜΕΙΑΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΥΛΙΚΩΝ', '', 0, 40);
INSERT INTO department VALUES (553, 'ΔΑΣΟΠΟΝΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΠΑΡΑΡΤΗΜΑ ΚΑΡΔΙΤΣΑΣ', 0, 37);
INSERT INTO department VALUES (8006, 'ΓΕΝΙΚΟ ΤΜΗΜΑ ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'ΣΧΟΛΗ ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 33);
INSERT INTO department VALUES (8007, 'ΓΕΝΙΚΟ ΤΜΗΜΑ', 'ΠΟΛΥΤΕΧΝΙΚΗ ΣΧΟΛΗ', 0, 8);
INSERT INTO department VALUES (8009, 'ΓΕΝΙΚΟ ΤΜΗΜΑ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 23);
INSERT INTO department VALUES (8005, 'ΓΕΝΙΚΟ ΤΜΗΜΑ ΜΑΘΗΜΑΤΙΚΩΝ', '', 0, 40);
INSERT INTO department VALUES (8008, 'ΓΕΝΙΚΟ ΤΜΗΜΑ', '', 0, 10);
INSERT INTO department VALUES (8010, 'ΓΕΝΙΚΟ ΤΜΗΜΑ ΔΙΚΑΙΟΥ', '', 0, 27);
INSERT INTO department VALUES (8011, 'ΓΕΝΙΚΟ ΤΜΗΜΑ', '', 0, 28);
INSERT INTO department VALUES (8012, 'ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 45);
INSERT INTO department VALUES (8013, 'ΕΠΙΣΤΗΜΩΝ ΤΕΧΝΟΛΟΓΙΑΣ', '', 0, 45);
INSERT INTO department VALUES (8014, 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 45);
INSERT INTO department VALUES (8015, 'ΚΕΝΤΡΟ ΞΕΝΩΝ ΓΛΩΣΣΩΝ ΚΑΙ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ', '', 0, 41);
INSERT INTO department VALUES (8016, 'ΕΙΔΙΚΗ ΠΑΙΔΑΓΩΓΙΚΗ ΑΚΑΔΗΜΙΑ ΘΕΣΣΑΛΟΝΙΚΗΣ', '', 0, 45);
INSERT INTO department VALUES (743, 'ΜΟΥΣΕΙΟΛΟΓΙΑΣ, ΜΟΥΣΕΙΟΓΡΑΦΙΑΣ ΚΑΙ ΣΧΕΔΙΑΣΜΟΥ ΕΚΘΕΣΕΩΝ', '', 0, 39);
INSERT INTO department VALUES (8017, 'ΓΕΝΙΚΟ ΤΜΗΜΑ ΠΑΙΔΑΓΩΓΙΚΩΝ ΜΑΘΗΜΑΤΩΝ', 'ΑΣΠΑΙΤΕ', 0, 9);
INSERT INTO department VALUES (709, 'ΗΛΕΚΤΡΟΝΙΚΗΣ', '', 0, 35);
INSERT INTO department VALUES (324, 'ΕΠΙΣΤΗΜΗΣ ΖΩΙΚΗΣ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΥΔΑΤΟΚΑΛΛΙΕΡΓΕΙΩΝ', '', 0, 10);
INSERT INTO department VALUES (8004, 'ΓΕΝΙΚΟ ΤΜΗΜΑ ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 42);
INSERT INTO department VALUES (532, 'ΦΥΣΙΚΩΝ ΠΟΡΩΝ ΚΑΙ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', '', 0, 35);
INSERT INTO department VALUES (747, 'ΔΙΑΤΡΟΦΗΣ ΚΑΙ ΔΙΑΙΤΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 37);
INSERT INTO department VALUES (323, 'ΕΠΙΣΤΗΜΗΣ ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', '', 0, 10);
INSERT INTO department VALUES (750, 'ΕΣΩΤΕΡΙΚΗΣ ΑΡΧΙΤΕΚΤΟΝΙΚΗΣ, ΔΙΑΚΟΣΜΗΣΗΣ ΚΑΙ ΣΧΕΔΙΑΣΜΟΥ ΑΝΤΙΚΕΙΜΕΝΩΝ', 'ΣΧΟΛΗ ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ & ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 41);
INSERT INTO department VALUES (186, 'ΓΛΩΣΣΑΣ ΦΙΛΟΛΟΓΙΑΣ ΚΑΙ ΠΟΛΙΤΙΣΜΟΥ ΠΑΡΕΥΞΕΙΝΙΩΝ ΧΩΡΩΝ', '', 0, 11);
INSERT INTO department VALUES (161, 'ΔΙΕΘΝΩΝ ΚΑΙ ΕΥΡΩΠΑΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 22);
INSERT INTO department VALUES (123, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΔΗΜΟΣΙΑΣ ΔΙΟΙΚΗΣΗΣ', 'ΝΟΜΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (124, 'ΔΗΜΟΣΙΑΣ ΔΙΟΙΚΗΣΗΣ', '', 0, 27);
INSERT INTO department VALUES (125, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΙΣΤΟΡΙΑΣ', '', 0, 27);
INSERT INTO department VALUES (145, 'ΙΣΤΟΡΙΑΣ', '', 0, 14);
INSERT INTO department VALUES (522, 'ΣΧΕΔΙΑΣΜΟΥ & ΤΕΧΝΟΛΟΓΙΑΣ ΞΥΛΟΥ & ΕΠΙΠΛΟΥ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΕΦΑΡΜΟΓΩΝ', 0, 37);
INSERT INTO department VALUES (739, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', '', 0, 37);
INSERT INTO department VALUES (170, 'ΨΥΧΟΛΟΓΙΑΣ', '', 0, 27);
INSERT INTO department VALUES (171, 'ΠΡΟΓΡΑΜΜΑ ΨΥΧΟΛΟΓΙΑΣ ΤΟΥ ΤΜΗΜΑΤΟΣ ΦΙΛΟΣΟΦΙΑΣ ΠΑΙΔΑΓΩΓΙΚΗΣ ΚΑΙ ΨΥΧΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (172, 'ΨΥΧΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (173, 'ΜΕΘΟΔΟΛΟΓΙΑΣ ΙΣΤΟΡΙΑΣ ΚΑΙ ΘΕΩΡΙΑΣ ΤΗΣ ΕΠΙΣΤΗΜΗΣ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 12);
INSERT INTO department VALUES (415, 'ΠΡΟΓΡΑΜΜΑ ΙΕΡΑΤΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 3);
INSERT INTO department VALUES (417, 'ΠΡΟΓΡΑΜΜΑ ΕΚΚΛΗΣΙΑΣΤΙΚΗΣ ΜΟΥΣΙΚΗΣ ΚΑΙ ΨΑΛΤΙΚΗΣ ', '', 0, 3);
INSERT INTO department VALUES (229, 'ΝΑΥΠΗΓΩΝ ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', '', 0, 13);
INSERT INTO department VALUES (231, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', '', 0, 13);
INSERT INTO department VALUES (227, 'ΑΓΡΟΝΟΜΩΝ ΚΑΙ ΤΟΠΟΓΡΑΦΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 8);
INSERT INTO department VALUES (230, 'ΜΗΧΑΝΙΚΩΝ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ', '', 0, 28);
INSERT INTO department VALUES (232, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 23);
INSERT INTO department VALUES (178, 'ΠΑΙΔΑΓΩΓΙΚΟ ΤΜΗΜΑ ΕΙΔΙΚΗΣ ΑΓΩΓΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΤΟΥ ΑΝΘΡΩΠΟΥ', 0, 19);
INSERT INTO department VALUES (295, 'ΙΑΤΡΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 12);
INSERT INTO department VALUES (297, 'ΙΑΤΡΙΚΗΣ', '', 0, 8);
INSERT INTO department VALUES (299, 'ΙΑΤΡΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 23);
INSERT INTO department VALUES (301, 'ΙΑΤΡΙΚΗΣ', '', 0, 20);
INSERT INTO department VALUES (315, 'ΟΙΚΟΝΟΜΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', '', 0, 24);
INSERT INTO department VALUES (210, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 18);
INSERT INTO department VALUES (217, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ  ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', '', 0, 13);
INSERT INTO department VALUES (225, 'ΑΓΡΟΝΟΜΩΝ ΚΑΙ ΤΟΠΟΓΡΑΦΩΝ ΜΗΧΑΝΙΚΩΝ', '', 0, 13);
INSERT INTO department VALUES (235, 'ΧΗΜΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', '', 0, 13);
INSERT INTO department VALUES (241, 'ΜΗΧΑΝΙΚΩΝ ΜΕΤΑΛΛΕΙΩΝ ΜΕΤΑΛΛΟΥΡΓΩΝ', '', 0, 13);
INSERT INTO department VALUES (246, 'ΕΦΑΡΜΟΣΜΕΝΩΝ ΜΑΘΗΜΑΤΙΚΩΝ ΚΑΙ ΦΥΣΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 13);
INSERT INTO department VALUES (355, 'ΔΙΕΘΝΩΝ ΚΑΙ ΕΥΡΩΠΑΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 24);
INSERT INTO department VALUES (357, 'ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'ΝΟΜΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ  ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (453, 'ΠΟΛΙΤΙΚΩΝ ΔΟΜΙΚΩΝ ΕΡΓΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 35);
INSERT INTO department VALUES (455, 'ΠΟΛΙΤΙΚΩΝ ΔΟΜΙΚΩΝ ΕΡΓΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 41);
INSERT INTO department VALUES (456, 'ΠΟΛΙΤΙΚΩΝ ΔΟΜΙΚΩΝ ΕΡΓΩΝ', '', 0, 37);
INSERT INTO department VALUES (457, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΠΟΛΙΤΙΚΩΝ ΔΟΜΙΚΩΝ ΕΡΓΩΝ', 'ΑΣΠΑΙΤΕ', 0, 9);
INSERT INTO department VALUES (461, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 40);
INSERT INTO department VALUES (536, 'ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 34);
INSERT INTO department VALUES (538, 'ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 31);
INSERT INTO department VALUES (540, 'ΖΩΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 31);
INSERT INTO department VALUES (541, 'ΖΩΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 37);
INSERT INTO department VALUES (544, 'ΜΗΧΑΝΟΛΟΓΙΑ ΚΑΙ ΥΔΑΤΙΝΩΝ ΠΟΡΩΝ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 38);
INSERT INTO department VALUES (545, 'ΜΗΧΑΝΙΚΗΣ ΒΙΟΣΥΣΤΗΜΑΤΩΝ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 37);
INSERT INTO department VALUES (546, 'ΤΕΧΝΟΛΟΓΙΑΣ ΒΙΟΛΟΓΙΚΗΣ ΓΕΩΡΓΙΑΣ ΚΑΙ ΤΡΟΦΙΜΩΝ', '', 0, 32);
INSERT INTO department VALUES (602, 'ΕΦ. ΞΕΝΩΝ ΓΛΩΣΣΩΝ ΣΤΗ ΔΙΟΙΚΗΣΗ & ΣΤΟ ΕΜΠΟΡΙΟ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 31);
INSERT INTO department VALUES (603, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 29);
INSERT INTO department VALUES (607, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 39);
INSERT INTO department VALUES (609, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 37);
INSERT INTO department VALUES (610, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 31);
INSERT INTO department VALUES (611, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 35);
INSERT INTO department VALUES (615, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (616, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', '', 0, 39);
INSERT INTO department VALUES (618, 'ΛΟΓΟΘΕΡΑΠΕΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 39);
INSERT INTO department VALUES (619, 'ΕΡΓΟΘΕΡΑΠΕΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (711, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 29);
INSERT INTO department VALUES (713, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΙΚΩΝ ΣΥΣΤΗΜΑΤΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 40);
INSERT INTO department VALUES (714, 'ΑΥΤΟΜΑΤΙΣΜΟΥ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 40);
INSERT INTO department VALUES (715, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 40);
INSERT INTO department VALUES (716, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 'ΤΕΧΝΟΛ. ΤΡΟΦΙΜΩΝ & ΔΙΑΤΡΟΦΗΣ', 0, 29);
INSERT INTO department VALUES (718, 'ΟΙΝΟΛΟΓΙΑΣ & ΤΕΧΝΟΛΟΓΙΑΣ ΠΟΤΩΝ', 'ΤΕΧΝΟΛ. ΤΡΟΦΙΜΩΝ & ΔΙΑΤΡΟΦΗΣ', 0, 29);
INSERT INTO department VALUES (722, 'ΑΥΤΟΜΑΤΙΣΜΟΥ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 42);
INSERT INTO department VALUES (207, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 11);
INSERT INTO department VALUES (212, 'ΔΑΣΟΛΟΓΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΦΥΣΙΚΩΝ ΠΟΡΩΝ', '', 0, 11);
INSERT INTO department VALUES (223, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ & ΜΗΧΑΝΙΚΩΝ Η/Υ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 11);
INSERT INTO department VALUES (224, 'ΜΗΧΑΝΙΚΩΝ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 11);
INSERT INTO department VALUES (234, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 11);
INSERT INTO department VALUES (302, 'ΙΑΤΡΙΚΗΣ', '', 0, 11);
INSERT INTO department VALUES (343, 'ΚΟΙΝΩΝΙΚΗΣ ΔΙΟΙΚΗΣΗΣ', '', 0, 11);
INSERT INTO department VALUES (349, 'ΔΙΕΘΝΩΝ ΟΙΚΟΝΟΜΙΚΩΝ ΣΧΕΣΕΩΝ ΚΑΙ ΑΝΑΠΤΥΞΗΣ', '', 0, 11);
INSERT INTO department VALUES (353, 'ΑΓΡΟΤΙΚΗΣ ΑΝΑΠΤΥΞΗΣ', '', 0, 11);
INSERT INTO department VALUES (404, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', '', 0, 11);
INSERT INTO department VALUES (476, 'ΜΗΧΑΝΙΚΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 11);
INSERT INTO department VALUES (247, 'ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (249, 'ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 20);
INSERT INTO department VALUES (311, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'ΝΟΜΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ  ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (366, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', '', 0, 14);
INSERT INTO department VALUES (604, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 40);
INSERT INTO department VALUES (106, 'ΕΛΛΗΝΙΚΗΣ ΦΙΛΟΛΟΓΙΑΣ', '', 0, 11);
INSERT INTO department VALUES (108, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΕΘΝΟΛΟΓΙΑΣ', '', 0, 11);
INSERT INTO department VALUES (121, 'ΝΟΜΙΚΗΣ', '', 0, 11);
INSERT INTO department VALUES (101, 'ΘΕΟΛΟΓΙΑΣ', 'ΘΕΟΛΟΓΙΚΗ', 0, 12);
INSERT INTO department VALUES (102, 'ΦΙΛΟΣΟΦΙΑΣ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (103, 'ΘΕΟΛΟΓΙΑΣ', 'ΘΕΟΛΟΓΙΚΗ', 0, 8);
INSERT INTO department VALUES (104, 'ΙΣΤΟΡΙΑΣ, ΑΡΧΑΙΟΛΟΓΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΠΟΛΙΤΙΣΜΙΚΩΝ ΑΓΑΘΩΝ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΠΟΛΙΤΙΣΜΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 25);
INSERT INTO department VALUES (105, 'ΚΟΙΝΩΝΙΚΗΣ ΘΕΟΛΟΓΙΑΣ', 'ΘΕΟΛΟΓΙΚΗ', 0, 12);
INSERT INTO department VALUES (107, 'ΠΟΙΜΑΝΤΙΚΗΣ ΚΑΙ  ΚΟΙΝΩΝΙΚΗΣ ΘΕΟΛΟΓΙΑΣ', 'ΘΕΟΛΟΓΙΚΗ', 0, 8);
INSERT INTO department VALUES (109, 'ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (110, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (111, 'ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (112, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (113, 'ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 20);
INSERT INTO department VALUES (114, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 20);
INSERT INTO department VALUES (117, 'ΝΟΜΙΚΗΣ', 'ΝΟΜΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (118, 'ΦΙΛΟΣΟΦΙΑΣ - ΠΑΙΔΑΓΩΓΙΚΗΣ ΚΑΙ ΨΥΧΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (119, 'ΝΟΜΙΚΗΣ', 'ΝΟΜΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ  ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (120, 'ΦΙΛΟΣΟΦΙΑΣ & ΠΑΙΔΑΓΩΓΙΚΗΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (122, 'ΦΙΛΟΣΟΦΙΑΣ ΠΑΙΔΑΓΩΓΙΚΗΣ ΚΑΙ ΨΥΧΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 20);
INSERT INTO department VALUES (143, 'ΠΑΙΔΑΓΩΓΙΚΟ  ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (360, 'ΓΕΩΠΟΝΙΑΣ ΙΧΘΥΟΛΟΓΙΑΣ ΚΑΙ ΥΔΑΤΙΝΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΓΕΩΠΟΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 19);
INSERT INTO department VALUES (543, 'ΑΓΡΟΤΙΚΗΣ ΑΝΑΠΤΥΞΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΑΓΡΟΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 1);
INSERT INTO department VALUES (126, 'ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', '', 0, 27);
INSERT INTO department VALUES (127, 'ΑΓΓΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (128, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 12);
INSERT INTO department VALUES (129, 'ΑΓΓΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (130, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 0, 20);
INSERT INTO department VALUES (131, 'ΓΑΛΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (133, 'ΓΑΛΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (134, 'ΕΠΙΣΤΗΜΩΝ ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΠΑΙΔΑΓΩΓΙΚΗ', 0, 8);
INSERT INTO department VALUES (135, 'ΓΕΡΜΑΝΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (136, 'ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ ΚΑΙ ΤΗΣ ΑΓΩΓΗΣ ΣΤΗΝ ΠΡΟΣΧΟΛΙΚΗ ΗΛΙΚΙΑ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (137, 'ΓΕΡΜΑΝΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (139, 'ΙΤΑΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 8);
INSERT INTO department VALUES (140, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΠΑΙΔΑΓΩΓΙΚΗ', 0, 8);
INSERT INTO department VALUES (141, 'ΠΑΙΔΑΓΩΓΙΚΟ  ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (146, 'ΘΕΑΤΡΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (147, 'ΔΗΜΟΣΙΟΓΡΑΦΙΑΣ ΚΑΙ ΜΕΣΩΝ ΜΑΖΙΚΗΣ ΕΝΗΜΕΡΩΣΗΣ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 8);
INSERT INTO department VALUES (148, 'ΕΠΙΚΟΙΝΩΝΙΑΣ & ΜΜΕ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 12);
INSERT INTO department VALUES (150, 'ΔΙΕΘΝΩΝ ΚΑΙ ΕΥΡΩΠΑΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 15);
INSERT INTO department VALUES (152, 'ΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΠΕΡΙΦΕΡΕΙΑΚΗΣ ΑΝΑΠΤΥΞΗΣ', '', 0, 27);
INSERT INTO department VALUES (153, 'ΕΠΙΚΟΙΝΩΝΙΑΣ, ΜΕΣΩΝ ΚΑΙ ΠΟΛΙΤΙΣΜΟΥ', '', 0, 27);
INSERT INTO department VALUES (154, 'ΕΚΠΑΙΔΕΥΣΗΣ ΚΑΙ ΑΓΩΓΗΣ ΣΤΗΝ ΠΡΟΣΧΟΛΙΚΗ ΗΛΙΚΙΑ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 12);
INSERT INTO department VALUES (155, 'ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΤΡΑΠΕΖΙΚΗΣ ΔΙΟΙΚΗΤΙΚΗΣ', '', 0, 24);
INSERT INTO department VALUES (156, 'ΠΑΙΔΑΓΩΓΙΚΟ ΝΗΠΙΑΓΩΓΩΝ', 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 0, 20);
INSERT INTO department VALUES (157, 'ΝΑΥΤΙΛΙΑΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 24);
INSERT INTO department VALUES (159, 'ΚΟΙΝΩΝΙΚΗΣ ΠΟΛΙΤΙΚΗΣ', '', 0, 27);
INSERT INTO department VALUES (163, 'ΚΙΝΗΜΑΤΟΓΡΑΦΟΥ', 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 0, 8);
INSERT INTO department VALUES (165, 'ΚΟΙΝΩΝΙΚΗΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ', '', 0, 27);
INSERT INTO department VALUES (168, 'ΘΕΑΤΡΟΥ', 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 0, 8);
INSERT INTO department VALUES (169, 'ΘΕΑΤΡΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (167, 'ΚΟΙΝΩΝΙΚΗΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ ΚΑΙ ΙΣΤΟΡΙΑΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (262, 'ΨΗΦΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ', '', 0, 24);
INSERT INTO department VALUES (144, 'ΟΙΚΙΑΚΗΣ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΟΙΚΟΛΟΓΙΑΣ', '', 0, 43);
INSERT INTO department VALUES (164, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΤΟΥ ΑΝΘΡΩΠΟΥ', 0, 19);
INSERT INTO department VALUES (166, 'ΠΑΙΔΑΓΩΓΙΚΟ ΠΡΟΣΧΟΛΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΤΟΥ ΑΝΘΡΩΠΟΥ', 0, 19);
INSERT INTO department VALUES (174, 'ΕΚΠΑΙΔΕΥΤΙΚΗΣ ΚΑΙ ΚΟΙΝΩΝΙΚΗΣ ΠΟΛΙΤΙΚΗΣ', '', 0, 22);
INSERT INTO department VALUES (175, 'ΦΙΛΟΛΟΓΙΑΣ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (176, 'ΒΑΛΚΑΝΙΚΩΝ ΣΛΑΒΙΚΩΝ ΚΑΙ ΑΝΑΤΟΛΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 22);
INSERT INTO department VALUES (179, 'ΔΙΕΘΝΩΝ ΚΑΙ ΕΥΡΩΠΑΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 27);
INSERT INTO department VALUES (142, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ  ΕΚΠΑΙΔΕΥΣΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 0, 11);
INSERT INTO department VALUES (160, 'ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ ΣΤΗΝ ΠΡΟΣΧΟΛΙΚΗ ΗΛΙΚΙΑ', 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 0, 11);
INSERT INTO department VALUES (182, 'ΙΤΑΛΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (183, 'ΙΣΠΑΝΙΚΗΣ ΓΛΩΣΣΑΣ ΚΑΙ ΦΙΛΟΛΟΓΙΑΣ ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (184, 'ΒΑΛΚΑΝΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 18);
INSERT INTO department VALUES (185, 'ΠΛΑΣΤΙΚΩΝ ΤΕΧΝΩΝ & ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΤΕΧΝΗΣ', '', 0, 20);
INSERT INTO department VALUES (187, 'ΚΟΙΝΩΝΙΚΗΣ ΚΑΙ ΕΚΠΑΙΔΕΥΤΙΚΗΣ ΠΟΛΙΤΙΚΗΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 25);
INSERT INTO department VALUES (188, 'ΤΟΥΡΚΙΚΩΝ ΣΠΟΥΔΩΝ ΚΑΙ ΣΥΓΧΡΟΝΩΝ ΑΣΙΑΤΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (189, 'ΦΙΛΟΛΟΓΙΑΣ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΠΟΛΙΤΙΣΜΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 25);
INSERT INTO department VALUES (190, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΑΝΘΡΩΠΙΝΗΣ ΚΙΝΗΣΗΣ ΚΑΙ ΠΟΙΟΤΗΤΑΣ ΤΗΣ ΖΩΗΣ', 0, 25);
INSERT INTO department VALUES (191, 'ΠΕΡΙΦΕΡΕΙΑΚΗΣ ΟΙΚΟΝΟΜΙΚΗΣ ΑΝΑΠΤΥΞΗΣ', '', 0, 26);
INSERT INTO department VALUES (192, 'ΣΛΑΒΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (203, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 8);
INSERT INTO department VALUES (205, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 23);
INSERT INTO department VALUES (211, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 8);
INSERT INTO department VALUES (213, 'ΜΗΧΑΝΟΛΟΓΩΝ &  ΑΕΡΟΝΑΥΠΗΓΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 23);
INSERT INTO department VALUES (214, 'ΜΗΧΑΝΙΚΩΝ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', '', 0, 28);
INSERT INTO department VALUES (215, 'ΜΗΧΑΝΙΚΩΝ ΗΛΕΚΤΡΟΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ ΚΑΙ ΠΛΗΡΟΦΟΡΙΚΗΣ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 23);
INSERT INTO department VALUES (219, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 8);
INSERT INTO department VALUES (221, 'ΗΛΕΚΤΡΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΥΠΟΛΟΓΙΣΤΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 23);
INSERT INTO department VALUES (180, 'ΝΑΥΤΙΛΙΑΣ ΚΑΙ ΕΠΙΧΕΙΡΗΜΑΤΙΚΩΝ ΥΠΗΡΕΣΙΩΝ', 'ΕΠΙΣΤΗΜΩΝ ΔΙΟΙΚΗΣΗΣ', 0, 16);
INSERT INTO department VALUES (181, 'ΜΕΣΟΓΕΙΑΚΩΝ ΣΠΟΥΔΩΝ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (177, 'ΙΣΤΟΡΙΑΣ, ΑΡΧΑΙΟΛΟΓΙΑΣ ΚΑΙ ΚΟΙΝΩΝΙΚΗΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ', 'ΕΠΙΣΤΗΜΩΝ ΤΟΥ ΑΝΘΡΩΠΟΥ', 0, 19);
INSERT INTO department VALUES (208, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 19);
INSERT INTO department VALUES (220, 'ΜΗΧΑΝΙΚΩΝ Η/Υ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ ΚΑΙ ΔΙΚΤΥΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 19);
INSERT INTO department VALUES (233, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 8);
INSERT INTO department VALUES (237, 'ΧΗΜΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 8);
INSERT INTO department VALUES (239, 'ΧΗΜΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 23);
INSERT INTO department VALUES (242, 'ΜΗΧΑΝΙΚΩΝ ΟΡΥΚΤΩΝ ΠΟΡΩΝ', '', 0, 28);
INSERT INTO department VALUES (243, 'ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (245, 'ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (253, 'ΦΥΣΙΚΗΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (255, 'ΦΥΣΙΚΗΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (257, 'ΦΥΣΙΚΗΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (259, 'ΦΥΣΙΚΗΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 20);
INSERT INTO department VALUES (263, 'ΧΗΜΕΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (265, 'ΧΗΜΕΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (267, 'ΧΗΜΕΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (269, 'ΧΗΜΕΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 20);
INSERT INTO department VALUES (272, 'ΜΗΧΑΝΙΚΩΝ ΕΠΙΣΤΗΜΗΣ ΥΛΙΚΩΝ', 'ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΩΝ', 0, 20);
INSERT INTO department VALUES (273, 'ΓΕΩΠΟΝΙΑΣ', '', 0, 8);
INSERT INTO department VALUES (201, 'ΠΟΛΙΤΙΚΩΝ ΜΗΧΑΝΙΚΩΝ', '', 0, 13);
INSERT INTO department VALUES (209, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', '', 0, 13);
INSERT INTO department VALUES (275, 'ΔΑΣΟΛΟΓΙΑΣ ΚΑΙ ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', '', 0, 8);
INSERT INTO department VALUES (277, 'ΒΙΟΛΟΓΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (279, 'ΒΙΟΛΟΓΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (280, 'ΒΙΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΩΝ', 'ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΩΝ', 0, 20);
INSERT INTO department VALUES (281, 'ΒΙΟΛΟΓΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (283, 'ΓΕΩΛΟΓΙΑΣ ΚΑΙ ΓΕΩΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (285, 'ΓΕΩΛΟΓΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (287, 'ΓΕΩΛΟΓΙΑΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (288, 'ΕΠΙΣΤΗΜΗΣ ΤΩΝ ΥΛΙΚΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 23);
INSERT INTO department VALUES (289, 'ΦΑΡΜΑΚΕΥΤΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 12);
INSERT INTO department VALUES (291, 'ΦΑΡΜΑΚΕΥΤΙΚΗΣ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 8);
INSERT INTO department VALUES (293, 'ΦΑΡΜΑΚΕΥΤΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 23);
INSERT INTO department VALUES (238, 'ΜΗΧΑΝΙΚΩΝ ΣΧΕΔΙΑΣΗΣ ΠΡΟΙΟΝΤΩΝ ΚΑΙ ΣΥΣΤΗΜΑΤΩΝ', '', 0, 16);
INSERT INTO department VALUES (250, 'ΕΠΙΣΤΗΜΩΝ ΤΗΣ ΘΑΛΑΣΣΑΣ', 'ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 0, 16);
INSERT INTO department VALUES (252, 'ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (276, 'ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 0, 16);
INSERT INTO department VALUES (228, 'ΜΗΧΑΝΙΚΩΝ ΧΩΡΟΤΑΞΙΑΣ, ΠΟΛΕΟΔΟΜΙΑΣ ΚΑΙ ΠΕΡΙΦΕΡΕΙΑΚΗΣ ΑΝΑΠΤΥΞΗΣ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 19);
INSERT INTO department VALUES (236, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 19);
INSERT INTO department VALUES (274, 'ΓΕΩΠΟΝΙΑΣ ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ ΚΑΙ ΑΓΡΟΤΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΓΕΩΠΟΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 19);
INSERT INTO department VALUES (284, 'ΒΙΟΧΗΜΕΙΑΣ ΚΑΙ ΒΙΟΤΕΧΝΟΛΟΓΙΑΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 19);
INSERT INTO department VALUES (303, 'ΟΔΟΝΤΙΑΤΡΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 12);
INSERT INTO department VALUES (305, 'ΟΔΟΝΤΙΑΤΡΙΚΗΣ', '', 0, 8);
INSERT INTO department VALUES (306, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 12);
INSERT INTO department VALUES (307, 'ΚΤΗΝΙΑΤΡΙΚΗΣ', '', 0, 8);
INSERT INTO department VALUES (309, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'ΝΟΜΙΚΩΝ ΟΙΚΟΝΟΜΙΚΩΝ ΚΑΙ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (312, 'ΟΙΚΟΝΟΜΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', '', 0, 15);
INSERT INTO department VALUES (313, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 15);
INSERT INTO department VALUES (314, 'ΜΑΡΚΕΤΙΝΓΚ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ', '', 0, 15);
INSERT INTO department VALUES (316, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 24);
INSERT INTO department VALUES (317, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 22);
INSERT INTO department VALUES (318, 'ΣΤΑΤΙΣΤΙΚΗΣ ΚΑΙ ΑΣΦΑΛΙΣΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', '', 0, 24);
INSERT INTO department VALUES (319, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 23);
INSERT INTO department VALUES (322, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 22);
INSERT INTO department VALUES (325, 'ΓΕΩΠΟΝΙΚΗΣ ΒΙΟΤΕΧΝΟΛΟΓΙΑΣ', '', 0, 10);
INSERT INTO department VALUES (326, 'ΑΓΡΟΤΙΚΗΣ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΑΝΑΠΤΥΞΗΣ', '', 0, 10);
INSERT INTO department VALUES (327, 'ΑΞΙΟΠΟΙΗΣΗΣ ΦΥΣΙΚΩΝ ΠΟΡΩΝ ΚΑΙ ΓΕΩΡΓΙΚΗΣ ΜΗΧΑΝΙΚΗΣ', '', 0, 10);
INSERT INTO department VALUES (328, 'ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', '', 0, 10);
INSERT INTO department VALUES (329, 'ΣΤΑΤΙΣΤΙΚΗΣ', '', 0, 15);
INSERT INTO department VALUES (330, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 12);
INSERT INTO department VALUES (331, 'ΗΛΕΚΤΡΟΝΙΚΩΝ ΜΗΧΑΝΙΚΩΝ ΚΑΙ ΜΗΧΑΝΙΚΩΝ ΥΠΟΛΟΓΙΣΤΩΝ', '', 0, 28);
INSERT INTO department VALUES (333, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', '', 0, 15);
INSERT INTO department VALUES (334, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΠΑΙΔΑΓΩΓΙΚΗ', 0, 18);
INSERT INTO department VALUES (335, 'ΕΦΑΡΜΟΣΜΕΝΗΣ ΠΛΗΡΟΦΟΡΙΚΗΣ', '', 0, 22);
INSERT INTO department VALUES (336, 'ΒΙΟΜΗΧΑΝΙΚΗΣ ΔΙΟΙΚΗΣΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', '', 0, 24);
INSERT INTO department VALUES (337, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', '', 0, 22);
INSERT INTO department VALUES (338, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 8);
INSERT INTO department VALUES (339, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', '', 0, 24);
INSERT INTO department VALUES (340, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 20);
INSERT INTO department VALUES (341, 'ΠΑΙΔΑΓΩΓΙΚΟ ΝΗΠΙΑΓΩΓΩΝ', 'ΠΑΙΔΑΓΩΓΙΚΗ', 0, 18);
INSERT INTO department VALUES (345, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 20);
INSERT INTO department VALUES (347, 'ΛΟΓΙΣΤΙΚΗΣ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ', '', 0, 15);
INSERT INTO department VALUES (352, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 23);
INSERT INTO department VALUES (304, 'ΙΑΤΡΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 21);
INSERT INTO department VALUES (310, 'ΓΕΩΓΡΑΦΙΑΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (320, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΕΠΙΣΤΗΜΩΝ ΔΙΟΙΚΗΣΗΣ', 0, 16);
INSERT INTO department VALUES (348, 'ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (294, 'ΕΠΙΣΤΗΜΗΣ ΔΙΑΙΤΟΛΟΓΙΑΣ - ΔΙΑΤΡΟΦΗΣ', '', 0, 43);
INSERT INTO department VALUES (300, 'ΙΑΤΡΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 19);
INSERT INTO department VALUES (308, 'ΚΤΗΝΙΑΤΡΙΚΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΥΓΕΙΑΣ', 0, 19);
INSERT INTO department VALUES (332, 'ΜΗΧΑΝΟΛΟΓΩΝ ΜΗΧΑΝΙΚΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 19);
INSERT INTO department VALUES (350, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 19);
INSERT INTO department VALUES (358, 'ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΥΠΟΛΟΓΙΣΤΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', 0, 25);
INSERT INTO department VALUES (359, 'ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', 0, 25);
INSERT INTO department VALUES (361, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'ΕΠΙΣΤΗΜΩΝ ΔΙΟΙΚΗΣΗΣ ΚΑΙ ΟΙΚΟΝΟΜΙΑΣ', 0, 25);
INSERT INTO department VALUES (362, 'ΘΕΑΤΡΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 0, 25);
INSERT INTO department VALUES (363, 'ΜΗΧΑΝΙΚΩΝ ΧΩΡΟΤΑΞΙΑΣ ΚΑΙ ΑΝΑΠΤΥΞΗΣ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 8);
INSERT INTO department VALUES (364, 'ΜΑΡΚΕΤΙΝΓΚ ΚΑΙ ΔΙΟΙΚΗΣΗΣ ΛΕΙΤΟΥΡΓΙΩΝ', '', 0, 22);
INSERT INTO department VALUES (365, 'ΔΙΟΙΚΗΣΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', '', 0, 22);
INSERT INTO department VALUES (367, 'ΤΕΧΝΩΝ ΗΧΟΥ ΚΑΙ ΕΙΚΟΝΑΣ', '', 0, 14);
INSERT INTO department VALUES (369, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΜΕ ΕΦΑΡΜΟΓΕΣ ΣΤΗ ΒΙΟΙΑΤΡΙΚΗ', '', 0, 26);
INSERT INTO department VALUES (370, 'ΑΡΧΙΤΕΚΤΟΝΩΝ ΜΗΧΑΝΙΚΩΝ', '', 0, 28);
INSERT INTO department VALUES (371, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 'ΠΟΛΥΤΕΧΝΙΚΗ', 0, 18);
INSERT INTO department VALUES (373, 'ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', '', 0, 11);
INSERT INTO department VALUES (374, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 11);
INSERT INTO department VALUES (385, 'ΞΕΝΩΝ ΓΛΩΣΣΩΝ, ΜΕΤΑΦΡΑΣΗΣ ΚΑΙ ΔΙΕΡΜΗΝΕΙΑΣ', '', 0, 14);
INSERT INTO department VALUES (400, 'ΟΡΓΑΝΩΣΗΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΑΘΛΗΤΙΣΜΟΥ', 'ΕΠΙΣΤΗΜΩΝ ΑΝΘΡΩΠΙΝΗΣ ΚΙΝΗΣΗΣ ΚΑΙ ΠΟΙΟΤΗΤΑΣ ΤΗΣ ΖΩΗΣ', 0, 25);
INSERT INTO department VALUES (401, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 12);
INSERT INTO department VALUES (402, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ  (ΣΕΡΡΩΝ)', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 8);
INSERT INTO department VALUES (403, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', 'ΑΝΕΞΑΡΤΗΤΑ ΤΜΗΜΑΤΑ', 0, 8);
INSERT INTO department VALUES (406, 'ΜΟΥΣΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 0, 8);
INSERT INTO department VALUES (407, 'ΜΟΥΣΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 14);
INSERT INTO department VALUES (408, 'ΜΟΥΣΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 12);
INSERT INTO department VALUES (409, 'ΜΟΥΣΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΗΣ', '', 0, 22);
INSERT INTO department VALUES (411, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΔΙΕΘΝΩΝ ΣΧΕΣΕΩΝ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 25);
INSERT INTO department VALUES (413, 'ΠΡΟΓΡΑΜΜΑ ΙΕΡΑΤΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 2);
INSERT INTO department VALUES (414, 'ΠΡΟΓΡΑΜΜΑ ΙΕΡΑΤΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 5);
INSERT INTO department VALUES (416, 'ΠΡΟΓΡΑΜΜΑ ΙΕΡΑΤΙΚΩΝ ΣΠΟΥΔΩΝ', '', 0, 4);
INSERT INTO department VALUES (418, 'ΠΡΟΓΡΑΜΜΑ ΕΚΚΛΗΣΙΑΣΤΙΚΗΣ ΜΟΥΣΙΚΗΣ ΚΑΙ ΨΑΛΤΙΚΗΣ ', '', 0, 4);
INSERT INTO department VALUES (419, 'ΠΡΟΓΡΑΜΜΑ ΔΙΑΧΕΙΡΙΣΗΣ ΕΚΚΛΗΣΙΑΣΤΙΚΩΝ ΚΕΙΜΗΛΙΩΝ', '', 0, 2);
INSERT INTO department VALUES (420, 'ΠΡΟΓΡΑΜΜΑ ΔΙΑΧΕΙΡΙΣΗΣ ΕΚΚΛΗΣΙΑΣΤΙΚΩΝ ΚΕΙΜΗΛΙΩΝ', '', 0, 5);
INSERT INTO department VALUES (443, 'ΠΟΛΙΤΙΚΩΝ ΕΡΓΩΝ ΥΠΟΔΟΜΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 29);
INSERT INTO department VALUES (444, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΠΟΛΙΤΙΚΩΝ ΕΡΓΩΝ ΥΠΟΔΟΜΗΣ', 'ΑΣΠΑΙΤΕ', 0, 9);
INSERT INTO department VALUES (445, 'ΠΟΛΙΤΙΚΩΝ ΔΟΜΙΚΩΝ ΕΡΓΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 40);
INSERT INTO department VALUES (449, 'ΠΟΛΙΤΙΚΩΝ ΕΡΓΩΝ  ΥΠΟΔΟΜΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 39);
INSERT INTO department VALUES (451, 'ΠΟΛΙΤΙΚΩΝ ΕΡΓΩΝ ΥΠΟΔΟΜΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΕΦΑΡΜΟΓΩΝ', 0, 37);
INSERT INTO department VALUES (354, 'ΠΟΛΙΤΙΣΜΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (356, 'ΓΕΩΓΡΑΦΙΑΣ', '', 0, 43);
INSERT INTO department VALUES (412, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΜΑΤΙΚΗΣ', '', 0, 43);
INSERT INTO department VALUES (405, 'ΕΠΙΣΤΗΜΗΣ ΦΥΣΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΑΘΛΗΤΙΣΜΟΥ', '', 0, 19);
INSERT INTO department VALUES (447, 'ΠΟΛΙΤΙΚΩΝ ΕΡΓΩΝ ΥΠΟΔΟΜΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 1);
INSERT INTO department VALUES (465, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 39);
INSERT INTO department VALUES (467, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΕΦΑΡΜΟΓΩΝ', 0, 37);
INSERT INTO department VALUES (469, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 35);
INSERT INTO department VALUES (471, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 33);
INSERT INTO department VALUES (475, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 41);
INSERT INTO department VALUES (477, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΑΣΠΑΙΤΕ', 0, 9);
INSERT INTO department VALUES (479, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 42);
INSERT INTO department VALUES (480, 'ΤΕΧΝΟΛΟΓΙΑΣ ΙΑΤΡΙΚΩΝ ΟΡΓΑΝΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 29);
INSERT INTO department VALUES (483, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 40);
INSERT INTO department VALUES (487, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 39);
INSERT INTO department VALUES (489, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΕΦΑΡΜΟΓΩΝ', 0, 37);
INSERT INTO department VALUES (491, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 35);
INSERT INTO department VALUES (493, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 33);
INSERT INTO department VALUES (497, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΑΣΠΑΙΤΕ', 0, 9);
INSERT INTO department VALUES (498, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 36);
INSERT INTO department VALUES (499, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 42);
INSERT INTO department VALUES (501, 'ΗΛΕΚΤΡΟΝΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 29);
INSERT INTO department VALUES (503, 'ΗΛΕΚΤΡΟΝΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 40);
INSERT INTO department VALUES (506, 'ΠΛΗΡΟΦΟΡΙΚΗΣ & ΤΕΧΝΟΛΟΓΙΑΣ Η/Υ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 36);
INSERT INTO department VALUES (507, 'ΕΚΠΑΙΔΕΥΤΙΚΩΝ ΗΛΕΚΤΡΟΝΙΚΗΣ', 'ΑΣΠΑΙΤΕ', 0, 9);
INSERT INTO department VALUES (508, 'ΤΕΧΝΟΛΟΓΙΑΣ ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', '', 0, 32);
INSERT INTO department VALUES (509, 'ΤΟΠΟΓΡΑΦΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 29);
INSERT INTO department VALUES (510, 'ΓΕΩΠΛΗΡΟΦΟΡΙΚΗΣ & ΤΟΠΟΓΡΑΦΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 41);
INSERT INTO department VALUES (511, 'ΝΑΥΠΗΓΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 29);
INSERT INTO department VALUES (512, 'ΕΝΕΡΓΕΙΑΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 29);
INSERT INTO department VALUES (513, 'ΤΕΧΝΟΛΟΓΙΑΣ ΠΕΤΡΕΛΑΙΟΥ ΚΑΙ ΦΥΣΙΚΟΥ ΑΕΡΙΟΥ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 33);
INSERT INTO department VALUES (517, 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ', 'ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ & ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 29);
INSERT INTO department VALUES (518, 'ΦΩΤΟΓΡΑΦΙΑΣ ΚΑΙ ΟΠΤΙΚΟΑΚΟΥΣΤΙΚΩΝ ΤΕΧΝΩΝ', 'ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ & ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 29);
INSERT INTO department VALUES (519, 'ΕΣΩΤΕΡΙΚΗΣ ΑΡΧΙΤΕΚΤΟΝΙΚΗΣ, ΔΙΑΚΟΣΜΗΣΗΣ ΚΑΙ ΣΧΕΔΙΑΣΜΟΥ ΑΝΤΙΚΕΙΜΕΝΩΝ', 'ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ & ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 29);
INSERT INTO department VALUES (520, 'ΣΥΝΤΗΡΗΣΗΣ ΑΡΧΑΙΟΤΗΤΩΝ & ‘ΕΡΓΩΝ ΤΕΧΝΗΣ', 'ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ & ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 29);
INSERT INTO department VALUES (521, 'ΓΡΑΦΙΣΤΙΚΗΣ', 'ΓΡΑΦΙΚΩΝ ΤΕΧΝΩΝ & ΚΑΛΛΙΤΕΧΝΙΚΩΝ ΣΠΟΥΔΩΝ', 0, 29);
INSERT INTO department VALUES (523, 'ΑΝΑΚΑΙΝΙΣΗΣ & ΑΠΟΚΑΤΑΣΤΑΣΗΣ ΚΤΙΡΙΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 39);
INSERT INTO department VALUES (524, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΜΕΣΩΝ ΜΑΖΙΚΗΣ ΕΝΗΜΕΡΩΣΗΣ', '', 0, 39);
INSERT INTO department VALUES (525, 'ΕΦΑΡΜΟΓΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΣΤΗ ΔΙΟΙΚΗΣΗ ΚΑΙ ΣΤΗΝ ΟΙΚΟΝΟΜΙΑ', '', 0, 39);
INSERT INTO department VALUES (527, 'ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΑΣΦΑΛΙΣΤΙΚΗΣ', '', 0, 35);
INSERT INTO department VALUES (528, 'ΑΡΧΙΤΕΚΤΟΝΙΚΗΣ ΤΟΠΙΟΥ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 33);
INSERT INTO department VALUES (529, 'ΤΕΧΝΟΛΟΓΙΑΣ ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', '', 0, 34);
INSERT INTO department VALUES (533, 'ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 37);
INSERT INTO department VALUES (535, 'ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 35);
INSERT INTO department VALUES (505, 'ΗΛΕΚΤΡΟΝΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 1);
INSERT INTO department VALUES (473, 'ΜΗΧΑΝΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 30);
INSERT INTO department VALUES (495, 'ΗΛΕΚΤΡΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 30);
INSERT INTO department VALUES (514, 'ΤΕΧΝΟΛΟΓΙΩΝ ΑΝΤΙΡΡΥΠΑΝΣΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 30);
INSERT INTO department VALUES (548, 'ΙΧΘΥΟΚΟΜΙΑΣ ΑΛΙΕΙΑΣ', '', 0, 31);
INSERT INTO department VALUES (550, 'ΔΑΣΟΠΟΝΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ  ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 33);
INSERT INTO department VALUES (551, 'ΔΑΣΟΠΟΝΙΑΣ ΚΑΙ ΔΙΑΧΕΙΡΙΣΗΣ ΦΥΣΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 36);
INSERT INTO department VALUES (552, 'ΘΕΡΜΟΚΗΠΙΑΚΩΝ ΚΑΛΛΙΕΡΓΕΙΩΝ ΚΑΙ ΑΝΘΟΚΟΜΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 35);
INSERT INTO department VALUES (554, 'ΘΕΡΜΟΚΗΠΙΑΚΩΝ ΚΑΛΛΙΕΡΓΕΙΩΝ & ΑΝΘΟΚΟΜΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 34);
INSERT INTO department VALUES (555, 'ΥΔΑΤΟΚΑΛΛΙΕΡΓΕΙΩΝ ΚΑΙ ΑΛΙΕΥΤΙΚΗΣ ΔΙΑΧΕΙΡΙΣΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 38);
INSERT INTO department VALUES (556, 'ΑΝΘΟΚΟΜΙΑΣ & ΑΡΧΙΤΕΚΤΟΝΙΚΗΣ ΤΟΠΙΟΥ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 31);
INSERT INTO department VALUES (557, 'ΘΕΡΜΟΚΗΠΙΑΚΩΝ ΚΑΛΛΙΕΡΓΕΙΩΝ & ΑΝΘΟΚΟΜΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 38);
INSERT INTO department VALUES (559, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 40);
INSERT INTO department VALUES (563, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 39);
INSERT INTO department VALUES (565, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 37);
INSERT INTO department VALUES (567, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 35);
INSERT INTO department VALUES (569, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 33);
INSERT INTO department VALUES (575, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 41);
INSERT INTO department VALUES (577, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 42);
INSERT INTO department VALUES (578, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 31);
INSERT INTO department VALUES (579, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 38);
INSERT INTO department VALUES (581, 'ΕΜΠΟΡΙΑΣ & ΔΙΑΦΗΜΙΣΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 29);
INSERT INTO department VALUES (582, 'ΔΙΟΙΚΗΣΗΣ & ΔΙΑΧΕΙΡΙΣΗΣ ΕΡΓΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 37);
INSERT INTO department VALUES (584, 'ΤΟΠΙΚΗΣ ΑΥΤΟΔΙΟΙΚΗΣΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 34);
INSERT INTO department VALUES (585, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 29);
INSERT INTO department VALUES (586, 'ΔΙΑΧΕΙΡΙΣΗΣ ΠΛΗΡΟΦΟΡΙΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 33);
INSERT INTO department VALUES (587, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 33);
INSERT INTO department VALUES (589, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 39);
INSERT INTO department VALUES (590, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', '', 0, 32);
INSERT INTO department VALUES (591, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 37);
INSERT INTO department VALUES (593, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 35);
INSERT INTO department VALUES (594, 'ΔΙΟΙΚΗΣΗ ΜΟΝΑΔΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 34);
INSERT INTO department VALUES (595, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 41);
INSERT INTO department VALUES (597, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 42);
INSERT INTO department VALUES (598, 'ΔΙΟΙΚΗΣΗΣ ΚΟΙΝΩΝΙΚΩΝ-ΣΥΝΕΤΑΙΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ ΚΑΙ ΟΡΓΑΝΩΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 38);
INSERT INTO department VALUES (599, 'ΒΙΒΛΙΟΘΗΚΟΝΟΜΙΑΣ ΚΑΙ ΣΥΣΤΗΜΑΤΩΝ ΠΛΗΡΟΦΟΡΗΣΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 29);
INSERT INTO department VALUES (537, 'ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ - ΓΕΩΠΟΝΙΑΣ', 0, 30);
INSERT INTO department VALUES (542, 'ΖΩΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ - ΓΕΩΠΟΝΙΑΣ', 0, 30);
INSERT INTO department VALUES (558, 'ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 'ΔΙΟΙΚΗΣΗΣ ΟΙΚΟΝΟΜΙΑΣ', 0, 30);
INSERT INTO department VALUES (571, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ ΟΙΚΟΝΟΜΙΑΣ', 0, 30);
INSERT INTO department VALUES (580, 'ΔΙΕΘΝΟΥΣ ΕΜΠΟΡΙΟΥ', '', 0, 30);
INSERT INTO department VALUES (592, 'ΒΙΟΜΗΧΑΝΙΚΟΥ ΣΧΕΔΙΑΣΜΟΥ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 30);
INSERT INTO department VALUES (620, 'ΛΟΓΟΘΕΡΑΠΕΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 31);
INSERT INTO department VALUES (621, 'ΙΑΤΡΙΚΩΝ ΕΡΓΑΣΤΗΡΙΩΝ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (625, 'ΙΑΤΡΙΚΩΝ ΕΡΓΑΣΤΗΡΙΩΝ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 37);
INSERT INTO department VALUES (627, 'ΡΑΔΙΟΛΟΓΙΑΣ - ΑΚΤΙΝΟΛΟΓΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (628, 'ΔΙΑΤΡΟΦΗΣ & ΔΙΑΙΤΟΛΟΓΙΑΣ', '', 0, 35);
INSERT INTO department VALUES (629, 'ΟΔΟΝΤΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (631, 'ΔΙΟΙΚ. ΜΟΝ. ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 29);
INSERT INTO department VALUES (633, 'ΔΗΜΟΣΙΑΣ ΥΓΙΕΙΝΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (635, 'ΟΠΤΙΚΗΣ ΚΑΙ ΟΠΤΟΜΕΤΡΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (637, 'ΑΙΣΘΗΤΙΚΗΣ ΚΑΙ ΚΟΣΜΗΤΟΛΟΓΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (641, 'ΚΟΙΝΩΝΙΚΗΣ ΕΡΓΑΣΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 39);
INSERT INTO department VALUES (643, 'ΚΟΙΝΩΝΙΚΗΣ ΕΡΓΑΣΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 35);
INSERT INTO department VALUES (644, 'ΜΟΥΣΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ & ΑΚΟΥΣΤΙΚΗΣ', '', 0, 35);
INSERT INTO department VALUES (645, 'ΝΟΣΗΛΕΥΤΙΚΗΣ Α', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (649, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 39);
INSERT INTO department VALUES (651, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 37);
INSERT INTO department VALUES (652, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 36);
INSERT INTO department VALUES (653, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 35);
INSERT INTO department VALUES (654, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 31);
INSERT INTO department VALUES (655, 'ΜΑΙΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (659, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 36);
INSERT INTO department VALUES (661, 'ΚΟΙΝΩΝΙΚΗΣ ΕΡΓΑΣΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (663, 'ΛΟΓΟΘΕΡΑΠΕΙΑΣ', '', 0, 34);
INSERT INTO department VALUES (667, 'ΕΠΙΣΚΕΠΤΩΝ/ΤΡΙΩΝ ΥΓΕΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (690, 'ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (692, 'ΒΡΕΦΟΝΗΠΙΟΚΟΜΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 31);
INSERT INTO department VALUES (696, 'ΛΑΙΚΗΣ ΚΑΙ ΠΑΡΑΔΟΣΙΑΚΗΣ ΜΟΥΣΙΚΗΣ', 'ΜΟΥΣΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', 0, 31);
INSERT INTO department VALUES (697, 'ΤΕΧΝΟΛΟΓΙΑΣ ΗΧΟΥ ΚΑΙ ΜΟΥΣΙΚΩΝ ΟΡΓΑΝΩΝ', 'ΜΟΥΣΙΚΗΣ ΤΕΧΝΟΛΟΓΙΑΣ', 0, 32);
INSERT INTO department VALUES (698, 'ΤΕΧΝΟΛΟΓΙΑΣ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΟΙΚΟΛΟΓΙΑΣ', '', 0, 32);
INSERT INTO department VALUES (701, 'ΚΛΩΣΤΟΥΦΑΝΤΟΥΡΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 40);
INSERT INTO department VALUES (705, 'ΗΛΕΚΤΡΟΝΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 36);
INSERT INTO department VALUES (710, 'Τεχνολογίας Πληροφορικής και Τηλεπικοινωνιών (Μετονομασία του Τμήματος Τηλεπληροφορικής και Διοίκηση', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 31);
INSERT INTO department VALUES (662, 'ΜΑΙΕΥΤΙΚΗΣ', '', 0, 30);
INSERT INTO department VALUES (723, 'ΤΕΧΝΟΛΟΓΙΑΣ ΠΛΗΡΟΦΟΡΙΚΗΣ & ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΕΦΑΡΜΟΓΩΝ', 0, 37);
INSERT INTO department VALUES (724, 'ΒΙΟΜΗΧΑΝΙΚΗΣ ΠΛΗΡΟΦΟΡΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 33);
INSERT INTO department VALUES (725, 'ΕΦΑΡΜΟΣΜΕΝΗΣ ΠΛΗΡΟΦΟΡΙΚΗΣ & ΠΟΛΥΜΕΣΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 35);
INSERT INTO department VALUES (727, 'ΠΛΗΡΟΦΟΡΙΚΗΣ & ΕΠΙΚΟΙΝΩΝΙΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 41);
INSERT INTO department VALUES (728, 'ΕΠΙΧΕΙΡΗΜΑΤΙΚΟΥ  ΣΧΕΔΙΑΣΜΟΥ & ΠΛΗΡΟΦΟΡΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 39);
INSERT INTO department VALUES (729, 'ΕΦΑΡΜΟΓΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΣΤΗ ΔΙΟΙΚΗΣΗ & ΣΤΗΝ ΟΙΚΟΝΟΜΙΑ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 38);
INSERT INTO department VALUES (731, 'ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΕΛΕΓΚΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 34);
INSERT INTO department VALUES (732, 'ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΗΣ ΚΑΙ ΕΛΕΓΚΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 31);
INSERT INTO department VALUES (733, 'ΕΦΑΡΜΟΓΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΣΤΗ ΔΙΟΙΚΗΣΗ ΚΑΙ ΣΤΗΝ ΟΙΚΟΝΟΜΙΑ', '', 0, 32);
INSERT INTO department VALUES (736, 'ΤΗΛΕΠΙΚΟΙΝΩΝΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ ΚΑΙ ΔΙΚΤΥΩΝ', '', 0, 38);
INSERT INTO department VALUES (737, 'ΤΕΧΝΟΛΟΓΙΑΣ ΑΕΡΟΣΚΑΦΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 42);
INSERT INTO department VALUES (738, 'ΑΝΑΚΑΙΝΙΣΗΣ ΚΑΙ ΑΠΟΚΑΤΑΣΤΑΣΗΣ ΚΤΙΡΙΩΝ', '', 0, 37);
INSERT INTO department VALUES (740, 'ΔΙΟΙΚΗΣΗΣ ΣΥΣΤΗΜΑΤΩΝ ΕΦΟΔΙΑΣΜΟΥ', '', 0, 42);
INSERT INTO department VALUES (741, 'ΔΗΜΟΣΙΩΝ ΣΧΕΣΕΩΝ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΣ', '', 0, 32);
INSERT INTO department VALUES (742, 'ΟΠΤΙΚΗΣ ΚΑΙ ΟΠΤΟΜΕΤΡΙΑΣ', '', 0, 39);
INSERT INTO department VALUES (744, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', '', 0, 33);
INSERT INTO department VALUES (745, 'ΕΜΠΟΡΙΑΣ ΚΑΙ ΔΙΑΦΗΜΙΣΗΣ', '', 0, 36);
INSERT INTO department VALUES (746, 'ΕΜΠΟΡΙΑΣ ΚΑΙ ΔΙΑΦΗΜΙΣΗΣ', '', 0, 39);
INSERT INTO department VALUES (748, 'ΠΡΟΣΤΑΣΙΑΣ ΚΑΙ ΣΥΝΤΗΡΗΣΗΣ ΠΟΛΙΤΙΣΜΙΚΗΣ ΚΛΗΡΟΝΟΜΙΑΣ', '', 0, 32);
INSERT INTO department VALUES (751, 'ΕΠΙΧΕΙΡΗΜ. ΣΧΕΔΙΑΣΜΟΥ ΚΑΙ ΠΛΗΡΟΦ. ΣΥΣΤΗΜΑΤΩΝ', '', 0, 35);
INSERT INTO department VALUES (752, 'ΟΙΝΟΛΟΓΙΑΣ & ΤΕΧΝΟΛΟΓΙΑΣ ΠΟΤΩΝ', '', 0, 33);
INSERT INTO department VALUES (7000, 'ΝΟΣΗΛΕΥΤΙΚΗΣ Β', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 29);
INSERT INTO department VALUES (9996, 'ΕΙΚΑΣΤΙΚΩΝ ΚΑΙ ΕΦΑΡΜΟΣΜΕΝΩΝ ΤΕΧΝΩΝ', '', 0, 18);
INSERT INTO department VALUES (9997, 'ΕΙΚΑΣΤΙΚΩΝ ΤΕΧΝΩΝ', '', 0, 6);
INSERT INTO department VALUES (9998, 'ΕΙΚΑΣΤΙΚΩΝ ΚΑΙ ΕΦΑΡΜΟΣΜΕΝΩΝ ΤΕΧΝΩΝ', 'ΚΑΛΩΝ ΤΕΧΝΩΝ', 0, 8);
INSERT INTO department VALUES (271, 'ΔΙΑΧΕΙΡΙΣΗΣ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΦΥΣΙΚΩΝ ΠΟΡΩΝ', 'ΔΙΑΧΕΙΡΙΣΗΣ ΦΥΣΙΚΩΝ ΠΟΡΩΝ ΚΑΙ ΕΠΙΧΕΙΡΗΣΕΩΝ', 0, 17);
INSERT INTO department VALUES (712, 'ΠΛΗΡΟΦΟΡΙΚΗΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 1);
INSERT INTO department VALUES (717, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 0, 1);
INSERT INTO department VALUES (719, 'ΔΙΑΤΡΟΦΗΣ ΚΑΙ ΔΙΑΙΤΟΛΟΓΙΑΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', 0, 1);
INSERT INTO department VALUES (720, 'ΑΥΤΟΜΑΤΙΣΜΟΥ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 1);
INSERT INTO department VALUES (721, 'ΓΕΩΤΕΧΝΟΛΟΓΙΑΣ & ΠΕΡΙΒΑΛΛΟΝΤΟΣ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 30);
INSERT INTO department VALUES (726, 'ΕΜΠΟΡΙΑΣ & ΠΟΙΟΤΙΚΟΥ ΕΛΕΓΧΟΥ ΑΓΡΟΤΙΚΩΝ ΠΡΟΙΟΝΤΩΝ', 'ΤΕΧΝΟΛΟΓΙΑΣ - ΓΕΩΠΟΝΙΑΣ', 0, 30);
INSERT INTO department VALUES (730, 'ΔΗΜΟΣΙΩΝ ΣΧΕΣΕΩΝ & ΕΠΙΚΟΙΝΩΝΙΑΣ', '', 0, 30);
INSERT INTO department VALUES (734, 'ΕΦΑΡΜΟΓΩΝ ΠΛΗΡΟΦΟΡΙΚΗΣ ΣΤΗ ΔΙΟΙΚΗΣΗ ΚΑΙ ΣΤΗΝ ΟΙΚΟΝΟΜΙΑ', '', 0, 30);
INSERT INTO department VALUES (515, 'ΤΕΧΝΟΛΟΓΙΑΣ ΤΡΟΦΙΜΩΝ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 34);
INSERT INTO department VALUES (384, 'ΘΕΩΡΙΑΣ ΚΑΙ ΙΣΤΟΡΙΑΣ ΤΗΣ ΤΕΧΝΗΣ', '', 0, 6);
INSERT INTO department VALUES (753, 'ΑΥΤΟΜΑΤΙΣΜΟΥ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 38);
INSERT INTO department VALUES (115, 'ΦΙΛΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 21);
INSERT INTO department VALUES (116, 'ΙΣΤΟΡΙΑΣ ΚΑΙ ΑΡΧΑΙΟΛΟΓΙΑΣ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 21);
INSERT INTO department VALUES (132, 'ΠΑΙΔΑΓΩΓΙΚΟ ΔΗΜΟΤΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 0, 21);
INSERT INTO department VALUES (138, 'ΦΙΛΟΣΟΦΙΚΩΝ ΚΑΙ ΚΟΙΝΩΝΙΚΩΝ ΣΠΟΥΔΩΝ', 'ΦΙΛΟΣΟΦΙΚΗ', 0, 21);
INSERT INTO department VALUES (149, 'ΚΟΙΝΩΝΙΟΛΟΓΙΑΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (151, 'ΨΥΧΟΛΟΓΙΑΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (158, 'ΠΑΙΔΑΓΩΓΙΚΟ ΠΡΟΣΧΟΛΙΚΗΣ ΕΚΠΑΙΔΕΥΣΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΑΓΩΓΗΣ', 0, 21);
INSERT INTO department VALUES (200, 'ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΥΛΙΚΩΝ', 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (216, 'ΕΠΙΣΤΗΜΗΣ ΥΠΟΛΟΓΙΣΤΩΝ', 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (248, 'ΕΦΑΡΜΟΣΜΕΝΩΝ ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (251, 'ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (261, 'ΦΥΣΙΚΗΣ', 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (270, 'ΧΗΜΕΙΑΣ', 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (282, 'ΒΙΟΛΟΓΙΑΣ', 'ΘΕΤΙΚΩΝ & ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (321, 'ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (351, 'ΠΟΛΙΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ', 'ΚΟΙΝΩΝΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 21);
INSERT INTO department VALUES (162, 'ΕΠΙΣΤΗΜΩΝ ΠΡΟΣΧΟΛΙΚΗΣ ΑΓΩΓΗΣ ΚΑΙ ΕΚΠΑΙΔΕΥΤΙΚΟΥ ΣΧΕΔΙΑΣΜΟΥ', 'ΑΝΘΡΩΠΙΣΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (218, 'ΣΤΑΤΙΣΤΙΚΗΣ ΚΑΙ ΑΝΑΛΟΓΙΣΤΙΚΩΝ ΚΑΙ ΧΡΗΜΑΤΟΟΙΚΟΝΟΜΙΚΩΝ ΜΑΘΗΜΑΤΙΚΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (222, 'ΜΗΧΑΝΙΚΩΝ ΟΙΚΟΝΟΜΙΑΣ ΚΑΙ ΔΙΟΙΚΗΣΗΣ', 'ΕΠΙΣΤΗΜΩΝ ΔΙΟΙΚΗΣΗΣ', 0, 16);
INSERT INTO department VALUES (344, 'ΜΗΧΑΝΙΚΩΝ ΠΛΗΡΟΦΟΡΙΑΚΩΝ ΚΑΙ ΕΠΙΚΟΙΝΩΝΙΑΚΩΝ ΣΥΣΤΗΜΑΤΩΝ', 'ΘΕΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ', 0, 16);
INSERT INTO department VALUES (346, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ ΑΓΡΟΤΙΚΩΝ ΠΡΟΙΟΝΤΩΝ ΚΑΙ ΤΡΟΦΙΜΩΝ', 'ΔΙΑΧΕΙΡΙΣΗΣ ΦΥΣΙΚΩΝ ΠΟΡΩΝ ΚΑΙ ΕΠΙΧΕΙΡΗΣΕΩΝ', 0, 17);
INSERT INTO department VALUES (368, 'ΔΙΑΧΕΙΡΙΣΗΣ ΠΟΛΙΤΙΣΜΙΚΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ ΚΑΙ ΝΕΩΝ ΤΕΧΝΟΛΟΓΙΩΝ', 'ΔΙΑΧΕΙΡΙΣΗΣ ΦΥΣΙΚΩΝ ΠΟΡΩΝ ΚΑΙ ΕΠΙΧΕΙΡΗΣΕΩΝ', 0, 17);
INSERT INTO department VALUES (516, 'ΟΧΗΜΑΤΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 0, 1);
INSERT INTO department VALUES (531, 'ΦΥΤΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 1);
INSERT INTO department VALUES (539, 'ΖΩΙΚΗΣ ΠΑΡΑΓΩΓΗΣ', 'ΤΕΧΝΟΛΟΓΙΑΣ ΓΕΩΠΟΝΙΑΣ', 0, 1);
INSERT INTO department VALUES (561, 'ΛΟΓΙΣΤΙΚΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 1);
INSERT INTO department VALUES (583, 'ΕΜΠΟΡΙΑΣ & ΔΙΑΦΗΜΙΣΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 1);
INSERT INTO department VALUES (601, 'ΒΙΒΛΙΟΘΗΚΟΝΟΜΙΑΣ ΚΑΙ ΣΥΣΤΗΜΑΤΩΝ ΠΛΗΡΟΦΟΡΗΣΗΣ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 1);
INSERT INTO department VALUES (605, 'ΤΟΥΡΙΣΤΙΚΩΝ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ & ΟΙΚΟΝΟΜΙΑΣ', 0, 1);
INSERT INTO department VALUES (617, 'ΦΥΣΙΚΟΘΕΡΑΠΕΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 1);
INSERT INTO department VALUES (623, 'ΙΑΤΡΙΚΩΝ ΕΡΓΑΣΤΗΡΙΩΝ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 1);
INSERT INTO department VALUES (639, 'ΑΙΣΘΗΤΙΚΗΣ ΚΑΙ ΚΟΣΜΗΤΟΛΟΓΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 1);
INSERT INTO department VALUES (647, 'ΝΟΣΗΛΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 1);
INSERT INTO department VALUES (657, 'ΜΑΙΕΥΤΙΚΗΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 1);
INSERT INTO department VALUES (694, 'ΒΡΕΦΟΝΗΠΙΟΚΟΜΙΑΣ', 'ΕΠΑΓΓΕΛΜΑΤΩΝ ΥΓΕΙΑΣ & ΠΡΟΝΟΙΑΣ', 0, 1);
INSERT INTO department VALUES (703, 'ΣΧΕΔΙΑΣΜΟΥ & ΠΑΡΑΓΩΓΗΣ ΕΝΔΥΜΑΤΩΝ', '', 0, 1);
INSERT INTO department VALUES (530, 'ΕΠΙΧΕΙΡΗΣΙΑΚΗΣ ΠΛΗΡΟΦΟΡΙΚΗΣ', '', 0, 30);
INSERT INTO department VALUES (596, 'ΔΙΟΙΚΗΣΗΣ ΕΠΙΧΕΙΡΗΣΕΩΝ', 'ΔΙΟΙΚΗΣΗΣ ΟΙΚΟΝΟΜΙΑΣ', 0, 30);
INSERT INTO department VALUES (735, 'ΠΛΗΡΟΦΟΡΙΚΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ ΥΠΟΛΟΓΙΣΤΩΝ', '', 0, 30);
INSERT INTO department VALUES (749, 'ΔΙΟΙΚΗΣΗΣ ΣΥΣΤΗΜΑΤΩΝ ΕΦΟΔΙΑΣΜΟΥ', '', 0, 30);
INSERT INTO department VALUES (372, 'ΕΠΙΣΤΗΜΗΣ ΤΡΟΦΙΜΩΝ ΚΑΙ ΔΙΑΤΡΟΦΗΣ', '', 0, 16);
INSERT INTO department VALUES (549, 'ΤΥΠΟΠΟΙΗΣΗΣ ΚΑΙ ΔΙΑΚΙΝΗΣΗΣ ΠΡΟΙΟΝΤΩΝ', '', 1, 1);
INSERT INTO department VALUES (547, 'ΤΕΧΝΟΛ. ΑΛΙΕΙΑΣ & ΥΔΑΤΟΚΑΛΛΙΕΡΓΕΙΩΝ', '', 1, 1);
INSERT INTO department VALUES (290, 'ΜΟΡΙΑΚΗΣ ΒΙΟΛΟΓΙΑΣ ΚΑΙ ΓΕΝΕΤΙΚΗΣ', '', 0, 11);
INSERT INTO department VALUES (342, 'ΑΡΧΕΙΟΝΟΜΙΑΣ ΚΑΙ ΒΙΒΛΙΟΘΗΚΟΝΟΜΙΑΣ', '', 0, 14);
INSERT INTO department VALUES (240, 'ΔΙΟΙΚΗΤΙΚΗΣ ΕΠΙΣΤΗΜΗΣ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑΣ', '', 0, 15);


--
-- TOC entry 2031 (class 0 OID 164391)
-- Dependencies: 1583
-- Data for Name: subject; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2023 (class 0 OID 164322)
-- Dependencies: 1575 2018 2014 2018 2018 2031 2018 2018
-- Data for Name: position; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2007 (class 0 OID 164220)
-- Dependencies: 1559 2023 2009
-- Data for Name: candidacy; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2008 (class 0 OID 164225)
-- Dependencies: 1560 2017 2007
-- Data for Name: candidacy_filebody; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2012 (class 0 OID 164245)
-- Dependencies: 1564 2018 2009
-- Data for Name: candidate_degrees; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2013 (class 0 OID 164252)
-- Dependencies: 1565 2018 2009
-- Data for Name: candidate_publications; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2010 (class 0 OID 164235)
-- Dependencies: 1562 2007
-- Data for Name: candidatecommittee; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2024 (class 0 OID 164330)
-- Dependencies: 1576 2030 2018
-- Data for Name: professor; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2011 (class 0 OID 164240)
-- Dependencies: 1563 2010 2018 2024
-- Data for Name: candidatecommitteemembership; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2021 (class 0 OID 164312)
-- Dependencies: 1573 2030 2019
-- Data for Name: institutionmanager; Type: TABLE DATA; Schema: public; Owner: apella
--

INSERT INTO institutionmanager VALUES (109, 1);


--
-- TOC entry 2033 (class 0 OID 167526)
-- Dependencies: 1586 2014 2030 2021
-- Data for Name: departmentassistant; Type: TABLE DATA; Schema: public; Owner: apella
--


--
-- TOC entry 2015 (class 0 OID 164272)
-- Dependencies: 1567 2023
-- Data for Name: electoralbody; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2016 (class 0 OID 164277)
-- Dependencies: 1568 2018 2024 2015
-- Data for Name: electoralbodymembership; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2020 (class 0 OID 164307)
-- Dependencies: 1572 2030 2019 2021
-- Data for Name: institutionassistant; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2022 (class 0 OID 164317)
-- Dependencies: 1574 2030
-- Data for Name: ministrymanager; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2027 (class 0 OID 164345)
-- Dependencies: 1579
-- Data for Name: rank; Type: TABLE DATA; Schema: public; Owner: apella
--

INSERT INTO rank VALUES (1, 'Καθηγητής', 'Αναπληρωτής', 0);
INSERT INTO rank VALUES (2, 'Καθηγητής', 'Καθηγητής', 0);
INSERT INTO rank VALUES (3, 'Ερευνητής', 'Διευθυντής Ερευνών', 0);
INSERT INTO rank VALUES (4, 'Ερευνητής', 'Κύριος Ερευνητής', 0);


--
-- TOC entry 2025 (class 0 OID 164335)
-- Dependencies: 1577 2014 2024 2031 2018 2027 2031 2019
-- Data for Name: professordomestic; Type: TABLE DATA; Schema: public; Owner: apella
--


--
-- TOC entry 2026 (class 0 OID 164340)
-- Dependencies: 1578 2024 2031 2027
-- Data for Name: professorforeign; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2028 (class 0 OID 164353)
-- Dependencies: 1580 2018 2023
-- Data for Name: recommendatorycommittee; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2029 (class 0 OID 164358)
-- Dependencies: 1581 2024 2028
-- Data for Name: recommendatorycommitteemembership; Type: TABLE DATA; Schema: public; Owner: apella
--



--
-- TOC entry 2034 (class 0 OID 167666)
-- Dependencies: 1587 2014 2018
-- Data for Name: register; Type: TABLE DATA; Schema: public; Owner: apella
--

--
-- TOC entry 2035 (class 0 OID 167688)
-- Dependencies: 1588 2014 2018
-- Data for Name: registermitroo; Type: TABLE DATA; Schema: public; Owner: apella
--



-- Completed on 2012-08-22 15:10:20

--
-- PostgreSQL database dump complete
--

