package com.machfour.macros.core

import com.machfour.macros.objects.*
import com.machfour.macros.objects.inbuilt.Nutrients
import com.machfour.macros.objects.inbuilt.Nutrients.QUANTITY

// Map of Nutrients to NutrientValues
class NutrientData internal constructor (
    val dataCompleteIfNotNull: Boolean = true
) {

    private val data: Array<NutrientValue?> = arrayOfNulls(Nutrients.numNutrients)
    private val isDataComplete: Array<Boolean> = Array(Nutrients.numNutrients) { false }

    // TODO should this be passed in explicitly to show dependence?
    var isImmutable: Boolean = false
        private set
    fun setImmutable() {
        this.isImmutable = true
    }

    override fun equals(other: Any?): Boolean {
        return (other as? NutrientData)?.data?.contentDeepEquals(data) ?: false
    }

    override fun hashCode(): Int = data.contentHashCode()

    override fun toString(): String {
        val str = StringBuilder("NutrientData [")
        for (n in Nutrients.nutrients) {
            str.append("$n : ${get(n)}, ")
        }
        str.append("]")
        return str.toString()
    }

    fun clear() {
        for (i in data.indices) {
            data[i] = null
            isDataComplete[i] = false
        }
    }

    val nutrientValues: List<NutrientValue>
        get() {
            return data.filterNotNull()
        }
    val nutrientValuesExcludingQuantity: List<NutrientValue>
        get() {
            return nutrientValues.filter{ it !== quantityObj }
        }

    private fun assertMutable() {
        assert(!isImmutable) { "NutrientData has been made immutable" }
    }

    var quantityObj: NutrientValue
        get() {
            val quantityValue = this[QUANTITY]
            checkNotNull(quantityValue) { "Error - quantity value not initialised" }
            return quantityValue
        }
        set(value) {
            this[QUANTITY] = value
        }

    operator fun get(n: Nutrient): NutrientValue? = data[n.index]

    fun amountOf(n: Nutrient) = get(n)?.value

    operator fun set(n: Nutrient, value: NutrientValue?) {
        assertMutable()
        data[n.index] = value
        if (dataCompleteIfNotNull) {
            isDataComplete[n.index] = value != null
        }
    }

    fun hasCompleteData(n: Nutrient) = isDataComplete[n.index]
    internal fun markCompleteData(n: Nutrient, complete: Boolean) {
        isDataComplete[n.index] = complete
    }

    companion object {
        val Nutrient.index : Int
            get() = id.toInt()
    }

}
