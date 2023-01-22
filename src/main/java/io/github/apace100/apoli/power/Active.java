package io.github.apace100.apoli.power;

import java.util.Objects;

public interface Active {

    void onUse();
    Key getKey();
    void setKey(Key key);

    class Key {

        public String key = "none";
        public boolean continuous = false;

        @Override
        public boolean equals(final Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof Active.Key otherKey))
                return false;

            return otherKey.key.equals(this.key) && otherKey.continuous == this.continuous;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key, this.continuous);
        }
    }
}
