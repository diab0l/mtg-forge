package forge.trackable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import forge.card.ColorSet;
import forge.card.mana.ManaCost;
import forge.game.GameEntityView;
import forge.game.card.CardView;
import forge.game.card.CounterType;
import forge.game.card.CardView.CardStateView;
import forge.game.player.PlayerView;
import forge.game.spellability.StackItemView;

public class TrackableTypes {
    public static abstract class TrackableType<T> {
        private TrackableType() {
        }

        protected abstract T getDefaultValue();
        protected abstract T deserialize(TrackableDeserializer td, T oldValue);
        protected abstract void serialize(TrackableSerializer ts, T value);
    }

    public static final TrackableType<Boolean> BooleanType = new TrackableType<Boolean>() {
        @Override
        public Boolean getDefaultValue() {
            return false;
        }

        @Override
        public Boolean deserialize(TrackableDeserializer td, Boolean oldValue) {
            return td.readBoolean();
        }

        @Override
        public void serialize(TrackableSerializer ts, Boolean value) {
            ts.write(value);
        }
    };
    public static final TrackableType<Integer> IntegerType = new TrackableType<Integer>() {
        @Override
        public Integer getDefaultValue() {
            return 0;
        }

        @Override
        public Integer deserialize(TrackableDeserializer td, Integer oldValue) {
            return td.readInt();
        }

        @Override
        public void serialize(TrackableSerializer ts, Integer value) {
            ts.write(value);
        }
    };
    public static final TrackableType<String> StringType = new TrackableType<String>() {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public String deserialize(TrackableDeserializer td, String oldValue) {
            return td.readString();
        }

        @Override
        public void serialize(TrackableSerializer ts, String value) {
            ts.write(value);
        }
    };

    //make this quicker than having to define a new class for every single enum
    private static HashMap<Class<? extends Enum<?>>, TrackableType<?>> enumTypes = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> TrackableType<E> EnumType(final Class<E> enumType) {
        TrackableType<E> type = (TrackableType<E>)enumTypes.get(enumType);
        if (type == null) {
            type = new TrackableType<E>() {
                @Override
                public E getDefaultValue() {
                    return null;
                }

                @Override
                public E deserialize(TrackableDeserializer td, E oldValue) {
                    try {
                        return Enum.valueOf(enumType, td.readString());
                    }
                    catch (Exception e) {
                        return oldValue;
                    }
                }

                @Override
                public void serialize(TrackableSerializer ts, E value) {
                    ts.write(value.name());
                }
            };
            enumTypes.put(enumType, type);
        }
        return type;
    }

