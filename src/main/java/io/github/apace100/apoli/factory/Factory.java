package io.github.apace100.apoli.factory;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.Validatable;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

public interface Factory {

    Identifier getSerializerId();

    SerializableData getSerializableData();

    Instance receive(RegistryByteBuf buf);

    Instance fromData(SerializableData.Instance data);

    interface Instance extends Validatable {

        default SerializableData getSerializableData() {
            return this.getFactory().getSerializableData();
        }

        default Identifier getSerializerId() {
            return this.getFactory().getSerializerId();
        }

        SerializableData.Instance getData();

        Factory getFactory();

        @Override
        default void validate() throws Exception {
            this.getData().validate();
        }

        default void send(RegistryByteBuf buf) {

            Factory factory = this.getFactory();

            buf.writeIdentifier(factory.getSerializerId());
            factory.getSerializableData().send(buf, this.getData());

        }

    }

}
