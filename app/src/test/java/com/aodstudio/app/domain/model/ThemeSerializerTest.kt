package com.aodstudio.app.domain.model

import com.aodstudio.app.core.common.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for domain models, ThemeSerializer JSON serialization,
 * schema validation, and error handling.
 */
class ThemeSerializerTest {

    @Test
    fun `serialize and deserialize default theme preserves data integrity`() {
        val originalTheme = AODTheme.createDefaultTheme("Test Minimal")

        val jsonString = ThemeSerializer.serialize(originalTheme)
        assertTrue("JSON string should not be empty", jsonString.isNotEmpty())

        val result = ThemeSerializer.deserialize(jsonString)
        assertTrue("Deserialization should succeed", result is Result.Success)

        val deserializedTheme = (result as Result.Success).data
        assertEquals(originalTheme.id, deserializedTheme.id)
        assertEquals(originalTheme.name, deserializedTheme.name)
        assertEquals(originalTheme.schemaVersion, deserializedTheme.schemaVersion)
        assertEquals(originalTheme.elements.size, deserializedTheme.elements.size)

        val firstElement = deserializedTheme.elements.first()
        assertEquals(AODElementType.CLOCK, firstElement.type)
        assertEquals("HH:mm", firstElement.properties[AODElement.PROP_FORMAT])
    }

    @Test
    fun `deserialize rejects invalid schema versions`() {
        val futureJson = """
            {
                "id": "test_future",
                "name": "Future Theme",
                "schemaVersion": 999,
                "canvas": { "width": 1080, "height": 2400, "background": "#000000" },
                "elements": []
            }
        """.trimIndent()

        val result = ThemeSerializer.deserialize(futureJson)
        assertTrue("Deserialization should fail for future schema version", result is Result.Error)
        val errorMessage = (result as Result.Error).message
        assertTrue("Error message should mention schema version", errorMessage.contains("schema version"))
    }

    @Test
    fun `deserialize rejects malformed JSON`() {
        val malformedJson = "{ id: invalid json string "

        val result = ThemeSerializer.deserialize(malformedJson)
        assertTrue("Deserialization should fail for malformed JSON", result is Result.Error)
    }

    @Test
    fun `validateTheme rejects blank name or id`() {
        val blankIdTheme = AODTheme(id = "", name = "Valid Name")
        val blankNameTheme = AODTheme(id = "valid_id", name = "")

        val res1 = ThemeSerializer.validateTheme(blankIdTheme)
        assertTrue("Validation should fail for blank ID", res1 is Result.Error)

        val res2 = ThemeSerializer.validateTheme(blankNameTheme)
        assertTrue("Validation should fail for blank name", res2 is Result.Error)
    }

    @Test
    fun `all element types can be serialized and deserialized`() {
        val elements = AODElementType.values().mapIndexed { index, type ->
            AODElement(
                id = "elem_$index",
                name = "Element $type",
                type = type,
                x = index * 10f,
                y = index * 20f
            )
        }

        val theme = AODTheme(
            name = "All Types Theme",
            elements = elements
        )

        val json = ThemeSerializer.serialize(theme)
        val result = ThemeSerializer.deserialize(json)

        assertTrue(result is Result.Success)
        val loaded = (result as Result.Success).data
        assertEquals(AODElementType.values().size, loaded.elements.size)
        assertEquals(AODElementType.NOTIFICATION, loaded.elements[3].type)
    }
}
