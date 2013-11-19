package dk.statsbiblioteket.newspaper.metadatachecker.checker;

import java.io.IOException;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import org.w3c.dom.Document;

public abstract class XmlFileChecker extends DefaultTreeEventHandler {
    private List<XmlAttributeChecker> checkers;
    protected final ResultCollector resultCollector;

    public XmlFileChecker(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (shouldCheckEvent(event)) {
            doValidate(event);
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData(), true);
            if (doc == null) {
                addFailure(event, "Could not parse xml from " + event.getName());
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (checkers == null) {
            checkers = getCheckers();
        }
        for (XmlAttributeChecker checker : checkers) {
            checker.validate(event, doc);
        }
    }

    private void addFailure(AttributeParsingEvent event, String description) {
        resultCollector.addFailure(
                event.getName(), MetadataFailureType.METADATA.value(), getClass().getName(), description);
    }

    protected abstract List<XmlAttributeChecker> getCheckers();
    protected abstract boolean shouldCheckEvent(AttributeParsingEvent event);
}
