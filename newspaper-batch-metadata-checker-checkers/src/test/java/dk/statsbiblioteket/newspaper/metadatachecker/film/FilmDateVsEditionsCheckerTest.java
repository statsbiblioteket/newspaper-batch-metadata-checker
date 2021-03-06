package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class FilmDateVsEditionsCheckerTest {
    private ResultCollector resultCollector;
    private Document batchXmlStructure;
    private FilmDateVsEditionsChecker checker;

    @BeforeMethod
    public void setupEnvironment() {
        resultCollector = mock(ResultCollector.class);
        batchXmlStructure = DOM.stringToDOM("<node></node>");
        XPathSelector xpathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        checker = new FilmDateVsEditionsChecker(resultCollector, xpathSelector, batchXmlStructure);
    }

    @Test
    public void goodCaseTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-10-10-1", "2013-10-11-1", "2013-10-12-2");
        Document filmDoc = createFilmXmlDoc("2013-10-10", "2013-10-12");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionTooOldTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2012-10-10-1", "2013-10-12-1");
        Document filmDoc = createFilmXmlDoc("2013-10-10", "2013-10-12");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyFailure(filmEvent.getName());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionTooYoungTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-10-10-1", "2013-10-13-2");
        Document filmDoc = createFilmXmlDoc("2013-10-10", "2013-10-12");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyFailure(filmEvent.getName());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionTooOldFuzzyTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2012-10-10-1", "2013-10-1");
        Document filmDoc = createFilmXmlDoc("2013-10", "2013-10");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyFailure(filmEvent.getName());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionTooYoungFuzzyTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-10-1", "2013-11-01-2");
        Document filmDoc = createFilmXmlDoc("2013-10", "2013-10");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyFailure(filmEvent.getName());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyFuzzyEditionContained() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-10-01-1", "2013-11-1", "2013-12-31-1");
        Document filmDoc = createFilmXmlDoc("2013-10-01", "2013-12-31");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyFuzzyStartDate() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-10-1", "2013-11-1", "2013-12-31-1");
        Document filmDoc = createFilmXmlDoc("2013-10", "2013-12-31");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyAnotherFuzzyStartDate() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-11-1", "2013-11-01-1", "2013-12-31-1");
        Document filmDoc = createFilmXmlDoc("2013-11", "2013-12-31");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyYetAnotherFuzzyStartDate() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-11-01-1", "2013-11-1", "2013-12-31-1");
        Document filmDoc = createFilmXmlDoc("2013-11", "2013-12-31");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyFuzzyEndDate() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2012-10-01-1", "2013-11-1", "2013-1");
        Document filmDoc = createFilmXmlDoc("2012-10-01", "2013");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyFuzzyStartAndEndDate() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-1", "2013-11-1", "2013-1");
        Document filmDoc = createFilmXmlDoc("2013", "2013");
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }



    private AttributeParsingEvent createFilmEvent(final String filmID) {
        return new AttributeParsingEvent("reelID/" + filmID + ".film.xml") {
            @Override
            public InputStream getData() throws IOException {
                return null;
            }
            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
    }

    private Document createFilmXmlDoc(final String startDate, final String endDate) {
        final String filmXmlStructure =
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                        "    <avis:startDate>" + startDate + "</avis:startDate>\n" +
                        "    <avis:endDate>" + endDate + "</avis:endDate>\n" +
                        "</avis:reelMetadata>";
        return DOM.stringToDOM(filmXmlStructure);
    }

    private void verifyFailure(String eventName) {
        verify(resultCollector).addFailure(eq(eventName), eq(FailureType.METADATA.value()),
                eq(FilmDateVsEditionsChecker.class.getSimpleName()), anyString());
    }

    /**
     * Adds a film node to a batchXmlStructure with the given short name.
     */
    private void addFilmNode(Document doc, String filmID) {
        Element filmNode = batchXmlStructure.createElement("node");
        filmNode.setAttribute("shortName", filmID);
        doc.getFirstChild().appendChild(filmNode);
    }

    /**
     * Adds a edition node to the indicated film node in the supplied film xml document.
     */
    private void addEditionNode(Document doc, String filmID, String... newEditionIDs) {
        for (String newEditionID : newEditionIDs) {
            Element editionNode = batchXmlStructure.createElement("node");
            editionNode.setAttribute("shortName", newEditionID);

            String xPathStr = "/node/node[@shortName='" + filmID + "']";
            Node filmNode = DOM.createXPathSelector().selectNodeList(doc, xPathStr).item(0);
            filmNode.appendChild(editionNode);
        }
    }
}
