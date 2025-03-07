package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.android.material.chip.Chip
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.ui.components.SensitiveIcon
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData


@ExperimentalMaterialApi
@Composable
fun FilePropertySimpleCard(
    file: FileViewData,
    isSelectMode: Boolean = false,
    onCheckedChanged: (Boolean)->Unit,
    onDeleteMenuItemClicked: () -> Unit,
    onToggleNsfwMenuItemClicked: () -> Unit,
) {
    var actionMenuExpandedState by remember {
        mutableStateOf(false)
    }

    var confirmDeleteTargetId by remember {
        mutableStateOf<FileProperty.Id?>(null)
    }


    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp),
        backgroundColor = if(file.isSelected) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        },
        onClick = {
            if(isSelectMode) {
                onCheckedChanged.invoke(!file.isSelected)
            }else{
                actionMenuExpandedState = true
            }

        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier
                        .height(64.dp)
                        .width(64.dp)
                        .padding(end = 4.dp)
                ){
                    Image(
                        painter = rememberImagePainter(
                            file.fileProperty.thumbnailUrl
                                ?: file.fileProperty.url
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .height(64.dp)
                            .width(64.dp),
                        contentScale = ContentScale.Crop
                    )
                    if(file.fileProperty.isSensitive) {
                        SensitiveIcon()
                    }
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        file.fileProperty.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Row {
                        Text(
                            file.fileProperty.type,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            file.fileProperty.size.toString()
                        )
                    }
                }


            }
            Box(
                modifier = Modifier.align(Alignment.End)
            ){
                FileActionDropdownMenu(

                    expanded = actionMenuExpandedState,
                    onDismissRequest = {
                        actionMenuExpandedState = false
                    },
                    onNsfwMenuItemClicked = {
                        actionMenuExpandedState = false
                        onToggleNsfwMenuItemClicked()
                    },
                    onDeleteMenuItemClicked = {
                        actionMenuExpandedState = false
                        confirmDeleteTargetId = file.fileProperty.id
                    },
                    property = file.fileProperty
                )
            }

        }


    }
    if(confirmDeleteTargetId != null) {
        ConfirmDeleteFilePropertyDialog(
            filename = file.fileProperty.name,
            onDismissRequest = {
                confirmDeleteTargetId = null
            },
            onConfirmed = {
                confirmDeleteTargetId = null
                onDeleteMenuItemClicked()
            }
        )
    }

}

