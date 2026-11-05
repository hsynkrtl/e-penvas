package com.example.saber.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saber.canvas.CanvasMath
import com.example.saber.canvas.PaperPattern
import com.example.saber.model.ShapeType
import com.example.saber.model.Stroke
import com.example.saber.model.ToolType
import com.example.ui.theme.PaperDarkBg
import com.example.ui.theme.PaperLightBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    notebookId: Long,
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(notebookId) {
        viewModel.loadPage(notebookId, 0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (uiState.isSmartDarkMode) PaperDarkBg else PaperLightBg)
    ) {
        // Drawing & Paper Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.currentTool) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.onTouchDown(offset)
                        },
                        onDrag = { change: PointerInputChange, _ ->
                            change.consume()
                            viewModel.onTouchMove(change.position)
                        },
                        onDragEnd = {
                            viewModel.onTouchUp()
                        },
                        onDragCancel = {
                            viewModel.onTouchUp()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Paper Pattern (Grid, Ruled, Dots, Blank, Cornell)
                PaperPattern.drawPaper(
                    drawScope = this,
                    style = uiState.paperStyle,
                    isSmartDarkMode = uiState.isSmartDarkMode
                )

                // 2. Render all strokes: Highlighters first (so they sit behind text), then pens/shapes
                val highlighters = uiState.strokes.filter { it.toolType == ToolType.HIGHLIGHTER }
                val normalStrokes = uiState.strokes.filter { it.toolType != ToolType.HIGHLIGHTER }

                fun drawSingleStroke(stroke: Stroke) {
                    val resolvedColor = CanvasMath.resolveInkColor(stroke.color, uiState.isSmartDarkMode)
                    val path = if (stroke.shapeType != ShapeType.NONE) {
                        CanvasMath.createShapePath(stroke.points, stroke.shapeType)
                    } else {
                        CanvasMath.createSmoothPath(stroke.points)
                    }

                    if (stroke.toolType == ToolType.HIGHLIGHTER) {
                        // Saber uniform blend mode: avoid multi-stroke accumulation discoloration
                        drawPath(
                            path = path,
                            color = resolvedColor.copy(alpha = 0.42f),
                            style = DrawStroke(
                                width = stroke.width,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            ),
                            blendMode = BlendMode.SrcOver
                        )
                    } else {
                        drawPath(
                            path = path,
                            color = resolvedColor,
                            style = DrawStroke(
                                width = stroke.width,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                highlighters.forEach { drawSingleStroke(it) }
                normalStrokes.forEach { drawSingleStroke(it) }

                // 3. Render stroke currently being drawn
                uiState.currentStroke?.let { active ->
                    drawSingleStroke(active)
                }
            }
        }

        // Top Navigation & Page Controls Header
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück zur Übersicht",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Notebook Title
                Text(
                    text = uiState.notebookTitle.ifEmpty { "Notizbuch" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                // Page Navigation Pill (e.g. "< Seite 1 / 2 >")
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.previousPage() },
                            enabled = uiState.pageIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Vorherige Seite",
                                tint = if (uiState.pageIndex > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "${uiState.pageIndex + 1} / ${uiState.totalPages}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = { viewModel.nextPage() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            val isLast = uiState.pageIndex + 1 >= uiState.totalPages
                            Icon(
                                imageVector = if (isLast) Icons.Default.Add else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = if (isLast) "Neue Seite anlegen" else "Nächste Seite",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Share / Export Button
                IconButton(
                    onClick = {
                        ExportHelper.exportAndSharePage(
                            context = context,
                            title = uiState.notebookTitle,
                            pageIndex = uiState.pageIndex,
                            strokes = uiState.strokes,
                            paperStyle = uiState.paperStyle,
                            isSmartDarkMode = uiState.isSmartDarkMode
                        )
                    },
                    modifier = Modifier.testTag("editor_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Seite teilen oder exportieren",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Overflow Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Weitere Optionen",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Seite leeren") },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Floating Tool Palette (Movable, draggable capsule for maximum writing area)
        FloatingToolPalette(
            currentTool = uiState.currentTool,
            onToolSelected = { viewModel.selectTool(it) },
            currentColor = uiState.currentColor,
            onColorSelected = { viewModel.selectColor(it) },
            currentWidth = uiState.currentWidth,
            onWidthSelected = { viewModel.selectWidth(it) },
            currentShape = uiState.currentShape,
            onShapeSelected = { viewModel.selectShape(it) },
            canUndo = uiState.canUndo,
            onUndo = { viewModel.undo() },
            canRedo = uiState.canRedo,
            onRedo = { viewModel.redo() },
            paperStyle = uiState.paperStyle,
            onPaperStyleChange = { viewModel.changePaperStyle(it) },
            isSmartDarkMode = uiState.isSmartDarkMode,
            onToggleSmartDarkMode = { viewModel.toggleSmartDarkMode() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        // Clear confirmation dialog
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Seite leeren?") },
                text = { Text("Möchtest du wirklich alle Zeichnungen und Notizen auf dieser Seite entfernen? Du kannst dies mit Rückgängig wiederherstellen.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearPage()
                            showClearDialog = false
                        }
                    ) {
                        Text("Leeren", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}
