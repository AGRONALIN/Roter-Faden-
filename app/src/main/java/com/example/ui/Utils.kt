package com.example.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*

fun Modifier.verticalFadingEdge(
    topEdge: Dp = 0.dp,
    bottomEdge: Dp = 0.dp
) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val height = size.height
        if (height <= 0f) return@drawWithContent
        
        val topPx = topEdge.toPx().coerceAtMost(height / 2f)
        val bottomPx = bottomEdge.toPx().coerceAtMost(height / 2f)
        
        if (topPx > 0f && bottomPx > 0f) {
            val stop1 = topPx / height
            val stop2 = 1f - (bottomPx / height)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    stop1 to Color.Black,
                    stop2 to Color.Black,
                    1f to Color.Transparent,
                    startY = 0f,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        } else if (topPx > 0f) {
            val stop1 = topPx / height
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    stop1 to Color.Black,
                    1f to Color.Black,
                    startY = 0f,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        } else if (bottomPx > 0f) {
            val stop2 = 1f - (bottomPx / height)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Black,
                    stop2 to Color.Black,
                    1f to Color.Transparent,
                    startY = 0f,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        } else {
            drawRect(
                color = Color.Black,
                blendMode = BlendMode.DstIn
            )
        }
    }

fun buildAutoLinkedText(
    text: String,
    glossaryItems: List<com.example.data.GlossaryItem>,
    literatureItems: List<com.example.data.LiteratureItem>,
    highlightColor: Color,
    literatureColor: Color = Color(0xFF2E7D32),
    navController: androidx.navigation.NavController
): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        append(text)
        
        data class MatchRange(val start: Int, val end: Int, val route: String, val color: Color)
        val matches = mutableListOf<MatchRange>()
        
        val sortedLit = literatureItems.sortedByDescending { it.title.length }
        val sortedGlossary = glossaryItems.sortedByDescending { it.term.length }
        
        for (lit in sortedLit) {
            val title = lit.title
            if (title.isBlank()) continue
            var startIndex = text.indexOf(title, ignoreCase = true)
            while (startIndex >= 0) {
                val endIndex = startIndex + title.length
                val hasOverlap = matches.any { 
                    (startIndex >= it.start && startIndex < it.end) || 
                    (endIndex > it.start && endIndex <= it.end) ||
                    (it.start >= startIndex && it.start < endIndex)
                }
                if (!hasOverlap) {
                    matches.add(MatchRange(startIndex, endIndex, "literature_detail/${lit.id}?source=text", literatureColor))
                }
                startIndex = text.indexOf(title, startIndex + title.length, ignoreCase = true)
            }
        }
        
        for (glossary in sortedGlossary) {
            val term = glossary.term
            if (term.isBlank()) continue
            var startIndex = text.indexOf(term, ignoreCase = true)
            while (startIndex >= 0) {
                val endIndex = startIndex + term.length
                val hasOverlap = matches.any { 
                    (startIndex >= it.start && startIndex < it.end) || 
                    (endIndex > it.start && endIndex <= it.end) ||
                    (it.start >= startIndex && it.start < endIndex)
                }
                if (!hasOverlap) {
                    matches.add(MatchRange(startIndex, endIndex, "glossary_detail/${glossary.id}?source=text", highlightColor))
                }
                startIndex = text.indexOf(term, startIndex + term.length, ignoreCase = true)
            }
        }
        
        for (match in matches) {
            addStyle(
                style = androidx.compose.ui.text.SpanStyle(
                    color = match.color,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                start = match.start,
                end = match.end
            )
            addLink(
                androidx.compose.ui.text.LinkAnnotation.Clickable(match.route) {
                    navController.navigate(match.route)
                },
                start = match.start,
                end = match.end
            )
        }
    }
}

