package com.example.saber.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saber.model.PaperStyle
import com.example.saber.model.ShapeType
import com.example.saber.model.ToolType
import com.example.ui.theme.HighlightCyan
import com.example.ui.theme.HighlightGreen
import com.example.ui.theme.HighlightOrange
import com.example.ui.theme.HighlightPink
import com.example.ui.theme.HighlightYellow
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkBlue
import com.example.ui.theme.InkGreen
import com.example.ui.theme.InkOrange
import com.example.ui.theme.InkPurple
import com.example.ui.theme.InkRed
import com.example.ui.theme.SaberAmber
import kotlin.math.roundToInt

@Composable
fun FloatingToolPalette(
    currentTool: ToolType,
    onToolSelected: (ToolType) -> Unit,
    currentColor: Long,
    onColorSelected: (Long) -> Unit,
    currentWidth: Float,
    onWidthSelected: (Float) -> Unit,
    currentShape: ShapeType,
    onShapeSelected: (ShapeType) -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    canRedo: Boolean,
    onRedo: () -> Unit,
    paperStyle: PaperStyle,
    onPaperStyleChange: (PaperStyle) -> Unit,
    isSmartDarkMode: Boolean,
    onToggleSmartDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val inkColors = listOf(
        InkBlack,
        InkBlue,
        InkRed,
        InkGreen,
        InkPurple,
        InkOrange
    )

    val highlighterColors = listOf(
        HighlightYellow,
        HighlightGreen,
        HighlightCyan,
        HighlightPink,
        HighlightOrange
    )

    val activeColorList = if (currentTool == ToolType.HIGHLIGHTER) highlighterColors else inkColors

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        if (!isExpanded) {
            // Collapsed Floating Bubble
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 8.dp,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .clickable { isExpanded = true }
                    .testTag("floating_palette_collapsed_bubble")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val icon = when (currentTool) {
                        ToolType.PEN -> Icons.Default.Edit
                        ToolType.HIGHLIGHTER -> Icons.Default.Highlight
                        ToolType.ERASER -> Icons.Default.Close
                        ToolType.SHAPE -> Icons.Default.Category
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Werkzeuge öffnen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Mini color indicator pip
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(currentColor))
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }
        } else {
            // Expanded Floating Tool Palette
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("floating_palette_expanded")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header / Drag handle & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Verschieben",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Saber Studio",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Smart Dark Mode Toggle
                            IconButton(
                                onClick = onToggleSmartDarkMode,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSmartDarkMode) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                    contentDescription = "Smart Dark Mode",
                                    tint = if (isSmartDarkMode) SaberAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Undo
                            IconButton(
                                onClick = onUndo,
                                enabled = canUndo,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Rückgängig",
                                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Redo
                            IconButton(
                                onClick = onRedo,
                                enabled = canRedo,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = "Wiederholen",
                                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Minimize Button
                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Palette minimieren",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Tool Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ToolButton(
                            title = "Stift",
                            icon = Icons.Default.Edit,
                            isSelected = currentTool == ToolType.PEN,
                            onClick = { onToolSelected(ToolType.PEN) },
                            modifier = Modifier.testTag("tool_pen_button")
                        )
                        ToolButton(
                            title = "Marker",
                            icon = Icons.Default.Highlight,
                            isSelected = currentTool == ToolType.HIGHLIGHTER,
                            onClick = { onToolSelected(ToolType.HIGHLIGHTER) },
                            modifier = Modifier.testTag("tool_highlighter_button")
                        )
                        ToolButton(
                            title = "Radierer",
                            icon = Icons.Default.Close,
                            isSelected = currentTool == ToolType.ERASER,
                            onClick = { onToolSelected(ToolType.ERASER) },
                            modifier = Modifier.testTag("tool_eraser_button")
                        )
                        ToolButton(
                            title = "Formen",
                            icon = Icons.Default.Category,
                            isSelected = currentTool == ToolType.SHAPE,
                            onClick = { onToolSelected(ToolType.SHAPE) },
                            modifier = Modifier.testTag("tool_shapes_button")
                        )
                    }

                    // Shapes sub-row if SHAPE is selected
                    AnimatedVisibility(
                        visible = currentTool == ToolType.SHAPE,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ShapeChip("Linie", currentShape == ShapeType.LINE) { onShapeSelected(ShapeType.LINE) }
                                ShapeChip("Rechteck", currentShape == ShapeType.RECTANGLE) { onShapeSelected(ShapeType.RECTANGLE) }
                                ShapeChip("Kreis", currentShape == ShapeType.CIRCLE) { onShapeSelected(ShapeType.CIRCLE) }
                                ShapeChip("Pfeil", currentShape == ShapeType.ARROW) { onShapeSelected(ShapeType.ARROW) }
                            }
                        }
                    }

                    // Color & Width Pickers (visible if not ERASER)
                    if (currentTool != ToolType.ERASER) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Color swatches row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            activeColorList.forEach { col ->
                                val colorLong = col.toArgb().toLong()
                                val isSelected = (currentColor == colorLong)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .clickable { onColorSelected(colorLong) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stroke Width presets
                        val widthPresets = if (currentTool == ToolType.HIGHLIGHTER) {
                            listOf(16f to "Fein", 28f to "Mittel", 44f to "Breit")
                        } else {
                            listOf(2.5f to "Fein", 5f to "Mittel", 9f to "Stark", 16f to "Fett")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            widthPresets.forEach { (w, label) ->
                                val isSelected = (currentWidth == w)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .clickable { onWidthSelected(w) }
                                        .padding(horizontal = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(w.coerceIn(4f, 16f).dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Paper pattern quick switch row
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Papier: ${paperStyle.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PaperStyle.values().forEach { style ->
                                val isSelected = (style == paperStyle)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.clickable { onPaperStyleChange(style) }
                                ) {
                                    Text(
                                        text = style.title.take(3),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ShapeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
