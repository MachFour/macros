package com.machfour.macros.util;

import java.util.Objects;

// Best way to make a class portable across platforms is to bring it with you :)
public final class Pair<S, T> {
    public final S first;
    public final T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            // also checks for null
            return false;
        }
        return Objects.equals(first, ((Pair) o).first) && Objects.equals(second, ((Pair) o).second);
    }
}
