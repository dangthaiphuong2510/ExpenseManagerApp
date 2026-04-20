package com.example.expensemanager.designsystem.theme

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R

object AppIcons {

    private val ColoredIcons: Set<Int>
        get() = setOf(
            Google,
        )

    val CategoryIconsList = listOf(
        "ic_food" to R.drawable.ic_food,
        "ic_transport" to R.drawable.ic_transport,
        "ic_shopping" to R.drawable.ic_shopping,
        "ic_makeup" to R.drawable.ic_makeup,
        "ic_education" to R.drawable.ic_education,
        "ic_home" to R.drawable.ic_home,
        "ic_medical" to R.drawable.ic_medical,
        "ic_money" to R.drawable.ic_money,
        "ic_others" to R.drawable.ic_others
    )


    fun getIconIdByName(iconKey: String?): Int {
        if (iconKey.isNullOrBlank()) return R.drawable.ic_others

        val key = iconKey.trim().lowercase()

        val resId = CategoryIconsList.find { it.first == key }?.second
        if (resId != null) return resId

        val guessedKey = getIconKeyByName(key)
        return CategoryIconsList.find { it.first == guessedKey }?.second ?: R.drawable.ic_others
    }


    fun getIconKeyByName(name: String?): String {
        if (name.isNullOrBlank()) return "ic_others"

        val lowerName = name.trim().lowercase()

        return when {
            lowerName.contains("food") || lowerName.contains("eat") || lowerName.contains("lunch") ||
                    lowerName.contains("dinner") || lowerName.contains("coffee") -> "ic_food"

            lowerName.contains("transport") || lowerName.contains("car") || lowerName.contains("bike") ||
                    lowerName.contains("fuel") || lowerName.contains("gas") || lowerName.contains("grab") -> "ic_transport"

            lowerName.contains("shop") || lowerName.contains("buy") || lowerName.contains("market") ||
                    lowerName.contains("clothes") || lowerName.contains("mall") || lowerName.contains("shoes") -> "ic_shopping"

            lowerName.contains("makeup") || lowerName.contains("beauty") || lowerName.contains("cosmetic") ||
                    lowerName.contains("skincare") || lowerName.contains("salon") -> "ic_makeup"

            lowerName.contains("study") || lowerName.contains("school") || lowerName.contains("education") ||
                    lowerName.contains("book") || lowerName.contains("course") -> "ic_education"

            lowerName.contains("home") || lowerName.contains("house") || lowerName.contains("rent") ||
                    lowerName.contains("bill") || lowerName.contains("electricity") || lowerName.contains("water") -> "ic_home"

            lowerName.contains("health") || lowerName.contains("medical") || lowerName.contains("doctor") ||
                    lowerName.contains("hospital") || lowerName.contains("medicine") -> "ic_medical"

            lowerName.contains("salary") || lowerName.contains("money") || lowerName.contains("income") ||
                    lowerName.contains("bonus") || lowerName.contains("cash") -> "ic_money"

            else -> "ic_others"
        }
    }

    @Composable
    fun getIconByName(iconKey: String): ImageVector {
        val resId = getIconIdByName(iconKey)
        return ImageVector.vectorResource(id = resId)
    }

    // --- Navigation Icons ---
    val NavHome = R.drawable.ic_home
    val NavBudget = R.drawable.ic_wallet
    val NavCategory = R.drawable.ic_category
    val NavReport = R.drawable.ic_report
    val NavSettings = R.drawable.ic_settings

    // --- Auth Icons ---
    val Google = R.drawable.ic_google
    val Email = R.drawable.ic_email
    val Password = R.drawable.ic_pass
    val ConfirmPassword = R.drawable.ic_confirmpass
    val Eye = R.drawable.ic_eye
    val EyeCrossed = R.drawable.ic_eye_crossed
    val User = R.drawable.ic_user

    // --- Action Icons ---
    val Add = R.drawable.ic_add
    val Edit = R.drawable.ic_edit
    val Delete = R.drawable.ic_trash
    val Download = R.drawable.ic_download
    val ThemeMode = R.drawable.ic_thememode
    val SettingSliders = R.drawable.ic_settings_sliders
    val Menu = R.drawable.ic_menu
    val MenuDots = R.drawable.ic_menu_dots
    val Logout = R.drawable.ic_logout
    val Rate = R.drawable.ic_star

    val ChevronRight = R.drawable.ic_right
    val ChevronLeft = R.drawable.ic_left
    val More = Icons.Rounded.MoreVert
    val Notifications = R.drawable.ic_notification
    val Info = R.drawable.ic_info
    val Calendar = R.drawable.ic_calendar
    val Search = R.drawable.ic_search

    @Composable
    fun getAddIcon() = ImageVector.vectorResource(R.drawable.ic_add)

    @Composable
    fun MyIcon(
        resourceId: Int,
        modifier: Modifier = Modifier,
        size: Dp = 22.dp,
        tint: Color = if (ColoredIcons.contains(resourceId)) Color.Unspecified else LocalContentColor.current
    ) {
        Icon(
            painter = painterResource(id = resourceId),
            contentDescription = null,
            modifier = modifier.size(size),
            tint = tint
        )
    }
}