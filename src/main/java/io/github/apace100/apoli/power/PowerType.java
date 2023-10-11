package io.github.apace100.apoli.power;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PowerType<T extends Power> {

    private final PowerFactory<T>.Instance factory;
    private final Identifier identifier;

    private String nameTranslationKey;
    private String descriptionTranslationKey;

    private Text name;
    private Text description;

    private boolean isHidden = false;

    public PowerType(Identifier id, PowerFactory<T>.Instance factory) {
        this.identifier = id;
        this.factory = factory;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public PowerFactory<T>.Instance getFactory() {
        return factory;
    }

    public PowerType setHidden() {
        this.isHidden = true;
        return this;
    }

    public void setTranslationKeys(String name, String description) {
        this.nameTranslationKey = name;
        this.descriptionTranslationKey = description;
    }

    public void setDisplayTexts(Text name, Text description) {
        this.name = name;
        this.description = description;
    }

    public T create(LivingEntity entity) {

        T power = factory.apply(this, entity);

        power.setSerializableData(factory.getFactory().getSerializableData());
        power.setDataInstance(factory.getDataInstance());

        return power;

    }

    public boolean isHidden() {
        return this.isHidden;
    }

    public boolean isActive(Entity entity) {
        if(entity instanceof LivingEntity && identifier != null) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
            if(component.hasPower(this)) {
                return component.getPower(this).isActive();
            }
        }
        return false;
    }

    public T get(Entity entity) {
        if(entity instanceof LivingEntity) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
            return component.getPower(this);
        }
        return null;
    }

    public String getOrCreateNameTranslationKey() {
        if(nameTranslationKey == null || nameTranslationKey.isEmpty()) {
            nameTranslationKey =
                "power." + identifier.getNamespace() + "." + identifier.getPath() + ".name";
        }
        return nameTranslationKey;
    }

    public MutableText getName() {
        return name != null ? name.copy() : Text.translatable(getOrCreateNameTranslationKey());
    }

    public String getOrCreateDescriptionTranslationKey() {
        if(descriptionTranslationKey == null || descriptionTranslationKey.isEmpty()) {
            descriptionTranslationKey =
                "power." + identifier.getNamespace() + "." + identifier.getPath() + ".description";
        }
        return descriptionTranslationKey;
    }

    public MutableText getDescription() {
        return description != null ? description.copy() : Text.translatable(getOrCreateDescriptionTranslationKey());
    }

    public JsonObject toJson() {

        JsonObject jsonObject = factory.toJson();

        jsonObject.add("name", SerializableDataTypes.TEXT.write(getName()));
        jsonObject.add("description", SerializableDataTypes.TEXT.write(getDescription()));

        return jsonObject;

    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof PowerType)) {
            return false;
        }
        Identifier id = ((PowerType)obj).getIdentifier();
        return identifier.equals(id);
    }
}