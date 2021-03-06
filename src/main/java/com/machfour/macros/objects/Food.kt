package com.machfour.macros.objects

import com.machfour.macros.core.*
import com.machfour.macros.core.schema.FoodTable
import com.machfour.macros.core.schema.ServingTable
import com.machfour.macros.objects.helpers.Factories
import com.machfour.macros.objects.inbuilt.Units
import java.lang.StringBuilder

open class Food internal constructor(dataMap: ColumnData<Food>, objectSource: ObjectSource) :
        MacrosEntityImpl<Food>(dataMap, objectSource) {

    companion object {
        val descriptionColumns = listOf(
            FoodTable.NAME,
            FoodTable.BRAND,
            FoodTable.VARIETY,
            FoodTable.EXTRA_DESC,
            FoodTable.NOTES,
            FoodTable.INDEX_NAME,
            FoodTable.DATA_SOURCE,
            FoodTable.DATA_NOTES,
        )

        // Dynamically create either a Food or CompositeFood depending on the datamap passsed in.
        // Hooray for preferring static constructors over new!!!
        val factory: Factory<Food>
            get() = Factories.food
        
        val table: Table<Food>
            get() = FoodTable.instance

        fun indexNamePrototype(
            basicName: String,
            brand: String?,
            variety: String?,
            extraDesc: String?,
        ): String {
            // use sortable name but replace sequences of spaces (and dashes) with a single dash
            return prettyFormat(basicName, brand, variety, extraDesc,
                withBrand = true, withVariety = true, withExtra = false, sortable = true
            ).replace(Regex("[\\s-]+"), replacement = "-")
        }

        /*
         * Order of fields:
         * if sortable:
         *     <name>, <brand>, <variety> (<extra desc>)
         * else:
         *     <brand> <variety> <name> (<extra desc>)
         */
        fun prettyFormat(
            basicName: String,
            brand: String?,
            variety: String?,
            extraDesc: String?,
            withBrand : Boolean = true,
            withVariety : Boolean = true,
            withExtra : Boolean = false,
            sortable : Boolean = false,
            coerceBlankAsNull: Boolean = false,
        ): String {
            val isPresent: (String?) -> Boolean
                    = if (coerceBlankAsNull) { { !it.isNullOrEmpty() } } else { { it != null } }

            val prettyName = StringBuilder(basicName)
            if (sortable) {
                if (withBrand && isPresent(brand)) {
                    prettyName.append(", ").append(brand)
                }
                if (isPresent(variety)) {
                    prettyName.append(", ").append(variety)
                }
            } else {
                if (withVariety && isPresent(variety)) {
                    prettyName.insert(0, "$variety ")
                }
                if (withBrand && isPresent(brand)) {
                    prettyName.insert(0, "$brand ")
                }
            }
            if (withExtra && isPresent(extraDesc)) {
                prettyName.append(" (").append(extraDesc).append(")")
            }
            return prettyName.toString()
        }

    }

    private val servingsInternal: MutableList<Serving> = ArrayList()

    val servings: List<Serving>
        get() = servingsInternal

    var defaultServing: Serving? = null
        private set

    // TODO check nutrient data is initialised
    open val nutrientData: NutrientData = NutrientData()

    val foodType: FoodType = FoodType.fromString(dataMap[FoodTable.FOOD_TYPE]!!)

    var foodCategory: FoodCategory? = null
        private set

    open fun addNutrientValue(nv: FoodNutrientValue) {
        // TODO check ID matches
        nv.setFood(this)
        nutrientData[nv.nutrient] = nv
    }

    fun setFoodCategory(c: FoodCategory) {
        assert(foodCategory == null && foreignKeyMatches(this, FoodTable.CATEGORY, c))
        foodCategory = c
    }

    override val table: Table<Food>
        get() = Companion.table

    override val factory: Factory<Food>
        get() = Companion.factory

    fun addServing(s: Serving) {
        assert(!servingsInternal.contains(s) && foreignKeyMatches(s, ServingTable.FOOD_ID, this))
        servingsInternal.add(s)
        if (s.isDefault) {
            setDefaultServing(s)
        }
    }

    private fun setDefaultServing(s: Serving) {
        assert(defaultServing == null) { "Default serving already set" }
        assert(servingsInternal.contains(s)) { "Serving does not belong to this food" }
        defaultServing = s
    }

    fun getServingById(id: Long): Serving? {
        return servingsInternal.firstOrNull { it.id == id }
    }

    fun getServingByName(name: String): Serving? {
        return servingsInternal.firstOrNull { it.name == name }
    }

    // Returns list of quantity units and servings which can be used to measure this food
    val validMeasurements: List<PortionMeasurement>
        get() {
            val naturalUnit = nutrientData.qtyUnit
            return ArrayList<PortionMeasurement>().apply {
                add(naturalUnit)
                // allow conversion if density is given
                if (density != null) {
                    if (naturalUnit.type === UnitType.VOLUME) {
                        add(Units.GRAMS)
                    } else {
                        add(Units.MILLILITRES)
                    }
                }
                addAll(servings)
            }
        }

    val usdaIndex: Int?
        get() = getData(FoodTable.USDA_INDEX)

    val nuttabIndex: String?
        get() = getData(FoodTable.NUTTAB_INDEX)

    val dataSource: String?
        get() = getData(FoodTable.DATA_SOURCE)

    val dataNotes: String?
        get() = getData(FoodTable.DATA_NOTES)

    val density: Double?
        get() = getData(FoodTable.DENSITY)



    override fun equals(other: Any?): Boolean {
        return other is Food && super.equals(other)
    }

    override fun hashCode(): Int = super.hashCode()

    val shortName: String
        get() = prettyFormat(withBrand = false, withVariety = false)

    val longName: String
        get() = prettyFormat(withExtra = true)

    val mediumName: String
        get() = prettyFormat()

    val sortableName: String
        get() = prettyFormat(withExtra = true, sortable = true)

    val basicName: String
        get() = getData(FoodTable.NAME)!!

    val variety: String?
        get() = getData(FoodTable.VARIETY)

    val brand: String?
        get() = getData(FoodTable.BRAND)

    val extraDesc: String?
        get() = getData(FoodTable.EXTRA_DESC)

    val notes: String?
        get() = getData(FoodTable.NOTES)

    val indexName: String
        get() = getData(FoodTable.INDEX_NAME)!!

    val categoryName: String
        get() = getData(FoodTable.CATEGORY)!!

    val searchRelevance: Int
        get() = getData(FoodTable.SEARCH_RELEVANCE)!!

    private fun prettyFormat(
        withBrand : Boolean = true,
        withVariety : Boolean = true,
        withExtra : Boolean = false,
        sortable : Boolean = false
    ): String {
        return prettyFormat(
            basicName = basicName,
            brand = brand,
            variety = variety,
            extraDesc = extraDesc,
            withBrand = withBrand,
            withVariety = withVariety,
            withExtra = withExtra,
            sortable = sortable,

        )
    }

}