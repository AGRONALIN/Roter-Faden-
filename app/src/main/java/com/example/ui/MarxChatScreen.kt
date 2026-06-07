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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarxChatScreen(
    viewModel: AppViewModel,
    navController: NavController
) {
    val chatHistory by viewModel.marxChatHistory.collectAsState()
    val isThinking by viewModel.isMarxThinking.collectAsState()
    val speechBubbleText by viewModel.marxSpeechBubbleText.collectAsState()
    val customMarxImagePath by viewModel.customMarxImagePath.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    
    var isMarxSpeaking by remember { mutableStateOf(false) }
    var isKalleSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(speechBubbleText) {
        // Trigger speaking animations when speech content updates and is a valid speech
        if (speechBubbleText.isNotBlank() && 
            !speechBubbleText.startsWith("Karl Marx schärft") && 
            chatHistory.isNotEmpty()
        ) {
            isMarxSpeaking = true
            kotlinx.coroutines.delay(6500) // Animated active speech for 6.5s
            isMarxSpeaking = false
        }
    }

    // Auto trigger Kalle speaking movement when chatHistory increases (user asks a novel question)
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty() && chatHistory.last().role == "user") {
            isKalleSpeaking = true
            kotlinx.coroutines.delay(3500) // speaks for 3.5s
            isKalleSpeaking = false
        }
    }

    val isAngry = remember(speechBubbleText, isMarxSpeaking) {
        isMarxSpeaking && (
            speechBubbleText.contains("Ausbeut", ignoreCase = true) ||
            speechBubbleText.contains("Klassenkampf", ignoreCase = true) ||
            speechBubbleText.contains("Bourgeois", ignoreCase = true) ||
            speechBubbleText.contains("Kapital", ignoreCase = true) ||
            speechBubbleText.contains("Revolution", ignoreCase = true) ||
            speechBubbleText.contains("Ausbeuter", ignoreCase = true) ||
            speechBubbleText.contains("Arbeiterklasse", ignoreCase = true) ||
            speechBubbleText.contains("Kapitalismus", ignoreCase = true) ||
            speechBubbleText.contains("System", ignoreCase = true) ||
            speechBubbleText.contains("Geld", ignoreCase = true) ||
            speechBubbleText.contains("!", ignoreCase = true) ||
            speechBubbleText.length > 70
        )
    }

    BackHandler {
        viewModel.triggerReturnFromMarxTransition()
        navController.popBackStack()
    }

    // Auto scroll down user questions when a new message is added
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Karl Marx Studio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = ImmersiveTextPrimary
                            )
                        )
                        Text(
                            text = "Die absolute Wahrheit des Proletariats ☭",
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
                        },
                        modifier = Modifier.testTag("marx_chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Zurück",
                            tint = ImmersiveTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearMarxChat() },
                        modifier = Modifier.testTag("marx_chat_clear_button")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Upper Section: Scrollable history of user questions (comrades asking questions)
                Box(
                    modifier = Modifier
                        .weight(0.5f) // Keep it smaller so the theatre stage gets plenty of space!
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    val userQuestions = chatHistory.filter { it.role == "user" }
                    if (userQuestions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Fragen bisher. Genosse Proletarier wartet darauf, Marx herauszufordern!",
                                color = ImmersiveTextSecondary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatHistory) { message ->
                                if (message.role == "user") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                                                .background(CreamRed.copy(alpha = 0.15f))
                                                .border(1.dp, CreamRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp))
                                                .padding(10.dp)
                                                .widthIn(max = 280.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "GENOSSE INTERPELLANT",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = CreamRed,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = message.text,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = ImmersiveTextPrimary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Middle Section: Karl Marx & Proletariat Interactive Dialogue Stage!
                Column(
                    modifier = Modifier
                        .weight(1.8f) // Let it occupy a healthy amount of space!
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    
                    // Setup speaking and thinking dynamic values for both LowPoly and custom Image
                    val infiniteTransition = rememberInfiniteTransition(label = "marx_all_anims")
                    
                    val bobbingDuration = if (isThinking) 130 else if (isMarxSpeaking) 320 else 1800
                    val bobbingRange = if (isThinking) 1.2f else if (isMarxSpeaking) 6.0f else 2.5f
                    
                    val marxTranslationY by infiniteTransition.animateFloat(
                        initialValue = -bobbingRange,
                        targetValue = bobbingRange,
                        animationSpec = infiniteRepeatable(
                            animation = tween(bobbingDuration, easing = SineToLinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "marxTranslationY"
                    )

                    val maxTilt = if (isThinking) 2.5f else if (isMarxSpeaking) (if (isAngry) 7f else 4f) else 0f
                    val tiltDuration = if (isThinking) 75 else if (isMarxSpeaking) (if (isAngry) 150 else 350) else 1250
                    val marxRotationZ by infiniteTransition.animateFloat(
                        initialValue = -maxTilt,
                        targetValue = maxTilt,
                        animationSpec = infiniteRepeatable(
                            animation = tween(tiltDuration, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "marxRotationZ"
                    )

                    val eyebrowMaxOffset = if (isThinking) -2f else if (isMarxSpeaking) -4f else 0f
                    val eyebrowDuration = if (isThinking) 150 else if (isMarxSpeaking) 220 else 1000
                    val eyebrowOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = eyebrowMaxOffset,
                        animationSpec = infiniteRepeatable(
                            animation = tween(eyebrowDuration, easing = SineToLinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "eyebrowOffset"
                    )

                    val mouthMaxOffset = if (isMarxSpeaking || isThinking) 5.0f else 0f
                    val mouthDuration = if (isThinking) 95 else 140
                    val mouthOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = mouthMaxOffset,
                        animationSpec = infiniteRepeatable(
                            animation = tween(mouthDuration, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "mouthOffset"
                    )

                    // Proletariat (Kalle) movement values based on speaking/listening
                    val kalleBobbingDuration = if (isKalleSpeaking) 250 else 2000
                    val kalleBobbingRange = if (isKalleSpeaking) 5.0f else 2.0f
                    val kalleTranslationY by infiniteTransition.animateFloat(
                        initialValue = -kalleBobbingRange,
                        targetValue = kalleBobbingRange,
                        animationSpec = infiniteRepeatable(
                            animation = tween(kalleBobbingDuration, easing = SineToLinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "kalleTranslationY"
                    )

                    val kalleTilt = if (isKalleSpeaking) 3.5f else 0.5f
                    val kalleRotationZ by infiniteTransition.animateFloat(
                        initialValue = -kalleTilt,
                        targetValue = kalleTilt,
                        animationSpec = infiniteRepeatable(
                            animation = tween(if (isKalleSpeaking) 300 else 1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "kalleRotationZ"
                    )

                    val kalleMouthOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = if (isKalleSpeaking) 4.5f else 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(120, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "kalleMouthOffset"
                    )

                    // Giant sweatdrop animation for Kalle when Marx goes berserk (angry)
                    val kalleSweatTranslationY by infiniteTransition.animateFloat(
                        initialValue = -12f,
                        targetValue = 28f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "kalleSweatTranslationY"
                    )

                    // 1. Dual Speech Bubbles Layout!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Kalle (Proletariat) Speech Bubble (Left aligned)
                        val lastUserQuestion = remember(chatHistory) {
                            chatHistory.lastOrNull { it.role == "user" }?.text ?: "Genosse Karl, mein Fabrikbesitzer behält allen Profit! Wie wehren wir uns?"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .align(Alignment.Start)
                        ) {
                            ProletariatSpeechBubble(
                                text = lastUserQuestion,
                                isSpeaking = isKalleSpeaking
                            )
                        }

                        // Marx Speech Bubble (Right aligned)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .align(Alignment.End)
                        ) {
                            MarxSpeechBubble(
                                text = speechBubbleText,
                                isThinking = isThinking,
                                isAngry = isAngry
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Interactive Character Stage Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // LEFT SIDE: Comrade Kalle
                        Box(
                            modifier = Modifier
                                .size(105.dp)
                                .shadow(6.dp, CircleShape)
                                .background(ImmersiveSurface, CircleShape)
                                .border(
                                    3.dp,
                                    if (isKalleSpeaking) ImmersiveGreen else ImmersiveBorder,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ProletariatLowPolyBust(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                translationY = kalleTranslationY,
                                rotationZ = kalleRotationZ,
                                eyebrowOffset = if (isAngry) -3f else 0f, // raise eyebrows under worry
                                mouthOffset = kalleMouthOffset,
                                isNervous = isAngry
                            )

                            // Animated Blue Sweatdrop sliding down when Marx is angry!
                            if (isAngry) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            this.translationY = with(density) { kalleSweatTranslationY.dp.toPx() }
                                        }
                                ) {
                                    val w = size.width
                                    val h = size.height
                                    // Draw persistent sweatdrop near left temple
                                    val dropPath = Path().apply {
                                        moveTo(w * 0.32f, h * 0.35f)
                                        quadraticTo(w * 0.30f, h * 0.42f, w * 0.32f, h * 0.45f)
                                        quadraticTo(w * 0.34f, h * 0.45f, w * 0.34f, h * 0.42f)
                                        close()
                                    }
                                    drawPath(dropPath, Color(0xFF29B6F6))
                                }
                            }
                        }

                        // MIDDLE: A small comic "DEBATTE" or "REVOLTE" badge
                        Box(
                            modifier = Modifier
                                .padding(bottom = 35.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .background(
                                    if (isAngry) Color(0xFFD50000) else ImmersiveGreen,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAngry) "⚡ REVOLTE ⚡" else "☭ DEBATTE ☭",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        // RIGHT SIDE: Karl Marx
                        // Integrate screen shaking to highlight Karl's intense rage!
                        val shakeX = if (isAngry) {
                            val pulseX by infiniteTransition.animateFloat(
                                initialValue = -4f,
                                targetValue = 4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(45, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseX"
                            )
                            pulseX
                        } else 0f

                        val shakeY = if (isAngry) {
                            val pulseY by infiniteTransition.animateFloat(
                                initialValue = -3f,
                                targetValue = 3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(40, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseY"
                            )
                            pulseY
                        } else 0f

                        Box(
                            modifier = Modifier
                                .size(105.dp)
                                .shadow(6.dp, CircleShape)
                                .background(
                                    if (isAngry) Color(0xFFFFCCBC) else ImmersiveSurface,
                                    CircleShape
                                )
                                .border(
                                    3.dp,
                                    if (isAngry) Color(0xFFB71C1C) else ImmersiveGreen,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .graphicsLayer {
                                    this.translationX = with(density) { shakeX.dp.toPx() }
                                    this.translationY = with(density) { (marxTranslationY + shakeY).dp.toPx() }
                                    this.rotationZ = marxRotationZ
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Angry background speed-lines/auras!
                            if (isAngry) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val r = size.width / 2f
                                    val c = center
                                    // Radially dynamic fire sparks
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color(0xFFFF3D00).copy(alpha = 0.45f), Color.Transparent),
                                            center = c,
                                            radius = r
                                        )
                                    )
                                }
                            }

                            if (customMarxImagePath != null) {
                                AsyncImage(
                                    model = customMarxImagePath,
                                    contentDescription = "Karl Marx custom image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            if (isMarxSpeaking) {
                                                this.scaleY = 1f + (mouthOffset / 60f)
                                                this.scaleX = 1f - (mouthOffset / 120f)
                                            } else if (isThinking) {
                                                val pulsingSizeVal = (mouthOffset / 100f)
                                                this.scaleX = 1f + pulsingSizeVal
                                                this.scaleY = 1f + pulsingSizeVal
                                            }
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                MarxLowPolyBust(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    translationY = 0f, // handled by box graphicsLayer
                                    rotationZ = 0f,    // handled by box graphicsLayer
                                    eyebrowOffset = if (isAngry) eyebrowOffset - 3f else eyebrowOffset, // lower heavy eyebrows in furor
                                    mouthOffset = mouthOffset
                                )
                            }

                            // Rising steam puff animations!
                            if (isAngry) {
                                AnimeSteamPuff(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = 10.dp, y = (-5).dp)
                                )
                                AnimeSteamPuff(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-10).dp, y = (-5).dp)
                                )
                            }

                            // Floating Anime Anger vein (💢) on top-right of Marx
                            if (isAngry) {
                                AnimeAngerVein(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-12).dp, y = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Section: Input Field for questions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .shadow(4.dp, RoundedCornerShape(28.dp))
                        .background(ImmersiveSurface, RoundedCornerShape(28.dp))
                        .border(1.dp, ImmersiveBorder, RoundedCornerShape(28.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Frage an Karl Marx...", color = ImmersiveTextSecondary) },
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
                            modifier = Modifier.size(20.dp)
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
