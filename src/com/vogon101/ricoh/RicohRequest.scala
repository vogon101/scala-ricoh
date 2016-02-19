package com.vogon101.ricoh

import java.awt.Image
import java.awt.image.RenderedImage
import java.io.File
import javax.imageio.ImageIO

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, DefaultHttpClient}
import org.apache.http.util.EntityUtils

import scala.util.parsing.json._

/**
  * Created by poserf on 19/02/2016.
  */
class RicohRequest (
                     command: String,
                     params: String = "",
                     session: String = "",
                     base: String="osc/commands/execute",
                     payload: Boolean = true
                   ){
  //TODO: Make params a list

  var _lastResponse: CloseableHttpResponse = null

  var timeTaken = 0

  def execute():CloseableHttpResponse = {
    println("Running command " + command + " at " + base)
    // create an HttpPost object
    val post = new HttpPost("http://192.168.1.1/" + base)

    post.setConfig(RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).setConnectionRequestTimeout(1000).build())

    println("POST REQUEST CREATED")

    // set the Content-type
    post.setHeader("Content-type", "application/json")

    println("HEADER SET")

    val body =  if (payload) s"""{
                | "name":"camera.$command",
                | "parameters": {
                |    ${if (session =="") "" else s""""sessionId":"$session"${if (params != "") "," else ""}"""}
                |    $params
                |  }
                |}""".stripMargin else ""

    println("BODY DONE")

    //println(body)



    post.setEntity(new StringEntity(body))

    println("STARTING EXECUTE")
    _lastResponse = (new DefaultHttpClient).execute(post)
    println("DONE EXECUTE")
    lastResponse
  }

  /**
    * Only works when called first time
    */
  def getBody(response: CloseableHttpResponse) = {
    EntityUtils.toString(response.getEntity)
  }

  def body = getBody(lastResponse)

  def parseJson(json: String):Option[Map[String, Any]] = {
    val parsed = JSON.parseFull(json)
    //println(parsed)
    parsed match {
      case Some(map: Map[String, Any]) => Some(map)
      case None => None
      case _ => None
    }
  }

  def bodyAsJSON = parseJson(body)

  def lastResponse = _lastResponse
}

object Ricoh {


  def genSessionID() = {
    val request = new RicohRequest("startSession", session = "")
    request.execute()
    val JSONbody = request.bodyAsJSON
    if (JSONbody isEmpty)
      throw new Exception("Session token response failed to parse")
    else
      JSONbody.get("results").asInstanceOf[Map[String, String]]("sessionId")
  }

  def getImage(imgURI: String, sessionID: String = genSessionID()): RenderedImage = {
    val request = new RicohRequest("getImage", s""" "fileUri": "$imgURI", "_type": "full" """, sessionID)
    request.execute()

    val bytes = request.lastResponse.getEntity.getContent

    ImageIO.read(bytes)
  }

  def state = {
    println("STATE CALL")
    val request = new RicohRequest("", base = "osc/state", payload = false)
    println("REQUEST CREATED")
    request.execute()
    println("REQUEST DONE")
    request.bodyAsJSON.get
  }

  def closeSession(session: String): Unit = {
    val request = new RicohRequest("closeSession", session = session)
    request.execute()
    val JSONbody = request.bodyAsJSON
    if (JSONbody isEmpty)
      throw new Exception("Session-close response failed to parse")
    else
      JSONbody
  }

  def latestFileUri = {

  }
}
