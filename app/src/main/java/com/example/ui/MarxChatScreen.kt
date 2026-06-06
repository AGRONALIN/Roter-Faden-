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
import androidx.navigation.NavController
import com.example.data.ChatMessage
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.*
import kotlinx.coroutines.launch

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
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    val userQuestions = chatHistory.filter { it.role == "user" }
                    if (userQuestions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Fragen bisher.\nStelle Karl Marx eine Frage über das Kapital, Ausbeutung oder die Revolution!",
                                color = ImmersiveTextSecondary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
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
                                                .padding(12.dp)
                                                .widthIn(max = 280.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "DU (GENOSSE)",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = CreamRed,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = message.text,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = ImmersiveTextPrimary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Hidden model response list because Marx speaks directly through his figure bust & main speech bubble!
                                    // But we can show a small log or historical bubble if needed. Let's keep it strictly speaking-centric
                                    // as requested: "marx soll aber nicht als chat antworten sondern diese kleine figur sein und in eine sprechblase sprechen"
                                }
                            }
                        }
                    }
                }

                // Middle Section: Karl Marx Custom Dynamic Figure + His Speach Bubble!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Speech Bubble over Marx
                    MarxSpeechBubble(
                        text = speechBubbleText,
                        isThinking = isThinking
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Low-Poly Animated Karl Marx Bust
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .shadow(8.dp, CircleShape)
                            .background(ImmersiveSurface, CircleShape)
                            .border(2.dp, ImmersiveGreen, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (customMarxImagePath != null) {
                            val infiniteTransition = rememberInfiniteTransition(label = "marx_img_breath")
                            val translationY by infiniteTransition.animateFloat(
                                initialValue = -2f,
                                targetValue = 2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = SineToLinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "floatingY"
                            )
                            AsyncImage(
                                model = customMarxImagePath,
                                contentDescription = "Karl Marx custom image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(y = translationY.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            MarxLowPolyBust(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
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
    isThinking: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(CreamRed, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "KARL MARX:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFF3D2C1), // rich gold/cream
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
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
                        text = "Schreibt",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    BouncingDots()
                }
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
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
                .background(Color.White, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .scaleAnimation(dot2Scale)
                .background(Color.White, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .scaleAnimation(dot3Scale)
                .background(Color.White, CircleShape)
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
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "marx_breath")
    val translationY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = SineToLinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingY"
    )

    Canvas(
        modifier = modifier
            .offset(y = translationY.dp)
    ) {
        val w = size.width
        val h = size.height

        // Colors matching low-poly shading
        val hairColorDark = Color(0xFF5A5F63)
        val hairColorMedium = Color(0xFFC5C9CD)
        val hairColorLight = Color(0xFFEDEFF1)
        val faceColorShadow = Color(0xFFE5B599)
        val faceColorMedium = Color(0xFFF7D0BC)
        val faceColorLight = Color(0xFFFFE0D0)
        val coatColorDark = Color(0xFF232B2B)
        val coatColorShadow = Color(0xFF141919)
        val beardColorShadow = Color(0xFFBCC2C6)
        val beardColorLight = Color(0xFFF5F7F8)
        val beardColorPure = Color(0xFFFFFEFE)

        // Draw Suit / Shoulders
        val suitPath = Path().apply {
            moveTo(w * 0.15f, h * 0.95f)
            lineTo(w * 0.35f, h * 0.75f)
            lineTo(w * 0.5f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.75f)
            lineTo(w * 0.85f, h * 0.95f)
            close()
        }
        drawPath(suitPath, coatColorDark)

        val suitLeftFacet = Path().apply {
            moveTo(w * 0.15f, h * 0.95f)
            lineTo(w * 0.35f, h * 0.75f)
            lineTo(w * 0.5f, h * 0.85f)
            close()
        }
        drawPath(suitLeftFacet, coatColorShadow)

        // Shirt Collar (triangle)
        val collarPath = Path().apply {
            moveTo(w * 0.43f, h * 0.75f)
            lineTo(w * 0.5f, h * 0.82f)
            lineTo(w * 0.57f, h * 0.75f)
            close()
        }
        drawPath(collarPath, Color.White)

        // Draw Hair (Bushy background facets)
        val leftHairPath = Path().apply {
            moveTo(w * 0.3f, h * 0.5f)
            lineTo(w * 0.15f, h * 0.32f)
            lineTo(w * 0.25f, h * 0.18f)
            lineTo(w * 0.45f, h * 0.15f)
            lineTo(w * 0.45f, h * 0.38f)
            close()
        }
        drawPath(leftHairPath, hairColorDark)

        val rightHairPath = Path().apply {
            moveTo(w * 0.7f, h * 0.5f)
            lineTo(w * 0.85f, h * 0.32f)
            lineTo(w * 0.75f, h * 0.18f)
            lineTo(w * 0.55f, h * 0.15f)
            lineTo(w * 0.55f, h * 0.38f)
            close()
        }
        drawPath(rightHairPath, hairColorLight)

        // Faceted Top Hair
        val topHairLeft = Path().apply {
            moveTo(w * 0.25f, h * 0.18f)
            lineTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.5f, h * 0.22f)
            lineTo(w * 0.4f, h * 0.24f)
            close()
        }
        drawPath(topHairLeft, hairColorMedium)

        val topHairRight = Path().apply {
            moveTo(w * 0.75f, h * 0.18f)
            lineTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.5f, h * 0.22f)
            lineTo(w * 0.6f, h * 0.24f)
            close()
        }
        drawPath(topHairRight, hairColorLight)

        // Face Base
        val faceLeft = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.32f, h * 0.28f)
            lineTo(w * 0.35f, h * 0.55f)
            lineTo(w * 0.5f, h * 0.62f)
            close()
        }
        drawPath(faceLeft, faceColorShadow)

        val faceRight = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.68f, h * 0.28f)
            lineTo(w * 0.65f, h * 0.55f)
            lineTo(w * 0.5f, h * 0.62f)
            close()
        }
        drawPath(faceRight, faceColorLight)

        val foreheadFacet = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.42f, h * 0.25f)
            lineTo(w * 0.5f, h * 0.32f)
            lineTo(w * 0.58f, h * 0.25f)
            close()
        }
        drawPath(foreheadFacet, faceColorMedium)

        // Eyes Faceted
        val leftEye = Path().apply {
            moveTo(w * 0.4f, h * 0.36f)
            lineTo(w * 0.46f, h * 0.37f)
            lineTo(w * 0.44f, h * 0.39f)
            close()
        }
        drawPath(leftEye, Color(0xFF1B1F22))

        val rightEye = Path().apply {
            moveTo(w * 0.6f, h * 0.36f)
            lineTo(w * 0.54f, h * 0.37f)
            lineTo(w * 0.56f, h * 0.39f)
            close()
        }
        drawPath(rightEye, Color(0xFF1B1F22))

        // Left eyebrow
        val leftEyebrow = Path().apply {
            moveTo(w * 0.36f, h * 0.32f)
            lineTo(w * 0.47f, h * 0.34f)
            lineTo(w * 0.44f, h * 0.31f)
            close()
        }
        drawPath(leftEyebrow, Color.DarkGray)

        // Right eyebrow
        val rightEyebrow = Path().apply {
            moveTo(w * 0.64f, h * 0.32f)
            lineTo(w * 0.53f, h * 0.34f)
            lineTo(w * 0.56f, h * 0.31f)
            close()
        }
        drawPath(rightEyebrow, Color.DarkGray)

        // Nose Facets
        val noseLeft = Path().apply {
            moveTo(w * 0.5f, h * 0.32f)
            lineTo(w * 0.46f, h * 0.48f)
            lineTo(w * 0.5f, h * 0.5f)
            close()
        }
        drawPath(noseLeft, faceColorShadow)

        val noseRight = Path().apply {
            moveTo(w * 0.5f, h * 0.32f)
            lineTo(w * 0.54f, h * 0.48f)
            lineTo(w * 0.5f, h * 0.5f)
            close()
        }
        drawPath(noseRight, faceColorLight)

        // Beard (Faceted layout that wraps around face and chin)
        val beardLeftTop = Path().apply {
            moveTo(w * 0.32f, h * 0.44f)
            lineTo(w * 0.22f, h * 0.6f)
            lineTo(w * 0.4f, h * 0.72f)
            lineTo(w * 0.42f, h * 0.53f)
            close()
        }
        drawPath(beardLeftTop, beardColorShadow)

        val beardRightTop = Path().apply {
            moveTo(w * 0.68f, h * 0.44f)
            lineTo(w * 0.78f, h * 0.6f)
            lineTo(w * 0.6f, h * 0.72f)
            lineTo(w * 0.58f, h * 0.53f)
            close()
        }
        drawPath(beardRightTop, beardColorLight)

        val beardBottomLeft = Path().apply {
            moveTo(w * 0.4f, h * 0.72f)
            lineTo(w * 0.35f, h * 0.88f)
            lineTo(w * 0.5f, h * 0.95f)
            lineTo(w * 0.5f, h * 0.78f)
            close()
        }
        drawPath(beardBottomLeft, beardColorShadow)

        val beardBottomRight = Path().apply {
            moveTo(w * 0.6f, h * 0.72f)
            lineTo(w * 0.65f, h * 0.88f)
            lineTo(w * 0.5f, h * 0.95f)
            lineTo(w * 0.5f, h * 0.78f)
            close()
        }
        drawPath(beardBottomRight, beardColorPure)

        // Mustache facets
        val mustacheLeft = Path().apply {
            moveTo(w * 0.42f, h * 0.53f)
            lineTo(w * 0.35f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.62f)
            close()
        }
        drawPath(mustacheLeft, beardColorShadow)

        val mustacheRight = Path().apply {
            moveTo(w * 0.58f, h * 0.53f)
            lineTo(w * 0.65f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.62f)
            close()
        }
        drawPath(mustacheRight, beardColorPure)
    }
}

val SineToLinearEasing: Easing = Easing { fraction ->
    kotlin.math.sin(fraction * Math.PI.toFloat() - Math.PI.toFloat() / 2f) * 0.5f + 0.5f
}
