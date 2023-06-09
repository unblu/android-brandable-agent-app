package com.unblu.brandeableagentapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unblu.brandeableagentapp.R
import com.unblu.brandeableagentapp.model.AuthenticationType
import com.unblu.brandeableagentapp.ui.theme.Rubik

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    inputHeight: Dp,
    inputBackground: Color,
    borderColor: Color,
    inputTextColor: Color,
    singleLine : Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    // Label
    Column(
        Modifier
            .fillMaxWidth()
    )
    {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Rubik,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        )
        Spacer(Modifier.width(4.dp))
        OutlinedTextField(
            shape = RoundedCornerShape(4.dp),
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .height(inputHeight)
                .background(color = inputBackground),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            textStyle = TextStyle(
                fontFamily = Rubik,
                fontSize = (inputHeight.value*0.35).sp),
            singleLine = singleLine,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = inputTextColor,
                cursorColor = inputTextColor,
                backgroundColor = Color.Transparent,
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                trailingIconColor = colorResource(id = R.color.login_input_text)
            )
        )
    }
}

data class AlertDialogColors(
    val background: Color,
    val confirmTextColor: Color,
    val cancelTextColor: Color,
    val titleColor: Color,
    val textColor: Color
)

object AlertDialogColorDefaults {
    @Composable
    fun alertDialogColors(): AlertDialogColors {
        return AlertDialogColors(
            background = colorResource(id = R.color.alert_dialog_background),
            textColor = colorResource(id = R.color.alert_dialog_text),
            titleColor = colorResource(id = R.color.alert_dialog_title),
            cancelTextColor = colorResource(id = R.color.alert_dialog_cancel_text),
            confirmTextColor = colorResource(id = R.color.alert_dialog_confirm_text)
        )
    }

}

@Composable
fun CustomAlertDialog(
    alertDialogColors: AlertDialogColors,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onCancel() },
        title = {
            Text(
                text = title,
                color = alertDialogColors.titleColor
            )
        },
        text = {
            Text(
                text = message,
                color = alertDialogColors.textColor
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(
                    text = confirmText,
                    color = alertDialogColors.confirmTextColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text(
                    text = cancelText,
                    color = alertDialogColors.cancelTextColor
                )
            }
        },
        backgroundColor = alertDialogColors.background
    )
}

@Composable
fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    LabeledTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        inputHeight = 52.dp,
        inputBackground = Color.White,
        borderColor = Color.Gray,
        inputTextColor = Color.Black,
        singleLine = false,
    )
}

@Composable
fun RadioButtonGroup(
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    defaultOption: AuthenticationType
) {
    Row(
        Modifier
            .fillMaxWidth()
    ) {
        options.forEach { option ->
            Row(
                Modifier
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(
                    selected = option == defaultOption.name,
                    onClick = {
                        onOptionSelected(option)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

fun Modifier.onDoubleClick(onDoubleClick: () -> Unit): Modifier = composed {
    val lastClickTimestamp = remember { mutableStateOf(0L) }
    val doubleClickMillis = 250
    this.clickable {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastClickTimestamp.value < doubleClickMillis) {
            onDoubleClick.invoke()
        }
        lastClickTimestamp.value = currentTimestamp
    }
}

@Preview(
    name = "Samsung Galaxy A51 Preview",
    widthDp = 392,  // approximate width in dp (1080px / 2.75)
    heightDp = 873  // approximate height in dp (2400px / 2.75)
)
@Composable
fun GalaxyA51Preview() {
    Surface {
        LabeledTextField(
            label = "Label",
            value = "Input",
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
            inputHeight = 56.dp,
            inputBackground = Color.LightGray,
            borderColor = Color.Gray,
            inputTextColor = Color.Black,
            singleLine = true
        )
    }
}