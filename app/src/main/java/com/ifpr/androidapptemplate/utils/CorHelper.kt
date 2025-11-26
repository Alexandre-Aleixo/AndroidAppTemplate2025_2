package com.ifpr.androidapptemplate.utils

import com.ifpr.androidapptemplate.R

object CorHelper {

    // Mapeamento atualizado para a paleta ROXO/CIANO
    val iconeParaCorMap: Map<Int, Int> = mapOf(
        // Roxo (Cor Principal)
        R.drawable.ic_home_black_24dp to R.color.colorPrimary,

        // Variação de Roxo / Ciano
        R.drawable.ic_work_black_24dp to R.color.colorPrimaryVariant,

        // Ciano (Cor Secundária de Destaque)
        R.drawable.ic_shopping_cart_black_24dp to R.color.colorSecondary,

        // Ciano Variação
        R.drawable.ic_school_black_24dp to R.color.colorSecondaryVariant,

        // Vermelho/Alerta para a cor final
        R.drawable.ic_person_black_24dp to R.color.red_delete,
    )

    // COR PADRÃO: Usando a nova cor de texto secundário.
    val COR_PADRAO: Int = R.color.textColorSecondary

    fun getColorResId(iconeResId: Int): Int {
        return iconeParaCorMap[iconeResId] ?: COR_PADRAO
    }
}