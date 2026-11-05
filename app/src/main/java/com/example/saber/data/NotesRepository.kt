package com.example.saber.data

import com.example.saber.model.PaperStyle
import com.example.saber.model.ShapeType
import com.example.saber.model.Stroke
import com.example.saber.model.StrokePoint
import com.example.saber.model.ToolType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NotesRepository(
    private val notebookDao: NotebookDao,
    private val pageDao: PageDao
) {
    val allNotebooks: Flow<List<NotebookEntity>> = notebookDao.getAllNotebooks()
    val allFolders: Flow<List<String>> = notebookDao.getAllFolders()

    suspend fun getNotebookById(id: Long): NotebookEntity? = notebookDao.getNotebookById(id)

    suspend fun createNotebook(
        title: String,
        folder: String = "Hauptordner",
        coverColor: Long = 0xFF4338CAL,
        paperStyle: PaperStyle = PaperStyle.GRID
    ): Long {
        val notebook = NotebookEntity(
            title = title,
            folder = folder,
            coverColor = coverColor,
            defaultPaperStyle = paperStyle,
            pageCount = 1
        )
        val notebookId = notebookDao.insertNotebook(notebook)
        // Automatically create Page 0
        pageDao.insertPage(
            PageEntity(
                notebookId = notebookId,
                pageIndex = 0,
                paperStyle = paperStyle,
                strokesJson = "[]"
            )
        )
        return notebookId
    }

    suspend fun updateNotebook(notebook: NotebookEntity) {
        notebookDao.updateNotebook(notebook)
    }

    suspend fun deleteNotebook(notebookId: Long) {
        notebookDao.deleteNotebookById(notebookId)
    }

    fun getPagesForNotebook(notebookId: Long): Flow<List<PageEntity>> {
        return pageDao.getPagesForNotebook(notebookId)
    }

    suspend fun getOrCreatePage(notebookId: Long, pageIndex: Int, defaultPaperStyle: PaperStyle = PaperStyle.GRID): PageEntity {
        val existing = pageDao.getPageByIndex(notebookId, pageIndex)
        if (existing != null) return existing

        val newPage = PageEntity(
            notebookId = notebookId,
            pageIndex = pageIndex,
            paperStyle = defaultPaperStyle,
            strokesJson = "[]"
        )
        val id = pageDao.insertPage(newPage)
        // Update page count on notebook
        val count = pageDao.getPageCount(notebookId)
        val nb = notebookDao.getNotebookById(notebookId)
        if (nb != null && count > nb.pageCount) {
            notebookDao.updateNotebook(nb.copy(pageCount = count, updatedAt = System.currentTimeMillis()))
        }
        return newPage.copy(id = id)
    }

    suspend fun savePageStrokes(pageId: Long, notebookId: Long, strokes: List<Stroke>, paperStyle: PaperStyle) {
        val json = StrokeSerializer.toJson(strokes)
        pageDao.updatePageStrokes(pageId, json)
        val nb = notebookDao.getNotebookById(notebookId)
        if (nb != null) {
            notebookDao.updateNotebook(nb.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updatePagePaperStyle(page: PageEntity, style: PaperStyle) {
        pageDao.updatePage(page.copy(paperStyle = style, updatedAt = System.currentTimeMillis()))
    }

    suspend fun ensureSampleData() {
        val currentNotebooks = allNotebooks.first()
        if (currentNotebooks.isEmpty()) {
            // Welcome notebook with sample handwriting & shapes demonstrating Saber's features
            val welcomeId = createNotebook(
                title = "Willkommen bei Saber",
                folder = "Einführung",
                coverColor = 0xFF4F46E5L, // Indigo
                paperStyle = PaperStyle.GRID
            )

            // Let's create demo strokes illustrating:
            // 1. Text header "Saber Notes"
            // 2. A yellow highlighter stroke
            // 3. A geometric shape (Rectangle & Arrow)
            val demoStrokes = listOf(
                // "S"
                Stroke(
                    points = listOf(
                        StrokePoint(140f, 180f),
                        StrokePoint(120f, 170f),
                        StrokePoint(90f, 190f),
                        StrokePoint(100f, 220f),
                        StrokePoint(130f, 230f),
                        StrokePoint(140f, 260f),
                        StrokePoint(110f, 280f),
                        StrokePoint(80f, 270f)
                    ),
                    color = 0xFF18181BL,
                    width = 6f,
                    toolType = ToolType.PEN
                ),
                // "a"
                Stroke(
                    points = listOf(
                        StrokePoint(180f, 230f),
                        StrokePoint(160f, 240f),
                        StrokePoint(160f, 270f),
                        StrokePoint(185f, 280f),
                        StrokePoint(195f, 250f),
                        StrokePoint(195f, 280f)
                    ),
                    color = 0xFF18181BL,
                    width = 5f,
                    toolType = ToolType.PEN
                ),
                // "b"
                Stroke(
                    points = listOf(
                        StrokePoint(220f, 180f),
                        StrokePoint(220f, 280f),
                        StrokePoint(225f, 240f),
                        StrokePoint(250f, 240f),
                        StrokePoint(255f, 270f),
                        StrokePoint(225f, 280f)
                    ),
                    color = 0xFF18181BL,
                    width = 5f,
                    toolType = ToolType.PEN
                ),
                // "e"
                Stroke(
                    points = listOf(
                        StrokePoint(275f, 255f),
                        StrokePoint(305f, 255f),
                        StrokePoint(305f, 235f),
                        StrokePoint(275f, 235f),
                        StrokePoint(275f, 275f),
                        StrokePoint(305f, 280f)
                    ),
                    color = 0xFF18181BL,
                    width = 5f,
                    toolType = ToolType.PEN
                ),
                // "r"
                Stroke(
                    points = listOf(
                        StrokePoint(325f, 240f),
                        StrokePoint(325f, 280f),
                        StrokePoint(325f, 250f),
                        StrokePoint(350f, 240f)
                    ),
                    color = 0xFF18181BL,
                    width = 5f,
                    toolType = ToolType.PEN
                ),
                // Saber Highlighter behind title
                Stroke(
                    points = listOf(
                        StrokePoint(70f, 240f),
                        StrokePoint(370f, 240f)
                    ),
                    color = 0xFFFACC15L, // Yellow
                    width = 32f,
                    toolType = ToolType.HIGHLIGHTER
                ),
                // Demo rectangle shape
                Stroke(
                    points = listOf(
                        StrokePoint(70f, 350f),
                        StrokePoint(450f, 480f)
                    ),
                    color = 0xFF0D9488L, // Teal
                    width = 4f,
                    toolType = ToolType.SHAPE,
                    shapeType = ShapeType.RECTANGLE
                ),
                // Demo Arrow shape
                Stroke(
                    points = listOf(
                        StrokePoint(260f, 490f),
                        StrokePoint(260f, 570f)
                    ),
                    color = 0xFF4F46E5L, // Indigo
                    width = 5f,
                    toolType = ToolType.SHAPE,
                    shapeType = ShapeType.ARROW
                )
            )

            val page = pageDao.getPageByIndex(welcomeId, 0)
            if (page != null) {
                savePageStrokes(page.id, welcomeId, demoStrokes, PaperStyle.GRID)
            }

            // Second notebook: Vorlesung & Notizen (Liniert)
            createNotebook(
                title = "Vorlesungsnotizen & Ideen",
                folder = "Studium",
                coverColor = 0xFF0D9488L, // Teal
                paperStyle = PaperStyle.RULED
            )

            // Third notebook: Skizzenbuch (Blanko)
            createNotebook(
                title = "Kreatives Skizzenbuch",
                folder = "Design",
                coverColor = 0xFFEA580CL, // Orange
                paperStyle = PaperStyle.BLANK
            )
        }
    }
}
