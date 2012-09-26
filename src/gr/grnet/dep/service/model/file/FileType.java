package gr.grnet.dep.service.model.file;

public enum FileType {
	BIOGRAFIKO,
	TAYTOTHTA,
	BEBAIWSH_STRATIOTIKIS_THITIAS,
	FORMA_SYMMETOXIS,
	DIMOSIEYSI,
	PTYXIO,
	YPOPSIFIOTITA,
	ANAFORA,
	EISIGITIKI_ANAFORA,
	FEK,
	PROFILE,
	MITROO,
	PRAKTIKO_SYNEDRIASIS, // Πρακτικό της συνεδρίασης της επιτροπής για την ανάδειξη των αξιολογητών,
	TEKMIRIOSI_AKSIOLOGITI, // Τεκμηρίωση σε περίπτωση που οι αξιολογητές δεν ανήκουν στο εξωτερικό μητρώο,
	AITIMA_EPITROPIS, // Αίτημα της επιτροπής προς τους αξιολογητές (στο παραπάνω πεδίο;)
	AKSIOLOGISI, // Γραπτή αξιολόγηση κάθε αξιολογητή για κάθε υποψήφιο (2 πεδία, ένα για κάθε αξιολογητή που θα έχει πολλαπλά pdf, ένα για κάθε υποψήφιο)
	EISIGISI, // Εισηγήσεις καθηγητών/ερευνητών που έχουν προτείνει οι υποψήφιοι (πολλαπλά pdf).
	PROSKLISI_KOSMITORA, // Πρόσκληση του κοσμήτορα για τη σύγκληση της επιτροπής για την επιλογή.
	PRAKTIKO_EPILOGIS, // Πρακτικό επιλογής
	DIAVIVASTIKO_PRAKTIKOU, // Διαβιβαστικό του πρακτικού από τον Κοσμήτορα στον Πρύτανη.
	PRAKSI_DIORISMOU, // Πράξη διορισμού 
	APOFASI_ANAPOMPIS, // Απόφαση αναπομπής του φακέλου επιλογής.
	DIOIKITIKO_EGGRAFO, // Διοικητικά έγγραφα
	ORGANISMOS, // Οργανισμό 
	ESWTERIKOS_KANONISMOS, //Εσωτερικό Κανονισμό του Ιδρύματος.
}
