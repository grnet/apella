-- You can use this file to load seed data into the database using SQL statements
--insert into Users (id, username, firstname, lastname, email, phone_number) values (0, 'anglen', 'Aggelos', 'Lenis', 'john.smith@mailinator.com', '2125551212')
INSERT INTO Institution (ID, version, Name) VALUES (1, 0, 'ΑΛΕΞΑΝΔΡΕΙΟ ΤΕΙ ΘΕΣΣΑΛΟΝΙΚΗΣ');
INSERT INTO Department (id, version, department, school, institution_id) VALUES (1, 0, 'ΟΧΗΜΑΤΩΝ', 'ΤΕΧΝΟΛΟΓΙΚΩΝ ΕΦΑΡΜΟΓΩΝ', 1);
INSERT INTO Rank (ID, version, Name) VALUES (1, 0, 'ΚΑΘΗΓΗΤΑΡΑ');
INSERT INTO Subject (ID, version, Name) VALUES (1, 0, 'World of Warcraft');
