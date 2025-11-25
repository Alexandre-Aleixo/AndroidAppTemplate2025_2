package com.ifpr.androidapptemplate.utils

import com.ifpr.androidapptemplate.R

object CorHelper {

    val iconeParaCorMap: Map<Int, Int> = mapOf(
        R.drawable.ic_home_black_24dp to R.color.color_primary_light,
        R.drawable.ic_work_black_24dp to R.color.purple_200,
        R.drawable.ic_shopping_cart_black_24dp to R.color.teal_200,
        R.drawable.ic_school_black_24dp to R.color.blue_light,
        R.drawable.ic_person_black_24dp to R.color.green_dark,
    )

    val COR_PADRAO: Int = R.color.gray_dark

    fun getColorResId(iconeResId: Int): Int {
        return iconeParaCorMap[iconeResId] ?: COR_PADRAO
    }
}