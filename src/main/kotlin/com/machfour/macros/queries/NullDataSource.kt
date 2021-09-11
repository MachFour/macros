package com.machfour.macros.queries

import com.machfour.macros.sql.NullDatabase

open class NullDataSource: StaticDataSource(NullDatabase())