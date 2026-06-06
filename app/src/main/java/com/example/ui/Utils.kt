package com.example.ui

import android.content.Context
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
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = "Eingefügtes Bild",
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

data class ImageTransform(
    val scale: Float = 1.0f,
    val offsetX: Float = 0.0f,
    val offsetY: Float = 0.0f,
    val rotation: Float = 0.0f,
    val contentScale: String = "Crop", // "Crop", "Fit"
    val colorFilter: String = "None" // "None", "Grayscale", "Sepia", "Revolution Red", "Invert"
)

fun saveImageTransform(context: android.content.Context, key: String, transform: ImageTransform) {
    val prefs = context.getSharedPreferences("image_transforms", android.content.Context.MODE_PRIVATE)
    prefs.edit().apply {
        putFloat("${key}_scale", transform.scale)
        putFloat("${key}_offsetX", transform.offsetX)
        putFloat("${key}_offsetY", transform.offsetY)
        putFloat("${key}_rotation", transform.rotation)
        putString("${key}_contentScale", transform.contentScale)
        putString("${key}_colorFilter", transform.colorFilter)
        apply()
    }
}

fun loadImageTransform(context: android.content.Context, key: String): ImageTransform {
    val prefs = context.getSharedPreferences("image_transforms", android.content.Context.MODE_PRIVATE)
    return ImageTransform(
        scale = prefs.getFloat("${key}_scale", 1.0f),
        offsetX = prefs.getFloat("${key}_offsetX", 0.0f),
        offsetY = prefs.getFloat("${key}_offsetY", 0.0f),
        rotation = prefs.getFloat("${key}_rotation", 0.0f),
        contentScale = prefs.getString("${key}_contentScale", "Crop") ?: "Crop",
        colorFilter = prefs.getString("${key}_colorFilter", "None") ?: "None"
    )
}

