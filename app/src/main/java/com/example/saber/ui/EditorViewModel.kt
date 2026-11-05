package com.example.saber.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saber.canvas.CanvasMath
import com.example.saber.data.NotesRepository
import com.example.saber.data.PageEntity
import com.example.saber.data.StrokeSerializer
import com.example.saber.model.PaperStyle
import com.example.saber.model.ShapeType
import com.example.saber.model.Stroke
import com.example.saber.model.StrokePoint
import com.example.saber.model.ToolType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val notebookId: Long = 0,
    val notebookTitle: String = "",
    val pageId: Long = 0,
    val pageIndex: Int = 0,
    val totalPages: Int = 1,
    val paperStyle: PaperStyle = PaperStyle.GRID,
    val strokes: List<Stroke> = emptyList(),
    val currentStroke: Stroke? = null,
    val currentTool: ToolType = ToolType.PEN,
    val currentColor: Long = 0xFF18181BL, // Default Ink Black
    val currentWidth: Float = 4.5f,
    val currentShape: ShapeType = ShapeType.LINE,
    val isSmartDarkMode: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val zoomScale: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val isSaving: Boolean = false
)

class EditorViewModel(
    private val repository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<List<Stroke>>()
    private val redoStack = mutableListOf<List<Stroke>>()

    fun loadPage(notebookId: Long, pageIndex: Int = 0) {
        viewModelScope.launch {
            val notebook = repository.getNotebookById(notebookId) ?: return@launch
            val page = repository.getOrCreatePage(notebookId, pageIndex, notebook.defaultPaperStyle)
            val loadedStrokes = StrokeSerializer.fromJson(page.strokesJson)

            undoStack.clear()
            redoStack.clear()

            _uiState.update {
                it.copy(
                    notebookId = notebookId,
                    notebookTitle = notebook.title,
                    pageId = page.id,
                    pageIndex = pageIndex,
                    totalPages = maxOf(notebook.pageCount, pageIndex + 1),
                    paperStyle = page.paperStyle,
                    strokes = loadedStrokes,
                    currentStroke = null,
                    canUndo = false,
                    canRedo = false
                )
            }
        }
    }

    fun onTouchDown(point: Offset, pressure: Float = 1f) {
        val state = _uiState.value
        val strokePoint = StrokePoint(point.x, point.y, pressure)

        when (state.currentTool) {
            ToolType.ERASER -> {
                eraseAtPoint(point)
            }
            ToolType.PEN, ToolType.HIGHLIGHTER -> {
                val newStroke = Stroke(
                    points = listOf(strokePoint),
                    color = state.currentColor,
                    width = if (state.currentTool == ToolType.HIGHLIGHTER) 28f else state.currentWidth,
                    toolType = state.currentTool,
                    shapeType = ShapeType.NONE
                )
                _uiState.update { it.copy(currentStroke = newStroke) }
            }
            ToolType.SHAPE -> {
                val newStroke = Stroke(
                    points = listOf(strokePoint, strokePoint),
                    color = state.currentColor,
                    width = state.currentWidth,
                    toolType = ToolType.SHAPE,
                    shapeType = state.currentShape
                )
                _uiState.update { it.copy(currentStroke = newStroke) }
            }
        }
    }

    fun onTouchMove(point: Offset, pressure: Float = 1f) {
        val state = _uiState.value
        when (state.currentTool) {
            ToolType.ERASER -> {
                eraseAtPoint(point)
            }
            ToolType.PEN, ToolType.HIGHLIGHTER -> {
                val active = state.currentStroke ?: return
                val updatedPoints = active.points + StrokePoint(point.x, point.y, pressure)
                _uiState.update { it.copy(currentStroke = active.copy(points = updatedPoints)) }
            }
            ToolType.SHAPE -> {
                val active = state.currentStroke ?: return
                val start = active.points.firstOrNull() ?: StrokePoint(point.x, point.y)
                _uiState.update {
                    it.copy(currentStroke = active.copy(points = listOf(start, StrokePoint(point.x, point.y))))
                }
            }
        }
    }

    fun onTouchUp() {
        val state = _uiState.value
        val completed = state.currentStroke ?: return

        // Push current state to undo
        pushUndo(state.strokes)

        val updatedStrokes = state.strokes + completed
        _uiState.update {
            it.copy(
                strokes = updatedStrokes,
                currentStroke = null,
                canUndo = true,
                canRedo = false
            )
        }
        redoStack.clear()
        saveStrokesDebounced(updatedStrokes)
    }

    private fun eraseAtPoint(point: Offset) {
        val state = _uiState.value
        val remaining = state.strokes.filterNot { stroke ->
            CanvasMath.isPointNearStroke(point, stroke, hitThreshold = 18f)
        }
        if (remaining.size != state.strokes.size) {
            pushUndo(state.strokes)
            _uiState.update {
                it.copy(
                    strokes = remaining,
                    canUndo = true,
                    canRedo = false
                )
            }
            redoStack.clear()
            saveStrokesDebounced(remaining)
        }
    }

    private fun pushUndo(strokes: List<Stroke>) {
        undoStack.add(strokes)
        if (undoStack.size > 40) {
            undoStack.removeAt(0)
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeAt(undoStack.lastIndex)
        val current = _uiState.value.strokes
        redoStack.add(current)

        _uiState.update {
            it.copy(
                strokes = previous,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
        saveStrokesDebounced(previous)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        val current = _uiState.value.strokes
        undoStack.add(current)

        _uiState.update {
            it.copy(
                strokes = next,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
        saveStrokesDebounced(next)
    }

    fun clearPage() {
        val current = _uiState.value.strokes
        if (current.isEmpty()) return
        pushUndo(current)
        _uiState.update {
            it.copy(
                strokes = emptyList(),
                canUndo = true,
                canRedo = false
            )
        }
        redoStack.clear()
        saveStrokesDebounced(emptyList())
    }

    fun selectTool(tool: ToolType) {
        _uiState.update { it.copy(currentTool = tool) }
    }

    fun selectColor(color: Long) {
        _uiState.update { it.copy(currentColor = color) }
    }

    fun selectWidth(width: Float) {
        _uiState.update { it.copy(currentWidth = width) }
    }

    fun selectShape(shape: ShapeType) {
        _uiState.update { it.copy(currentShape = shape) }
    }

    fun changePaperStyle(style: PaperStyle) {
        _uiState.update { it.copy(paperStyle = style) }
        viewModelScope.launch {
            val state = _uiState.value
            repository.savePageStrokes(state.pageId, state.notebookId, state.strokes, style)
        }
    }

    fun toggleSmartDarkMode() {
        _uiState.update { it.copy(isSmartDarkMode = !it.isSmartDarkMode) }
    }

    fun nextPage() {
        val nextIndex = _uiState.value.pageIndex + 1
        loadPage(_uiState.value.notebookId, nextIndex)
    }

    fun previousPage() {
        val prevIndex = _uiState.value.pageIndex - 1
        if (prevIndex >= 0) {
            loadPage(_uiState.value.notebookId, prevIndex)
        }
    }

    private fun saveStrokesDebounced(strokes: List<Stroke>) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.pageId > 0) {
                repository.savePageStrokes(state.pageId, state.notebookId, strokes, state.paperStyle)
            }
        }
    }
}
