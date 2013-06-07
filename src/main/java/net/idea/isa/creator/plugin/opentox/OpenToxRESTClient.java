package net.idea.isa.creator.plugin.opentox;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.idea.isa.creator.plugin.opentox.resource.ResourceDescription;
import net.idea.isa.creator.plugin.opentox.xml.ResourceXMLHandler;
import net.idea.opentox.cli.OTClient;
import net.idea.opentox.cli.structure.Substance;

import org.isatools.isacreator.configuration.RecommendedOntology;
import org.isatools.isacreator.gui.ApplicationManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;
import org.isatools.isacreator.plugins.host.service.PluginOntologyCVSearch;
import org.isatools.isacreator.plugins.registries.OntologySearchPluginRegistry;
import org.opentox.rest.RestException;

/**
 * Created by the Ideaconsult Ltd.
 *
 * @author Nina Jeliazkova (jeliazkova.nina@gmail.com)
 *         <p/>
 *         Date: 18/05/2012
 *         Time: 13:28
 * 
 * Based on https://github.com/ISA-tools/NovartisMetastore by       
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 *         <p/>
 *         Date: 12/09/2011
 *         Time: 16:51
 */
public class OpenToxRESTClient implements PluginOntologyCVSearch {
	
	public static List<ResourceDescription> resourceInformation;
    public static String queryURL;
    
    static {
        ResourceXMLHandler xmlHandler = new ResourceXMLHandler();
        resourceInformation = xmlHandler.parseXML();
    }

    public Map<OntologySourceRefObject, List<OntologyTerm>> searchRepository(String term) {
        return searchRepository(term, new HashMap<String, RecommendedOntology>(), false);

    }
    
    enum OTRESOURCE {
    	OT,
    	OTALG,
    	OTMOD,
    	OTDATA
    }
    
    private enum SearchMode {auto,similarity,substructure};
    
    private Map<OntologySourceRefObject, List<OntologyTerm>> performQuery(
    							Map<OntologySourceRefObject, List<OntologyTerm>> results,
    							OTClient otclient,
    							String term, ResourceDescription resourceDescription) {

        try {
        	OTRESOURCE tbresource = OTRESOURCE.valueOf(resourceDescription.getResourceAbbreviation());
			OntologySourceRefObject source = new OntologySourceRefObject(
										resourceDescription.getResourceAbbreviation(),  
										resourceDescription.getQueryURL(),
										"",
										resourceDescription.getResourceName());
        	switch (tbresource) {
        	case OT: {
        		SearchMode mode = SearchMode.auto;
        		StringBuilder query = null;
        		String[] terms = term.split(" ");
        		for (String t : terms) 
        			try { mode=SearchMode.valueOf(t);} catch (Exception x) {
        				if (query==null) query = new StringBuilder(); else query.append(" ");
        				query.append(t);
        			}
        		URL root = new URL(resourceDescription.getQueryURL());
        		List<URL> items = null;        			
        		switch (mode) {
        		case auto: {
        			items = otclient.getSubstanceClient().searchExactStructuresURI(root,query.toString());
        			break;
        		}
        		case similarity: {
            		items = otclient.getSubstanceClient().searchSimilarStructuresURI(root,query.toString(),0.75);
            		break;
        		}
        		case substructure: {
            		items = otclient.getSubstanceClient().searchSubstructuresURI(root,query.toString());
            		break;
        		}
        		default: {
        			items = otclient.getSubstanceClient().searchSimilarStructuresURI(root,query.toString(),0.75);
        		}
        		}
        		if (items!=null && items.size()>0) {
        			List<Substance> substances = new ArrayList<Substance>();
        			for (URL item: items) {
        				substances.addAll(otclient.getSubstanceClient().getIdentifiers(root, item));
        			}
        			convertResourceResult(substances,source,results);
        		}
        		break;
        	}
        	}
            return results;
        } catch (RestException x) {
            System.out.println(String.format("[%s] %s Error connecting to %s",x.getStatus(),x.getMessage(), resourceDescription.getQueryURL()));
            x.printStackTrace();
        } catch (MalformedURLException e) {
            System.out.println("Wrong URL ...");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("No file found. Assuming connection is down...");
            e.printStackTrace();
        } finally {
        }
        return new HashMap<OntologySourceRefObject, List<OntologyTerm>>();
    }
    
