package com.example.firstapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstapplication.ui.theme.ActiveFontColor
import com.example.firstapplication.ui.theme.ButtonPressedBack
import com.example.firstapplication.ui.theme.MenuBack
import com.example.firstapplication.ui.theme.MenuVERYBack

@Composable
fun MainWindow(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val baseUrl = "http://10.0.2.2:8000"

    // Состояние для переключения между режимами
    var selectedMode by remember { mutableStateOf<InputMode?>(null) }

    // Launcher для выбора файла
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectFile(it, context)
            selectedMode = InputMode.FILE
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuVERYBack)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Piano Roll Converter",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ActiveFontColor,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
        )
        Text(
            text = "Convert audio to MIDI notes",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Показываем результат если есть
        viewModel.transcriptionResult?.let { result ->
            ResultCard(
                result = result,
                onDismiss = { viewModel.clearResult() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Показываем ошибку если есть
        viewModel.errorMessage?.let { error ->
            ErrorCard(
                message = error,
                onDismiss = { viewModel.clearError() }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Индикатор загрузки
        AnimatedVisibility(
            visible = viewModel.isTranscribing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingCard()
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Выбор режима ввода
        if (!viewModel.isTranscribing && viewModel.transcriptionResult == null) {
            when (selectedMode) {
                null -> {
                    // Начальный экран - выбор режима
                    Text(
                        text = "Choose input method",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InputModeCard(
                            icon = Icons.Default.AudioFile,
                            title = "Upload File",
                            subtitle = "MP3, WAV",
                            modifier = Modifier.weight(1f),
                            onClick = { fileLauncher.launch("audio/*") }
                        )

                        InputModeCard(
                            icon = Icons.Default.Link,
                            title = "YouTube",
                            subtitle = "Paste link",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedMode = InputMode.YOUTUBE }
                        )
                    }
                }

                InputMode.FILE -> {
                    // Режим файла
                    FileInputSection(
                        fileName = viewModel.selectedFileName,
                        fileSize = viewModel.selectedFileSize,
                        isTranscribing = viewModel.isTranscribing,
                        onSelectFile = { fileLauncher.launch("audio/*") },
                        onConvert = { viewModel.transcribeFromFile(context, baseUrl) },
                        onBack = {
                            selectedMode = null
                            viewModel.clearSelectedFile()
                        }
                    )
                }

                InputMode.YOUTUBE -> {
                    // Режим YouTube
                    YoutubeInputSection(
                        link = viewModel.youtubeLink,
                        onLinkChange = { viewModel.updateYoutubeLink(it) },
                        isTranscribing = viewModel.isTranscribing,
                        onConvert = { viewModel.transcribeFromYoutube(baseUrl) },
                        onBack = { selectedMode = null }
                    )
                }
            }
        }
    }
}

enum class InputMode {
    FILE, YOUTUBE
}

@Composable
private fun InputModeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MenuBack),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ActiveFontColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ActiveFontColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun FileInputSection(
    fileName: String?,
    fileSize: String?,
    isTranscribing: Boolean,
    onSelectFile: () -> Unit,
    onConvert: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кнопка назад
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Карточка с выбранным файлом или кнопка выбора
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (fileName != null) ButtonPressedBack else MenuBack
            ),
            shape = RoundedCornerShape(20.dp),
            onClick = { if (fileName == null) onSelectFile() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (fileName != null) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = null,
                        tint = ActiveFontColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = fileName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    fileSize?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onSelectFile) {
                            Text("Change file")
                        }
                        Button(
                            onClick = onConvert,
                            enabled = !isTranscribing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActiveFontColor,
                                contentColor = MenuVERYBack
                            )
                        ) {
                            Text("Convert")
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ActiveFontColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = ActiveFontColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select audio file",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MP3 or WAV",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun YoutubeInputSection(
    link: String,
    onLinkChange: (String) -> Unit,
    isTranscribing: Boolean,
    onConvert: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кнопка назад
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Карточка с полем ввода
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MenuBack),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = ActiveFontColor,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "YouTube Link",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = link,
                    onValueChange = onLinkChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://youtube.com/watch?v=...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActiveFontColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onConvert,
                    enabled = link.isNotBlank() && !isTranscribing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActiveFontColor,
                        contentColor = MenuVERYBack,
                        disabledContainerColor = ActiveFontColor.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Convert",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MenuBack),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = ActiveFontColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Processing audio...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "This may take a moment",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ResultCard(
    result: PianoResponse,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ButtonPressedBack),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ActiveFontColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Success!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActiveFontColor
                    )
                    Text(
                        text = "${result.notes.size} notes detected",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Дополнительная информация
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                result.key?.let { key ->
                    InfoChip(icon = Icons.Default.MusicNote, text = key)
                }
                result.tempo?.let { tempo ->
                    InfoChip(icon = Icons.Default.Speed, text = "$tempo BPM")
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .background(ActiveFontColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Center.Vertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ActiveFontColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = ActiveFontColor
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}