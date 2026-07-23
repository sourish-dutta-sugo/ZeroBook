package com.zerobook.app

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun verifyBackupRulesExcludeDatabase() {
    var backupRulesFile = java.io.File("src/main/res/xml/backup_rules.xml")
    if (!backupRulesFile.exists()) {
      backupRulesFile = java.io.File("app/src/main/res/xml/backup_rules.xml")
    }
    assertTrue("backup_rules.xml should exist", backupRulesFile.exists())
    val content = backupRulesFile.readText()
    assertTrue("backup_rules.xml should exclude ZeroBook.db", content.contains("""path="ZeroBook.db""""))
    assertTrue("backup_rules.xml should exclude ZeroBook.db-shm", content.contains("""path="ZeroBook.db-shm""""))
    assertTrue("backup_rules.xml should exclude ZeroBook.db-wal", content.contains("""path="ZeroBook.db-wal""""))
  }

  @Test
  fun verifyDataExtractionRulesExcludeDatabase() {
    var extractionRulesFile = java.io.File("src/main/res/xml/data_extraction_rules.xml")
    if (!extractionRulesFile.exists()) {
      extractionRulesFile = java.io.File("app/src/main/res/xml/data_extraction_rules.xml")
    }
    assertTrue("data_extraction_rules.xml should exist", extractionRulesFile.exists())
    val content = extractionRulesFile.readText()
    assertTrue("data_extraction_rules.xml should exclude ZeroBook.db", content.contains("""path="ZeroBook.db""""))
    assertTrue("data_extraction_rules.xml should exclude ZeroBook.db-shm", content.contains("""path="ZeroBook.db-shm""""))
    assertTrue("data_extraction_rules.xml should exclude ZeroBook.db-wal", content.contains("""path="ZeroBook.db-wal""""))
  }
}
