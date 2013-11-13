package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.statsbiblioteket.util.Strings.getStackTrace;

/**
 *
 */
public class AltoXPathEventHandler extends DefaultTreeEventHandler {

    private ResultCollector resultCollector;
    private MfPakDAO mfPakDAO;
    private Batch batch;

    /**
     * Constructor for this class.
     * @param resultCollector the result collector to collect errors in
     * @param mfPakDAO a DAO object from which one can read relevant external properties of a batch.
     * @param batch a batch object representing the batch being analysed.
     */
    public AltoXPathEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
        this.resultCollector = resultCollector;
        this.mfPakDAO = mfPakDAO;
        this.batch = batch;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith("alto.xml")) {
            try {
                doValidate(event);
            } catch (Exception e) {    //Fault Barrier
                resultCollector.addFailure(
                        event.getName(),
                        "metadata",
                        getClass().getName(),
                        "Error processing ALTO metadata.",
                        getStackTrace(e)
                );
            }
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        XPathSelector xpath = DOM.createXPathSelector("alto", "http://www.loc.gov/standards/alto/ns-v2#");
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData());
            if (doc == null) {
                resultCollector.addFailure(
                        event.getName(),
                        "metadata",
                        getClass().getName(),
                        "Could not parse xml from " + event.getName(),
                        event.getName()
                );
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String xpath2J3 = "alto:alto/alto:Description/alto:sourceImageInformation/alto:fileName";
        String pathInAlto = xpath.selectString(doc, xpath2J3);
        String expectedName = event.getName().replaceAll("/", "\\\\");
        expectedName = expectedName.replace(".alto.xml", ".jp2");
        if (pathInAlto == null || !expectedName.equals(pathInAlto)) {
            resultCollector.addFailure(event.getName(),
                    "metadata",
                    getClass().getName(),
                    "2J-3: file path in ALTO file '" + pathInAlto + "' does not match actual file path '" + expectedName + "'.",
                    xpath2J3);
        }
    }

}