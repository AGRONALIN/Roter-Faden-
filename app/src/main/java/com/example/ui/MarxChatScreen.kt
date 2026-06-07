package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.example.data.ChatMessage
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalDensity

class ComicSpeechBubbleShape(
    private val cornerRadius: Dp = 16.dp,
    private val arrowWidth: Dp = 20.dp,
    private val arrowHeight: Dp = 14.dp,
    private val isLeftArrow: Boolean = true
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { cornerRadius.toPx() }
        val arrowWidthPx = with(density) { arrowWidth.toPx() }
        val arrowHeightPx = with(density) { arrowHeight.toPx() }
        
        val path = Path().apply {
            // Rounded rectangle body of speech bubble
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height - arrowHeightPx,
                    cornerRadius = CornerRadius(radiusPx)
                )
            )
            // Speech tail pointing down at who is speaking
            val arrowX = if (isLeftArrow) size.width * 0.25f else size.width * 0.75f
            moveTo(arrowX - arrowWidthPx / 2f, size.height - arrowHeightPx)
            if (isLeftArrow) {
                lineTo(arrowX - 6f, size.height) // tilted for comic character
            } else {
                lineTo(arrowX + 6f, size.height)
            }
            lineTo(arrowX + arrowWidthPx / 2f, size.height - arrowHeightPx)
            close()
        }
        return Outline.Generic(path)
    }
}

// Procedural Retro Sound Synthesizer for Animal Crossing voices
fun playProceduralGrumbel(pitchHz: Float, isMuted: Boolean) {
    if (isMuted) return
    // Simple light tone synthesizer playing via android.media.AudioTrack
    try {
        val thread = Thread {
            try {
                val sampleRate = 8000
                val numSamples = 200 // crisp brief beeps
                val sample = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / pitchHz)
                    // Synthesize smooth clean bell-like wave to avoid clicking
                    sample[i] = (Math.sin(angle) * Short.MAX_VALUE * 0.25f).toInt().toShort()
                }
                val audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    android.media.AudioTrack.MODE_STATIC
                )
                audioTrack.write(sample, 0, numSamples)
                audioTrack.play()
                Thread.sleep(80)
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore audio-track failures gracefully to ensure total app stability
            }
        }
        thread.start()
    } catch (e: Exception) {
        // Fallback
    }
}