    public Map<OntologySourceRefObject, List<OntologyTerm>> searchRepository(String term, 
    			Map<String, RecommendedOntology> recommendedOntologies, boolean searchAll) {
		 Map<OntologySourceRefObject, List<OntologyTerm>> results = new HashMap<OntologySourceRefObject, List<OntologyTerm>>();
    	 OTClient otClient = new OTClient();
    	 try {
    		 boolean loggedin = false;
		        for (ResourceDescription resourceDescription : resourceInformation) {

		        	if (!loggedin) {
		        		loggedin = otClient.login(resourceDescription.getUsername(),resourceDescription.getPassword());
		        	}
		            String fieldDetails = ApplicationManager.getCurrentlySelectedFieldName();
		            // only do the search if the field matches one expected by the system
		            if (searchAll) {
		                results.putAll(performQuery(results,otClient,term, resourceDescription));
		            } else {
		                if (checkIfResourceHasField(resourceDescription, fieldDetails) || checkIfResourceIsRecommended(resourceDescription, recommendedOntologies)) {
		                    System.out.println("Querying on " + resourceDescription.getResourceName() + " for " + term + " on " + fieldDetails);
		                    results.putAll(performQuery(results,otClient, term, resourceDescription));
		                }
		            }
		        }
		     
         } catch (MalformedURLException e) {
             System.out.println("Wrong URL ...");
             e.printStackTrace();
         } catch (Exception e) {
             System.out.println("No file found. Assuming connection is down...");
             e.printStackTrace();
         } finally {
        		try { otClient.logout(); } catch (Exception x) {}
         }	        

         return results;
    }
    
    /**
     * We can check against current assay and the field
     *
     * @param resourceDescription - resource to check
     * @param fieldDetails        - field to look for
     * @return true or false. True if the resource should be searched on for this field.
     */
    public boolean checkIfResourceHasField(ResourceDescription resourceDescription, String fieldDetails) {
    	return false;
    	/*
        if (fieldDetails == null) {
            return false;
        } else {
            String fieldName = fieldDetails.substring(fieldDetails.lastIndexOf(":>") + 2).trim();

            if (resourceDescription.getResourceFields().containsKey(fieldName)) {
                String assayMeasurement, assayTechnology = "";
                if (fieldDetails.contains("using")) {
                    String[] fields = fieldDetails.split("using");
                    assayMeasurement = fields[0].trim();
                    assayTechnology = fields[1].substring(0, fields[1].lastIndexOf(":>")).trim();
                } else {
                    assayMeasurement = fieldDetails.substring(0, fieldDetails.lastIndexOf(":>"));
                }

                for (ResourceField resourceField : resourceDescription.getResourceFields().get(fieldName)) {
                    if (resourceField.getAssayMeasurement().isEmpty()) {
                        return true;
                    } else if (assayMeasurement.equalsIgnoreCase(resourceField.getAssayMeasurement())
                            && assayTechnology.equalsIgnoreCase(resourceField.getAssayTechnology())) {
                        return true;
                    }
                }
            }
            return false;
        }
        */
    }

    
    private boolean checkIfResourceIsRecommended(ResourceDescription resourceDescription, Map<String, RecommendedOntology> recommendedOntologies) {
        for (String ontology : recommendedOntologies.keySet()) {
            if (recommendedOntologies.get(ontology).getOntology().getOntologyAbbreviation().equals(resourceDescription.getResourceAbbreviation())) {
                return true;
            }
        }
        return false;
    }
  
    public void registerSearch() {
        OntologySearchPluginRegistry.registerPlugin(this);
    }

    public void deregisterSearch() {
        OntologySearchPluginRegistry.deregisterPlugin(this);
    }

  
    protected void convertResourceResult(List<? extends Substance> resources,OntologySourceRefObject source,Map<OntologySourceRefObject, List<OntologyTerm>> results) {
    	if (resources==null) return;
    	ArrayList<OntologyTerm> terms = new ArrayList<OntologyTerm>();
    	 for(Substance resource:resources) {
    		 String uri = resource.getResourceIdentifier().toExternalForm();
             OntologyTerm ontologyTerm = new OntologyTerm(uri,null, source);
             ontologyTerm.setOntologyPurl(source.getSourceFile()+"/");
             ontologyTerm.setOntologySourceAccession(uri);
             ontologyTerm.setOntologyTermName(String.format("%s:%s",
            		 	source.getSourceName(),ontologyTerm.getOntologySourceAccession()));
            	 //do smth specific
             terms.add(ontologyTerm);
             ontologyTerm.addToComments("WWW", String.format("<html><a href='%s'>%s</a></html>",uri,uri));
             if (resource.getName()!= null)
            	 ontologyTerm.addToComments("Name", resource.getName());
             if (resource.getCas()!=null)
            	 ontologyTerm.addToComments("CAS RN", resource.getCas());
             if (resource.getInChIKey()!=null)
            	 ontologyTerm.addToComments("InChI Key", resource.getInChIKey());
             ontologyTerm.addToComments("Chemical structure", String.format("<html><img src='%s?media=image/png' alt='%s' title='%s'></html>",
            		 uri,uri,uri));
             System.out.println(resource.getName());
         }
    	 if (terms!=null && (terms.size()>0)) results.put(source, terms);
    }

	public Set<String> getAvailableResourceAbbreviations() {
		 Set<String> abbreviations = new TreeSet<String>();
		 for (ResourceDescription resourceDescription : resourceInformation)
			 abbreviations.add(resourceDescription.getResourceAbbreviation());
		 return abbreviations;
	}

	public boolean hasPreferredResourceForCurrentField(
			Map<String, RecommendedOntology> arg0) {
		System.out.println("hasPreferredResourceForCurrentField");
		System.out.println(arg0);
		return false;
	}
}


