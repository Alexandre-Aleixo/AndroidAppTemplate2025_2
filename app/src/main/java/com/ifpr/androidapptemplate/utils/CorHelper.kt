package com.ifpr.androidapptemplate.utils

import com.ifpr.androidapptemplate.R

object CorHelper {

    val iconeParaCorMap: Map<Int, Int> = mapOf(
        R.drawable.ic_home_black_24dp to R.color.colorPrimary,

        R.drawable.ic_work_black_24dp to R.color.colorPrimaryVariant,

        R.drawable.ic_shopping_cart_black_24dp to R.color.colorSecondary,

        R.drawable.ic_school_black_24dp to R.color.colorSecondaryVariant,

        R.drawable.ic_person_black_24dp to R.color.red_delete,
    )

    val COR_PADRAO: Int = R.color.textColorSecondary

    fun getColorResId(iconeResId: Int): Int {
        return iconeParaCorMap[iconeResId] ?: COR_PADRAO
    }
}