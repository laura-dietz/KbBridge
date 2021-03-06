package edu.umass.ciir.kbbridge.data

import edu.umass.ciir.kbbridge.nlp.TextNormalizer
import edu.umass.ciir.kbbridge.util.WikiXmlTextExtractor
import edu.umass.ciir.kbbridge.search.DocumentBridgeMap

/**
 * User: jdalton
 * Date: 3/29/13
 */
object WikipediaDocumentSource extends DocumentTextSource {

  override def fullText (docId:String) :String = {
    val document = DocumentBridgeMap.getKbDocumentProvider.getDocument(docId)
    val text = TextNormalizer.normalizeText(WikiXmlTextExtractor.extractText(document))
    text
  }

}
