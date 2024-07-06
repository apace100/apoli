package io.github.apace100.apoli.power;

//  TODO: This is deprecated, remove this! -eggohito
public class SetEntityGroupPower {

}
//public class SetEntityGroupPower extends Power implements Prioritized<SetEntityGroupPower> {
//
//    private final EntityGroup group;
//    private final int priority;
//
//    public SetEntityGroupPower(PowerType<?> type, LivingEntity entity, EntityGroup group, int priority) {
//        super(type, entity);
//        this.group = group;
//        this.priority = priority;
//    }
//
//    @Override
//    public int getPriority() {
//        return priority;
//    }
//
//    public EntityGroup getGroup() {
//        return group;
//    }
//
//    public static PowerFactory createFactory() {
//        return new PowerFactory<>(
//            Apoli.identifier("entity_group"),
//            new SerializableData()
//                .add("group", SerializableDataTypes.ENTITY_GROUP)
//                .add("priority", SerializableDataTypes.INT, 0),
//            data -> (type, player) -> new SetEntityGroupPower(
//                type,
//                player,
//                data.get("group"),
//                data.get("priority")
//            )
//        ).allowCondition();
//    }
//
//}
