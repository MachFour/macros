package com.machfour.macros.queries

import com.machfour.macros.persistence.NullDatabase

open class NullDataSource: StaticDataSource(NullDatabase())