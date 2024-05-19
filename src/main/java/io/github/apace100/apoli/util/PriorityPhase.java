package io.github.apace100.apoli.util;

import java.util.function.IntPredicate;

public enum PriorityPhase implements IntPredicate {

    BEFORE {

        @Override
        public boolean test(int value) {
            return value >= 0;
        }

    },

    AFTER {

        @Override
        public boolean test(int value) {
            return value < 0;
        }

    }

}