// 3 Custom Styled Mii Busts
@Composable
fun EngelsLowPolyBust(
    modifier: Modifier = Modifier,
    translationY: Float = 0f,
    rotationZ: Float = 0f,
    eyebrowOffset: Float = 0f,
    mouthOffset: Float = 0f
) {
    Canvas(
        modifier = modifier
            .graphicsLayer {
                this.translationY = translationY
                this.rotationZ = rotationZ
            }
    ) {
        val w = size.width
        val h = size.height

        val suitColor = Color(0xFF4E342E) // Victorian fine brown tweed
        val vestColor = Color(0xFF3E2723)
        val suitPath = Path().apply {
            moveTo(w * 0.28f, h * 0.95f)
            lineTo(w * 0.32f, h * 0.76f)
            quadraticTo(w * 0.5f, h * 0.72f, w * 0.68f, h * 0.76f)
            lineTo(w * 0.72f, h * 0.95f)
            close()
        }
        drawPath(suitPath, suitColor)

        val vestPath = Path().apply {
            moveTo(w * 0.44f, h * 0.76f)
            lineTo(w * 0.56f, h * 0.76f)
            lineTo(w * 0.5f, h * 0.90f)
            close()
        }
        drawPath(vestPath, vestColor)

        val skinColor = Color(0xFFFFD180)
        drawOval(
            color = skinColor,
            topLeft = Offset(w * 0.28f, h * 0.26f),
            size = Size(w * 0.44f, h * 0.38f)
        )

        val hairColor = Color(0xFF7D5F53)
        drawOval(
            color = hairColor,
            topLeft = Offset(w * 0.27f, h * 0.17f),
            size = Size(w * 0.46f, h * 0.17f)
        )

        val beardY = mouthOffset * 0.35f
        val engelsBeardPath = Path().apply {
            moveTo(w * 0.30f, h * 0.51f + beardY)
            lineTo(w * 0.38f, h * 0.88f + beardY)
            lineTo(w * 0.62f, h * 0.88f + beardY)
            lineTo(w * 0.70f, h * 0.51f + beardY)
            close()
        }
        drawPath(engelsBeardPath, Color(0xFF8D6E63))
        drawCircle(color = Color(0xFF78909C), radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.71f + beardY))

        drawCircle(color = Color(0xFF263238), radius = w * 0.024f, center = Offset(w * 0.41f, h * 0.36f))
        drawCircle(color = Color(0xFF263238), radius = w * 0.024f, center = Offset(w * 0.59f, h * 0.36f))

        val eyLeanLeft = h * 0.26f + eyebrowOffset * 0.15f
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(w * 0.34f, eyLeanLeft),
            size = Size(w * 0.12f, h * 0.035f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(w * 0.54f, eyLeanLeft),
            size = Size(w * 0.12f, h * 0.035f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        drawCircle(
            color = Color(0xFFFFB74D),
            radius = w * 0.04f,
            center = Offset(w * 0.5f, h * 0.43f)
        )

        drawRoundRect(
            color = Color(0xFF8D6E63),
            topLeft = Offset(w * 0.34f, h * 0.48f + beardY * 0.8f),
            size = Size(w * 0.17f, h * 0.07f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        drawRoundRect(
            color = Color(0xFF7D5F53),
            topLeft = Offset(w * 0.49f, h * 0.48f + beardY * 0.8f),
            size = Size(w * 0.17f, h * 0.07f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        val enMouthY = h * 0.55f
        if (mouthOffset > 0.1f) {
            drawCircle(
                color = Color(0xFF3E2723),
                radius = w * 0.03f + mouthOffset * 0.25f,
                center = Offset(w * 0.5f, enMouthY)
            )
        } else {
            val smilePath = Path().apply {
                moveTo(w * 0.46f, enMouthY)
                quadraticTo(w * 0.5f, enMouthY + h * 0.03f, w * 0.54f, enMouthY)
            }
            drawPath(
                path = smilePath,
                color = Color(0xFF2D1A1A),
                style = Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

@Composable
fun LeninLowPolyBust(
    modifier: Modifier = Modifier,
    translationY: Float = 0f,
    rotationZ: Float = 0f,
    eyebrowOffset: Float = 0f,
    mouthOffset: Float = 0f
) {
    Canvas(
        modifier = modifier
            .graphicsLayer {
                this.translationY = translationY
                this.rotationZ = rotationZ
            }
    ) {
        val w = size.width
        val h = size.height

        val coatColor = Color(0xFF212121)
        val vestColor = Color(0xFFB01212)
        val coatPath = Path().apply {
            moveTo(w * 0.28f, h * 0.95f)
            lineTo(w * 0.32f, h * 0.76f)
            quadraticTo(w * 0.5f, h * 0.72f, w * 0.68f, h * 0.76f)
            lineTo(w * 0.72f, h * 0.95f)
            close()
        }
        drawPath(coatPath, coatColor)

        val vestPath = Path().apply {
            moveTo(w * 0.44f, h * 0.76f)
            lineTo(w * 0.56f, h * 0.76f)
            lineTo(w * 0.5f, h * 0.88f)
            close()
        }
        drawPath(vestPath, vestColor)

        val skinColor = Color(0xFFFFD180)
        drawOval(
            color = skinColor,
            topLeft = Offset(w * 0.28f, h * 0.26f),
            size = Size(w * 0.44f, h * 0.37f)
        )

        val capColor = Color(0xFF37474F)
        val capBrim = Color(0xFF212121)
        val capPath = Path().apply {
            moveTo(w * 0.24f, h * 0.29f)
            cubicTo(w * 0.23f, h * 0.12f, w * 0.77f, h * 0.12f, w * 0.76f, h * 0.29f)
            close()
        }
        drawPath(capPath, capColor)
        
        val capBrimPath = Path().apply {
            moveTo(w * 0.25f, h * 0.29f)
            lineTo(w * 0.75f, h * 0.29f)
            quadraticTo(w * 0.5f, h * 0.35f, w * 0.25f, h * 0.29f)
        }
        drawPath(capBrimPath, capBrim)

        drawCircle(color = Color.Black, radius = w * 0.024f, center = Offset(w * 0.41f, h * 0.42f))
        drawCircle(color = Color.Black, radius = w * 0.024f, center = Offset(w * 0.59f, h * 0.42f))

        val eyLenY = h * 0.35f + eyebrowOffset * 0.12f
        val leftEyebrow = Path().apply {
            moveTo(w * 0.33f, eyLenY + h * 0.02f)
            lineTo(w * 0.47f, eyLenY)
            lineTo(w * 0.45f, eyLenY - h * 0.03f)
            lineTo(w * 0.31f, eyLenY - h * 0.01f)
            close()
        }
        drawPath(leftEyebrow, Color(0xFF3E2723))

        val rightEyebrow = Path().apply {
            moveTo(w * 0.67f, eyLenY + h * 0.02f)
            lineTo(w * 0.53f, eyLenY)
            lineTo(w * 0.55f, eyLenY - h * 0.03f)
            lineTo(w * 0.69f, eyLenY - h * 0.01f)
            close()
        }
        drawPath(rightEyebrow, Color(0xFF3E2723))

        drawCircle(
            color = Color(0xFFFFB74D),
            radius = w * 0.038f,
            center = Offset(w * 0.5f, h * 0.49f)
        )

        val beardY = mouthOffset * 0.4f
        val goateePath = Path().apply {
            moveTo(w * 0.41f, h * 0.57f + beardY)
            lineTo(w * 0.5f, h * 0.74f + beardY)
            lineTo(w * 0.59f, h * 0.57f + beardY)
            close()
        }
        drawPath(goateePath, Color(0xFFBF360C))
        
        drawRoundRect(
            color = Color(0xFFD84315),
            topLeft = Offset(w * 0.38f, h * 0.54f + beardY * 0.8f),
            size = Size(w * 0.11f, h * 0.05f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
        )
        drawRoundRect(
            color = Color(0xFFBF360C),
            topLeft = Offset(w * 0.51f, h * 0.54f + beardY * 0.8f),
            size = Size(w * 0.11f, h * 0.05f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
        )

        val lenMouthY = h * 0.59f
        if (mouthOffset > 0.1f) {
            drawCircle(
                color = Color(0xFF3E2723),
                radius = w * 0.035f + mouthOffset * 0.3f,
                center = Offset(w * 0.5f, lenMouthY)
            )
        } else {
            val smilePath = Path().apply {
                moveTo(w * 0.47f, lenMouthY)
                quadraticTo(w * 0.5f, lenMouthY + 6f, w * 0.53f, lenMouthY)
            }
            drawPath(smilePath, Color(0xFF2D1A1A), style = Stroke(width = 4f))
        }
    }
}

@Composable
fun LuxemburgLowPolyBust(
    modifier: Modifier = Modifier,
    translationY: Float = 0f,
    rotationZ: Float = 0f,
    eyebrowOffset: Float = 0f,
    mouthOffset: Float = 0f
) {
    Canvas(
        modifier = modifier
            .graphicsLayer {
                this.translationY = translationY
                this.rotationZ = rotationZ
            }
    ) {
        val w = size.width
        val h = size.height

        val dressColor = Color(0xFF311B92)
        val collarColor = Color(0xFFECEFF1)
        val cameoRuby = Color(0xFFC62828)
        
        val shoulders = Path().apply {
            moveTo(w * 0.28f, h * 0.95f)
            lineTo(w * 0.32f, h * 0.78f)
            quadraticTo(w * 0.5f, h * 0.74f, w * 0.68f, h * 0.78f)
            lineTo(w * 0.72f, h * 0.95f)
            close()
        }
        drawPath(shoulders, dressColor)

        val collarPath = Path().apply {
            moveTo(w * 0.45f, h * 0.78f)
            lineTo(w * 0.5f, h * 0.70f)
            lineTo(w * 0.55f, h * 0.78f)
            close()
        }
        drawPath(collarPath, collarColor)
        drawCircle(color = cameoRuby, radius = w * 0.03f, center = Offset(w * 0.5f, h * 0.81f))

        val skinColor = Color(0xFFFFD180)
        drawOval(
            color = skinColor,
            topLeft = Offset(w * 0.29f, h * 0.28f),
            size = Size(w * 0.42f, h * 0.38f)
        )

        val hairColor = Color(0xFF263238)
        val activeRose = Color(0xFFD50000)
        
        drawOval(
            color = hairColor,
            topLeft = Offset(w * 0.25f, h * 0.16f),
            size = Size(w * 0.5f, h * 0.20f)
        )
        drawCircle(color = hairColor, radius = w * 0.12f, center = Offset(w * 0.30f, h * 0.34f))
        drawCircle(color = hairColor, radius = w * 0.12f, center = Offset(w * 0.70f, h * 0.34f))
        drawCircle(color = hairColor, radius = w * 0.09f, center = Offset(w * 0.50f, h * 0.13f))
        drawCircle(color = activeRose, radius = w * 0.038f, center = Offset(w * 0.62f, h * 0.17f))

        val leftEye = Offset(w * 0.42f, h * 0.41f)
        val rightEye = Offset(w * 0.58f, h * 0.41f)
        drawCircle(color = Color(0xFF3E2723), radius = w * 0.028f, center = leftEye)
        drawCircle(color = Color(0xFF3E2723), radius = w * 0.028f, center = rightEye)
        drawLine(color = Color.Black, strokeWidth = 3f, start = leftEye + Offset(-w*0.04f, -h*0.02f), end = leftEye + Offset(w*0.04f, -h*0.02f))
        drawLine(color = Color.Black, strokeWidth = 3f, start = rightEye + Offset(-w*0.04f, -h*0.02f), end = rightEye + Offset(w*0.04f, -h*0.02f))

        val eyRosY = h * 0.33f + eyebrowOffset * 0.1f
        val eyebrowLeft = Path().apply {
            moveTo(w * 0.35f, eyRosY)
            quadraticTo(w * 0.42f, eyRosY - 4f, w * 0.48f, eyRosY)
        }
        drawPath(eyebrowLeft, Color.Black, style = Stroke(width = 3f))

        val eyebrowRight = Path().apply {
            moveTo(w * 0.52f, eyRosY)
            quadraticTo(w * 0.58f, eyRosY - 4f, w * 0.65f, eyRosY)
        }
        drawPath(eyebrowRight, Color.Black, style = Stroke(width = 3f))

        drawCircle(
            color = Color(0xFFFFB74D),
            radius = w * 0.035f,
            center = Offset(w * 0.5f, h * 0.48f)
        )

        val rosaMouthY = h * 0.57f
        if (mouthOffset > 0.1f) {
            drawCircle(
                color = Color(0xFF3E2723),
                radius = w * 0.035f + mouthOffset * 0.28f,
                center = Offset(w * 0.5f, rosaMouthY)
            )
            drawCircle(
                color = Color(0xFFE57373),
                radius = w * 0.016f + mouthOffset * 0.14f,
                center = Offset(w * 0.50f, rosaMouthY + 3f)
            )
        } else {
            val smilePath = Path().apply {
                moveTo(w * 0.44f, rosaMouthY)
                quadraticTo(w * 0.5f, rosaMouthY + h * 0.04f, w * 0.56f, rosaMouthY)
            }
            drawPath(smilePath, Color(0xFFC2185B), style = Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
    }
}

data class HistoricalBook(
    val id: String,
    val title: String,
    val author: String,
    val coverColor: Color,
    val description: String,
    val pages: List<String>,
    val relX: Float,
    val relY: Float,
    val room: String
)

enum class QuestObjective(val title: String, val description: String) {
    START("Willkommen, Genosse!", "Bewege dich durch Tippen auf den Bildschirm."),
    READ_MANIFEST("Erforscher der Ideen", "Finde und lies 'Das Kommunistische Manifest' im Hauptsaal."),
    TALK_MARX("Der Theoretiker", "Sprich mit Karl Marx. (Aktions-Knopf)"),
    FIND_ENGELS("Der Beobachter", "Gehe durch die LINKE TÜR in Engels' Büro."),
    READ_LAGE("Die Lage", "Lies Friedrich Engels' Studie auf dem Podest."),
    FIND_LENIN("Der Umsturz", "Besuche Wladimir Lenin im rechten Saal."),
    FIND_ROSA("Die Freiheit", "Erkunde den Rosengarten (Tür oben rechts) und finde Rosa Luxemburg."),
    TALK_ROSA("Die Freiheitskämpferin", "Sprich mit Rosa im Garten."),
    COMPLETED("Revolutionär", "Du hast das historische Archiv erfolgreich erkundet!")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarxChatScreen(
    viewModel: AppViewModel,
    navController: NavController
) {
    val activeCharId by viewModel.activeNpcId.collectAsState()
    
    // Choose selected states dynamically
    val chatHistory by when (activeCharId) {
        "engels" -> viewModel.engelsChatHistory.collectAsState()
        "lenin" -> viewModel.leninChatHistory.collectAsState()
        "luxemburg" -> viewModel.luxemburgChatHistory.collectAsState()
        else -> viewModel.marxChatHistory.collectAsState()
    }
    
    val isThinking by when (activeCharId) {
        "engels" -> viewModel.isEngelsThinking.collectAsState()
        "lenin" -> viewModel.isLeninThinking.collectAsState()
        "luxemburg" -> viewModel.isLuxemburgThinking.collectAsState()
        else -> viewModel.isMarxThinking.collectAsState()
    }
    
    val speechBubbleText by when (activeCharId) {
        "engels" -> viewModel.engelsSpeechBubbleText.collectAsState()
        "lenin" -> viewModel.leninSpeechBubbleText.collectAsState()
        "luxemburg" -> viewModel.luxemburgSpeechBubbleText.collectAsState()
        else -> viewModel.marxSpeechBubbleText.collectAsState()
    }
    
    val customMarxImagePath by viewModel.customMarxImagePath.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    
    // Dynamic room stage size
    var stageWidth by remember { mutableStateOf(300f) }
    var stageHeight by remember { mutableStateOf(240f) }

    // Animal Crossing specific game state
    var playerX by remember { mutableStateOf(150f) }
    var playerY by remember { mutableStateOf(190f) }
    var currentRoom by remember { mutableStateOf("hauptsaal") } // "hauptsaal", "engels_buero", "lenin_saal", "rosas_garten"
    var activeDirection by remember { mutableStateOf<String?>(null) }
    var targetX by remember { mutableStateOf<Float?>(null) }
    var targetY by remember { mutableStateOf<Float?>(null) }
    var isSoundMuted by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(true) }
    var speechDelayActive by remember { mutableStateOf(false) }

    var currentObjective by remember { mutableStateOf(QuestObjective.START) }

    val npcX = remember(stageWidth) { stageWidth / 2f }
    val npcY = remember(stageHeight) { stageHeight * 0.35f }
    val distToNPC = remember(playerX, playerY, npcX, npcY) {
        val dx = playerX - npcX
        val dy = playerY - npcY
        Math.sqrt((dx * dx + dy * dy).toDouble())
    }
    val isNearNPC = distToNPC <= 65.0

    val booksInRoom = remember(stageWidth, stageHeight) {
        listOf(
            HistoricalBook(
                id = "kapital",
                title = "Das Kapital",
                author = "Karl Marx",
                coverColor = Color(0xFF8D1C1C),
                description = "Kritik der politischen Ökonomie (1867)",
                pages = listOf(
                    "Willkommen beim Hauptwerk 'Das Kapital'! 📕\n\nHier analysiert Karl Marx die Gesetzmäßigkeiten des kapitalistischen Systems. Im Zentrum seiner Analyse steht die 'Ware' und ihr Doppelcharakter: Gebrauchswert und Tauschwert.\n\nFokussiere dich auf den strukturellen Kern der gesellschaftlichen Verhältnisse!",
                    "Wert & Mehrwert: 💰\n\nArbeiter verkaufen ihre Arbeitskraft. Der Wert dieser Kraft bemisst sich nach den Kosten ihrer biologischen und sozialen Reproduktion. Da die Schicht lang ist, erzeugt die Arbeit weit mehr Wert als sie kostet – das ist der Mehrwert für den Kapitalbesitzer!",
                    "Marxistischer Humor: 🎩\n\n\"Gelf macht nicht glücklich. Es beruhigt höchstens die Bourgeoisie!\"\n\nMarx' Tipp: Wer das Buch verstanden hat, sollte sein Taschengeld vergesellschaften und mit Friedrich teilen!"
                ),
                relX = 0.22f,
                relY = 0.82f,
                room = "hauptsaal"
            ),
            HistoricalBook(
                id = "manifest",
                title = "Das Kommunistische Manifest",
                author = "K. Marx & F. Engels",
                coverColor = Color(0xFFC62828),
                description = "Das Manifest der Kommunistischen Partei (1848)",
                pages = listOf(
                    "Das berühmteste Manifest der Geschichte! 📜\n\nGeschrieben 1848 im Auftrag des Bundes der Kommunisten. Es entfesselte eine revolutionäre Weltbewegung.\n\n'Ein Gespenst geht um in Europa – das Gespenst des Kommunismus.'",
                    "Klassenkampf im Zeitraffer: ⚔️\n\n'Die Geschichte aller bisherigen Gesellschaft ist die Geschichte von Klassenkämpfen.'\n\nFreie und Sklaven, Barone und Leibeigene, kurzum: Unterdrücker und Unterdrückte standen im ständigen Gegensatz.",
                    "Der legendäre Schlachtruf: 📢\n\n'Proletarier aller Länder, vereinigt euch!'\n\nGeheimnis am Rande: Karl dachte lange nach, während Friedrich den Druck vorfinanzierte und herbeieilte!"
                ),
                relX = 0.78f,
                relY = 0.82f,
                room = "hauptsaal"
            ),
            HistoricalBook(
                id = "lage_ar",
                title = "Die Lage der Arbeiterklasse",
                author = "Friedrich Engels",
                coverColor = Color(0xFF4E342E),
                description = "Untersuchungen in Manchester (1845)",
                pages = listOf(
                    "Die bittere Realität der Spinnereien! 🏭\n\nDer junge Engels wurde von seinem Vater geschickt, um die Familienspinnerei in Manchester zu beaufsichtigen. Stattdessen dokumentierte er das himmelschreiende Elend der Slums.",
                    "Erste empirische Soziologie: 📊\n\nUnterernährung, tödliche Kinderarbeit und Seuchen. Engels bewies damit wissenschaftlich, dass der industrielle Kapitalismus Menschen systematisch verschleißt und krank macht.",
                    "Die Engels-Anomalie: 👔\n\nObwohl er die Spinnereien anklagte, besaß er Fabrikanteile! Seine Gewinne schickte er Marx nach London, um dessen Tee, Tinte und 'Das Kapital' überhaupt erst zu finanzieren."
                ),
                relX = 0.2f,
                relY = 0.62f,
                room = "engels_buero"
            ),
            HistoricalBook(
                id = "staat_und_rev",
                title = "Staat und Revolution",
                author = "W. I. Lenin",
                coverColor = Color(0xFF263238),
                description = "Lehre über den politischen Staat (1917)",
                pages = listOf(
                    "Die Machtfrage im Revolutionsjahr! 🏛️\n\nLenin verfasste diese Schrift im finnischen Untergrund kurz vor der Oktoberrevolution. Er zeigt: Der bürgerliche Staat ist kein neutrales Schiedsgericht, sondern Machtmittel einer Klasse gegen die anderen.",
                    "Das Absterben des Staates: 📉\n\nNach dem Umsturz ergreift die Arbeiterklasse die Macht (Diktatur des Proletariats). Wenn Klassenunterschiede schließlich weltweit verschwinden, stirbt auch das Staatsgebilde von allein ab.",
                    "Sowjetische Weisheit: 🧐\n\n'Vertrauen ist gut, Kontrolle ist besser!'\n\nLenin fügt schmunzelnd hinzu: Kontrolliere im Spiel jeden Korridor und rede stets höflich mit Genossin Rosa!"
                ),
                relX = 0.82f,
                relY = 0.68f,
                room = "lenin_saal"
            ),
            HistoricalBook(
                id = "reform_oder_rev",
                title = "Reform oder Revolution?",
                author = "Rosa Luxemburg",
                coverColor = Color(0xFF1B5E20),
                description = "Gegen den Revisionismus (1899)",
                pages = listOf(
                    "Gegen das Verwässern der Ideale! 🌹\n\nRosa Luxemburgs scharfzüngiger Debattenbeitrag. Sie schlussfolgert: Reformen bessern nur Symptome, kurieren aber niemals die Ausbeutung des Profitsystems.",
                    "Der revolutionäre Hebel: ⚙️\n\nReformen sind gut für den Alltag, aber wer sie als Ersatz für die Überwindung des Kapitalismus sieht, wählt den falschen Pfad.\n\n'Freiheit ist immer Freiheit der Andersdenkenden.'",
                    "Natur & Menschlichkeit: 🐦\n\nAus dem Gefängnis schrieb Rosa herzergreifende Briefe über Vögel, Hummeln und Lebensfreude. Eine warmherzige Intellektuelle für einen gerechteren, demokratischen Sozialismus!"
                ),
                relX = 0.25f,
                relY = 0.52f,
                room = "rosas_garten"
            )
        )
    }

    val activeRoomBooks = remember(currentRoom, booksInRoom) {
        booksInRoom.filter { it.room == currentRoom }
    }

    var nearbyBook by remember { mutableStateOf<HistoricalBook?>(null) }
    var selectedBookForReading by remember { mutableStateOf<HistoricalBook?>(null) }
    var currentReaderPage by remember { mutableStateOf(0) }

    // Proximity to bookstands checker
    LaunchedEffect(playerX, playerY, activeRoomBooks, stageWidth, stageHeight) {
        var closest: HistoricalBook? = null
        var minDist = Float.MAX_VALUE
        for (book in activeRoomBooks) {
            val bx = book.relX * stageWidth
            val by = book.relY * stageHeight
            val dx = playerX - bx
            val dy = playerY - by
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (dist <= 38f && dist < minDist) {
                minDist = dist
                closest = book
            }
        }
        nearbyBook = closest
    }

    // SLIDING COLLISION RESOLUTION: Completely prevents players from overlapping with either the NPC or bookstands
    LaunchedEffect(playerX, playerY, npcX, npcY, currentRoom, activeRoomBooks, stageWidth, stageHeight) {
        var updatedX = playerX
        var updatedY = playerY
        var changed = false

        // 1. Collision with active room NPC
        val dxNpc = playerX - npcX
        val dyNpc = playerY - npcY
        val distNpc = Math.sqrt((dxNpc * dxNpc + dyNpc * dyNpc).toDouble()).toFloat()
        val minNpcDist = 52f // Perfect spacing to keep avatars fully disjoint
        if (distNpc < minNpcDist) {
            val angle = if (distNpc > 0.1f) Math.atan2(dyNpc.toDouble(), dxNpc.toDouble()) else 1.57
            updatedX = npcX + (Math.cos(angle) * minNpcDist).toFloat()
            updatedY = npcY + (Math.sin(angle) * minNpcDist).toFloat()
            changed = true
        }

        // 2. Collision with bookstands / pedestals in active room
        for (book in activeRoomBooks) {
            val bx = book.relX * stageWidth
            val by = book.relY * stageHeight
            val dxB = updatedX - bx
            val dyB = updatedY - by
            val distB = Math.sqrt((dxB * dxB + dyB * dyB).toDouble()).toFloat()
            val minBookDist = 38f // Keeps player from overlapping pedestals
            if (distB < minBookDist) {
                val angle = if (distB > 0.1f) Math.atan2(dyB.toDouble(), dxB.toDouble()) else 1.57
                updatedX = bx + (Math.cos(angle) * minBookDist).toFloat()
                updatedY = by + (Math.sin(angle) * minBookDist).toFloat()
                changed = true
            }
        }

        if (changed) {
            playerX = updatedX.coerceIn(20f, stageWidth - 20f)
            playerY = updatedY.coerceIn(30f, stageHeight - 30f)
            targetX = null
            targetY = null
        }
    }

    // Cache theme colors inside Composable context before Canvas draws them
    val canvasBorderColor = ImmersiveBorder
    val canvasSurfaceColor = ImmersiveSurface

    // Dynamic triggers for transition fade effects
    var screenTransitionOpacity by remember { mutableStateOf(1f) }
    LaunchedEffect(currentRoom) {
        screenTransitionOpacity = 0.1f
        kotlinx.coroutines.delay(250)
        screenTransitionOpacity = 1f
    }

    // Walking game ticker loop
    LaunchedEffect(activeDirection, stageWidth, stageHeight) {
        if (activeDirection != null) {
            targetX = null
            targetY = null
            while (true) {
                val speed = 9f
                var dx = 0f
                var dy = 0f
                when (activeDirection) {
                    "up" -> dy = -speed
                    "down" -> dy = speed
                    "left" -> dx = -speed
                    "right" -> dx = speed
                }
                playerX = (playerX + dx).coerceIn(20f, stageWidth - 20f)
                playerY = (playerY + dy).coerceIn(30f, stageHeight - 30f)
                
                // Audio feedback for grass/carpet/parquet steps
                if (!isSoundMuted && Math.random() < 0.2) {
                    val stepPitch = if (currentRoom == "rosas_garten") 240f else 150f
                    playProceduralGrumbel(stepPitch, isSoundMuted)
                }
                kotlinx.coroutines.delay(65)
            }
        }
    }

    // Touch to Move walk ticker loop
    LaunchedEffect(targetX, targetY, stageWidth, stageHeight) {
        if (targetX != null && targetY != null) {
            while (true) {
                val tx = targetX ?: break
                val ty = targetY ?: break
                val dx = tx - playerX
                val dy = ty - playerY
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist < 10f) {
                    playerX = tx
                    playerY = ty
                    targetX = null
                    targetY = null
                    break
                } else {
                    val step = 11f
                    playerX = (playerX + (dx / dist) * step).coerceIn(20f, stageWidth - 20f)
                    playerY = (playerY + (dy / dist) * step).coerceIn(30f, stageHeight - 30f)
                }
                if (!isSoundMuted && Math.random() < 0.22) {
                    val stepPitch = if (currentRoom == "rosas_garten") 240f else 150f
                    playProceduralGrumbel(stepPitch, isSoundMuted)
                }
                kotlinx.coroutines.delay(50)
            }
        }
    }

    // Quest System Logic Loop
    LaunchedEffect(currentObjective, currentRoom, activeCharId, selectedBookForReading, speechBubbleText, targetX, activeDirection) {
        when (currentObjective) {
            QuestObjective.START -> {
                if (targetX != null || activeDirection != null) {
                    currentObjective = QuestObjective.READ_MANIFEST
                }
            }
            QuestObjective.READ_MANIFEST -> {
                if (selectedBookForReading?.id == "manifest") {
                    currentObjective = QuestObjective.TALK_MARX
                }
            }
            QuestObjective.TALK_MARX -> {
                if (activeCharId == "marx" && speechBubbleText.isNotBlank()) {
                    currentObjective = QuestObjective.FIND_ENGELS
                }
            }
            QuestObjective.FIND_ENGELS -> {
                if (currentRoom == "engels_buero") {
                    currentObjective = QuestObjective.READ_LAGE
                }
            }
            QuestObjective.READ_LAGE -> {
                if (selectedBookForReading?.id == "lage_ar") {
                    currentObjective = QuestObjective.FIND_LENIN
                }
            }
            QuestObjective.FIND_LENIN -> {
                if (currentRoom == "lenin_saal") {
                    currentObjective = QuestObjective.FIND_ROSA
                }
            }
            QuestObjective.FIND_ROSA -> {
                if (currentRoom == "rosas_garten") {
                    currentObjective = QuestObjective.TALK_ROSA
                }
            }
            QuestObjective.TALK_ROSA -> {
                if (activeCharId == "luxemburg" && speechBubbleText.isNotBlank()) {
                    currentObjective = QuestObjective.COMPLETED
                }
            }
            QuestObjective.COMPLETED -> {
                // Done
            }
        }
    }

    // Door Transitions: Checks coordinates of doors.
    LaunchedEffect(playerX, playerY, stageWidth, stageHeight) {
        val room = currentRoom
        if (room == "hauptsaal") {
            // Door LEFT to Manchester office of Friedrich Engels
            if (playerX <= 25f && playerY in (stageHeight * 0.35f)..(stageHeight * 0.65f)) {
                currentRoom = "engels_buero"
                viewModel.activeNpcId.value = "engels"
                playerX = stageWidth - 35f 
                playerY = stageHeight / 2f
                playProceduralGrumbel(160f, isSoundMuted)
            }
            // Door RIGHT to Wladimir Lenin's Soviets Room
            else if (playerX >= stageWidth - 25f && playerY in (stageHeight * 0.35f)..(stageHeight * 0.65f)) {
                currentRoom = "lenin_saal"
                viewModel.activeNpcId.value = "lenin"
                playerX = 35f 
                playerY = stageHeight / 2f
                playProceduralGrumbel(280f, isSoundMuted)
            }
            // Door TOP to Rosa Luxemburg's garden
            else if (playerY <= 35f && playerX in (stageWidth * 0.35f)..(stageWidth * 0.65f)) {
                currentRoom = "rosas_garten"
                viewModel.activeNpcId.value = "luxemburg"
                playerX = stageWidth / 2f
                playerY = stageHeight - 45f 
                playProceduralGrumbel(380f, isSoundMuted)
            }
        } else {
            // Returning from side rooms to Hauptsaal through their entry doors
            if (room == "engels_buero" && playerX >= stageWidth - 25f) {
                currentRoom = "hauptsaal"
                viewModel.activeNpcId.value = "marx"
                playerX = 40f
                playerY = stageHeight / 2f
                playProceduralGrumbel(110f, isSoundMuted)
            } else if (room == "lenin_saal" && playerX <= 25f) {
                currentRoom = "hauptsaal"
                viewModel.activeNpcId.value = "marx"
                playerX = stageWidth - 40f
                playerY = stageHeight / 2f
                playProceduralGrumbel(110f, isSoundMuted)
            } else if (room == "rosas_garten" && playerY >= stageHeight - 35f) {
                currentRoom = "hauptsaal"
                viewModel.activeNpcId.value = "marx"
                playerX = stageWidth / 2f
                playerY = 60f
                playProceduralGrumbel(110f, isSoundMuted)
            }
        }
    }

    var isNpcSpeaking by remember { mutableStateOf(false) }
    var isKalleSpeaking by remember { mutableStateOf(false) }

    // Animal Crossing gibberish voice cascade on message change
    LaunchedEffect(speechBubbleText) {
        if (speechBubbleText.isNotBlank() && 
            !speechBubbleText.contains("schärft") && 
            !speechBubbleText.contains("rechnet") && 
            !speechBubbleText.contains("entwirft") && 
            !speechBubbleText.contains("verfasst")
        ) {
            isNpcSpeaking = true
            val pitchHz = when (activeCharId) {
                "marx" -> 110f
                "engels" -> 160f
                "lenin" -> 270f
                "luxemburg" -> 360f
                else -> 150f
            }
            // Rapid bleep cascade matching Animal Crossing text typing
            coroutineScope.launch {
                for (i in 0..5) {
                    playProceduralGrumbel(pitchHz + (i * 12f) + (Math.random() * 20f).toFloat(), isSoundMuted)
                    kotlinx.coroutines.delay(130)
                }
                isNpcSpeaking = false
            }
        }
    }

    // Speech trigger for player Kalle
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty() && chatHistory.last().role == "user") {
            isKalleSpeaking = true
            coroutineScope.launch {
                for (i in 0..3) {
                    playProceduralGrumbel(200f + (i * 10f), isSoundMuted)
                    kotlinx.coroutines.delay(120)
                }
                isKalleSpeaking = false
            }
        }
    }

    val isAngry = remember(speechBubbleText, isNpcSpeaking) {
        isNpcSpeaking && (
            speechBubbleText.contains("Ausbeut", ignoreCase = true) ||
            speechBubbleText.contains("Klassenkampf", ignoreCase = true) ||
            speechBubbleText.contains("Bourgeois", ignoreCase = true) ||
            speechBubbleText.contains("Revolution", ignoreCase = true) ||
            speechBubbleText.contains("Kapitalismus", ignoreCase = true) ||
            speechBubbleText.contains("!", ignoreCase = true)
        )
    }

    BackHandler {
        viewModel.triggerReturnFromMarxTransition()
        navController.popBackStack()
    }

    // Automatically scrolls down history
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    // Camera Zoom Calculations
    val isTalkingActive = isNearNPC && (isNpcSpeaking || isKalleSpeaking || isThinking || speechBubbleText.isNotBlank())
    val cameraScale by animateFloatAsState(
        targetValue = if (isTalkingActive) 1.9f else 1.0f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "cameraScale"
    )

    val midX = remember(playerX, npcX) { (playerX + npcX) / 2f }
    val midY = remember(playerY, npcY) { (playerY + npcY) / 2f }

    val cameraTransX by animateFloatAsState(
        targetValue = if (isTalkingActive) (stageWidth / 2f - midX) else 0f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "cameraTransX"
    )
    val cameraTransY by animateFloatAsState(
        targetValue = if (isTalkingActive) (stageHeight / 2f - midY) else 0f,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "cameraTransY"
    )

    val density = LocalDensity.current

    // Target coordinates for speech bubble hovering in screen (HUD) space
    val npcScreenX = if (isTalkingActive) {
        (stageWidth / 2f) + cameraScale * (npcX - midX)
    } else {
        npcX
    }

    val npcScreenY = if (isTalkingActive) {
        (stageHeight / 2f) + cameraScale * (npcY - midY)
    } else {
        npcY
    }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rotes Tal (AC Edition) ☭",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = ImmersiveTextPrimary
                            )
                        )
                        Text(
                            text = when (currentRoom) {
                                "hauptsaal" -> "Haupthalle der Gewerkschaften"
                                "engels_buero" -> "Engels' Schreibstube (Manchester)"
                                "lenin_saal" -> "Lenins Versammlungsraum (Soviets)"
                                else -> "Rosas Rosengarten der Freiheit"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ImmersiveGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerReturnFromMarxTransition()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Zurück",
                            tint = ImmersiveTextPrimary
                        )
                    }
                },
                actions = {
                    // Audio Toggle Button
                    IconButton(
                        onClick = { isSoundMuted = !isSoundMuted }
                    ) {
                        Text(
                            text = if (isSoundMuted) "🔇" else "🔊",
                            fontSize = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { showExplanationDialog = true }
                    ) {
                        Text(text = "❓", fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = { viewModel.clearMarxChat() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Chat leeren",
                            tint = CreamRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ImmersiveBackground.copy(alpha = 0.95f),
                    titleContentColor = ImmersiveTextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ImmersiveBackground)
        ) {
            // 1. DYNAMIC NATIVE RESPONSIVE GAME AREA
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = screenTransitionOpacity }
            ) {
                val stageWidthVal = maxWidth.value
                val stageHeightVal = maxHeight.value
                
                LaunchedEffect(maxWidth, maxHeight) {
                    stageWidth = stageWidthVal
                    stageHeight = stageHeightVal
                }

                // Dynamic tap marker visualization state (blinking communist gold star / glow circle that expands and fades)
                var tapMarkerX by remember { mutableStateOf<Float?>(null) }
                var tapMarkerY by remember { mutableStateOf<Float?>(null) }
                val tapMarkerAnim = remember { Animatable(0f) }

                // Zooming World Viewport
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(stageWidth, stageHeight) {
                            detectTapGestures { offset ->
                                val localX = offset.x / density.density
                                val localY = offset.y / density.density
                                targetX = localX
                                targetY = localY
                                tapMarkerX = localX
                                tapMarkerY = localY
                                coroutineScope.launch {
                                    tapMarkerAnim.snapTo(1f)
                                    tapMarkerAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(450, easing = LinearOutSlowInEasing)
                                    )
                                    tapMarkerX = null
                                    tapMarkerY = null
                                }
                            }
                        }
                        .graphicsLayer {
                            scaleX = cameraScale
                            scaleY = cameraScale
                            translationX = with(density) { cameraTransX.dp.toPx() } * cameraScale
                            translationY = with(density) { cameraTransY.dp.toPx() } * cameraScale
                        }
                ) {
                    // BACKGROUND DECORATIONS CANVAS (3D OBLIQUEperspective)
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val w = size.width
                        val h = size.height
                        val wallHeight = h * 0.28f

                        // Draw Room Floor Tiles based on active environment style
                        when (currentRoom) {
                            "hauptsaal" -> {
                                // 3D Library Walls (Mahogany paneled wallpaper)
                                drawRect(
                                    color = Color(0xFF261208),
                                    topLeft = Offset(0f, 0f),
                                    size = Size(w, wallHeight)
                                )
                                // Wall Pillars at regular intervals for 3D depth
                                for (i in 1..4) {
                                    val px = (w * i / 5f)
                                    drawRect(
                                        color = Color(0xFF1B0C05),
                                        topLeft = Offset(px - 10f, 0f),
                                        size = Size(20f, wallHeight)
                                    )
                                    // Pillar highlights
                                    drawLine(
                                        color = Color(0xFF4E2C1B),
                                        start = Offset(px - 10f, 0f),
                                        end = Offset(px - 10f, wallHeight),
                                        strokeWidth = 2f
                                    )
                                }
                                // Bookshelves outlines between pillars
                                for (i in 0..4) {
                                    val startX = (w * i / 5f) + 16f
                                    val endX = (w * (i + 1) / 5f) - 16f
                                    if (endX > startX) {
                                        // Draw shelf rows
                                        for (sy in listOf(18f, 38f, 58f)) {
                                            if (sy < wallHeight - 10f) {
                                                drawLine(
                                                    color = Color(0xFF32170B),
                                                    start = Offset(startX, sy),
                                                    end = Offset(endX, sy),
                                                    strokeWidth = 3f
                                                )
                                                // Draw individual book colors (leather-red, yellow, gold, green)
                                                var bx = startX + 4f
                                                while (bx < endX - 6f) {
                                                    val bookW = 5f
                                                    val bookH = 12f
                                                    drawRect(
                                                        color = when ((bx.toInt() / 7) % 4) {
                                                            0 -> Color(0xFF8D1C1C)
                                                            1 -> Color(0xFFE5A93B)
                                                            2 -> Color(0xFF2E7D32)
                                                            else -> Color(0xFF4E342E)
                                                        },
                                                        topLeft = Offset(bx, sy - bookH),
                                                        size = Size(bookW, bookH)
                                                    )
                                                    bx += bookW + 2f
                                                }
                                            }
                                        }
                                    }
                                }

                                // Centered Warm Cozy Glowing Arched Fireplace
                                val fpW = w * 0.22f
                                val fpH = wallHeight * 0.75f
                                val fpX = (w - fpW)/2f
                                val fpY = wallHeight - fpH
                                // Mantel / brick structure
                                drawRect(
                                    color = Color(0xFF5D2415),
                                    topLeft = Offset(fpX, fpY),
                                    size = Size(fpW, fpH)
                                )
                                drawRect(
                                    color = Color(0xFF130906),
                                    topLeft = Offset(fpX + 16f, fpY + 12f),
                                    size = Size(fpW - 32f, fpH - 12f)
                                )
                                // Fire sparks
                                drawCircle(
                                    color = Color(0xFFFF5722),
                                    radius = 11f,
                                    center = Offset(w / 2f, wallHeight - 12f)
                                )
                                drawCircle(
                                    color = Color(0xFFFFC107).copy(alpha = 0.9f),
                                    radius = 7f,
                                    center = Offset(w / 2f - 4f, wallHeight - 11f)
                                )

                                // Crease shadow between wall and floor
                                drawLine(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    start = Offset(0f, wallHeight),
                                    end = Offset(w, wallHeight),
                                    strokeWidth = 4f
                                )

                                // DIAGONA_OBLIQUE SOVIET FLOOR (Isometric Chevron parquets)
                                drawRect(
                                    color = Color(0xFF3E2723).copy(alpha = 0.9f),
                                    topLeft = Offset(0f, wallHeight),
                                    size = Size(w, h - wallHeight)
                                )
                                val spacing = 32f
                                for (i in -20..30) {
                                    drawLine(
                                        color = canvasBorderColor.copy(alpha = 0.18f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing + (h - wallHeight) * 0.62f, h),
                                        strokeWidth = 2f
                                    )
                                    drawLine(
                                        color = canvasBorderColor.copy(alpha = 0.18f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing - (h - wallHeight) * 0.62f, h),
                                        strokeWidth = 2f
                                    )
                                }

                                // Large Oval Red Union Rug centered in 3D plane
                                drawOval(
                                    color = Color(0xFFC00020).copy(alpha = 0.22f),
                                    topLeft = Offset(w * 0.22f, wallHeight + (h - wallHeight) * 0.15f),
                                    size = Size(w * 0.56f, (h - wallHeight) * 0.68f)
                                )

                                // Round mahogany committee tables with volumes of Das Kapital
                                drawOval(
                                    color = Color(0xFF4E2C1B),
                                    topLeft = Offset(w * 0.32f, wallHeight + (h - wallHeight) * 0.34f),
                                    size = Size(w * 0.36f, (h - wallHeight) * 0.32f)
                                )
                                drawOval(
                                    color = Color(0xFF3E1F11),
                                    topLeft = Offset(w * 0.33f, wallHeight + (h - wallHeight) * 0.36f),
                                    size = Size(w * 0.34f, (h - wallHeight) * 0.28f)
                                )
                                // Das Kapital Leather Books on Desk
                                drawRoundRect(
                                    color = Color(0xFFE5A93B), // gold guilding
                                    topLeft = Offset(w * 0.46f, wallHeight + (h - wallHeight) * 0.46f),
                                    size = Size(16f, 10f),
                                    cornerRadius = CornerRadius(1.5f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF7B1FA2), // manifest manifesto
                                    topLeft = Offset(w * 0.52f, wallHeight + (h - wallHeight) * 0.44f),
                                    size = Size(14f, 10f),
                                    cornerRadius = CornerRadius(1.5f)
                                )
                            }
                            "engels_buero" -> {
                                // Warm gaslight Manchester factory study
                                drawRect(
                                    color = Color(0xFF1E272C),
                                    topLeft = Offset(0f, 0f),
                                    size = Size(w, wallHeight)
                                )
                                // Factory window looking out to industrial chimneys
                                drawRect(
                                    color = Color(0xFF14191C),
                                    topLeft = Offset(w * 0.35f, 6f),
                                    size = Size(w * 0.3f, wallHeight - 12f)
                                )
                                drawLine(
                                    color = Color(0xFF37474F),
                                    start = Offset(w * 0.5f, 6f),
                                    end = Offset(w * 0.5f, wallHeight - 6f),
                                    strokeWidth = 2.5f
                                )
                                drawLine(
                                    color = Color(0xFF37474F),
                                    start = Offset(w * 0.35f, wallHeight * 0.4f),
                                    end = Offset(w * 0.65f, wallHeight * 0.4f),
                                    strokeWidth = 2.5f
                                )
                                // Silhouette Chimneys in window
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(w * 0.43f, wallHeight * 0.45f),
                                    end = Offset(w * 0.43f, wallHeight - 8f),
                                    strokeWidth = 12f
                                )
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(w * 0.57f, wallHeight * 0.35f),
                                    end = Offset(w * 0.57f, wallHeight - 8f),
                                    strokeWidth = 14f
                                )

                                // Huge gears & steam mill machinery cogs on left wall
                                drawCircle(
                                    color = Color(0xFF546E7A).copy(alpha = 0.5f),
                                    radius = 28f,
                                    center = Offset(w * 0.15f, wallHeight * 0.45f),
                                    style = Stroke(width = 6f)
                                )
                                drawCircle(
                                    color = Color(0xFF455A64).copy(alpha = 0.5f),
                                    radius = 16f,
                                    center = Offset(w * 0.15f + 36f, wallHeight * 0.45f + 12f),
                                    style = Stroke(width = 4f)
                                )

                                drawLine(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    start = Offset(0f, wallHeight),
                                    end = Offset(w, wallHeight),
                                    strokeWidth = 3f
                                )

                                // Tile chevron planks (parquet floors)
                                drawRect(
                                    color = Color(0xFF4E342E).copy(alpha = 0.85f),
                                    topLeft = Offset(0f, wallHeight),
                                    size = Size(w, h - wallHeight)
                                )
                                val spacing = 26f
                                for (i in -20..30) {
                                    drawLine(
                                        color = Color(0xFF2D1B18).copy(alpha = 0.25f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing + (h - wallHeight) * 0.55f, h),
                                        strokeWidth = 2.2f
                                    )
                                    drawLine(
                                        color = Color(0xFF2D1B18).copy(alpha = 0.25f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing - (h - wallHeight) * 0.55f, h),
                                        strokeWidth = 2.2f
                                    )
                                }

                                // Rich vintage Persian circle rug under desk
                                drawOval(
                                    color = Color(0xFFD84315).copy(alpha = 0.22f),
                                    topLeft = Offset(w * 0.28f, wallHeight + 10f),
                                    size = Size(w * 0.44f, (h - wallHeight) * 0.72f)
                                )
                                // Drafting Desk blueprint drawing
                                drawRoundRect(
                                    color = Color(0xFF8D6E63),
                                    topLeft = Offset(w * 0.35f, wallHeight + (h - wallHeight) * 0.12f),
                                    size = Size(w * 0.3f, (h - wallHeight) * 0.38f),
                                    cornerRadius = CornerRadius(6f)
                                )
                                drawRect(
                                    color = Color(0xFFE3F2FD), // Blueprint
                                    topLeft = Offset(w * 0.38f, wallHeight + (h - wallHeight) * 0.18f),
                                    size = Size(w * 0.24f, (h - wallHeight) * 0.24f)
                                )
                                drawLine(
                                    color = Color(0xFF1565C0).copy(alpha = 0.4f),
                                    start = Offset(w * 0.4f, wallHeight + (h - wallHeight) * 0.22f),
                                    end = Offset(w * 0.6f, wallHeight + (h - wallHeight) * 0.38f),
                                    strokeWidth = 2f
                                )
                            }
                            "lenin_saal" -> {
                                // Brutalist gray concrete panel walls
                                drawRect(
                                    color = Color(0xFF424242),
                                    topLeft = Offset(0f, 0f),
                                    size = Size(w, wallHeight)
                                )
                                for (i in 1..4) {
                                    drawLine(
                                        color = Color(0xFF212121),
                                        start = Offset(w * i / 5f, 0f),
                                        end = Offset(w * i / 5f, wallHeight),
                                        strokeWidth = 3f
                                    )
                                }

                                // Giant Majestic Red Fabric Banners hanging down
                                drawRect(
                                    color = Color(0xFFD50000),
                                    topLeft = Offset(w * 0.1f, 0f),
                                    size = Size(w * 0.14f, wallHeight + 16f)
                                )
                                drawRect(
                                    color = Color(0xFFD50000),
                                    topLeft = Offset(w * 0.76f, 0f),
                                    size = Size(w * 0.14f, wallHeight + 16f)
                                )
                                // Golden Soviet Stars on Banners
                                drawCircle(
                                    color = Color(0xFFFFD54F),
                                    radius = 6f,
                                    center = Offset(w * 0.17f, wallHeight * 0.4f)
                                )
                                drawCircle(
                                    color = Color(0xFFFFD54F),
                                    radius = 6f,
                                    center = Offset(w * 0.83f, wallHeight * 0.4f)
                                )

                                drawLine(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    start = Offset(0f, wallHeight),
                                    end = Offset(w, wallHeight),
                                    strokeWidth = 3.5f
                                )

                                // Industrial concrete stone floor chevrons
                                drawRect(
                                    color = Color(0xFF2A2A2A),
                                    topLeft = Offset(0f, wallHeight),
                                    size = Size(w, h - wallHeight)
                                )
                                val spacing = 34f
                                for (i in -20..30) {
                                    drawLine(
                                        color = Color.Black.copy(alpha = 0.35f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing + (h - wallHeight) * 0.6f, h),
                                        strokeWidth = 2.5f
                                    )
                                    drawLine(
                                        color = Color.Black.copy(alpha = 0.35f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing - (h - wallHeight) * 0.6f, h),
                                        strokeWidth = 2.5f
                                    )
                                }

                                // Speaker's Rostrum (assembly podium) centered
                                drawRect(
                                    color = Color(0xFF757575), // concrete podium foundation
                                    topLeft = Offset(w * 0.32f, wallHeight),
                                    size = Size(w * 0.36f, 22f)
                                )
                                drawRect(
                                    color = Color(0xFF5D4037), // rich wood rostrum
                                    topLeft = Offset(w * 0.38f, wallHeight - 12f),
                                    size = Size(w * 0.24f, 24f)
                                )
                                // Red Front Shield of Podium
                                drawRect(
                                    color = Color(0xFFC62828),
                                    topLeft = Offset(w * 0.43f, wallHeight - 6f),
                                    size = Size(w * 0.14f, 15f)
                                )
                            }
                            "rosas_garten" -> {
                                // Beautiful Rose garden rich hedge wall
                                drawRect(
                                    color = Color(0xFF2E7D32),
                                    topLeft = Offset(0f, 0f),
                                    size = Size(w, wallHeight)
                                )
                                // Cute white wooden picket fences spanning top
                                var fx = 8f
                                while (fx < w) {
                                    // Picket posts
                                    drawRect(
                                        color = Color(0xFFECEFF1),
                                        topLeft = Offset(fx, 4f),
                                        size = Size(6f, wallHeight - 4f)
                                    )
                                    fx += 16f
                                }
                                // Horizontal rails
                                drawRect(
                                    color = Color(0xFFCFD8DC),
                                    topLeft = Offset(0f, wallHeight * 0.3f),
                                    size = Size(w, 4f)
                                )
                                drawRect(
                                    color = Color(0xFFCFD8DC),
                                    topLeft = Offset(0f, wallHeight * 0.7f),
                                    size = Size(w, 4f)
                                )

                                drawLine(
                                    color = Color(0xFF1B5E20).copy(alpha = 0.5f),
                                    start = Offset(0f, wallHeight),
                                    end = Offset(w, wallHeight),
                                    strokeWidth = 3f
                                )

                                // Soft emerald green grass floor with diagonal paths
                                drawRect(
                                    color = Color(0xFF4CAF50),
                                    topLeft = Offset(0f, wallHeight),
                                    size = Size(w, h - wallHeight)
                                )
                                val spacing = 32f
                                for (i in -20..30) {
                                    drawLine(
                                        color = Color(0xFF388E3C).copy(alpha = 0.3f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing + (h - wallHeight) * 0.5f, h),
                                        strokeWidth = 2f
                                    )
                                    drawLine(
                                        color = Color(0xFF388E3C).copy(alpha = 0.3f),
                                        start = Offset(i * spacing, wallHeight),
                                        end = Offset(i * spacing - (h - wallHeight) * 0.5f, h),
                                        strokeWidth = 2f
                                    )
                                }

                                // Multiple scattered clusters of pixel-style wild Roses bushes!
                                for (rx in listOf(w * 0.15f, w * 0.8f, w * 0.22f, w * 0.72f)) {
                                    val ry = wallHeight + (h - wallHeight) * 0.25f + ((rx.toInt() % 7) * 8f)
                                    // Bush base
                                    drawCircle(
                                        color = Color(0xFF1B5E20),
                                        radius = 22f,
                                        center = Offset(rx, ry)
                                    )
                                    // Beautiful red roses blooming inside bush
                                    drawCircle(
                                        color = Color(0xFFE91E63),
                                        radius = 5f,
                                        center = Offset(rx - 8f, ry - 6f)
                                    )
                                    drawCircle(
                                        color = Color(0xFFD81B60),
                                        radius = 4f,
                                        center = Offset(rx + 9f, ry + 4f)
                                    )
                                    drawCircle(
                                        color = Color(0xFFFF4081),
                                        radius = 4.5f,
                                        center = Offset(rx + 1f, ry - 10f)
                                    )
                                }

                                // Elegant garden bench structure on side
                                drawRoundRect(
                                    color = Color(0xFF8D6E63),
                                    topLeft = Offset(w * 0.14f, wallHeight + (h - wallHeight) * 0.5f),
                                    size = Size(w * 0.18f, 14f),
                                    cornerRadius = CornerRadius(3f)
                                )
                                drawLine(
                                    color = Color(0xFF4E342E),
                                    start = Offset(w * 0.14f + 4f, wallHeight + (h - wallHeight) * 0.5f + 14f),
                                    end = Offset(w * 0.14f + 4f, wallHeight + (h - wallHeight) * 0.5f + 25f),
                                    strokeWidth = 3f
                                )
                                drawLine(
                                    color = Color(0xFF4E342E),
                                    start = Offset(w * 0.14f + w * 0.18f - 4f, wallHeight + (h - wallHeight) * 0.5f + 14f),
                                    end = Offset(w * 0.14f + w * 0.18f - 4f, wallHeight + (h - wallHeight) * 0.5f + 25f),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        // 3D OVAL DROP SHADOWS FOR CHARACTERS
                        // Ground NPC
                        drawOval(
                            color = Color(0x60000000),
                            topLeft = Offset(npcX - 32f, npcY + 36f),
                            size = Size(64f, 15f)
                        )
                        // Ground Player
                        drawOval(
                            color = Color(0x60000000),
                            topLeft = Offset(playerX - 28f, playerY + 32f),
                            size = Size(56f, 14f)
                        )

                        // Draw bookstands / pedestals in active room
                        activeRoomBooks.forEach { book ->
                            val bx = book.relX * w
                            val by = book.relY * h

                            // Pedestal shadow
                            drawOval(
                                color = Color(0x60000000),
                                topLeft = Offset(bx - 16f, by + 18f),
                                size = Size(32f, 10f)
                            )

                            // 1. Pedestal base shaft (rich retro mahogany/dark stone look)
                            drawRect(
                                color = Color(0xFF4E342E),
                                topLeft = Offset(bx - 6f, by - 2f),
                                size = Size(12f, 22f)
                            )
                            // Highlight on shaft
                            drawRect(
                                color = Color(0xFF6D4C41),
                                topLeft = Offset(bx - 6f, by - 2f),
                                size = Size(3f, 22f)
                            )

                            // 2. Platform top plate
                            drawRect(
                                color = Color(0xFF3E2723),
                                topLeft = Offset(bx - 18f, by - 8f),
                                size = Size(36f, 7f)
                            )
                            // Golden front band/plate
                            drawRect(
                                color = Color(0xFFFFC107),
                                topLeft = Offset(bx - 10f, by - 6f),
                                size = Size(20f, 3f)
                            )

                            // 3. Open pages of the books
                            // Left page
                            drawRect(
                                color = Color(0xFFFFFFE0), // Pergament light cream
                                topLeft = Offset(bx - 14f, by - 16f),
                                size = Size(13f, 9f)
                            )
                            // Right page
                            drawRect(
                                color = Color(0xFFFFFFE0),
                                topLeft = Offset(bx + 1f, by - 16f),
                                size = Size(13f, 9f)
                            )

                            // Book bindings (the cover showing around the pages in b.coverColor)
                            drawLine(
                                color = book.coverColor,
                                start = Offset(bx - 16f, by - 16f),
                                end = Offset(bx + 16f, by - 16f),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = book.coverColor,
                                start = Offset(bx - 15f, by - 7f),
                                end = Offset(bx + 15f, by - 7f),
                                strokeWidth = 2f
                            )
                            // Micro red ribbon bookmark hanging down
                            drawLine(
                                color = Color.Red,
                                start = Offset(bx, by - 12f),
                                end = Offset(bx, by - 2f),
                                strokeWidth = 1.5f
                            )
                        }

                        // If Tap Marker is active, draw a gorgeous gold star/expanding ripple indicator
                        tapMarkerX?.let { tX ->
                            tapMarkerY?.let { tY ->
                                val alphaVal = tapMarkerAnim.value
                                drawCircle(
                                    color = Color(0xFFFFD54F).copy(alpha = alphaVal),
                                    radius = 24f * (1.5f - alphaVal),
                                    center = Offset(tX, tY),
                                    style = Stroke(width = 3f * alphaVal)
                                )
                                drawCircle(
                                    color = Color(0xFFFFD54F).copy(alpha = alphaVal * 0.4f),
                                    radius = 8f,
                                    center = Offset(tX, tY)
                                )
                            }
                        }
                    }

                    // DOOR BANNERS & LABELS IN THE HAUPTSAAL (Clean overlay text matching the aesthetic)
                    if (currentRoom == "hauptsaal") {
                        Text("← Engels' Schreibstube", style = MaterialTheme.typography.labelSmall.copy(color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold), modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp).graphicsLayer { rotationZ = -90f })
                        Text("Lenins Versammlung →", style = MaterialTheme.typography.labelSmall.copy(color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold), modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).graphicsLayer { rotationZ = 90f })
                        Text("↑ Rosengarten (Luxemburg)", style = MaterialTheme.typography.labelSmall.copy(color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold), modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp))
                    } else {
                        // Return doors
                        Text("↓ Zurück zur Halle", style = MaterialTheme.typography.labelSmall.copy(color = ImmersiveTextSecondary, fontWeight = FontWeight.Bold), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp))
                    }

                    // SPARKLES / DUST SPECS (AC VIBE)
                    Box(modifier = Modifier.fillMaxSize()) {
                        val infiniteTransition = rememberInfiniteTransition(label = "dust")
                        val dustY by infiniteTransition.animateFloat(
                            initialValue = -5f,
                            targetValue = 200f,
                            animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
                            label = "dustY"
                        )
                        Box(modifier = Modifier.size(6.dp).offset(x = 80.dp, y = dustY.dp).background(Color.White.copy(alpha = 0.4f), CircleShape))
                        Box(modifier = Modifier.size(5.dp).offset(x = (stageWidth - 80f).dp, y = (dustY + 40f).dp).background(Color.White.copy(alpha = 0.35f), CircleShape))
                    }

                    // RENDER THE ACTIVE ROOM HISTORICAL FIGURE (NPC) - ENLARGED FEWER DETAILS COVERED!
                    val infiniteTransition = rememberInfiniteTransition(label = "all_anims")
                    val bobbingRange = if (isThinking) 1f else if (isNpcSpeaking) 4.5f else 1.5f
                    val npcTranslationY by infiniteTransition.animateFloat(
                        initialValue = -bobbingRange,
                        targetValue = bobbingRange,
                        animationSpec = infiniteRepeatable(tween(if (isNpcSpeaking) 250 else 1500, easing = SineToLinearEasing), RepeatMode.Reverse),
                        label = "npcTranslationY"
                    )
                    val npcRotationZ by infiniteTransition.animateFloat(
                        initialValue = if (isNpcSpeaking) -3f else 0f,
                        targetValue = if (isNpcSpeaking) 3f else 0f,
                        animationSpec = infiniteRepeatable(tween(if (isNpcSpeaking) 320 else 1200, easing = LinearEasing), RepeatMode.Reverse),
                        label = "npcRot"
                    )
                    val mouthMaxOffset = if (isNpcSpeaking) 5.0f else 0f
                    val npcMouthOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = mouthMaxOffset,
                        animationSpec = infiniteRepeatable(tween(130, easing = LinearEasing), RepeatMode.Reverse),
                        label = "npcMouth"
                    )

                    Box(
                        modifier = Modifier
                            .size(106.dp) // Much larger (was 76.dp)
                            .offset(x = (npcX - 53f).dp, y = (npcY - 53f + npcTranslationY).dp)
                    ) {
                        when (activeCharId) {
                            "engels" -> EngelsLowPolyBust(
                                modifier = Modifier.fillMaxSize(),
                                rotationZ = npcRotationZ,
                                mouthOffset = npcMouthOffset
                            )
                            "lenin" -> LeninLowPolyBust(
                                modifier = Modifier.fillMaxSize(),
                                rotationZ = npcRotationZ,
                                mouthOffset = npcMouthOffset
                            )
                            "luxemburg" -> LuxemburgLowPolyBust(
                                modifier = Modifier.fillMaxSize(),
                                rotationZ = npcRotationZ,
                                mouthOffset = npcMouthOffset
                            )
                            else -> {
                                if (customMarxImagePath != null) {
                                    AsyncImage(
                                        model = customMarxImagePath,
                                        contentDescription = "Karl Marx custom image",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, ImmersiveGreen, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    MarxLowPolyBust(
                                        modifier = Modifier.fillMaxSize(),
                                        rotationZ = npcRotationZ,
                                        mouthOffset = npcMouthOffset
                                    )
                                }
                            }
                        }
                        
                        if (isAngry) {
                            AnimeAngerVein(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-12).dp)
                            )
                            AnimeSteamPuff(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = (-12).dp, y = (-20).dp)
                            )
                        }
                    }

                    // RENDER THE PLAYER CHAR (GENOSSE KALLE) - ENLARGED!
                    val playerTranslationY by infiniteTransition.animateFloat(
                        initialValue = -1.5f,
                        targetValue = 1.5f,
                        animationSpec = infiniteRepeatable(tween(if (activeDirection != null) 140 else 1600, easing = SineToLinearEasing), RepeatMode.Reverse),
                        label = "plaBob"
                    )
                    val playerRotationZ by infiniteTransition.animateFloat(
                        initialValue = if (activeDirection != null) -4f else 0f,
                        targetValue = if (activeDirection != null) 4f else 0f,
                        animationSpec = infiniteRepeatable(tween(180, easing = LinearEasing), RepeatMode.Reverse),
                        label = "plaRot"
                    )

                    Box(
                        modifier = Modifier
                            .size(100.dp) // Much larger (was 70.dp)
                            .offset(x = (playerX - 50f).dp, y = (playerY - 50f + playerTranslationY).dp)
                    ) {
                        ProletariatLowPolyBust(
                            modifier = Modifier.fillMaxSize(),
                            rotationZ = playerRotationZ,
                            mouthOffset = if (isKalleSpeaking) 4f else 0f,
                            isNervous = isAngry
                        )
                    }
                }

                // SIBLING HUD VIEWPORT: floating above the scaled world (remains crisp, beautifully-sized, no zoom pixelation!)
                if (isNearNPC) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp)
                            .shadow(6.dp, RoundedCornerShape(12.dp))
                            .background(ImmersiveGreen, RoundedCornerShape(12.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🗣️ SPRACHBEREICH (A)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                // TRANSLUCENT OVERHEAD RPG DIALOGUE CARD (Anchored at Top Center of game arena - completely clear of figures!)
                if (speechBubbleText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .fillMaxWidth(0.95f)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .background(
                                ImmersiveSurface.copy(alpha = 0.86f),
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                2.3.dp,
                                if (isAngry) Color(0xFFD50000).copy(alpha = 0.85f) else ImmersiveGreen.copy(alpha = 0.85f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "💬",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = when (activeCharId) {
                                            "engels" -> "FRIEDRICH ENGELS"
                                            "lenin" -> "W. I. LENIN"
                                            "luxemburg" -> "ROSA LUXEMBURG"
                                            else -> "KARL MARX"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isAngry) Color.Red else ImmersiveGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                // Small blinking status indicator
                                val bubbleTransition = rememberInfiniteTransition(label = "bubble_pulse")
                                val bubblePulse by bubbleTransition.animateFloat(
                                    initialValue = 0.4f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(tween(600, easing = SineToLinearEasing), RepeatMode.Reverse),
                                    label = "bubblePulse"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .graphicsLayer { alpha = bubblePulse }
                                        .background(if (isAngry) Color.Red else ImmersiveGreen, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = speechBubbleText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ImmersiveTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                // 4. HISTORICAL BOOK READER DIALOG / OVERLAY (Parchment scroll aesthetic)
                selectedBookForReading?.let { book ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.72f))
                            .clickable(enabled = true, onClick = { /* consume click to prevent character movement */ })
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .fillMaxHeight(0.82f)
                                .border(4.dp, Color(0xFF8D6E63), RoundedCornerShape(24.dp))
                                .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFDF6E2) // Antique Parchment warm tone
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Book Header
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "📜 HISTORISCHES ARCHIV 📜",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF8D6E63),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = book.title,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color(0xFF3E2723),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp
                                        )
                                    )
                                    Text(
                                        text = "von ${book.author}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF5D4037),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Divider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = Color(0xFFD7CCC8),
                                        thickness = 1.5.dp
                                    )
                                }

                                // Interactive Book Page Content Layout
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(
                                            Color(0x208D6E63),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(14.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Seite ${currentReaderPage + 1} von ${book.pages.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF795548),
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                        Text(
                                            text = book.pages[currentReaderPage],
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color(0xFF3E2723),
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Navigation and Close Controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Prev Page Button
                                    Button(
                                        onClick = {
                                            if (currentReaderPage > 0) {
                                                currentReaderPage--
                                                playProceduralGrumbel(300f, isSoundMuted)
                                            }
                                        },
                                        enabled = currentReaderPage > 0,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8D6E63),
                                            contentColor = Color.White,
                                            disabledContainerColor = Color(0xFFD7CCC8)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("◄ Zurück", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    // RED SEAL STYLED CLOSE BUTTON
                                    IconButton(
                                        onClick = {
                                            selectedBookForReading = null
                                            playProceduralGrumbel(110f, isSoundMuted)
                                        },
                                        modifier = Modifier
                                            .size(52.dp)
                                            .shadow(4.dp, CircleShape)
                                            .background(Color(0xFFC62828), CircleShape)
                                            .border(2.dp, Color(0xFFFFD54F), CircleShape)
                                    ) {
                                        Text("✖", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    }

                                    // Next Page Button
                                    Button(
                                        onClick = {
                                            if (currentReaderPage < book.pages.size - 1) {
                                                currentReaderPage++
                                                playProceduralGrumbel(300f, isSoundMuted)
                                            }
                                        },
                                        enabled = currentReaderPage < book.pages.size - 1,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8D6E63),
                                            contentColor = Color.White,
                                            disabledContainerColor = Color(0xFFD7CCC8)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Weiter ►", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1.5. QUEST TRACKER OVERLAY
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .fillMaxWidth(0.6f)
                    .shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ImmersiveSurface.copy(alpha = 0.95f)
                ),
                border = BorderStroke(1.5.dp, ImmersiveGreen)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯 ", fontSize = 16.sp)
                        Text(
                            text = "Aktuelles Ziel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ImmersiveGreen,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentObjective.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = ImmersiveTextPrimary
                        )
                    )
                    Text(
                        text = currentObjective.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ImmersiveTextSecondary,
                            lineHeight = 16.sp,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // 2. FLOATING HUD OVERLAYS (Controllers, Question suggestions, Chat input field)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SUGGESTED SHORTCUT CHIPS
                val shortcutQuestions = remember(activeCharId) {
                    when (activeCharId) {
                        "engels" -> listOf(
                            "Wie finanzierst du Karl?",
                            "Warum das Manifest?",
                            "Ausbeutung heute?"
                        )
                        "lenin" -> listOf(
                            "Was tun? Vanguard-Partei?",
                            "Was ist Imperialismus?",
                            "Wie machten wir Räte?"
                        )
                        "luxemburg" -> listOf(
                            "Ist Streik demokratisch?",
                            "Wahre Freiheit?",
                            "Krieg und Kapitalismus?"
                        )
                        else -> listOf(
                            "Wie fällt die Profitrate?",
                            "Was ist Lohnsklaverei?",
                            "Wer gewinnt den Kampf?"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    shortcutQuestions.forEach { question ->
                        Box(
                            modifier = Modifier
                                .clickable {
                                    if (!isThinking) {
                                        viewModel.askKarlMarx(question)
                                    }
                                }
                                .shadow(3.dp, RoundedCornerShape(12.dp))
                                .background(ImmersiveSurface.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                                .border(1.dp, ImmersiveBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ImmersiveGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // CONTROLLER DEVICE CONTROLS ROW (Redesigned as floating action)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // RIGHT CONTROLLER DEVICES: Action context-sensitive KEY
                    val isActionActive = isNearNPC || nearbyBook != null
                    val buttonColor = if (nearbyBook != null) Color(0xFFFFA000) else if (isNearNPC) ImmersiveGreen.copy(alpha = 0.9f) else Color.DarkGray.copy(alpha = 0.5f)
                    val buttonTextLabel = if (nearbyBook != null) "Lesen" else "Sprechen"
                    val iconVector = if (nearbyBook != null) "📖" else "🗣️"

                    AnimatedVisibility(visible = isActionActive) {
                        Button(
                            onClick = {
                                if (nearbyBook != null) {
                                    selectedBookForReading = nearbyBook
                                    currentReaderPage = 0
                                } else if (isNearNPC && !isThinking) {
                                    viewModel.askKarlMarx(shortcutQuestions[0])
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(iconVector, fontSize = 20.sp)
                                Text(
                                    text = buttonTextLabel.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // MAIN INPUT FIELD: Live Chat Query
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .background(ImmersiveSurface.copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                        .border(1.5.dp, ImmersiveBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Stelle an ${
                                    when (activeCharId) {
                                        "engels" -> "Friedrich"
                                        "lenin" -> "Wladimir"
                                        "luxemburg" -> "Rosa"
                                        else -> "Karl Marx"
                                    }
                                } eine live Frage...",
                                color = ImmersiveTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("marx_chat_input_text"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isThinking) {
                                val question = inputText
                                inputText = ""
                                keyboardController?.hide()
                                viewModel.askKarlMarx(question)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isThinking,
                        modifier = Modifier
                            .testTag("marx_chat_send_button")
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isThinking) ImmersiveGreen else ImmersiveBorder)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Senden",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarxSpeechBubble(
    text: String,
    isThinking: Boolean,
    isAngry: Boolean = false
) {
    val bubbleShape = ComicSpeechBubbleShape(isLeftArrow = false)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, bubbleShape)
            .background(
                if (isAngry) Color(0xFFFFEBEE) else ImmersiveSurface,
                bubbleShape
            )
            .border(
                3.dp,
                if (isAngry) Color(0xFFD50000) else ImmersiveGreen,
                bubbleShape
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .padding(bottom = 12.dp) // Leave negative room for speech arrow
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isAngry) "💢 KARL MARX (BERSERK):" else "☭ KARL MARX:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isAngry) Color(0xFFD50000) else ImmersiveGreen,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isThinking) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Analysiert das Kapital",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isAngry) Color(0xFFB71C1C) else ImmersiveTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    BouncingDots()
                }
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isAngry) Color(0xFF3E2723) else ImmersiveTextPrimary,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.ExtraBold // high contrast legibility!
                    )
                )
            }
        }
    }
}

@Composable
fun ProletariatSpeechBubble(
    text: String,
    isSpeaking: Boolean
) {
    val bubbleShape = ComicSpeechBubbleShape(isLeftArrow = true)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, bubbleShape)
            .background(
                if (isSpeaking) Color(0xFFECEFF1) else ImmersiveSurface,
                bubbleShape
            )
            .border(
                3.dp,
                if (isSpeaking) Color(0xFF1E88E5) else ImmersiveBorder,
                bubbleShape
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .padding(bottom = 12.dp) // Leave negative room for speech arrow
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🔧 GENOSSE INTERPELLANT (KALLE):",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ImmersiveTextPrimary,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold // high contrast legibility!
                )
            )
        }
    }
}

@Composable
fun AnimeAngerVein(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vein_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vein_scale"
    )

    Canvas(
        modifier = modifier
            .size(24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        val w = size.width
        val h = size.height
        val strokeWidthPx = 4.dp.toPx()
        val color = Color(0xFFD50000)

        // Draw 💢 anime anger vein mark (4 curved lines meeting at center)
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.35f),
            style = Stroke(strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
         drawArc(
            color = color,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.15f),
            size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.35f),
            style = Stroke(strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.5f),
            size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.35f),
            style = Stroke(strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
            size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.35f),
            style = Stroke(strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun AnimeSteamPuff(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "steam_puff")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "float"
    )

    Canvas(
        modifier = modifier
            .size(16.dp)
            .graphicsLayer {
                translationY = floatOffset
                this.alpha = alpha
            }
    ) {
        val w = size.width
        val h = size.height
        val puffBrush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.1f)),
            center = center,
            radius = w / 2f
        )
        drawCircle(
            brush = puffBrush,
            radius = w / 1.8f
        )
    }
}

@Composable
fun ProletariatLowPolyBust(
    modifier: Modifier = Modifier,
    translationY: Float = 0f,
    rotationZ: Float = 0f,
    eyebrowOffset: Float = 0f,
    mouthOffset: Float = 0f,
    isNervous: Boolean = false
) {
    Canvas(
        modifier = modifier
            .graphicsLayer {
                this.translationY = translationY
                this.rotationZ = rotationZ
            }
    ) {
        val w = size.width
        val h = size.height

        // Wii (Mii) Style: Kalle the Proletariat
        // 1. Sleek, rounded Mii-style Cylinder Body/Shirt (Classic Mii body layout)
        val shirtColor = Color(0xFF1E88E5) // Mii blue team shirt
        val overallBase = Color(0xFF1565C0)
        val overallsPath = Path().apply {
            moveTo(w * 0.28f, h * 0.95f)
            lineTo(w * 0.32f, h * 0.76f)
            quadraticTo(w * 0.5f, h * 0.72f, w * 0.68f, h * 0.76f)
            lineTo(w * 0.72f, h * 0.95f)
            close()
        }
        drawPath(overallsPath, shirtColor)

        // Overlay overalls straps
        drawRoundRect(
            color = overallBase,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.76f),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = overallBase,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.76f),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.2f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 2. Head: Perfectly round/oval cartoon Mii face
        val skinColor = Color(0xFFFFD180) // Iconic Mii peach skin
        val skinShadow = Color(0xFFFFB74D)
        
        // Draw Shadowed Head Back
        drawOval(
            color = skinShadow,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.23f, h * 0.24f),
            size = androidx.compose.ui.geometry.Size(w * 0.54f, h * 0.44f)
        )
        // Draw Main Mii face
        drawOval(
            color = skinColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.23f),
            size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.42f)
        )

        // 3. Mii-style Worker Flat Cap hat (resting on the head)
        val capColor = Color(0xFF455A64)
        val capBrimColor = Color(0xFF263238)
        
        // Cap dome shape
        val capPath = Path().apply {
            moveTo(w * 0.18f, h * 0.24f)
            cubicTo(w * 0.3f, h * 0.06f, w * 0.7f, h * 0.06f, w * 0.82f, h * 0.24f)
            close()
        }
        drawPath(capPath, capColor)
        
        // Cap visor/brim
        val capVisorPath = Path().apply {
            moveTo(w * 0.2f, h * 0.24f)
            lineTo(w * 0.8f, h * 0.24f)
            quadraticTo(w * 0.5f, h * 0.31f, w * 0.2f, h * 0.24f)
        }
        drawPath(capVisorPath, capBrimColor)

        // Button on top of cap
        drawCircle(
            color = capBrimColor,
            radius = w * 0.04f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.1f)
        )

        // 4. Eyes: Iconic cartoon Mii eye pills
        // Nervous/fear state of Kalle when Karl is angry: eyes look wide, tiny pupils!
        val leftEyeCenter = androidx.compose.ui.geometry.Offset(w * 0.39f, h * 0.41f)
        val rightEyeCenter = androidx.compose.ui.geometry.Offset(w * 0.61f, h * 0.41f)

        if (isNervous) {
            // Wide nervous circles
            drawCircle(
                color = Color.White,
                radius = w * 0.06f,
                center = leftEyeCenter
            )
            drawCircle(
                color = Color(0xFF1E88E5), // Blue worried iris
                radius = w * 0.04f,
                center = leftEyeCenter
            )
            drawCircle(
                color = Color.Black,
                radius = w * 0.02f,
                center = leftEyeCenter
            )
            
            drawCircle(
                color = Color.White,
                radius = w * 0.06f,
                center = rightEyeCenter
            )
            drawCircle(
                color = Color(0xFF1E88E5),
                radius = w * 0.04f,
                center = rightEyeCenter
            )
            drawCircle(
                color = Color.Black,
                radius = w * 0.02f,
                center = rightEyeCenter
            )
        } else {
            // Friendly black ovals (classic Mii eye type 1)
            drawOval(
                color = Color(0xFF212121),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.36f),
                size = androidx.compose.ui.geometry.Size(w * 0.07f, h * 0.1f)
            )
            drawOval(
                color = Color(0xFF212121),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.36f),
                size = androidx.compose.ui.geometry.Size(w * 0.07f, h * 0.1f)
            )
            // Tiny cute catchlights
            drawCircle(
                color = Color.White,
                radius = w * 0.012f,
                center = androidx.compose.ui.geometry.Offset(w * 0.375f, h * 0.38f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.012f,
                center = androidx.compose.ui.geometry.Offset(w * 0.605f, h * 0.38f)
            )
        }

        // 5. Eyebrows: Stylized vector Mii rectangles that respond dynamically to eyebrowOffset / nervous
        val eyYLeft = h * 0.32f + eyebrowOffset * 0.14f
        val eyYRight = h * 0.32f + eyebrowOffset * 0.14f

        val leftEyebrowPath = Path().apply {
            if (isNervous) {
                // Worried upward slant: / \
                moveTo(w * 0.32f, eyYLeft - 3f)
                lineTo(w * 0.46f, eyYLeft - 10f)
                lineTo(w * 0.44f, eyYLeft - 14f)
                lineTo(w * 0.30f, eyYLeft - 7f)
            } else {
                // Friendly horizontal flat block
                moveTo(w * 0.31f, eyYLeft - 4f)
                lineTo(w * 0.46f, eyYLeft - 4f)
                lineTo(w * 0.46f, eyYLeft - 9f)
                lineTo(w * 0.31f, eyYLeft - 9f)
            }
            close()
        }
        drawPath(leftEyebrowPath, Color(0xFF212121))

        val rightEyebrowPath = Path().apply {
            if (isNervous) {
                // Worried upward slant: \ /
                moveTo(w * 0.68f, eyYRight - 3f)
                lineTo(w * 0.54f, eyYRight - 10f)
                lineTo(w * 0.56f, eyYRight - 14f)
                lineTo(w * 0.70f, eyYRight - 7f)
            } else {
                // Friendly horizontal flat block
                moveTo(w * 0.54f, eyYRight - 4f)
                lineTo(w * 0.69f, eyYRight - 4f)
                lineTo(w * 0.69f, eyYRight - 9f)
                lineTo(w * 0.54f, eyYRight - 9f)
            }
            close()
        }
        drawPath(rightEyebrowPath, Color(0xFF212121))

        // 6. Nose: A round cartoon bulb nose
        drawCircle(
            color = Color(0xFFFFB74D),
            radius = w * 0.045f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.48f)
        )

        // 7. Mii Mustache: Cute round brown mustache halves
        val mustacheY = h * 0.53f
        val mustacheW = w * 0.15f
        val mustacheH = h * 0.07f
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, mustacheY),
            size = androidx.compose.ui.geometry.Size(mustacheW, mustacheH),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.50f, mustacheY),
            size = androidx.compose.ui.geometry.Size(mustacheW, mustacheH),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        // 8. Mouth: Classic dynamic Mii half-circle mouth behind/below nose
        val mouthY = h * 0.61f
        if (mouthOffset > 0.1f) {
            // Dynamic speaking Mii circle mouth (surprised/talking)
            drawCircle(
                color = Color(0xFF3E2723),
                radius = w * 0.04f + mouthOffset * 0.4f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, mouthY)
            )
            // Add a little red tongue inside the Mii mouth!
            drawCircle(
                color = Color(0xFFE57373),
                radius = w * 0.02f + mouthOffset * 0.2f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, mouthY + 6f)
            )
        } else {
            // Cozy slightly round smile
            val smilePath = Path().apply {
                moveTo(w * 0.44f, mouthY)
                quadraticTo(w * 0.5f, mouthY + h * 0.05f, w * 0.56f, mouthY)
            }
            drawPath(
                path = smilePath,
                color = Color(0xFF2D1A1A),
                style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

@Composable
fun BouncingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .scaleAnimation(dot1Scale)
                .background(ImmersiveTextPrimary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .scaleAnimation(dot2Scale)
                .background(ImmersiveTextPrimary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .scaleAnimation(dot3Scale)
                .background(ImmersiveTextPrimary, CircleShape)
        )
    }
}

private fun Modifier.scaleAnimation(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
)

/**
 * Custom-drawn highly stylized low-poly faceted Karl Marx bust / figure.
 * Resembles the 3D polygon mesh bust from the user request (2nd image).
 */
@Composable
fun MarxLowPolyBust(
    modifier: Modifier = Modifier,
    translationY: Float = 0f,
    rotationZ: Float = 0f,
    eyebrowOffset: Float = 0f,
    mouthOffset: Float = 0f
) {
    Canvas(
        modifier = modifier
            .graphicsLayer {
                this.rotationZ = rotationZ
            }
    ) {
        val w = size.width
        val h = size.height

        // Wii (Mii) Style: Karl Marx
        // 1. Sleek, rounded Mii-style Torso (Classic Mii body layout)
        val coatColor = Color(0xFF2E3033) // Mii deep charcoal suit
        val redTie = Color(0xFFD50000) // Crimson tie
        val coatPath = Path().apply {
            moveTo(w * 0.28f, h * 0.95f)
            lineTo(w * 0.32f, h * 0.76f)
            quadraticTo(w * 0.5f, h * 0.72f, w * 0.68f, h * 0.76f)
            lineTo(w * 0.72f, h * 0.95f)
            close()
        }
        drawPath(coatPath, coatColor)

        // Shirt and necktie
        val necktiePath = Path().apply {
            moveTo(w * 0.47f, h * 0.76f)
            lineTo(w * 0.53f, h * 0.76f)
            lineTo(w * 0.5f, h * 0.91f)
            close()
        }
        drawPath(necktiePath, redTie)

        // 2. Head: Peach Mii oval face
        val skinColor = Color(0xFFFFD180)
        drawOval(
            color = skinColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.27f, h * 0.25f),
            size = androidx.compose.ui.geometry.Size(w * 0.46f, h * 0.38f)
        )

        // 3. Puffy senior white hair (Mii cloud-hair style)
        val hairColor = Color(0xFFECEFF1)
        val hairColorShadow = Color(0xFFCFD8DC)
        
        // Left hairballs
        drawCircle(color = hairColorShadow, radius = w * 0.14f, center = androidx.compose.ui.geometry.Offset(w * 0.24f, h * 0.34f))
        drawCircle(color = hairColor, radius = w * 0.13f, center = androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.32f))
        
        // Right hairballs
        drawCircle(color = hairColorShadow, radius = w * 0.14f, center = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.34f))
        drawCircle(color = hairColor, radius = w * 0.13f, center = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.32f))

        // Top cap hair cloud
        drawOval(
            color = hairColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.26f, h * 0.12f),
            size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.18f)
        )

        // 4. Massive Cloud Beard (overlapping white fluffy Mii elements)
        val beardYOffset = mouthOffset * 0.35f
        drawCircle(color = hairColorShadow, radius = w * 0.14f, center = androidx.compose.ui.geometry.Offset(w * 0.34f, h * 0.58f + beardYOffset))
        drawCircle(color = hairColorShadow, radius = w * 0.14f, center = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.58f + beardYOffset))
        // Major bottom beard bubble
        drawCircle(color = hairColor, radius = w * 0.16f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.66f + beardYOffset))

        // 5. Classic Mii Eyebrows (thick gray ovals that angle based on eyebrowOffset)
        val eyMarxLeftY = h * 0.22f + eyebrowOffset * 0.15f
        val eyMarxRightY = h * 0.22f + eyebrowOffset * 0.15f
        drawRoundRect(
            color = Color(0xFF78909C),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, eyMarxLeftY),
            size = androidx.compose.ui.geometry.Size(w * 0.15f, h * 0.04f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFF78909C),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.53f, eyMarxRightY),
            size = androidx.compose.ui.geometry.Size(w * 0.15f, h * 0.04f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 6. Round Wire Glasses - Super iconic Mii Channel accessory!
        val leftLensCenter = androidx.compose.ui.geometry.Offset(w * 0.41f, h * 0.34f)
        val rightLensCenter = androidx.compose.ui.geometry.Offset(w * 0.59f, h * 0.34f)
        // Draw black wire frames
        drawCircle(color = Color.DarkGray, radius = w * 0.082f, center = leftLensCenter, style = Stroke(width = 4f))
        drawCircle(color = Color.DarkGray, radius = w * 0.082f, center = rightLensCenter, style = Stroke(width = 4f))
        // Glasses bridge
        drawLine(
            color = Color.DarkGray,
            strokeWidth = 4f,
            start = androidx.compose.ui.geometry.Offset(w * 0.472f, h * 0.34f),
            end = androidx.compose.ui.geometry.Offset(w * 0.528f, h * 0.34f)
        )

        // 7. Eyes behind glasses: Dot eyes
        drawCircle(color = Color.Black, radius = w * 0.024f, center = leftLensCenter)
        drawCircle(color = Color.Black, radius = w * 0.024f, center = rightLensCenter)

        // 8. Nose: Cute round button nose
        drawCircle(
            color = Color(0xFFFFB74D),
            radius = w * 0.042f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.41f)
        )

        // 9. Mii Mustache: overlapping white capsules
        drawRoundRect(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.46f + beardYOffset * 0.8f),
            size = androidx.compose.ui.geometry.Size(w * 0.19f, h * 0.08f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )
        drawRoundRect(
            color = Color(0xFFF5F5F5),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.49f, h * 0.46f + beardYOffset * 0.8f),
            size = androidx.compose.ui.geometry.Size(w * 0.19f, h * 0.08f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // 10. Voice Mouth: Classic dynamic simple line/crescent
        val marxMouthY = h * 0.54f
        if (mouthOffset > 0.1f) {
            drawCircle(
                color = Color(0xFF3E2723),
                radius = w * 0.035f + mouthOffset * 0.3f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, marxMouthY)
            )
            drawCircle(
                color = Color(0xFFE57373),
                radius = w * 0.018f + mouthOffset * 0.15f,
                center = androidx.compose.ui.geometry.Offset(w * 0.50f, marxMouthY + 4f)
            )
        } else {
            // Simple wavy senior smile
            val smilePath = Path().apply {
                moveTo(w * 0.45f, marxMouthY)
                quadraticTo(w * 0.5f, marxMouthY + h * 0.04f, w * 0.55f, marxMouthY)
            }
            drawPath(
                path = smilePath,
                color = Color(0xFF2D1A1A),
                style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

val SineToLinearEasing: Easing = Easing { fraction ->
    kotlin.math.sin(fraction * Math.PI.toFloat() - Math.PI.toFloat() / 2f) * 0.5f + 0.5f
}
