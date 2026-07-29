package app.xiguang.today

import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.Platform
import app.xiguang.domain.model.SavedCollection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

class TodayRandomSelectionTest {
    @Test
    fun emptyCollection_returnsNull() {
        assertNull(selectRandomCollection(emptyList(), previousCollectionId = null))
    }

    @Test
    fun singleCollection_canBeSelectedAgain() {
        val only = collection(1)

        assertEquals(
            only,
            selectRandomCollection(
                collections = listOf(only),
                previousCollectionId = only.id,
                random = Random(0),
            ),
        )
    }

    @Test
    fun multipleCollections_doNotRepeatPreviousSelection() {
        val collections = listOf(collection(1), collection(2), collection(3))

        repeat(20) { seed ->
            val selected = selectRandomCollection(
                collections = collections,
                previousCollectionId = 2,
                random = Random(seed),
            )

            assertNotEquals(2L, selected?.id)
        }
    }

    private fun collection(id: Long) = SavedCollection(
        id = id,
        canonicalUrl = "https://example.com/$id",
        originalUrl = "https://example.com/$id",
        title = "Collection $id",
        sharedText = null,
        platform = Platform.BLOG,
        contentType = ContentType.LINK,
        mimeType = null,
        previewUri = null,
        folderId = null,
        folderPath = null,
        note = null,
        isRead = false,
        createdAt = 0,
        updatedAt = 0,
    )
}
