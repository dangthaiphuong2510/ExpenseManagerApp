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
        get() = setOf(Google)

    val IconGroups = mapOf(
        "Food & Drink" to listOf(
            "ic_food" to R.drawable.ic_food,
            "ic_pizza" to R.drawable.ic_food_pizza_slice,
            "ic_coffee" to R.drawable.ic_food_coffee,
            "ic_apple" to R.drawable.ic_food_apple,
            "ic_burger" to R.drawable.ic_food_hamburger_soda,
            "ic_cake" to R.drawable.ic_food_cake_wedding,
            "ic_grocery" to R.drawable.ic_food_grocery_basket,
            "ic_glass" to R.drawable.ic_food_glass_cheers,
            "ic_noodles" to R.drawable.ic_food_noodles,
            "ic_croissant" to R.drawable.ic_food_croissant,
            "ic_chef" to R.drawable.ic_food_hat_chef,
        ),

        "Transport" to listOf(
            "ic_transport" to R.drawable.ic_transport,
            "ic_car" to R.drawable.ic_transport_car_alt,
            "ic_bike" to R.drawable.ic_transport_biking,
            "ic_bus" to R.drawable.ic_transport_bus,
            "ic_truck" to R.drawable.ic_transport_truck,
            "ic_taxi" to R.drawable.ic_transport_car_alt,
            "ic_plan" to R.drawable.ic_transport_plane,
        ),

        "Shopping" to listOf(
            "ic_shopping" to R.drawable.ic_shopping,
            "ic_makeup" to R.drawable.ic_makeup,
            "ic_shirt" to R.drawable.ic_shopping_shirt,
            "ic_shop_bags" to R.drawable.ic_shopping_bags,
            "ic_cart" to R.drawable.ic_shopping_cart_heart,
            "ic_fresher" to R.drawable.ic_shopping_air_freshener,
            "ic_dress" to R.drawable.ic_shopping_dress,
            "ic_cream" to R.drawable.ic_shopping_cream,
        ),

        "Life & Health" to listOf(
            "ic_education" to R.drawable.ic_education,
            "ic_medical" to R.drawable.ic_medical,
            "ic_syringe" to R.drawable.ic_medical_syringe,
            "ic_tooth" to R.drawable.ic_medical_tooth,
            "ic_capsules" to R.drawable.ic_medical_capsules,
            "ic_stethoscope" to R.drawable.ic_medical_stethoscope,
            "ic_medication" to R.drawable.ic_medical_medication,

            "ic_book" to R.drawable.ic_edu_book,
            "ic_flask" to R.drawable.ic_edu_flask,
            "ic_calculator" to R.drawable.ic_edu_calculator,
            "ic_graduation" to R.drawable.ic_edu_graduation_cap,
            "ic_globe" to R.drawable.ic_edu_globe_alt,
            "ic_books" to R.drawable.ic_edu_books_lightbulb,
            "ic_drawer" to R.drawable.ic_edu_drawer_alt,

        ),

        "Finance" to listOf(
            "ic_money" to R.drawable.ic_money,
            "ic_wallet" to R.drawable.ic_wallet,
            "ic_coin" to R.drawable.ic_money_coins,
            "ic_money_usd" to R.drawable.ic_money_usd_circle,
            "ic_trading" to R.drawable.ic_money_trading,
            "ic_expense" to R.drawable.ic_money_expense,
            "ic_briefcase" to R.drawable.ic_money_briefcase,
        ),
        "Family" to listOf(
            "ic_family" to R.drawable.ic_family,
            "ic_family_dryer" to R.drawable.ic_family_dryer,
            "ic_family_light_ceiling" to R.drawable.ic_family_light_ceiling,
            "ic_family_light_bulb" to R.drawable.ic_family_sink,
            "ic_family_light_switch" to R.drawable.ic_family_couch,
            "ic_family_light_bulb_on" to R.drawable.ic_family_vacuum,
            "ic_family_light_bulb_off" to R.drawable.ic_family_bed_alt,
        ),

        "Home" to listOf(
            "ic_home" to R.drawable.ic_home,
            "ic_bank" to R.drawable.ic_home_bank,
            "ic_city" to R.drawable.ic_home_city,
            "ic_heart" to R.drawable.ic_home_heart,
            "ic_mosque" to R.drawable.ic_home_mosque,
            "ic_school" to R.drawable.ic_home_school,
            "ic_store" to R.drawable.ic_home_house_day,
        ),

        "Money" to listOf(
            "ic_money" to R.drawable.ic_money,
            "ic_wallet" to R.drawable.ic_wallet,
            "ic_coin" to R.drawable.ic_money_coins,
            "ic_money_usd" to R.drawable.ic_money_usd_circle,
            "ic_trading" to R.drawable.ic_money_trading,
            "ic_expense" to R.drawable.ic_money_expense,
            "ic_briefcase" to R.drawable.ic_money_briefcase,
        ),

        "Statistical" to listOf(
            "ic_report" to R.drawable.ic_report,
            "ic_chart" to R.drawable.ic_statistical_chart_simple,
            "ic_chart_pie" to R.drawable.ic_statistical_curve_arrow,
            "ic_chart_line" to R.drawable.ic_statistical_arrow_grow,
            "ic_chart_bar" to R.drawable.ic_statistical_revenue_euro,
            "ic_chart_graph_curve," to R.drawable.ic_statistical_graph_curve,

            ),

        "Others" to listOf(
            "ic_others" to R.drawable.ic_others
        )
    )

    val CategoryIconsList: List<Pair<String, Int>> = IconGroups.values.flatten()


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
    val Check = R.drawable.ic_check


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