@Composable
fun TransformableLocalImage(
    path: String,
    transformKey: String,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var transform by remember(transformKey) {
        mutableStateOf(loadImageTransform(context, transformKey))
    }
    var showEditor by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
    ) {
        val bitmap = remember(path) {
            try {
                android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
        
        if (bitmap != null) {
            val colorMatrix = remember(transform.colorFilter) {
                when (transform.colorFilter) {
                    "Grayscale" -> {
                        val m = ColorMatrix()
                        m.setToSaturation(0f)
                        m
                    }
                    "Sepia" -> {
                        ColorMatrix(floatArrayOf(
                            0.393f, 0.769f, 0.189f, 0f, 0f,
                            0.349f, 0.686f, 0.168f, 0f, 0f,
                            0.272f, 0.534f, 0.131f, 0f, 0f,
                            0f,     0f,     0f,     1f, 0f
                        ))
                    }
                    "Revolution Red" -> {
                        ColorMatrix(floatArrayOf(
                            1.3f, 0.1f, 0.1f, 0f, 0f,
                            0.2f, 0.8f, 0.1f, 0f, 0f,
                            0.1f, 0.1f, 0.5f, 0f, 0f,
                            0f,   0f,   0f,   1f, 0f
                        ))
                    }
                    "Invert" -> {
                        ColorMatrix(floatArrayOf(
                            -1f, 0f,  0f,  0f, 255f,
                            0f,  -1f, 0f,  0f, 255f,
                            0f,  0f,  -1f, 0f, 255f,
                            0f,  0f,  0f,  1f, 0f
                        ))
                    }
                    else -> ColorMatrix()
                }
            }

            val resolvedScale = if (transform.contentScale == "Crop") {
                androidx.compose.ui.layout.ContentScale.Crop
            } else {
                androidx.compose.ui.layout.ContentScale.Fit
            }

            androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = "Eingefügtes Bild",
                colorFilter = if (transform.colorFilter != "None") ColorFilter.colorMatrix(colorMatrix) else null,
                contentScale = resolvedScale,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = transform.scale,
                        scaleY = transform.scale,
                        translationX = transform.offsetX,
                        translationY = transform.offsetY,
                        rotationZ = transform.rotation
                    )
            )

            // Edit Overlay Button
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { showEditor = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Bild bearbeiten ⚡",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showEditor) {
        ImageTransformDialog(
            path = path,
            initialTransform = transform,
            onDismiss = { showEditor = false },
            onSave = { updatedTransform ->
                saveImageTransform(context, transformKey, updatedTransform)
                transform = updatedTransform
                showEditor = false
            }
        )
    }
}

@Composable
fun ImageTransformDialog(
    path: String,
    initialTransform: ImageTransform,
    onDismiss: () -> Unit,
    onSave: (ImageTransform) -> Unit
) {
    var scale by remember { mutableStateOf(initialTransform.scale) }
    var offsetX by remember { mutableStateOf(initialTransform.offsetX) }
    var offsetY by remember { mutableStateOf(initialTransform.offsetY) }
    var rotation by remember { mutableStateOf(initialTransform.rotation) }
    var contentScaleState by remember { mutableStateOf(initialTransform.contentScale) }
    var colorFilterState by remember { mutableStateOf(initialTransform.colorFilter) }

    val bitmap = remember(path) {
        try {
            android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ImmersiveBackground.copy(alpha = 0.95f))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Abbrechen", color = ImmersiveTextSecondary, fontSize = 16.sp)
                        }
                        Text(
                            "Bild anpassen",
                            color = ImmersiveTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        TextButton(
                            onClick = {
                                onSave(
                                    ImageTransform(
                                        scale = scale,
                                        offsetX = offsetX,
                                        offsetY = offsetY,
                                        rotation = rotation,
                                        contentScale = contentScaleState,
                                        colorFilter = colorFilterState
                                    )
                                )
                            }
                        ) {
                            Text("Speichern", color = ImmersiveGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Text(
                        "Nutze Gesten zum Ziehen, Zoomen und Drehen direkt auf dem Bild.",
                        color = ImmersiveTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Gesture Workspace Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotate ->
                                    scale = (scale * zoom).coerceIn(0.5f, 6.0f)
                                    rotation = (rotation + rotate) % 360f
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val colorMatrix = remember(colorFilterState) {
                            when (colorFilterState) {
                                "Grayscale" -> {
                                    val m = ColorMatrix()
                                    m.setToSaturation(0f)
                                    m
                                }
                                "Sepia" -> {
                                    ColorMatrix(floatArrayOf(
                                        0.393f, 0.769f, 0.189f, 0f, 0f,
                                        0.349f, 0.686f, 0.168f, 0f, 0f,
                                        0.272f, 0.534f, 0.131f, 0f, 0f,
                                        0f,     0f,     0f,     1f, 0f
                                    ))
                                }
                                "Revolution Red" -> {
                                    ColorMatrix(floatArrayOf(
                                        1.3f, 0.1f, 0.1f, 0f, 0f,
                                        0.2f, 0.8f, 0.1f, 0f, 0f,
                                        0.1f, 0.1f, 0.5f, 0f, 0f,
                                        0f,   0f,   0f,   1f, 0f
                                    ))
                                }
                                "Invert" -> {
                                    ColorMatrix(floatArrayOf(
                                        -1f, 0f,  0f,  0f, 255f,
                                        0f,  -1f, 0f,  0f, 255f,
                                        0f,  0f,  -1f, 0f, 255f,
                                        0f,  0f,  0f,  1f, 0f
                                    ))
                                }
                                else -> ColorMatrix()
                            }
                        }

                        val resolvedScale = if (contentScaleState == "Crop") {
                            androidx.compose.ui.layout.ContentScale.Crop
                        } else {
                            androidx.compose.ui.layout.ContentScale.Fit
                        }

                        androidx.compose.foundation.Image(
                            bitmap = bitmap,
                            contentDescription = "Fokusiertes Bild",
                            colorFilter = if (colorFilterState != "None") ColorFilter.colorMatrix(colorMatrix) else null,
                            contentScale = resolvedScale,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY,
                                    rotationZ = rotation
                                )
                        )
                    }

                    // Sliders and controls detail panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ImmersiveSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Reset Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Transformationen", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Button(
                                onClick = {
                                    scale = 1.0f
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotation = 0f
                                    contentScaleState = "Crop"
                                    colorFilterState = "None"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CreamRed),
                                shape = RoundedCornerShape(percent = 50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Zurücksetzen", color = ImmersiveGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Zoom Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Zoom (${String.format(java.util.Locale.US, "%.1fx", scale)})", color = ImmersiveTextSecondary, fontSize = 12.sp)
                            }
                            Slider(
                                value = scale,
                                onValueChange = { scale = it },
                                valueRange = 0.5f..5.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = ImmersiveGreen,
                                    activeTrackColor = ImmersiveGreen,
                                    inactiveTrackColor = ImmersiveBorder
                                )
                            )
                        }

                        // Rotation Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Drehung (${rotation.toInt()}°)", color = ImmersiveTextSecondary, fontSize = 12.sp)
                            }
                            Slider(
                                value = rotation,
                                onValueChange = { rotation = it },
                                valueRange = 0f..360f,
                                colors = SliderDefaults.colors(
                                    thumbColor = ImmersiveGreen,
                                    activeTrackColor = ImmersiveGreen,
                                    inactiveTrackColor = ImmersiveBorder
                                )
                            )
                        }

                        // Content Scale Choice
                        Column {
                            Text("Bild-Anpassung", color = ImmersiveTextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val option1Selected = contentScaleState == "Crop"
                                val option2Selected = contentScaleState == "Fit"
                                
                                Button(
                                    onClick = { contentScaleState = "Crop" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (option1Selected) ImmersiveGreen else ImmersivePillBg
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Ausfüllen", color = if (option1Selected) Color.White else ImmersiveTextSecondary, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { contentScaleState = "Fit" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (option2Selected) ImmersiveGreen else ImmersivePillBg
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Einpassen", color = if (option2Selected) Color.White else ImmersiveTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        // Filters Scrollable Row
                        Column {
                            Text("Farbfilter & Bildstil", color = ImmersiveTextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val filters = listOf("None" to "Normal", "Grayscale" to "S/W", "Sepia" to "Sepia", "Revolution Red" to "Rote Welle", "Invert" to "Negativ")
                                items(filters.size) { idx ->
                                    val (filterId, label) = filters[idx]
                                    val selected = colorFilterState == filterId
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) ImmersiveGreen else ImmersivePillBg)
                                            .border(1.dp, if (selected) Color.White.copy(alpha = 0.3f) else ImmersiveBorder, RoundedCornerShape(8.dp))
                                            .clickable { colorFilterState = filterId }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(label, color = if (selected) Color.White else ImmersiveTextPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
