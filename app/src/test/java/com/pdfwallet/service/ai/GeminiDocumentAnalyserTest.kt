package com.pdfwallet.service.ai

import com.pdfwallet.util.Logger
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeminiDocumentAnalyserTest {

    private lateinit var logger: FakeLogger
    private lateinit var analyser: GeminiDocumentAnalyser

    class FakeLogger : Logger(mockAppLogDao(), kotlinx.coroutines.GlobalScope) {
        val errorLogs = mutableListOf<String>()
        override fun e(tag: String, message: String, throwable: Throwable?) {
            errorLogs.add(message)
        }
        // Dummy mock for the DAO just to satisfy constructor (not used in Logger.e since it's overridden)
        companion object {
            fun mockAppLogDao(): com.pdfwallet.data.db.AppLogDao {
                return object : com.pdfwallet.data.db.AppLogDao {
                    override suspend fun insert(log: com.pdfwallet.data.db.AppLog) {}
                    override fun getAllLogs(): kotlinx.coroutines.flow.Flow<List<com.pdfwallet.data.db.AppLog>> = kotlinx.coroutines.flow.flowOf()
                    override suspend fun clearAll() {}
                }
            }
        }
    }

    @Before
    fun setup() {
        logger = FakeLogger()
        analyser = GeminiDocumentAnalyser(logger)
    }

    @Test
    fun testParseGeminiResponse_MarkdownFencedJson_ParsesCorrectly() {
        val markdownFencedJson = """
            ```json
            {
                "documentType": "TRAIN_TICKET",
                "title": "Test Title"
            }
            ```
        """.trimIndent()
        
        val result = analyser.parseGeminiResponse(markdownFencedJson)
        assertEquals("TRAIN_TICKET", result.documentType)
        assertEquals("Test Title", result.title)
    }

    @Test
    fun testParseGeminiResponse_MalformedJson_LogsErrorAndReturnsDefault() {
        val malformedJson = "{ \"documentType\": \"TRAIN_TICKET\" " // missing closing brace
        
        val result = analyser.parseGeminiResponse(malformedJson)
        
        assertEquals("OTHER", result.documentType) // default
        assert(logger.errorLogs.any { it.contains("JSON parsing failed") })
    }
}
