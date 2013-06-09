package edu.umass.ciir.kbbridge.search

import scala.Predef._
import scala.Double
import edu.umass.ciir.models.StopWordList
import scala.collection.JavaConversions._
import edu.umass.ciir.memindex.Query
import org.lemurproject.galago.core.tools.Search.SearchResultItem
import org.lemurproject.galago.core.tools.Search
import edu.umass.ciir.kbbridge.util.{SeqTools, ConfInfo}
import edu.umass.ciir.kbbridge.data.repr.EntityRepr
import edu.umass.ciir.galago.GalagoQueryLib
import edu.umass.ciir.kbbridge.data.repr.EntityRepr
import org.lemurproject.galago.core.retrieval.ScoredDocument

/**
 * User: dietz
 * Date: 6/7/13
 * Time: 2:52 PM
 */

case class EntityRetrievalWeighting(lambdaQ:Double, lambdaV:Double, lambdaS:Double, lambdaM:Double)


class EntityReprRetrieval(galago:GalagoRetrieval, entityRetrievalWeighting:EntityRetrievalWeighting) {
  val defaultParams = GalagoQueryLib.paramSeqDep(galago.globalParameters, (0.21, 0.29, 0.50 ))


  def search(entity:EntityRepr, numResults:Int): Seq[ScoredDocument] = {
//    p.set("odw", 0.21D)
//    p.set("uniw", 0.29D)
//    p.set("uww", 0.50D)

    val queryQ = GalagoQueryLib.buildSeqDepForString(entity.entityName)
    val queryNV = {
      val innerQueries =
        for((nv, weight) <- entity.nameVariants.toSeq) yield {
          GalagoQueryLib.buildSeqDepForString(nv) -> weight
        }
      GalagoQueryLib.buildWeightedCombine(innerQueries)
    }

    val queryM = {
      val innerQueries =
        for((neighbor, weight) <- entity.neighbors.toSeq) yield {
          GalagoQueryLib.buildSeqDepForString(neighbor.entityName) -> weight
        }
      GalagoQueryLib.buildWeightedCombine(innerQueries)
    }

    val queryS = {
      GalagoQueryLib.buildWeightedCombine(entity.words.toSeq)
    }

    val fullQuery =
      GalagoQueryLib.buildWeightedCombine(Seq(
        queryQ ->  entityRetrievalWeighting.lambdaQ,
        queryNV ->  entityRetrievalWeighting.lambdaV,
        queryS ->  entityRetrievalWeighting.lambdaS,
        queryM ->  entityRetrievalWeighting.lambdaM
      ))

    System.out.println(fullQuery)

    galago.retrieveScoredDocuments(fullQuery, defaultParams, numResults)
  }


}
