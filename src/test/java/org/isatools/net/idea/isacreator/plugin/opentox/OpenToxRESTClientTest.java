package org.isatools.net.idea.isacreator.plugin.opentox;

import java.util.List;
import java.util.Map;

import net.idea.isa.creator.plugin.opentox.OpenToxRESTClient;

import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;
import org.junit.Test;

public class OpenToxRESTClientTest {

    @Test
    public void testSimilaritySearch() {
        OpenToxRESTClient client = new OpenToxRESTClient();
        Map<OntologySourceRefObject, List<OntologyTerm>> result = client.searchRepository("c1ccccc1 similarity",null,true);

        System.out.println("There are " + result.size() + " results");
        for (OntologySourceRefObject source : result.keySet()) {
            System.out.println("For " + source.getSourceName());

            for(OntologyTerm term : result.get(source)) {
                System.out.println(
                		String.format("getUniqueId=%s\ngetOntologyTermName=%s\ngetComments=%s\ngetOntologyPurl=%s\ngetOntologySource=%s\ngetOntologySourceAccession=%s\ngetOntologyVersionId=%s\ngetOntologySourceInformation=%s\n\n",
                		term.getUniqueId(),
                		term.getOntologyTermName(),
                		term.getComments(),
                		term.getOntologyPurl(),
                		term.getOntologySource(),
                		term.getOntologyTermAccession(),
                		term.getOntologyVersionId(),
                		term.getOntologySourceInformation()
                		));
                System.out.println(term.getComments()==null?"":term.getComments());
            }
        }
    }

    @Test
    public void testSearchByInChI() {
    	 OpenToxRESTClient client = new OpenToxRESTClient();
         Map<OntologySourceRefObject, List<OntologyTerm>> result = client.searchRepository("RZVAJINKPMORJF-UHFFFAOYSA-N inchikey",null,true);

         System.out.println("There are " + result.size() + " results");
         for (OntologySourceRefObject source : result.keySet()) {
             System.out.println("For " + source.getSourceName());

             for(OntologyTerm term : result.get(source)) {
                 System.out.println(
                 		String.format("getUniqueId=%s\ngetOntologyTermName=%s\ngetComments=%s\ngetOntologyPurl=%s\ngetOntologySource=%s\ngetOntologySourceAccession=%s\ngetOntologyVersionId=%s\ngetOntologySourceInformation=%s\n\n",
                 		term.getUniqueId(),
                 		term.getOntologyTermName(),
                 		term.getComments(),
                 		term.getOntologyPurl(),
                 		term.getOntologySource(),
                 		term.getOntologyTermAccession(),
                 		term.getOntologyVersionId(),
                 		term.getOntologySourceInformation()
                 		));
                 System.out.println(term.getComments()==null?"":term.getComments());
             }
         }
    }
}
