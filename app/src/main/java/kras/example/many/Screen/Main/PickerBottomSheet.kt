package kras.example.many.Screen.Main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kras.example.many.Color.MyColor
import kras.example.many.MySpacer
import kras.example.many.MyTxt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "Готово",
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MyColor.BOX,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MyColor.SUBTITLE.copy(alpha = 0.4f))
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            MyTxt(text = title, fontSize = 20, fontWeight = 600)
            MySpacer(height = 16)
            content()
            MySpacer(height = 20)
            SheetConfirmButton(text = confirmText, onClick = onConfirm)
        }
    }
}

@Composable
fun SheetConfirmButton(
    onClick: () -> Unit,
    text: String = "Готово",
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MyColor.SECTION_TITLE)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        MyTxt(text = text, color = MyColor.BACKGROUND, fontWeight = 600, fontSize = 16)
    }
}