    public static final TrackableType<CardView> CardViewType = new TrackableType<CardView>() {
        @Override
        protected CardView getDefaultValue() {
            return null;
        }

        @Override
        protected CardView deserialize(TrackableDeserializer td, CardView oldValue) {
            int id = td.readInt();
            if (id == -1) {
                return null;
            }
            return oldValue; //TODO: return index lookup
        }

        @Override
        protected void serialize(TrackableSerializer ts, CardView value) {
            ts.write(value == null ? -1 : value.getId()); //just write ID for lookup via index when deserializing
        }
    };
    public static final TrackableType<TrackableCollection<CardView>> CardViewCollectionType = new TrackableType<TrackableCollection<CardView>>() {
        @Override
        protected TrackableCollection<CardView> getDefaultValue() {
            return null;
        }

        @Override
        protected TrackableCollection<CardView> deserialize(TrackableDeserializer td, TrackableCollection<CardView> oldValue) {
            return td.readCollection(oldValue);
        }

        @Override
        protected void serialize(TrackableSerializer ts, TrackableCollection<CardView> value) {
            ts.write(value);
        }
    };
    public static final TrackableType<CardStateView> CardStateViewType = new TrackableType<CardStateView>() {
        @Override
        protected CardStateView getDefaultValue() {
            return null;
        }

        @Override
        protected CardStateView deserialize(TrackableDeserializer td, CardStateView oldValue) {
            oldValue.deserialize(td); //TODO handle old value being null or changing to null
            return oldValue;
        }

        @Override
        protected void serialize(TrackableSerializer ts, CardStateView value) {
            if (value == null) {
                ts.write(-1);
            }
            else {
                value.serialize(ts); //serialize card state views here since they won't be stored in an index
            }
        }
    };
    public static final TrackableType<PlayerView> PlayerViewType = new TrackableType<PlayerView>() {
        @Override
        protected PlayerView getDefaultValue() {
            return null;
        }

        @Override
        protected PlayerView deserialize(TrackableDeserializer td, PlayerView oldValue) {
            int id = td.readInt();
            if (id == -1) {
                return null;
            }
            return oldValue; //TODO: return index lookup
        }

        @Override
        protected void serialize(TrackableSerializer ts, PlayerView value) {
            ts.write(value == null ? -1 : value.getId()); //just write ID for lookup via index when deserializing
        }
    };
    public static final TrackableType<TrackableCollection<PlayerView>> PlayerViewCollectionType = new TrackableType<TrackableCollection<PlayerView>>() {
        @Override
        protected TrackableCollection<PlayerView> getDefaultValue() {
            return null;
        }

        @Override
        protected TrackableCollection<PlayerView> deserialize(TrackableDeserializer td, TrackableCollection<PlayerView> oldValue) {
            return td.readCollection(oldValue);
        }

        @Override
        protected void serialize(TrackableSerializer ts, TrackableCollection<PlayerView> value) {
            ts.write(value);
        }
    };
    public static final TrackableType<GameEntityView> GameEntityViewType = new TrackableType<GameEntityView>() {
        @Override
        protected GameEntityView getDefaultValue() {
            return null;
        }

        @Override
        protected GameEntityView deserialize(TrackableDeserializer td, GameEntityView oldValue) {
            switch (td.readInt()) {
            case 0:
                //int cardId = td.readInt();
                return oldValue; //TODO: lookup card by ID
            case 1:
                //int playerId = td.readInt();
                return oldValue; //TODO: lookup player by ID
            }
            return null;
        }

        @Override
        protected void serialize(TrackableSerializer ts, GameEntityView value) {
            //just write ID for lookup via index when deserializing, with an additional value to indicate type
            if (value instanceof CardView) {
                ts.write(0);
                ts.write(value.getId());
            }
            if (value instanceof PlayerView) {
                ts.write(1);
                ts.write(value.getId());
            }
            else {
                ts.write(-1);
            }
        }
    };
    public static final TrackableType<StackItemView> StackItemViewType = new TrackableType<StackItemView>() {
        @Override
        protected StackItemView getDefaultValue() {
            return null;
        }

        @Override
        protected StackItemView deserialize(TrackableDeserializer td, StackItemView oldValue) {
            oldValue.deserialize(td); //TODO handle old value being null or changing to null
            return oldValue;
        }

        @Override
        protected void serialize(TrackableSerializer ts, StackItemView value) {
            if (value == null) {
                ts.write(-1);
            }
            else {
                value.serialize(ts); //serialize card state views here since they won't be stored in an index
            }
        }
    };
    public static final TrackableType<ManaCost> ManaCostType = new TrackableType<ManaCost>() {
        @Override
        public ManaCost getDefaultValue() {
            return ManaCost.NO_COST;
        }

        @Override
        public ManaCost deserialize(TrackableDeserializer td, ManaCost oldValue) {
            String value = td.readString();
            if (value.length() > 0) {
                return ManaCost.deserialize(value);
            }
            return oldValue;
        }

        @Override
        public void serialize(TrackableSerializer ts, ManaCost value) {
            ts.write(ManaCost.serialize(value));
        }
    };
    public static final TrackableType<ColorSet> ColorSetType = new TrackableType<ColorSet>() {
        @Override
        public ColorSet getDefaultValue() {
            return ColorSet.getNullColor();
        }

        @Override
        public ColorSet deserialize(TrackableDeserializer td, ColorSet oldValue) {
            return ColorSet.fromMask(td.readInt());
        }

        @Override
        public void serialize(TrackableSerializer ts, ColorSet value) {
            ts.write(value.getColor());
        }
    };
    public static final TrackableType<Set<String>> StringSetType = new TrackableType<Set<String>>() {
        @Override
        public Set<String> getDefaultValue() {
            return null;
        }

        @Override
        public Set<String> deserialize(TrackableDeserializer td, Set<String> oldValue) {
            int size = td.readInt();
            if (size > 0) {
                Set<String> set = new HashSet<String>();
                for (int i = 0; i < size; i++) {
                    set.add(td.readString());
                }
                return set;
            }
            return null;
        }

        @Override
        public void serialize(TrackableSerializer ts, Set<String> value) {
            ts.write(value.size());
            for (String s : value) {
                ts.write(s);
            }
        }
    };
    public static final TrackableType<Map<String, String>> StringMapType = new TrackableType<Map<String, String>>() {
        @Override
        public Map<String, String> getDefaultValue() {
            return null;
        }

        @Override
        public Map<String, String> deserialize(TrackableDeserializer td, Map<String, String> oldValue) {
            int size = td.readInt();
            if (size > 0) {
                Map<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < size; i++) {
                    map.put(td.readString(), td.readString());
                }
                return map;
            }
            return null;
        }

