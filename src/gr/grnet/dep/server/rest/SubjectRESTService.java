package gr.grnet.dep.server.rest;

import gr.grnet.dep.server.rest.exceptions.RestException;
import gr.grnet.dep.service.model.Subject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.Status;

@Path("/subject")
@Stateless
public class SubjectRESTService extends RESTService {

	@Inject
	private Logger log;

	private static final Collection<Subject> ggetSubjects = new ArrayList<Subject>();
	static {
		ggetSubjects.add(new Subject("ΛΟΓΙΚΗ", "ΓΕΝΙΚΗ ΛΟΓΙΚΗ"));
		ggetSubjects.add(new Subject("ΛΟΓΙΚΗ", "ΑΛΓΕΒΡΑ ΤΟΥ BOOLE"));
		ggetSubjects.add(new Subject("ΛΟΓΙΚΗ", "MΑΘΗΜΑΤΙΚΗ ΛΟΓΙΚΗ"));
		ggetSubjects.add(new Subject("ΛΟΓΙΚΗ", "ΕΠΑΓΩΓΙΚΗ ΛΟΓΙΚΗ"));
		ggetSubjects.add(new Subject("ΛΟΓΙΚΗ", "ΑΛΛO"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΑΛΓΕΒΡΑ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΑΝΑΛΥΣΗ KAI ΣΥΝΑΡΤΗΣΙΑΚΗ ΑΝΑΛΥΣΗ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΓΕΩΜΕΤΡΙΑ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΘΕΩΡΙΑ ΑΡΙΘΜΩΝ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "AΡΙΘΜΗΤΙΚΗ ΑΝΑΛΥΣΗ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΕΠΙΧΕΙΡΗΣΙΑΚΗ ΕΡΕΥΝΑ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΠΙΘΑΝΟΤΗΤΕΣ KAI ΣΤΑΤΙΣΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΤΟΠΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΜΑΘΗΜΑΤΙΚΑ", "ΑΛΛΕΣ ΜΑΘΗΜΑΤΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΠΛΗΡΟΦΟΡΙΚΗ", "ΛΟΓΙΣΜΙΚΟ"));
		ggetSubjects.add(new Subject("ΠΛΗΡΟΦΟΡΙΚΗ", "ΥΛΙΚΟ"));
		ggetSubjects.add(new Subject("ΠΛΗΡΟΦΟΡΙΚΗ", "ΣΥΣΤΗΜΑΤΑ"));
		ggetSubjects.add(new Subject("ΠΛΗΡΟΦΟΡΙΚΗ", "ΤΕΧΝΗΤΗ ΝΟΗΜΟΣΥΝΗ"));
		ggetSubjects.add(new Subject("ΠΛΗΡΟΦΟΡΙΚΗ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΠΛΗΡΟΦΟΡΙΚΗΣ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΚΟΣΜΟΛΟΓΙΑ ΚΑΙ ΚΟΣΜΟΓΕΝΕΣΗ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "MEΣΟΠΛΑΝΗΤΙΚΟΣ ΧΩΡΟΣ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΟΠΤΙΚΗ ΑΣΤΡΟΝΟΜΙΑ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΠΛΑΝΗΤΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΡΑΔΙΟ ΑΣΤΡΟΝΟΜΙΑ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΗΛΙΑΚΟ ΣΥΣΤΗΜΑ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΑΣΤΡΟΜΕΤΡΙΑ, ΟΥΡΑΝΙΑ ΜΗΧΑΝΙΚΗ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΑΣΤΡΟΝΟΜΙΑ ΠΕΡΑΝ ΤΟΥ ΓΑΛΑΞΙΑ ΜΑΣ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΓΑΛΑΞΙΑΚΗ ΔΥΝΑΜΙΚΗ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΣΧΗΜΑΤΙΣΜΟΣ ΚΑΙ ΕΞΕΛΙΞΗ ΑΣΤΕΡΩΝ"));
		ggetSubjects.add(new Subject("ΑΣΤΡΟΝΟΜΙΑ ΚΑΙ ΑΣΤΡΟΦΥΣΙΚΗ", "ΑΛΛΕΣ ΑΣΤΡΟΝΟΜΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "AΚΟΥΣΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "HΛΕΚΤΡΟΜΑΓΝΗΤΙΣΜΟΣ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΗΛΕΚΤΡΟΝΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΦΥΣΙΚΗ ΤΩΝ  ΡΕΥΣΤΩΝ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΜΗΧΑΝΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΜΟΡΙΑΚΗ ΦΥΣΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΠΥΡΗΝΙΚΗ ΦΥΣΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΠΥΡΗΝΙΚΗ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΟΠΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΦΥΣΙΚΟΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΦΥΣΙΚΗ ΣΤΕΡΕΑΣ ΚΑΤΑΣΤΑΣΗΣ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΘΕΩΡΗΤΙΚΗ ΦΥΣΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΘΕΡΜΟΔΥΝΑΜΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "METΡΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΦΥΣΙΚΗ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΦΥΣΙΚΗΣ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΑΝΑΛΥΤΙΚΗ ΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΑΝΟΡΓΑΝΗ ΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΒΙΟΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΧΗΜΕΙΑ ΜΑΚΡΟΜΟΡΙΩΝ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΟΡΓΑΝΙΚΗ ΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΠΥΡΗΝΙΚΗ ΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΚΛΙΝΙΚΗ ΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "XHMEIA ΠΕΡΙΒΑΛΛΟΝΤΟΣ"));
		ggetSubjects.add(new Subject("ΧΗΜΕΙΑ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΧΗΜΕΙΑΣ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΦΥΣΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΒΙΟΜΑΘΗΜΑΤΙΚΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΒΙΟΕΠΙΣΤΗΜΕΣ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΒΙΟΛΟΓΙΑ ΚΥΤΤΑΡΩΝ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΓΕΝΕΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΦΥΣΙΟΛΟΓΙΑ ΤΟΥ ΑΝΘΡΩΠΟΥ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΑΝΟΣΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΜΙΚΡΟΒΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΜΟΡΙΑΚΗ ΒΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΡΑΔΙΟΒΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΘΑΛΑΣΣΙΑ ΒΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΒΙΟΛΟΓΙΑ ΦΥΤΩΝ (ΒΟΤΑΝΙΚΗ)"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΒΙΟΛΟΓΙΑ ΤΩΝ ΖΩΩΝ (ΖΩΟΛΟΓΙΑ)"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΕΝΤΟΜΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΑΘΛΗΤΙΚΗ ΕΠΙΣΤΗΜΗ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ THΣ ΖΩΗΣ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΕΠΙΣΤΗΜΩΝ ΖΩΗΣ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΑΤΜΟΣΦΑΙΡΑΣ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΚΛΙΜΑΤΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΓΕΩΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΓΕΩΔΑΙΣΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΓΕΩΓΡΑΦΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΓΕΝΙΚΗ ΓΕΩΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΠΑΛΑΙΟΝΤΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΟΡΥΚΤΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΤΕΚΤΟΝΙΚΗ ΓΕΩΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΓΕΩΘΕΡΜΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΓΕΩΦΥΣΙΚΗ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΥΔΡΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΜΕΤΕΩΡΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΘΑΛΑΣΣΙΑ ΓΕΩΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΩΚΕΑΝΟΓΡΑΦΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΟΙΚΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜΕΣ ΤΗΣ ΓΗΣ ΚΑΙ TOY ΔΙΑΣΤΗΜΑΤΟΣ", "ΑΛΛΕΣ ΕΠΙΣΤΗΜΕΣ ΤΗΣ  ΓΗΣ ΚΑΙ ΤΟΥ ΔΙΑΣΤΗΜΑΤΟΣ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΕΔΑΦΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΓΕΩΡΓΙΚΗ ΧΗΜΕΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΓΕΩΡΓΙΚΗ ΜΗΧΑΝΙΚΗ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΦΥΤΙΚΗ ΠΑΡΑΓΩΓΗ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΦΥΤΟΠΑΘΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "KTHNIATΡΙΚΗ ΕΠΙΣΤΗΜΗ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΖΩΟΤΕΧΝΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΙΧΘΥΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΑΓΡΟΤΙΚΗ ΟΙΚΟΝΟΜΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΑΣΟΠΟΝΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΙΑΧΕΙΡΙΣΗ ΟΡΕΙΝΩΝ ΥΔΑΤΩΝ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΛΙΒΑΔΟΠΟΝΙΑ"));
		ggetSubjects.add(new Subject("ΓΕΩΠΟΝΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΑΛΛΕΣ ΓΕΩΠΟΝΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΚΛΙΝΙΚΗ ΙΑΤΡΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΕΠΙΔΗΜΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΕΡΓΑΣΙΑΚΗ ΙΑΤΡΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΠΥΡΗΝΙΚΗ ΙΑΤΡΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΕΣΩΤΕΡΙΚΗ (ΜΗ ΧΕΙΡΟΥΡΓΙΚΗ) ΙΑΤΡΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΙΑΙΤΗΤΙΚΗ (ΔΙΑΙΤΟΛΟΓΙΑ)"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΠΑΘΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΥΝΑΜΙΚΗ ΤΩΝ ΦΑΡΜΑΚΩΝ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΦΑΡΜAΚΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΠΡΟΛΗΠΤΙΚΗ ΙΑΤΡΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΨΥΧΙΑΤΡΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΗΜΟΣΙΑ ΥΓΕΙΑ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΧΕΙΡΟΥΡΓΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΤΟΞΙΚΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΟΔΟΝΤΙΑΤΡΙΚΗ"));
		ggetSubjects.add(new Subject("ΙΑΤΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΑΛΛΕΣ ΙΑΤΡΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΑΕΡΟΝΑΥΤΙΚΗ ΜΗΧΑΝΟΛΟΓΙΑ KAI ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΒΙΟΧΗΜΙΚΗ ΤΕΧΝΟΛΟΓΙΑ-ΒΙΟΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΧΗΜΙΚΗ ΜΗΧΑΝΙΚΗ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ KΑΥΣΙΜΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΚΑΤΑΣΚΕΥΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΗΛΕΚΤΡΙΚΗ ΤΕΧΝΟΛΟΓΙΑ ΚΑΙ  ΜΗΧΑΝΙΚΗ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΗΛΕΚΤΡΟΝΙΚΗ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "TEΧΝΟΛΟΓΙΑ ΤΗΛΕΠΙΚΟΙΝΩΝΙΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΚΑΙ ΜΗΧΑΝΙΚΗ  ΠΕΡΙΒΑΛΛΟΝΤΟΣ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΤΡΟΦΙΜΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΒΙΟΜΗΧΑΝΙΚΗ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "TΕΧΝΟΛΟΓΙΑ ΟΡΓΑΝΩΝ ΜΕΤΡΗΣHΣ ΚΑΙ ΕΛΕΓΧΟΥ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΥΛΙΚΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΜΗΧΑΝΟΛΟΓΙΚΗ ΜΗΧΑΝΙΚΗ ΚΑΙ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΙΑΤΡΙΚΗ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΜΕΤΑΛΛΟΥΡΓΙΑΣ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΜΕΤΑΛΛΙΚΩΝ ΠΡΟΙΟΝΤΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΟΡΥΧΕΙΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΟΧΗΜΑΤΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΝΑΥΤΙΚΗ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΣΙΔΗΡΟΔΡΟΜΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΜΕΤΑΦΟΡΙΚΩΝ ΜΕΣΩΝ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΚΛΩΣΤΟΥΦΑΝΤΟΥΡΓΙΑΣ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΤΕΧΝΟΛΟΓΙΑ ΔΙΑΣΤΗΜΑΤΟΣ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΕΝΕΡΓΕΙΑΚΗ ΤΕΧΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΧΩΡΟΤΑΞΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΠΟΛΕΟΔΟΜΙΑ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΑΡΧΙΤΕΚΤΟΝΙΚΗ"));
		ggetSubjects.add(new Subject("ΤΕΧΝΟΛΟΓΙΑ", "ΑΛΛΕΣ ΤΕΧΝΟΛΟΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)", "ΠΟΛΙΤΙΣΤΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)", "ΚΟΙΝΩΝΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)", "ΕΘΝΟΓΡΑΦΙΑ ΚΑΙ ΕΘΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΑΝΘΡΩΠΟΛΟΓΙΑ (ΜΗ ΦΥΣΙΚΗ)", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΑΝΘΡΩΠΟΛΟΓΙΑΣ"));
		ggetSubjects.add(new Subject("ΔΗΜΟΓΡΑΦΙΑ", "ΘΕΩΡΙΑ ΚΑΙ ΜΕΘΟΔΟΛΟΓΙΑ ΕΡΕΥΝΑΣ ΚΑΙ ΑΝΑΛΥΣΗΣ"));
		ggetSubjects.add(new Subject("ΔΗΜΟΓΡΑΦΙΑ", "ΘΝΗΣΙΜΟΤΗΤΑ ΚΑΙ ΓΟΝΙΜΟΤΗΤΑ"));
		ggetSubjects.add(new Subject("ΔΗΜΟΓΡΑΦΙΑ", "ΙΣΤΟΡΙΚΗ ΔΗΜΟΓΡΑΦΙΑ"));
		ggetSubjects.add(new Subject("ΔΗΜΟΓΡΑΦΙΑ", "ΜΕΓΕΘΟΣ ΠΛΗΘΥΣΜΟΥ ΚΑΙ ΔΗΜΟΓΡΑΦΙΚΗ ΕΞΕΛΙΞΗ"));
		ggetSubjects.add(new Subject("ΔΗΜΟΓΡΑΦΙΑ", "ΓΕΩΓΡΑΦΙΚΗ ΔΗΜΟΓΡΑΦΙΑ"));
		ggetSubjects.add(new Subject("ΔΗΜΟΓΡΑΦΙΑ", "XAΡΑΚΤΗΡΙΣΤΙΚΑ ΠΛΗΘΥΣΜΟΥ"));
		ggetSubjects.add(new Subject("ΔΗΜΟΓΡΑΦΙΑ", "ΑΛΛΕΣ ΔΗΜΟΓΡΑΦΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΗΜΟΣΙΟΝΟΜΙΚΑ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΟΙΚΟΝΟΜΕΤΡΙΑ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΟΙΚΟΝΟΜΙΚΗ ΔΡΑΣΤΗΡΙΟΤΗΤΑ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΟΙΚΟΝΟΜΙΚΑ ΤΕΧΝΟΛΟΓΙΚΩΝ ΑΛΛΑΓΩΝ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΟΙΚΟΝΟΜΙΚΗ ΘΕΩΡΙΑ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΓΕΝΙΚΗ ΟΙΚΟΝΟΜΙΑ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΒΙΟΜΗΧΑΝΙΚΗ ΟΡΓΑΝΩΣΗ ΚΑΙ ΔΗΜΟΣΙΑ ΠΟΛΙΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΙΕΘΝΗΣ ΟΙΚΟΝΟΜΙΑ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΟΡΓΑΝΩΣΗ ΚΑΙ ΔΙΟΙΚΗΣΗ ΕΠΙΧΕΙΡΗΣΕΩΝ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΟΙΚΟΝΟΜΙΑ ΤΟΥ ΠΕΡΙΒΑΛΛΟΝΤΟΣ"));
		ggetSubjects.add(new Subject("ΟΙΚΟΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΟΙΚΟΝΟΜΙΚΩΝ ΕΠΙΣΤΗΜΩΝ"));
		ggetSubjects.add(new Subject("ΙΣΤΟΡΙΑ", "ΓΕΝΙΚΗ ΙΣΤΟΡΙΑ, ΘΕΩΡΙΑ"));
		ggetSubjects.add(new Subject("ΙΣΤΟΡΙΑ", "ΙΣΤΟΡΙΑ ΤΩΝ ΧΩΡΩΝ"));
		ggetSubjects.add(new Subject("ΙΣΤΟΡΙΑ", "ΕΙΔΙΚΕΣ ΙΣΤΟΡΙΕΣ"));
		ggetSubjects.add(new Subject("ΙΣΤΟΡΙΑ", "ΑΡΧΑΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΙΣΤΟΡΙΑ", "ΑΛΛΕΣ ΙΣΤΟΡΙΚΕΣ ΕΠΙΣΤΗΜΕΣ"));
		ggetSubjects.add(new Subject("ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΓΕΝΙΚΗ ΘΕΩΡΙΑ ΚΑΙ ΜΕΘΟΔΟΙ"));
		ggetSubjects.add(new Subject("ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΕΘΝΙΚΟ ΔΙΚΑΙΟ"));
		ggetSubjects.add(new Subject("ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΙΚΟΝΟΜΙΑ"));
		ggetSubjects.add(new Subject("ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΔΙΕΘΝΕΣ ΔΙΚΑΙΟ"));
		ggetSubjects.add(new Subject("ΝΟΜΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "AΛΛΕΣ ΝΟΜΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΕΦΑΡΜΟΣΜΕΝΗ ΓΛΩΣΣΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΔΙΑΧΡΟΝΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΘΕΩΡΗΤΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΓΕΩΓΡΑΦΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΣΥΓΧΡΟΝΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΛΕΞΙΚΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΚΟΙΝΩΝΙΚΗ ΓΛΩΣΣΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΑΡΧΑΙΕΣ ΓΛΩΣΣΕΣ"));
		ggetSubjects.add(new Subject("ΓΛΩΣΣΟΛΟΓΙΑ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΓΛΩΣΣΟΛΟΓΙΑΣ"));
		ggetSubjects.add(new Subject("ΠΑΙΔΑΓΩΓΙΚΗ", "ΘΕΩΡΙΑ ΚΑΙ ΜΕΘΟΔΟΙ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ"));
		ggetSubjects.add(new Subject("ΠΑΙΔΑΓΩΓΙΚΗ", "ΟΡΓΑΝΩΣΗ ΚΑΙ ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΤΗΣ ΕΚΠΑΙΔΕΥΣΗΣ"));
		ggetSubjects.add(new Subject("ΠΑΙΔΑΓΩΓΙΚΗ", "ΑΛΛΕΣ ΠΑΙΔΑΓΩΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΠΟΛΙΤΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΠΟΛΙΤΙΚΗ ΕΠΙΣΤΗΜΗ, ΓΕΝΙΚΑ"));
		ggetSubjects.add(new Subject("ΠΟΛΙΤΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΠΟΛΙΤIΚH EΠΙΣΤΗΜΩΝ"));
		ggetSubjects.add(new Subject("ΠΟΛΙΤΙΚΕΣ ΕΠΙΣΤΗΜΕΣ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΩΝ ΠΟΛΙΤΙΚΩΝ ΕΠΙΣΤΗΜΩΝ"));
		ggetSubjects.add(new Subject("ΨΥΧΟΛΟΓΙΑ", "ΓΕΝΙΚΗ ΨΥΧΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΨΥΧΟΛΟΓΙΑ", "ΨΥΧΟΛΟΓΙΑ ΕΦΗΒΟΥ ΚΑΙ ΠΑΙΔΙΟΥ"));
		ggetSubjects.add(new Subject("ΨΥΧΟΛΟΓΙΑ", "ΚΛΙΝΙΚΗ ΨΥΧΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΨΥΧΟΛΟΓΙΑ", "ΠΕΙΡΑΜΑΤΙΚΗ ΨΥΧΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΨΥΧΟΛΟΓΙΑ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΨΥΧΟΛΟΓΙΑΣ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "ΦΙΛΟΛΟΓΙΑ : ΘΕΩΡΙΑ, ΑΝΑΛΥΣΗ ΚΑΙ ΚΡΙΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "ΣΥΝΤΗΡΗΣΗ ΚΑΙ ΑΝΑΣΤΗΛΩΣΗ ΕΡΓΩΝ ΤΕΧΝΗΣ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "KAΛΕΣ ΤΕΧΝΕΣ : ΘΕΩΡΙΑ, ΑΝΑΛΥΣΗ ΚΑΙ ΚΡΙΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "ΚΙΝΗΜΑΤΟΓΡΑΦΙΑ, VIDEO"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "BYZANTINH ΦΙΛΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "ΙΣΤΟΡΙΑ ΤΟΥ ΘΕΑΤΡΟΥ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "ΝΕΟΕΛΛΗΝΙΚΗ ΦΙΛΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "ΘΕΑΤΡΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΕΠΙΣΤΗΜH ΤΕΧΝΩΝ ΚΑΙ ΓΡΑΜΜΑΤΩΝ", "AΛΛΕΣ ΚΑΛΛΙΤΕΧΝΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΚΟΙΝΩΝΙΟΛΟΓΙΑ", "ΓΕΝΙΚH KOINΩΝΙΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΚΟΙΝΩΝΙΟΛΟΓΙΑ", "ΚΟΙΝΩΝΙΚΗ ΑΛΛΑΓΗ ΚΑΙ ΑΝΑΠΤΥΞΗ"));
		ggetSubjects.add(new Subject("ΚΟΙΝΩΝΙΟΛΟΓΙΑ", "ΑΛΛΕΣ ΚΟΙΝΩΝΙΟΛΟΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
		ggetSubjects.add(new Subject("ΗΘΙΚΗ", "ΚΛΑΣΣΙΚΗ ΗΘΙΚΗ"));
		ggetSubjects.add(new Subject("ΗΘΙΚΗ", "ΗΘΙΚΗ ΤΩΝ ΑΤΟΜΩΝ"));
		ggetSubjects.add(new Subject("ΗΘΙΚΗ", "ΗΘΙΚΗ ΤΩΝ ΚΟΙΝΩΝΙΚΩΝ ΟΜΑΔΩΝ"));
		ggetSubjects.add(new Subject("ΗΘΙΚΗ", "ΗΘΙΚΗ ΤΩΝ ΕΠΙΣΤΗΜΩΝ"));
		ggetSubjects.add(new Subject("ΗΘΙΚΗ", "ΗΘΙΚΗ ΤΗΣ ΙΑΤΡΙΚΗΣ"));
		ggetSubjects.add(new Subject("ΗΘΙΚΗ", "AΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΣΧΕΤΙΚΕΣ MΕ ΤΗΝ ΗΘΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΓΝΩΣΗΣ ΚΑΙ ΕΠΙΣΤΗΜΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΚΗ ΑΝΘΡΩΠΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΓΕΝΙΚΗ ΦΙΛΟΣΟΦΙΑ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΚΑ ΣΥΣΤΗΜΑΤΑ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΕΠΙΣΤΗΜΗΣ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΦΥΣΗΣ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΑ ΤΗΣ ΚΟΙΝΩΝΙΑΣ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΚΑ ΣΥΣΤΗΜΑΤΑ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΙΣΤΟΡΙΑ ΤΗΣ ΦΙΛΟΣΟΦΙΑΣ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΑΙΝΟΜΕΝΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΦΙΛΟΣΟΦΙΚΗ ΛΟΓΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΑΙΣΘΗΤΙΚΗ"));
		ggetSubjects.add(new Subject("ΦΙΛΟΣΟΦΙΑ", "ΑΛΛΕΣ ΕΙΔΙΚΟΤΗΤΕΣ ΤΗΣ ΦΙΛΟΣΟΦΙΑΣ"));
		ggetSubjects.add(new Subject("ΘΕΟΛΟΓΙΑ", "ΓΕΝΙΚΗ ΘΕΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΘΕΟΛΟΓΙΑ", "ΣΥΣΤΗΜΑΤΙΚΗ ΘΕΟΛΟΓΙΑ"));
		ggetSubjects.add(new Subject("ΘΕΟΛΟΓΙΑ", "ΒΙΒΛΟΣ"));
		ggetSubjects.add(new Subject("ΘΕΟΛΟΓΙΑ", "ΑΛΛΕΣ ΘΕΟΛΟΓΙΚΕΣ ΕΙΔΙΚΟΤΗΤΕΣ"));
	}

	@GET
	@Path("/gget")
	public Collection<Subject> getGGET() {
		return ggetSubjects;
	}

	@GET
	@SuppressWarnings("unchecked")
	public Collection<Subject> get(@QueryParam("query") String query) {
		List<Subject> result = null;
		if (query == null) {
			result = em.createQuery("select distinct s from Subject s ")
				.getResultList();
		} else {
			result = em.createQuery("select s from Subject s where s.name like :query")
				.setParameter("query", query + "%")
				.getResultList();
		}
		return result;
	}

	@GET
	@Path("/{id:[0-9]+}")
	public Subject get(@PathParam("id") long id) {
		try {
			return (Subject) em.createQuery(
				"select i from Subject i " +
					"where i.id = :id")
				.setParameter("id", id)
				.getSingleResult();
		} catch (NoResultException e) {
			throw new RestException(Status.NOT_FOUND, "wrong.subject.id");
		}
	}
}
