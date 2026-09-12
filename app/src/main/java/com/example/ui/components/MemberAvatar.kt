package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.PhotoStorageUtil
import java.io.File

@Composable
fun MemberAvatar(
    photoUri: String,
    name: String = "",
    size: Dp = 48.dp,
    isEditable: Boolean = false,
    memberIdForStorage: String = name,
    onPhotoChanged: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = PhotoStorageUtil.savePhotoToInternal(context, uri, memberIdForStorage)
            onPhotoChanged?.invoke(localPath)
        }
    }

    val displayModel: Any? = when {
        photoUri.isBlank() -> null
        photoUri.startsWith("/") -> File(photoUri)
        else -> photoUri
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (isEditable && onPhotoChanged != null) {
                    Modifier.clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (displayModel != null) {
            AsyncImage(
                model = displayModel,
                contentDescription = "সদস্যের ছবি",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF059669).copy(alpha = 0.6f), CircleShape)
            )
        } else {
            // Default avatar with initials or stylish person icon
            val initial = name.trim().firstOrNull()?.toString() ?: ""
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                        )
                    )
                    .border(1.dp, Color(0xFF94A3B8).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (initial.isNotBlank() && initial.length == 1 && !initial[0].isDigit()) {
                    Text(
                        text = initial,
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "ডিফল্ট ছবি",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }

        // Camera badge if editable
        if (isEditable && onPhotoChanged != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(maxOf(20.dp, size * 0.32f))
                    .clip(CircleShape)
                    .background(Color(0xFF059669))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "ছবি পরিবর্তন",
                    tint = Color.White,
                    modifier = Modifier.size(maxOf(12.dp, size * 0.18f))
                )
            }
        }
    }
}
