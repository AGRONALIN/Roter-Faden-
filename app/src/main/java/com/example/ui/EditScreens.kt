package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

private suspend fun animateTextTyping(target: String, onUpdate: (String) -> Unit) {
    if (target.isEmpty()) {
        onUpdate("")
        return
    }
    val length = target.length
    val steps = (length / 8).coerceIn(12, 35)
    val delayMs = (400L / steps).coerceIn(8L, 25L)
    for (i in 1..steps) {
        val currentLen = (length * i / steps).coerceIn(0, length)
        onUpdate(target.take(currentLen))
        kotlinx.coroutines.delay(delayMs)
    }
    onUpdate(target)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditArgumentScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    argId: Int = -1
) {
    val recentArguments by viewModel.recentArguments.collectAsState()
    val existingArg = remember(recentArguments, argId) { recentArguments.find { it.id == argId } }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("creation_drafts", android.content.Context.MODE_PRIVATE) }

    var antiMarxist by remember(existingArg) {
        mutableStateOf(existingArg?.antiMarxistStatement ?: "")
    }
    var marxist by remember(existingArg) {
        mutableStateOf(existingArg?.marxistCounterArgument ?: "")
    }
    var category by remember(existingArg) {
        mutableStateOf(existingArg?.category ?: "")
    }
    var imagePath by remember(existingArg) {
        mutableStateOf(existingArg?.imagePath)
    }

    val draftAntiMarxist = remember { prefs.getString("draft_arg_anti_marxist", "") ?: "" }
    val draftMarxist = remember { prefs.getString("draft_arg_marxist", "") ?: "" }
    val draftCategory = remember { prefs.getString("draft_arg_category", "") ?: "" }
    val draftImagePath = remember {
        val p = prefs.getString("draft_arg_image_path", "")
        if (p.isNullOrEmpty()) null else p
    }

    val hasDraft = remember(draftAntiMarxist, draftMarxist, draftCategory, draftImagePath) {
        draftAntiMarxist.isNotEmpty() || draftMarxist.isNotEmpty() || draftCategory.isNotEmpty() || draftImagePath != null
    }

    var showRestoreDraftSuggestion by remember {
        mutableStateOf(existingArg == null && hasDraft)
    }

    LaunchedEffect(antiMarxist, marxist, category, imagePath) {
        if (showRestoreDraftSuggestion && (antiMarxist.isNotEmpty() || marxist.isNotEmpty() || category.isNotEmpty() || imagePath != null)) {
            showRestoreDraftSuggestion = false
        }
    }

    LaunchedEffect(antiMarxist, marxist, category, imagePath, showRestoreDraftSuggestion) {
        if (existingArg == null && !showRestoreDraftSuggestion) {
            prefs.edit().apply {
                putString("draft_arg_anti_marxist", antiMarxist)
                putString("draft_arg_marxist", marxist)
                putString("draft_arg_category", category)
                putString("draft_arg_image_path", imagePath ?: "")
                apply()
            }
        }
    }

    val draftPreviewLines = remember(draftCategory, draftAntiMarxist, draftMarxist) {
        val list = mutableListOf<String>()
        val cleanAnti = draftAntiMarxist.replace(Regex("\\[image:[^\\]]+\\]"), "[BILD]").trim()
        val cleanMarx = draftMarxist.replace(Regex("\\[image:[^\\]]+\\]"), "[BILD]").trim()
        if (draftCategory.isNotBlank()) {
            list.add("Kategorie: ${draftCategory.trim()}")
        }
        if (cleanAnti.isNotBlank()) {
            val line = "These: $cleanAnti"
            list.add(if (line.length > 50) line.take(47) + "..." else line)
        }
        if (cleanMarx.isNotBlank()) {
            val line = "Gegenargument: $cleanMarx"
            list.add(if (line.length > 50) line.take(47) + "..." else line)
        }
        if (list.isEmpty()) {
            list.add("Leerer Entwurf")
        }
        list.take(3)
    }

    val previewText = remember(draftPreviewLines) {
        draftPreviewLines.joinToString("\n")
    }
    
    val scope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val copiedPath = copyUriToInternalStorage(context, it)
            if (copiedPath != null) {
                imagePath = copiedPath
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = context.contentResolver.openInputStream(it)?.use { inputStream ->
                    inputStream.bufferedReader().use { reader -> reader.readText() }
                }
                if (jsonStr != null) {
                    viewModel.importData(jsonStr)
                    navController.popBackStack()
                }
            }
        }
    }

        PullToDismissContainer(
            onDismiss = { navController.popBackStack() }
        ) { nestedScrollConnection ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ImmersiveBackground
        ) { padding ->
        Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 100.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding() + 80.dp)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedVisibility(
                    visible = showRestoreDraftSuggestion,
                    enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400), expandFrom = androidx.compose.ui.Alignment.CenterVertically),
                    exit = fadeOut(animationSpec = tween(400)) + shrinkVertically(animationSpec = tween(400), shrinkTowards = androidx.compose.ui.Alignment.CenterVertically)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Entwurf gefunden 📝", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = previewText,
                                    color = ImmersiveTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 3,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            showRestoreDraftSuggestion = false
                                            imagePath = draftImagePath
                                            launch {
                                                animateTextTyping(draftCategory) { category = it }
                                            }
                                            launch {
                                                animateTextTyping(draftAntiMarxist) { antiMarxist = it }
                                            }
                                            launch {
                                                animateTextTyping(draftMarxist) { marxist = it }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Herstellen", color = ImmersiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                IconButton(
                                    onClick = {
                                        prefs.edit().apply {
                                            remove("draft_arg_anti_marxist")
                                            remove("draft_arg_marxist")
                                            remove("draft_arg_category")
                                            remove("draft_arg_image_path")
                                            apply()
                                        }
                                        showRestoreDraftSuggestion = false
                                    }
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Verwerfen", tint = ImmersiveTextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = category,
                onValueChange = { category = it },
                label = { Text("Kategorie (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                )
            )

            InlineRichTextEditor(
                value = antiMarxist,
                onValueChange = { antiMarxist = it },
                placeholder = "Antimarxistisches Argument"
            )

            InlineRichTextEditor(
                value = marxist,
                onValueChange = { marxist = it },
                placeholder = "Marxistisches Gegenargument"
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val closeInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .bounceScale(closeInteractionSource)
                    .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                interactionSource = closeInteractionSource
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Abbrechen", tint = ImmersiveTextPrimary)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ImmersiveSurface)
                    .border(1.dp, ImmersiveBorder, RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (existingArg != null) "Argument bearbeiten" else "Neues Argument",
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            }

            if (existingArg == null) {
                val importInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                IconButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier
                        .bounceScale(importInteractionSource)
                        .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                    interactionSource = importInteractionSource
                ) {
                    Icon(Icons.Filled.Download, contentDescription = "Importieren", tint = ImmersiveGreen)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .padding(horizontal = 24.dp)
                .imePadding()
        ) {
            val btnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Button(
                onClick = {
                    if (antiMarxist.isNotBlank() && marxist.isNotBlank()) {
                        if (existingArg != null) {
                            viewModel.updateArgument(existingArg.copy(
                                antiMarxistStatement = antiMarxist,
                                marxistCounterArgument = marxist,
                                category = category,
                                imagePath = imagePath
                            ))
                        } else {
                            viewModel.insertArgument(antiMarxist, marxist, category, imagePath)
                            prefs.edit().apply {
                                remove("draft_arg_anti_marxist")
                                remove("draft_arg_marxist")
                                remove("draft_arg_category")
                                remove("draft_arg_image_path")
                                apply()
                            }
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(56.dp)
                    .bounceScale(btnInteractionSource)
                    .clip(androidx.compose.foundation.shape.CircleShape),
                interactionSource = btnInteractionSource,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                enabled = antiMarxist.isNotBlank() && marxist.isNotBlank()
            ) {
                Text(
                    "Speichern",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveOnGreen,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditGlossaryScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    termId: Int = -1
) {
    val glossaryItems by viewModel.glossaryItems.collectAsState()
    val existingItem = remember(glossaryItems, termId) { glossaryItems.find { it.id == termId } }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("creation_drafts", android.content.Context.MODE_PRIVATE) }

    var term by remember(existingItem) {
        mutableStateOf(existingItem?.term ?: "")
    }
    var definition by remember(existingItem) {
        mutableStateOf(existingItem?.definition ?: "")
    }
    var imagePath by remember(existingItem) {
        mutableStateOf(existingItem?.imagePath)
    }

    val draftTerm = remember { prefs.getString("draft_gloss_term", "") ?: "" }
    val draftDefinition = remember { prefs.getString("draft_gloss_definition", "") ?: "" }
    val draftImagePath = remember {
        val p = prefs.getString("draft_gloss_image_path", "")
        if (p.isNullOrEmpty()) null else p
    }

    val hasDraft = remember(draftTerm, draftDefinition, draftImagePath) {
        draftTerm.isNotEmpty() || draftDefinition.isNotEmpty() || draftImagePath != null
    }

    var showRestoreDraftSuggestion by remember {
        mutableStateOf(existingItem == null && hasDraft)
    }

    LaunchedEffect(term, definition, imagePath) {
        if (showRestoreDraftSuggestion && (term.isNotEmpty() || definition.isNotEmpty() || imagePath != null)) {
            showRestoreDraftSuggestion = false
        }
    }

    LaunchedEffect(term, definition, imagePath, showRestoreDraftSuggestion) {
        if (existingItem == null && !showRestoreDraftSuggestion) {
            prefs.edit().apply {
                putString("draft_gloss_term", term)
                putString("draft_gloss_definition", definition)
                putString("draft_gloss_image_path", imagePath ?: "")
                apply()
            }
        }
    }

    val draftPreviewLines = remember(draftTerm, draftDefinition, draftImagePath) {
        val list = mutableListOf<String>()
        val cleanTerm = draftTerm.trim()
        val cleanDef = draftDefinition.replace(Regex("\\[image:[^\\]]+\\]"), "[BILD]").trim()
        if (cleanTerm.isNotBlank()) {
            val line = "Begriff: $cleanTerm"
            list.add(if (line.length > 50) line.take(47) + "..." else line)
        }
        if (cleanDef.isNotBlank()) {
            val line = "Definition: $cleanDef"
            list.add(if (line.length > 50) line.take(47) + "..." else line)
        }
        if (list.size < 3 && draftImagePath != null) {
            list.add("[BILD angehängt]")
        }
        if (list.isEmpty()) {
            list.add("Leerer Entwurf")
        }
        list.take(3)
    }

    val previewText = remember(draftPreviewLines) {
        draftPreviewLines.joinToString("\n")
    }
    
    val scope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val copiedPath = copyUriToInternalStorage(context, it)
            if (copiedPath != null) {
                imagePath = copiedPath
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = context.contentResolver.openInputStream(it)?.use { inputStream ->
                    inputStream.bufferedReader().use { reader -> reader.readText() }
                }
                if (jsonStr != null) {
                    viewModel.importData(jsonStr)
                    navController.popBackStack()
                }
            }
        }
    }

    PullToDismissContainer(
        onDismiss = { navController.popBackStack() }
    ) { nestedScrollConnection ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ImmersiveBackground
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 100.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(top = padding.calculateTopPadding() + 80.dp)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    AnimatedVisibility(
                        visible = showRestoreDraftSuggestion,
                        enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400), expandFrom = androidx.compose.ui.Alignment.CenterVertically),
                        exit = fadeOut(animationSpec = tween(400)) + shrinkVertically(animationSpec = tween(400), shrinkTowards = androidx.compose.ui.Alignment.CenterVertically)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Entwurf gefunden 📝", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = previewText,
                                        color = ImmersiveTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 3,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                showRestoreDraftSuggestion = false
                                                imagePath = draftImagePath
                                                launch {
                                                    animateTextTyping(draftTerm) { term = it }
                                                }
                                                launch {
                                                    animateTextTyping(draftDefinition) { definition = it }
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Herstellen", color = ImmersiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            prefs.edit().apply {
                                                remove("draft_gloss_term")
                                                remove("draft_gloss_definition")
                                                remove("draft_gloss_image_path")
                                                apply()
                                            }
                                            showRestoreDraftSuggestion = false
                                        }
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Verwerfen", tint = ImmersiveTextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = term,
                        onValueChange = { term = it },
                        label = { Text("Begriff") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary,
                            focusedBorderColor = ImmersiveGreen,
                            focusedLabelColor = ImmersiveGreen,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedContainerColor = ImmersiveSurface,
                            unfocusedContainerColor = ImmersiveSurface
                        )
                    )

                    InlineRichTextEditor(
                        value = definition,
                        onValueChange = { definition = it },
                        placeholder = "Definition"
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    val closeInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .bounceScale(closeInteractionSource)
                            .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                        interactionSource = closeInteractionSource
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Abbrechen", tint = ImmersiveTextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ImmersiveSurface)
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(50))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (existingItem != null) "Begriff bearbeiten" else "Neuer Begriff",
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                    }

                    if (existingItem == null) {
                        val importInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        IconButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier
                                .bounceScale(importInteractionSource)
                                .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                            interactionSource = importInteractionSource
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "Importieren", tint = ImmersiveGreen)
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 24.dp)
                        .imePadding()
                ) {
                    val btnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Button(
                        onClick = {
                            if (term.isNotBlank() && definition.isNotBlank()) {
                                if (existingItem != null) {
                                    viewModel.updateGlossary(existingItem.copy(
                                        term = term,
                                        definition = definition,
                                        imagePath = imagePath
                                    ))
                                } else {
                                    viewModel.insertGlossary(term, definition, imagePath)
                                    prefs.edit().apply {
                                        remove("draft_gloss_term")
                                        remove("draft_gloss_definition")
                                        remove("draft_gloss_image_path")
                                        apply()
                                    }
                                }
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(56.dp)
                            .bounceScale(btnInteractionSource)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        interactionSource = btnInteractionSource,
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                        enabled = term.isNotBlank() && definition.isNotBlank()
                    ) {
                        Text(
                            "Begriff speichern",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditLiteratureScreen(
    viewModel: AppViewModel,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    litId: Int = -1
) {
    val literatureList by viewModel.literatureList.collectAsState()
    val existingLit = remember(literatureList, litId) { literatureList.find { it.id == litId } }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("creation_drafts", android.content.Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var title by remember(existingLit) {
        mutableStateOf(existingLit?.title ?: "")
    }
    var author by remember(existingLit) {
        mutableStateOf(existingLit?.author ?: "")
    }
    var summary by remember(existingLit) {
        mutableStateOf(existingLit?.summary ?: "")
    }
    var imagePath by remember(existingLit) {
        mutableStateOf(existingLit?.imagePath)
    }

    val draftTitle = remember { prefs.getString("draft_lit_title", "") ?: "" }
    val draftAuthor = remember { prefs.getString("draft_lit_author", "") ?: "" }
    val draftSummary = remember { prefs.getString("draft_lit_summary", "") ?: "" }
    val draftImagePath = remember {
        val p = prefs.getString("draft_lit_image_path", "")
        if (p.isNullOrEmpty()) null else p
    }

    val hasDraft = remember(draftTitle, draftAuthor, draftSummary, draftImagePath) {
        draftTitle.isNotEmpty() || draftAuthor.isNotEmpty() || draftSummary.isNotEmpty() || draftImagePath != null
    }

    var showRestoreDraftSuggestion by remember {
        mutableStateOf(existingLit == null && hasDraft)
    }

    LaunchedEffect(title, author, summary, imagePath) {
        if (showRestoreDraftSuggestion && (title.isNotEmpty() || author.isNotEmpty() || summary.isNotEmpty() || imagePath != null)) {
            showRestoreDraftSuggestion = false
        }
    }

    LaunchedEffect(title, author, summary, imagePath, showRestoreDraftSuggestion) {
        if (existingLit == null && !showRestoreDraftSuggestion) {
            prefs.edit().apply {
                putString("draft_lit_title", title)
                putString("draft_lit_author", author)
                putString("draft_lit_summary", summary)
                putString("draft_lit_image_path", imagePath ?: "")
                apply()
            }
        }
    }

    val draftPreviewLines = remember(draftTitle, draftAuthor, draftSummary, draftImagePath) {
        val list = mutableListOf<String>()
        val cleanTitle = draftTitle.trim()
        val cleanAuthor = draftAuthor.trim()
        val cleanSummary = draftSummary.replace(Regex("\\[image:[^\\]]+\\]"), "[BILD]").trim()
        if (cleanTitle.isNotBlank()) {
            val line = "Titel: $cleanTitle"
            list.add(if (line.length > 50) line.take(47) + "..." else line)
        }
        if (cleanAuthor.isNotBlank()) {
            val line = "Autor: $cleanAuthor"
            list.add(if (line.length > 50) line.take(47) + "..." else line)
        }
        if (cleanSummary.isNotBlank()) {
            val line = "Inhalt: $cleanSummary"
            list.add(if (line.length > 50) line.take(47) + "..." else line)
        }
        if (list.size < 3 && draftImagePath != null) {
            list.add("[BILD angehängt]")
        }
        if (list.isEmpty()) {
            list.add("Leerer Entwurf")
        }
        list.take(3)
    }

    val previewText = remember(draftPreviewLines) {
        draftPreviewLines.joinToString("\n")
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val copiedPath = copyUriToInternalStorage(context, it)
            if (copiedPath != null) {
                imagePath = copiedPath
            }
        }
    }

    PullToDismissContainer(
        onDismiss = { navController.popBackStack() }
    ) { nestedScrollConnection ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ImmersiveBackground
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalFadingEdge(topEdge = 20.dp, bottomEdge = 100.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(top = padding.calculateTopPadding() + 80.dp)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    AnimatedVisibility(
                        visible = showRestoreDraftSuggestion,
                        enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400), expandFrom = androidx.compose.ui.Alignment.CenterVertically),
                        exit = fadeOut(animationSpec = tween(400)) + shrinkVertically(animationSpec = tween(400), shrinkTowards = androidx.compose.ui.Alignment.CenterVertically)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Entwurf gefunden 📝", color = ImmersiveTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = previewText,
                                        color = ImmersiveTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 3,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                showRestoreDraftSuggestion = false
                                                imagePath = draftImagePath
                                                launch {
                                                    animateTextTyping(draftTitle) { title = it }
                                                }
                                                launch {
                                                    animateTextTyping(draftAuthor) { author = it }
                                                }
                                                launch {
                                                    animateTextTyping(draftSummary) { summary = it }
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Herstellen", color = ImmersiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            prefs.edit().apply {
                                                remove("draft_lit_title")
                                                remove("draft_lit_author")
                                                remove("draft_lit_summary")
                                                remove("draft_lit_image_path")
                                                apply()
                                            }
                                            showRestoreDraftSuggestion = false
                                        }
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Verwerfen", tint = ImmersiveTextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titel") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary,
                            focusedBorderColor = ImmersiveGreen,
                            focusedLabelColor = ImmersiveGreen,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedContainerColor = ImmersiveSurface,
                            unfocusedContainerColor = ImmersiveSurface
                        )
                    )

                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Autor(en)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ImmersiveTextPrimary,
                            unfocusedTextColor = ImmersiveTextPrimary,
                            focusedBorderColor = ImmersiveGreen,
                            focusedLabelColor = ImmersiveGreen,
                            unfocusedBorderColor = ImmersiveBorder,
                            focusedContainerColor = ImmersiveSurface,
                            unfocusedContainerColor = ImmersiveSurface
                        )
                    )

                    InlineRichTextEditor(
                        value = summary,
                        onValueChange = { summary = it },
                        placeholder = "Zusammenfassung / Inhalt"
                    )

                    Spacer(modifier = Modifier.height(100.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    val closeInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .bounceScale(closeInteractionSource)
                            .background(ImmersiveSurface, androidx.compose.foundation.shape.CircleShape),
                        interactionSource = closeInteractionSource
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Abbrechen", tint = ImmersiveTextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ImmersiveSurface)
                            .border(1.dp, ImmersiveBorder, RoundedCornerShape(50))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (existingLit != null) "Literatur bearbeiten" else "Neue Literatur",
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.size(48.dp))
                }

                Box(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 24.dp)
                        .imePadding()
                ) {
                    val btnInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && author.isNotBlank() && summary.isNotBlank()) {
                                if (existingLit != null) {
                                    viewModel.updateLiterature(existingLit.copy(
                                        title = title,
                                        author = author,
                                        summary = summary,
                                        imagePath = imagePath
                                    ))
                                } else {
                                    viewModel.insertLiterature(title, author, summary, imagePath)
                                    prefs.edit().apply {
                                        remove("draft_lit_title")
                                        remove("draft_lit_author")
                                        remove("draft_lit_summary")
                                        remove("draft_lit_image_path")
                                        apply()
                                    }
                                }
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(56.dp)
                            .bounceScale(btnInteractionSource)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        interactionSource = btnInteractionSource,
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGreen),
                        enabled = title.isNotBlank() && author.isNotBlank() && summary.isNotBlank()
                    ) {
                        Text(
                            "Speichern",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveOnGreen,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}
