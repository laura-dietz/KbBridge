package edu.umass.ciir.kbbridge

import edu.umass.ciir.kbbridge.data.{SimpleEntityMention, EntityMention, WikipediaEntity}
import search.{EntityRetrievalWeighting, GalagoRetrieval, EntityReprRetrieval, GalagoCandidateGenerator}
import util.{ConfInfo, KbBridgeProperties}
import text2kb.{GalagoDoc2WikipediaEntity, TextEntityReprGenerator}

object NewEntityLinkerMain {

  val nilThreshold = -10

  val reprGenerator = new TextEntityReprGenerator()
  val galago = new GalagoRetrieval(
    jsonConfigFile= ConfInfo.galagoJsonParameterFile,
    galagoUseLocalIndex = true
  )
  val candidateGenerator = new EntityReprRetrieval(galago, EntityRetrievalWeighting(0.5, 0.25, 0.05, 0.2))
  val galagoDocConverter = new GalagoDoc2WikipediaEntity(galago)

  val reranker = new RankLibReranker(KbBridgeProperties.rankerModelFile)

  def link(query: EntityMention): Option[WikipediaEntity] = {

    val entityRepr = reprGenerator.createQVEntityRepr(query)

    val searchResult = candidateGenerator.search(entityRepr, 10)
    val cands =galagoDocConverter.galagoResultToWikipediaEntities(searchResult)

    for (cand <- cands){
      println(cand.wikipediaTitle+"  "+cand.score+" "+cand.rank+"\n")
      for((key,v) <- cand.metadata; if key != "xml") {
        if (key == "contextLinks"){
          println(key + "\t\t"+ (v.split("\n").take(10).mkString("","\n","...\n")))
        } else
          println(key +"\t\t"+v)
      }
//        +cand.metadata)
    }


    val reranked = reranker.rerankCandidatesGenerateFeatures(query, cands)

    if (reranked.size > 0) {
      println("Linking result:\tquery: " + query.entityName + " " + "\ttop cand: " + cands.head.wikipediaTitle + "\treranked: "
        + reranked.head.wikipediaTitle + "\tscore: " + reranked.head.score + "\tNIL?: " + (if (reranked.head.score > nilThreshold) false else true))

      println("Features:\n"+reranked.head.featureMap.map(_.toSeq.map(_.toString()).mkString("\n")))

      if (cands.head.score > nilThreshold) Some(reranked.head) else None
    } else {
      println("Linking result: query: " + query.entityName + " " + "top cand: NIL reranked: NIL")
      None
    }
  }


  def main(args: Array[String]) {

    KbBridgeProperties.loadProperties("./config/kbbridge.properties")
    println("Ranker model: " + KbBridgeProperties.rankerModelFile)

    val testEntity = new SimpleEntityMention("test", "PERSON", "test01", "Bill Clinton", "")
    link(testEntity)

  }
}