        @Override
        public void serialize(TrackableSerializer ts, Map<String, String> value) {
            ts.write(value.size());
            for (Entry<String, String> entry : value.entrySet()) {
                ts.write(entry.getKey());
                ts.write(entry.getValue());
            }
        }
    };
    public static final TrackableType<Map<Integer, Integer>> IntegerMapType = new TrackableType<Map<Integer, Integer>>() {
        @Override
        public Map<Integer, Integer> getDefaultValue() {
            return null;
        }

        @Override
        public Map<Integer, Integer> deserialize(TrackableDeserializer td, Map<Integer, Integer> oldValue) {
            int size = td.readInt();
            if (size > 0) {
                Map<Integer, Integer> map = new HashMap<Integer, Integer>();
                for (int i = 0; i < size; i++) {
                    map.put(td.readInt(), td.readInt());
                }
                return map;
            }
            return null;
        }

        @Override
        public void serialize(TrackableSerializer ts, Map<Integer, Integer> value) {
            ts.write(value.size());
            for (Entry<Integer, Integer> entry : value.entrySet()) {
                ts.write(entry.getKey());
                ts.write(entry.getValue());
            }
        }
    };
    public static final TrackableType<Map<Byte, Integer>> ManaMapType = new TrackableType<Map<Byte, Integer>>() {
        @Override
        public Map<Byte, Integer> getDefaultValue() {
            return null;
        }

        @Override
        public Map<Byte, Integer> deserialize(TrackableDeserializer td, Map<Byte, Integer> oldValue) {
            int size = td.readInt();
            if (size > 0) {
                Map<Byte, Integer> map = new HashMap<Byte, Integer>();
                for (int i = 0; i < size; i++) {
                    map.put(td.readByte(), td.readInt());
                }
                return map;
            }
            return null;
        }

        @Override
        public void serialize(TrackableSerializer ts, Map<Byte, Integer> value) {
            ts.write(value.size());
            for (Entry<Byte, Integer> entry : value.entrySet()) {
                ts.write(entry.getKey());
                ts.write(entry.getValue());
            }
        }
    };
    public static final TrackableType<Map<CounterType, Integer>> CounterMapType = new TrackableType<Map<CounterType, Integer>>() {
        @Override
        public Map<CounterType, Integer> getDefaultValue() {
            return null;
        }

        @Override
        public Map<CounterType, Integer> deserialize(TrackableDeserializer td, Map<CounterType, Integer> oldValue) {
            int size = td.readInt();
            if (size > 0) {
                Map<CounterType, Integer> map = new TreeMap<CounterType, Integer>();
                for (int i = 0; i < size; i++) {
                    map.put(CounterType.valueOf(td.readString()), td.readInt());
                }
                return map;
            }
            return null;
        }

        @Override
        public void serialize(TrackableSerializer ts, Map<CounterType, Integer> value) {
            ts.write(value.size());
            for (Entry<CounterType, Integer> entry : value.entrySet()) {
                ts.write(entry.getKey().name());
                ts.write(entry.getValue());
            }
        }
    };
}
