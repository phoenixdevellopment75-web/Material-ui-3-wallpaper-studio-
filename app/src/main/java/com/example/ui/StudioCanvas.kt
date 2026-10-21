package com.example.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.engine.CustomShapeType
import com.example.engine.StudioTypographyStyle
import com.example.engine.WallpaperParams

/**
 * High-performance Studio Canvas wrapper providing touch gestures,
 * selection bounding boxes, multi-element group transforms, and decoupled
 * top-anchored tuning toolbars for M3 shapes, Nagasaki Depth Text, and Photo Widgets.
 */
@Composable
fun StudioCanvas(
    params: WallpaperParams,
    selectedShapeId: String?,
    selectedTextId: String? = null,
    selectedImageId: String? = null,
    isMultiSelectMode: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onSelectShape: (String?) -> Unit,
    onSelectTextLayer: (String?) -> Unit = {},
    onSelectImageLayer: (String?) -> Unit = {},
    onToggleItemSelection: (String) -> Unit = {},
    onCommitShapePosition: (String, Float, Float) -> Unit,
    onCommitShapeScale: (String, Float) -> Unit,
    onCommitShapeDimensions: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitShapeRotation: (String, Float) -> Unit,
    onSetShapeColorIndex: (String, Int) -> Unit,
    onBringShapeToFront: (String) -> Unit,
    onSendShapeToBack: (String) -> Unit,
    onDuplicateShape: (String) -> Unit,
    onDeleteShape: (String) -> Unit,
    onToggleShapeWireframe: (String) -> Unit,
    onUpdateShapeStrokeWidth: (String, Float) -> Unit = { _, _ -> },
    onUpdateOpacity: (String, Float) -> Unit = { _, _ -> },
    onUpdateShadowRadius: (String, Float) -> Unit = { _, _ -> },
    onUpdateBlurRadius: (String, Float) -> Unit = { _, _ -> },
    // Depth Text Callbacks
    onUpdateTextContent: (String, String) -> Unit = { _, _ -> },
    onCommitTextPosition: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitTextScale: (String, Float) -> Unit = { _, _ -> },
    onCommitTextDimensions: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitTextRotation: (String, Float) -> Unit = { _, _ -> },
    onSetTextColorIndex: (String, Int) -> Unit = { _, _ -> },
    onBringTextToFront: (String) -> Unit = {},
    onSendTextToBack: (String) -> Unit = {},
    onDeleteText: (String) -> Unit = {},
    onUpdateTextOpacity: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextShadowRadius: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextTypographyStyle: (String, StudioTypographyStyle) -> Unit = { _, _ -> },
    onToggleTextOutlineOnly: (String) -> Unit = {},
    onUpdateTextStrokeWidth: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextGlowRadius: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextLetterSpacing: (String, Float) -> Unit = { _, _ -> },
    onUpdateTextLineHeight: (String, Float) -> Unit = { _, _ -> },
    onImportCustomFont: (String, Uri) -> Unit = { _, _ -> },
    onClearCustomFont: (String) -> Unit = {},
    // Picture-in-Widget Callbacks
    onCommitImagePosition: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitImageDimensions: (String, Float, Float) -> Unit = { _, _, _ -> },
    onCommitImageRotation: (String, Float) -> Unit = { _, _ -> },
    onUpdateImageShapeType: (String, CustomShapeType) -> Unit = { _, _ -> },
    onBringImageToFront: (String) -> Unit = {},
    onSendImageToBack: (String) -> Unit = {},
    onDuplicateImage: (String) -> Unit = {},
    onDeleteImage: (String) -> Unit = {},
    onToggleImageLockProportions: (String) -> Unit = {},
    onUpdateImageOpacity: (String, Float) -> Unit = { _, _ -> },
    onUpdateImageShadowRadius: (String, Float) -> Unit = { _, _ -> },
    // Multi-Select Group Callbacks
    onCommitGroupTranslation: (Float, Float) -> Unit = { _, _ -> },
    onCommitGroupScale: (Float) -> Unit = {},
    onSelectAllItems: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onDeleteSelectedGroup: () -> Unit = {},
    onBringGroupForward: () -> Unit = {},
    onSendGroupToBack: () -> Unit = {},
    onExitMultiSelect: () -> Unit = {},
    isFullscreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val selectedShape = params.customShapes.find { it.id == selectedShapeId }
    val selectedText = params.customTexts.find { it.id == selectedTextId }
    val selectedImage = params.customImages.find { it.id == selectedImageId }
    val isItemSelected = (selectedShape != null || selectedText != null || selectedImage != null || (isMultiSelectMode && selectedItemIds.isNotEmpty())) && !isFullscreen

    // Calculate safe positioning: docked directly below top app bar with explicit clearance
    val topBarHeight = 64.dp
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeTopPadding = topBarHeight + statusBarInset + 8.dp

    Box(modifier = modifier.fillMaxSize()) {
        // Wallpaper Canvas & Elements (Gestures, Items, Handles)
        StudioTouchCanvas(
            params = params,
            selectedShapeId = selectedShapeId,
            selectedTextId = selectedTextId,
            selectedImageId = selectedImageId,
            isMultiSelectMode = isMultiSelectMode,
            selectedItemIds = selectedItemIds,
            onSelectShape = onSelectShape,
            onSelectTextLayer = onSelectTextLayer,
            onSelectImageLayer = onSelectImageLayer,
            onToggleItemSelection = onToggleItemSelection,
            onCommitShapePosition = onCommitShapePosition,
            onCommitShapeScale = onCommitShapeScale,
            onCommitShapeDimensions = onCommitShapeDimensions,
            onCommitShapeRotation = onCommitShapeRotation,
            onSetShapeColorIndex = onSetShapeColorIndex,
            onBringShapeToFront = onBringShapeToFront,
            onSendShapeToBack = onSendShapeToBack,
            onDuplicateShape = onDuplicateShape,
            onDeleteShape = onDeleteShape,
            onToggleShapeWireframe = onToggleShapeWireframe,
            onUpdateShapeStrokeWidth = onUpdateShapeStrokeWidth,
            onUpdateOpacity = onUpdateOpacity,
            onUpdateShadowRadius = onUpdateShadowRadius,
            onUpdateBlurRadius = onUpdateBlurRadius,
            onUpdateTextContent = onUpdateTextContent,
            onCommitTextPosition = onCommitTextPosition,
            onCommitTextScale = onCommitTextScale,
            onCommitTextDimensions = onCommitTextDimensions,
            onCommitTextRotation = onCommitTextRotation,
            onSetTextColorIndex = onSetTextColorIndex,
            onBringTextToFront = onBringTextToFront,
            onSendTextToBack = onSendTextToBack,
            onDeleteText = onDeleteText,
            onUpdateTextOpacity = onUpdateTextOpacity,
            onUpdateTextShadowRadius = onUpdateTextShadowRadius,
            onUpdateTextTypographyStyle = onUpdateTextTypographyStyle,
            onToggleTextOutlineOnly = onToggleTextOutlineOnly,
            onUpdateTextStrokeWidth = onUpdateTextStrokeWidth,
            onUpdateTextGlowRadius = onUpdateTextGlowRadius,
            onCommitImagePosition = onCommitImagePosition,
            onCommitImageDimensions = onCommitImageDimensions,
            onCommitImageRotation = onCommitImageRotation,
            onCommitGroupTranslation = onCommitGroupTranslation,
            onCommitGroupScale = onCommitGroupScale,
            isFullscreen = isFullscreen,
            modifier = Modifier.fillMaxSize()
        )

        // Top Editing Toolbar (Only shown when an item is selected or multi-select is active)
        AnimatedVisibility(
            visible = isItemSelected,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = safeTopPadding, start = 16.dp, end = 16.dp)
                .zIndex(100f) // Ensure it always sits above the canvas
        ) {
            if (isMultiSelectMode && selectedItemIds.isNotEmpty()) {
                StudioMultiSelectToolbar(
                    selectedCount = selectedItemIds.size,
                    onSelectAll = onSelectAllItems,
                    onClearSelection = onClearSelection,
                    onBringGroupForward = onBringGroupForward,
                    onSendGroupToBack = onSendGroupToBack,
                    onDeleteGroup = onDeleteSelectedGroup,
                    onExitMultiSelect = onExitMultiSelect,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedShape != null) {
                StudioShapeToolbar(
                    shape = selectedShape,
                    palette = params.palette,
                    onColorSelected = { colorIdx -> onSetShapeColorIndex(selectedShape.id, colorIdx) },
                    onRotate = { deg -> onCommitShapeRotation(selectedShape.id, deg) },
                    onBringToFront = { onBringShapeToFront(selectedShape.id) },
                    onSendToBack = { onSendShapeToBack(selectedShape.id) },
                    onDuplicate = { onDuplicateShape(selectedShape.id) },
                    onDelete = { onDeleteShape(selectedShape.id) },
                    onToggleWireframe = { onToggleShapeWireframe(selectedShape.id) },
                    onUpdateStrokeWidth = { sw -> onUpdateShapeStrokeWidth(selectedShape.id, sw) },
                    onUpdateOpacity = { op -> onUpdateOpacity(selectedShape.id, op) },
                    onUpdateShadowRadius = { sr -> onUpdateShadowRadius(selectedShape.id, sr) },
                    onUpdateBlurRadius = { br -> onUpdateBlurRadius(selectedShape.id, br) },
                    onDeselect = { onSelectShape(null) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedText != null) {
                StudioTextToolbar(
                    textLayer = selectedText,
                    palette = params.palette,
                    onUpdateText = { str -> onUpdateTextContent(selectedText.id, str) },
                    onColorSelected = { cIdx -> onSetTextColorIndex(selectedText.id, cIdx) },
                    onRotate = { deg -> onCommitTextRotation(selectedText.id, deg) },
                    onBringToFront = { onBringTextToFront(selectedText.id) },
                    onSendToBack = { onSendTextToBack(selectedText.id) },
                    onDelete = { onDeleteText(selectedText.id) },
                    onUpdateOpacity = { op -> onUpdateTextOpacity(selectedText.id, op) },
                    onUpdateShadowRadius = { sr -> onUpdateTextShadowRadius(selectedText.id, sr) },
                    onDeselect = { onSelectTextLayer(null) },
                    onUpdateTypographyStyle = { style -> onUpdateTextTypographyStyle(selectedText.id, style) },
                    onImportCustomFont = { uri -> onImportCustomFont(selectedText.id, uri) },
                    onClearCustomFont = { onClearCustomFont(selectedText.id) },
                    onToggleOutlineOnly = { onToggleTextOutlineOnly(selectedText.id) },
                    onUpdateStrokeWidth = { sw -> onUpdateTextStrokeWidth(selectedText.id, sw) },
                    onUpdateGlowRadius = { gr -> onUpdateTextGlowRadius(selectedText.id, gr) },
                    onUpdateScaleX = { sx -> onCommitTextDimensions(selectedText.id, sx, selectedText.scaleY) },
                    onUpdateScaleY = { sy -> onCommitTextDimensions(selectedText.id, selectedText.scaleX, sy) },
                    onUpdateLetterSpacing = { sp -> onUpdateTextLetterSpacing(selectedText.id, sp) },
                    onUpdateLineHeight = { mult -> onUpdateTextLineHeight(selectedText.id, mult) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (selectedImage != null) {
                StudioImageToolbar(
                    imageLayer = selectedImage,
                    onUpdateShapeType = { shapeType -> onUpdateImageShapeType(selectedImage.id, shapeType) },
                    onRotate = { deltaDeg -> onCommitImageRotation(selectedImage.id, deltaDeg) },
                    onBringToFront = { onBringImageToFront(selectedImage.id) },
                    onSendToBack = { onSendImageToBack(selectedImage.id) },
                    onDuplicate = { onDuplicateImage(selectedImage.id) },
                    onDelete = { onDeleteImage(selectedImage.id) },
                    onToggleLockProportions = { onToggleImageLockProportions(selectedImage.id) },
                    onUpdateOpacity = { op -> onUpdateImageOpacity(selectedImage.id, op) },
                    onUpdateShadowRadius = { sr -> onUpdateImageShadowRadius(selectedImage.id, sr) },
                    onDeselect = { onSelectImageLayer(null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
