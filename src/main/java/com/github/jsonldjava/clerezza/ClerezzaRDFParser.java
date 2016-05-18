package com.github.jsonldjava.clerezza;

import java.util.HashMap;
import java.util.Map;

import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.Language;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.core.RDFParser;

/**
 * Converts a Clerezza {@link Graph} to the {@link RDFDataset} used
 * by the {@link JsonLdProcessor}
 * 
 * @author Rupert Westenthaler
 * 
 */
public class ClerezzaRDFParser implements RDFParser {

    private static String RDF_LANG_STRING = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";

    private long count = 0;

    @Override
    public RDFDataset parse(Object input) throws JsonLdError {
        count = 0;
        final Map<BlankNode, String> bNodeMap = new HashMap<BlankNode, String>(1024);
        final RDFDataset result = new RDFDataset();
        if (input instanceof Graph) {
            for (final Triple t : ((Graph) input)) {
                handleStatement(result, t, bNodeMap);
            }
        }
        bNodeMap.clear(); // help gc
        return result;
    }

    private void handleStatement(RDFDataset result, Triple t, Map<BlankNode, String> bNodeMap) {
        final String subject = getResourceValue(t.getSubject(), bNodeMap);
        final String predicate = getResourceValue(t.getPredicate(), bNodeMap);
        final RDFTerm object = t.getObject();

        if (object instanceof Literal) {

            final String value = ((Literal) object).getLexicalForm();
            final String language;
            final String datatype;
            if (object instanceof TypedLiteral) {
                language = null;
                datatype = getResourceValue(((TypedLiteral) object).getDataType(), bNodeMap);
            } else if (object instanceof PlainLiteral) {
                // we use RDF 1.1 literals so we do set the RDF_LANG_STRING
                // datatype
                datatype = RDF_LANG_STRING;
                final Language l = ((PlainLiteral) object).getLanguage();
                if (l == null) {
                    language = null;
                } else {
                    language = l.toString();
                }
            } else {
                throw new IllegalStateException("Unknown Literal class "
                        + object.getClass().getName());
            }
            result.addTriple(subject, predicate, value, datatype, language);
            count++;
        } else {
            result.addTriple(subject, predicate, getResourceValue((BlankNodeOrIRI) object, bNodeMap));
            count++;
        }

    }

    /**
     * The count of processed triples (not thread save)
     * 
     * @return the count of triples processed by the last {@link #parse(Object)}
     *         call
     */
    public long getCount() {
        return count;
    }

    private String getResourceValue(BlankNodeOrIRI nl, Map<BlankNode, String> bNodeMap) {
        if (nl == null) {
            return null;
        } else if (nl instanceof IRI) {
            return ((IRI) nl).getUnicodeString();
        } else if (nl instanceof BlankNode) {
            String bNodeId = bNodeMap.get(nl);
            if (bNodeId == null) {
                bNodeId = Integer.toString(bNodeMap.size());
                bNodeMap.put((BlankNode) nl, bNodeId);
            }
            return new StringBuilder("_:b").append(bNodeId).toString();
        } else {
            throw new IllegalStateException("Unknwon BlankNodeOrIRI type " + nl.getClass().getName()
                    + "!");
        }
    }
}

