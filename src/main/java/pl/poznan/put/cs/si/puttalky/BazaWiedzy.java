package pl.poznan.put.cs.si.puttalky;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/** Author: agalawrynowicz<br>
 * Date: 19-Dec-2016 */

public class BazaWiedzy {

    private OWLOntologyManager manager = null;
    private OWLOntology ontologia;
    private Set<OWLClass> listaKlas;
    private Set<OWLClass> listaDodatkow;
	private Set<OWLClass> listaIstniejacychPizz;

    OWLReasoner silnik;


	public void inicjalizuj() {
		InputStream plik = this.getClass().getResourceAsStream("/pizza.owl");
		manager = OWLManager.createOWLOntologyManager();
		
		try {
			ontologia = manager.loadOntologyFromOntologyDocument(plik);
			silnik = new Reasoner.ReasonerFactory().createReasoner(ontologia);
			listaKlas = ontologia.getClassesInSignature();
			listaDodatkow = new HashSet<OWLClass>();
			listaIstniejacychPizz = new HashSet<OWLClass>();

			OWLClass dodatek  = manager.getOWLDataFactory().getOWLClass(IRI.create("http://semantic.cs.put.poznan.pl/ontologie/pizza.owl#Dodatek"));
			for (org.semanticweb.owlapi.reasoner.Node<OWLClass> klasa: silnik.getSubClasses(dodatek, false)) {
				listaDodatkow.add(klasa.getRepresentativeElement());
			}


			OWLClass pizza  = manager.getOWLDataFactory().getOWLClass(IRI.create("http://semantic.cs.put.poznan.pl/ontologie/pizza.owl#Pizza"));
			for (org.semanticweb.owlapi.reasoner.Node<OWLClass> klasa: silnik.getSubClasses(pizza, false)) {
				listaIstniejacychPizz.add(klasa.getRepresentativeElement());

			}

			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    public Set<String> dopasujDodatek(String s){
    	Set<String> result = new HashSet<String>();
    	if(s.toLowerCase().equals("bez")) {
			result.add("not");
			return result;
		}
    	for (OWLClass klasa : listaDodatkow){

    		String sad = klasa.getIRI().getFragment();

    		if ( s.length()> 3 && klasa.getIRI().getFragment().toLowerCase().contains(s.substring(0,s.length()-2).toLowerCase())){
    			result.add(klasa.getIRI().toString());
    		}
   			else if ( s.length()> 2 &&klasa.getIRI().getFragment().toLowerCase().contains(s.substring(0,s.length()).toLowerCase())){
					result.add(klasa.getIRI().toString());
    		}
    	}
    	return result;
    }

	public Set<String> dopasujPizze(String s){
		Set<String> result = new HashSet<String>();
		for (OWLClass klasa : listaIstniejacychPizz){
			if ( s.length()>2 &&(klasa.toString().toLowerCase().contains(s.substring(0,s.length()-2).toLowerCase())) && !s.contains("pizza")){
				result.add(klasa.getIRI().toString());
			}
		}

		return result;
	}


    public String[] filtrujDodatki(String s){
		String[] dodatki = new HashSet<String>(Arrays.asList(s.split(";"))).toArray(new String[0]);
		return dodatki;

	}

    public Set<String> wyszukajPizzePoDodatkach(String s){

    	Set<String> pizze = new HashSet<String>();
    	OWLObjectProperty maDodatek = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create("http://semantic.cs.put.poznan.pl/ontologie/pizza.owl#maDodatek"));
    	Set<OWLClassExpression> ograniczeniaEgzystencjalne = new HashSet<OWLClassExpression>();
		boolean compliment=false;

		String[] dodatki = new HashSet<String>(Arrays.asList(s.split(";"))).toArray(new String[0]);
    	for(String iri : dodatki) {
    		if(iri.equals("not")){
    			compliment=true;
    			continue;
			}
			OWLClass dodatek = manager.getOWLDataFactory().getOWLClass(IRI.create(iri));
			OWLClassExpression wyrazenie = manager.getOWLDataFactory().getOWLObjectSomeValuesFrom(maDodatek, dodatek);
			if(compliment) {
				OWLObjectComplementOf ow = manager.getOWLDataFactory().getOWLObjectComplementOf(wyrazenie);
				ograniczeniaEgzystencjalne.add(ow);
				compliment=false;
			}else
			ograniczeniaEgzystencjalne.add(wyrazenie);

		}
    	OWLClassExpression pozadanaPizza = manager.getOWLDataFactory().getOWLObjectIntersectionOf(ograniczeniaEgzystencjalne);
    	
		for (org.semanticweb.owlapi.reasoner.Node<OWLClass> klasa: silnik.getSubClasses(pozadanaPizza, false)) {
			pizze.add(klasa.getEntities().iterator().next().asOWLClass().getIRI().getFragment());

		}
	
		return pizze;
    }

    public String iriNaNazwe(String iri){
		if(iri.equals("not")){
			return "bez";
		}
		OWLClass dodatek = manager.getOWLDataFactory().getOWLClass(IRI.create(iri));
    	return dodatek.getIRI().getFragment();
	}

	
	public static void main(String[] args) {
		BazaWiedzy baza = new BazaWiedzy();
		baza.inicjalizuj();
		
		OWLClass mieso = baza.manager.getOWLDataFactory().getOWLClass(IRI.create("http://semantic.cs.put.poznan.pl/ontologie/pizza.owl#DodatekMięsny"));
		for (org.semanticweb.owlapi.reasoner.Node<OWLClass> klasa: baza.silnik.getSubClasses(mieso, true)) {
			System.out.println("klasa:"+klasa.toString());
		}
		for (OWLClass d:  baza.listaDodatkow){
		//	System.out.println("dodatek: "+d.toString());
		}

	}

}
