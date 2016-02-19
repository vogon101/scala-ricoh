package com.vogon101.ricoh


import java.io.{ByteArrayInputStream, FileOutputStream, InputStream, File}
import javax.imageio.ImageIO

import org.apache.commons.io.IOUtils


/**
  * Created by poserf on 19/02/2016.
  */
object Main {

  var sessionID = ""

  def main (args: Array[String]): Unit = {
    while (true) {
      downloadNewPhotoTo("latest.jpg")
      Thread sleep 10*1000
    }
  }



  def downloadNewPhotoTo (file: String): Unit = {
    //Start a session
    sessionID = Ricoh.genSessionID()
    println("Session started: " + sessionID)

    var request = new RicohRequest("takePicture", session = sessionID)
    request.execute()

    Thread sleep 5000

    val state = Ricoh.state
    println("Current State: " + state.toString())
    val latestImage = state("state").asInstanceOf[Map[String, Any]]("_latestFileUri").toString
    println("Photo taken : " + latestImage)
    if (latestImage == ""){
      println("No image recently taken - state returned null")
      Ricoh.closeSession(sessionID)
      return
    }

    val image = Ricoh.getImage(latestImage, sessionID)
    if (image == null) {
      println("No image recently taken - function returned null")
      Ricoh.closeSession(sessionID)
      return
    }
    ImageIO.write(image, "JPG", new File(file))

    request = new RicohRequest("delete", s""" "fileUri":"$latestImage" """)

    Ricoh.closeSession(sessionID)

    println(s"Photo saved to $file")

    println("Attempting upload of latest image to server")

    PHPUploader.upload(file)

  }
}
