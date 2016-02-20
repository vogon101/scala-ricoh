package com.vogon101.ricoh

import java.io._
import java.net.{URL, HttpURLConnection}

/**
  * Created by poserf on 19/02/2016.
  */
object PHPUploader {

  def upload(filePath: String): Unit = {
    val httpUrlConnection = new URL("http://188.166.158.93/ricoh/upload.php").openConnection().asInstanceOf[HttpURLConnection]
    httpUrlConnection.setDoOutput(true)
    httpUrlConnection.setRequestMethod("POST")
    val os = httpUrlConnection.getOutputStream
    Thread.sleep(1000)
    val fis = new BufferedInputStream(new FileInputStream(filePath))

    while (fis.available() > 0) {
      os.write(fis.read())
    }

    os.close()
    val in = new BufferedReader(
      new InputStreamReader(
        httpUrlConnection.getInputStream)
    )

    var s = ""
    while (s != null) {
      s = in.readLine()
      if (s != null)
        System.out.println(s)
    }
    in.close()
    fis.close()
  }

}
