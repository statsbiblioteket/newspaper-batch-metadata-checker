package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.MetadataFailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class FilmNumberOfPicturesChecker extends XmlAttributeChecker {
    private final XPathSelector xPathSelector;
    private final Document batchXmlStructure;

    public FilmNumberOfPicturesChecker(
            ResultCollector resultCollector, XPathSelector xPathSelector, Document batchXmlStructure) {
        super(resultCollector, MetadataFailureType.METADATA);
        this.xPathSelector = xPathSelector;
        this.batchXmlStructure = batchXmlStructure;
    }

    public void validate(AttributeParsingEvent event, Document doc) {
        int numberOfPictures = Integer.parseInt(xPathSelector.selectString(doc,
                "/avis:reelMetadata/avis:numberOfPictures"));
        String filmID = event.getName().split("/")[1].replace(".film.xml","");
        NodeList pageImages = xPathSelector.selectNodeList(batchXmlStructure,
                "/node/node[@shortName='400022028241-1']/node/node/node[ends-with(@shortName,'.jp2')]");
        if (pageImages.getLength() != numberOfPictures) {
            addFailure(event, "2E-5: Number of images in film xml for film '" + filmID+ "' doesn't correspond to the number" +
                    "of actual page images.");
        }
    }
}
