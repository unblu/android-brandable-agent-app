package com.unblu.brandeableagentapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import com.unblu.brandeableagentapp.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelHeight: Dp,
    inputHeight: Dp,
    inputBackground: Color,
    borderColor: Color,
    inputTextColor: Color,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    // Label
    Text(
        modifier = Modifier
            .padding(8.dp)
            .height(labelHeight),
        text = label
    )

    // Input
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(inputHeight)
            .background(color = inputBackground)
            .border(1.dp, borderColor, shape = RoundedCornerShape(4.dp))
            .padding(1.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize(),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            maxLines = 1,
            textStyle = TextStyle.Default.copy(fontSize = (inputHeight.value*0.3).sp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = inputTextColor,
                cursorColor = inputTextColor,
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
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
            background = colorResource(id =R.color.alert_dialog_background ),
            textColor = colorResource(id = R.color.alert_dialog_text ),
            titleColor = colorResource(id =R.color.alert_dialog_title ),
            cancelTextColor = colorResource(id =R.color.alert_dialog_cancel_text ),
            confirmTextColor = colorResource(id =R.color.alert_dialog_confirm_text))
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
