package com.machfour.macros.objects;

import com.machfour.macros.core.*;

public class AttrMapping extends MacrosEntityImpl<AttrMapping> {

    private AttrMapping(ColumnData<AttrMapping> data, ObjectSource objectSource) {
        super(data, objectSource);
    }

    @Override
    public Table<AttrMapping> getTable() {
        return Schema.AttrMappingTable.instance();
    }

    @Override
    public Factory<AttrMapping> getFactory() {
        return factory();
    }
    public static Factory<AttrMapping> factory() {
        return AttrMapping::new;
    }
}