fun copyUriToInternalStorage(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val outputFile = java.io.File(context.filesDir, fileName)
        outputFile.outputStream().use { outputStream ->
            inputStream.use { it.copyTo(outputStream) }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun LocalImageFromPath(path: String, modifier: Modifier) {
    val bitmap = remember(path) {
        try {
            android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    if (bitmap != null) {
        val aspectRatio = remember(bitmap) {
            bitmap.width.toFloat() / bitmap.height.toFloat()
        }
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = "Eingefügtes Bild",
            modifier = modifier.aspectRatio(aspectRatio),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}

sealed class RichTextPart {
    data class Text(val content: String) : RichTextPart()
    data class Image(val path: String) : RichTextPart()
}

fun parseRichText(text: String): List<RichTextPart> {
    val regex = "\\[image:([^\\]]+)\\]".toRegex()
    val parts = mutableListOf<RichTextPart>()
    var lastIndex = 0
    
    regex.findAll(text).forEach { matchResult ->
        val matchStart = matchResult.range.first
        val matchEnd = matchResult.range.last + 1
        val imagePath = matchResult.groupValues[1]
        
        if (matchStart > lastIndex) {
            parts.add(RichTextPart.Text(text.substring(lastIndex, matchStart)))
        }
        parts.add(RichTextPart.Image(imagePath))
        lastIndex = matchEnd
    }
    
    if (lastIndex < text.length) {
        parts.add(RichTextPart.Text(text.substring(lastIndex)))
    }
    
    return parts
}

@Composable
fun RichTextWithImages(
    text: String,
    glossaryItems: List<com.example.data.GlossaryItem>,
    literatureItems: List<com.example.data.LiteratureItem>,
    highlightColor: Color,
    literatureColor: Color,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge.copy(color = ImmersiveTextPrimary)
) {
    val parts = remember(text) { parseRichText(text) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        parts.forEach { part ->
            when (part) {
                is RichTextPart.Text -> {
                    val trimmedText = part.content
                    if (trimmedText.isNotBlank() || trimmedText.contains('\n')) {
                        val annotatedText = remember(trimmedText, glossaryItems, literatureItems, highlightColor, literatureColor) {
                            buildAutoLinkedText(
                                text = trimmedText,
                                glossaryItems = glossaryItems,
                                literatureItems = literatureItems,
                                highlightColor = highlightColor,
                                literatureColor = literatureColor,
                                navController = navController
                            )
                        }
                        Text(
                            text = annotatedText,
                            style = textStyle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is RichTextPart.Image -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
                    ) {
                        LocalImageFromPath(
                            path = part.path,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }
            }
        }
    }
}

sealed class EditorBlock {
    data class Text(val id: String, var content: String) : EditorBlock()
    data class Image(val id: String, val path: String) : EditorBlock()
}

fun textToBlocks(text: String): List<EditorBlock> {
    val regex = "\\[image:([^\\]]+)\\]".toRegex()
    val blocks = mutableListOf<EditorBlock>()
    var lastIndex = 0
    var idCounter = 0
    
    regex.findAll(text).forEach { matchResult ->
        val matchStart = matchResult.range.first
        val matchEnd = matchResult.range.last + 1
        val imagePath = matchResult.groupValues[1]
        
        if (matchStart > lastIndex) {
            blocks.add(EditorBlock.Text("text_${idCounter++}", text.substring(lastIndex, matchStart)))
        }
        blocks.add(EditorBlock.Image("image_${idCounter++}", imagePath))
        lastIndex = matchEnd
    }
    
    if (lastIndex < text.length) {
        blocks.add(EditorBlock.Text("text_${idCounter++}", text.substring(lastIndex)))
    }
    
    if (blocks.isEmpty()) {
        blocks.add(EditorBlock.Text("text_${idCounter++}", ""))
    }
    
    return blocks
}

fun blocksToText(blocks: List<EditorBlock>): String {
    return buildString {
        blocks.forEach { block ->
            when (block) {
                is EditorBlock.Text -> append(block.content)
                is EditorBlock.Image -> append("[image:${block.path}]")
            }
        }
    }
}

fun cleanAndMinimizeBlocks(blocks: List<EditorBlock>): List<EditorBlock> {
    if (blocks.isEmpty()) return listOf(EditorBlock.Text("text_0", ""))
    val cleaned = mutableListOf<EditorBlock>()
    var currentText = StringBuilder()
    var textCount = 0
    
    for (block in blocks) {
        when (block) {
            is EditorBlock.Text -> {
                currentText.append(block.content)
            }
            is EditorBlock.Image -> {
                if (currentText.isNotEmpty()) {
                    cleaned.add(EditorBlock.Text("text_${textCount++}", currentText.toString()))
                    currentText = StringBuilder()
                }
                cleaned.add(block)
            }
        }
    }
    if (currentText.isNotEmpty() || cleaned.isEmpty()) {
        cleaned.add(EditorBlock.Text("text_${textCount++}", currentText.toString()))
    }
    return cleaned
}

fun moveBlock(blocks: List<EditorBlock>, index: Int, direction: Int): List<EditorBlock> {
    val newIndex = index + direction
    if (newIndex < 0 || newIndex >= blocks.size) return blocks
    val mutable = blocks.toMutableList()
    val temp = mutable[index]
    mutable[index] = mutable[newIndex]
    mutable[newIndex] = temp
    return mutable
}

@Composable
fun InlineRichTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    
    var lastSentValue by remember { mutableStateOf(value) }
    var blocks by remember {
        mutableStateOf(textToBlocks(value))
    }
    
    if (value != lastSentValue) {
        blocks = textToBlocks(value)
        lastSentValue = value
    }
    
    val updateParent = { newBlocks: List<EditorBlock> ->
        val clean = cleanAndMinimizeBlocks(newBlocks)
        blocks = clean
        val outText = blocksToText(clean)
        lastSentValue = outText
        onValueChange(outText)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is EditorBlock.Text -> {
                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let {
                            val copiedPath = copyUriToInternalStorage(context, it)
                            if (copiedPath != null) {
                                val newBlocks = blocks.toMutableList()
                                newBlocks.add(index + 1, EditorBlock.Image("img_${System.currentTimeMillis()}", copiedPath))
                                updateParent(newBlocks)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = block.content,
                        onValueChange = { newText ->
                            val newBlocks = blocks.toMutableList()
                            newBlocks[index] = EditorBlock.Text(block.id, newText)
                            updateParent(newBlocks)
                        },
                        label = { Text(placeholder) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = if (blocks.size == 1) 4 else 2,
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = ImmersiveTextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImmersiveGreen,
                            focusedLabelColor = ImmersiveGreen,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedContainerColor = ImmersiveSurface,
                            unfocusedContainerColor = ImmersiveSurface,
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            ) {
                                Text("🖼️", fontSize = 16.sp)
                            }
                        }
                    )
                }
                is EditorBlock.Image -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
                            .background(ImmersiveSurface)
                    ) {
                        LocalImageFromPath(
                            path = block.path,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                        
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (index > 0) {
                                Box(modifier = Modifier.clickable {
                                    val newBlocks = moveBlock(blocks, index, -1)
                                    updateParent(newBlocks)
                                }) {
                                    Text("⬆️", fontSize = 14.sp)
                                }
                            }
                            if (index < blocks.lastIndex) {
                                Box(modifier = Modifier.clickable {
                                    val newBlocks = moveBlock(blocks, index, 1)
                                    updateParent(newBlocks)
                                }) {
                                    Text("⬇️", fontSize = 14.sp)
                                }
                            }
                            Box(modifier = Modifier.clickable {
                                val newBlocks = blocks.toMutableList()
                                newBlocks.removeAt(index)
                                updateParent(newBlocks)
                            }) {
                                Text("❌", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
