package io.github.apace100.apoli.power;

public interface Active {

    void onUse();
    Key getKey();
    void setKey(Key key);

    class Key {

        public String key = "key.apoli.primary_active";
        public boolean continuous = false;
    }
}
