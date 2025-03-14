package org.nlogo.extensions.openai

import java.io.File
import org.nlogo.headless.TestLanguage

object Tests {
  // file paths are relative to the repository root
  val testFileNames = Seq("tests.txt")
  val testFiles     = testFileNames.map( (f) => (new File(f)).getCanonicalFile )
}

class Tests extends TestLanguage(Tests.testFiles) {
  System.setProperty("org.nlogo.preferHeadless", "true")
} 