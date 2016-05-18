package com.github.jsonldjava.clerezza;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Triple;
import org.junit.Test;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class ClerezzaTripleCallbackTest {

    @Test
    public void triplesTest() throws IOException, JsonLdError {
        final InputStream in = getClass().getClassLoader().getResourceAsStream(
                "testfiles/product.jsonld");
        final Object input = JsonUtils.fromInputStream(in);

        final ClerezzaTripleCallback callback = new ClerezzaTripleCallback();

        final Graph graph = (Graph) JsonLdProcessor.toRDF(input, callback);

        for (final Triple t : graph) {
            System.out.println(t);
        }
        assertEquals("ImmutableGraph size", 13, graph.size());

    }

    @Test
    public void curiesInContextTest() throws IOException, JsonLdError {
        final InputStream in = getClass().getClassLoader().getResourceAsStream(
                "testfiles/curies-in-context.jsonld");
        final Object input = JsonUtils.fromInputStream(in);

        final ClerezzaTripleCallback callback = new ClerezzaTripleCallback();

        final Graph graph = (Graph) JsonLdProcessor.toRDF(input, callback);

        for (final Triple t : graph) {
            System.out.println(t);
            assertTrue("Predicate got fully expanded", t.getPredicate().getUnicodeString()
                    .startsWith("http"));
        }
        assertEquals("ImmutableGraph size", 3, graph.size());

    }
}
