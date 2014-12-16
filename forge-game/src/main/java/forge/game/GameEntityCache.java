package forge.game;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class GameEntityCache<Entity extends GameEntity, View extends GameEntityView> {
    private HashMap<Integer, Entity> entityCache = new HashMap<Integer, Entity>();
 
    public void put(Integer id, Entity entity) {
        entityCache.put(id, entity);
    }

    public Entity get(View entityView) {
        if (entityView == null) { return null; }
        return entityCache.get(entityView.getId());
    }

    public void addToList(Iterable<View> views, List<Entity> list) {
        for (View view : views) {
            Entity entity = get(view);
            if (entity != null) {
                list.add(entity);
            }
        }
    }

    public List<Entity> getList(Iterable<View> views) {
        List<Entity> list = new ArrayList<Entity>();
        addToList(views, list);
        return list;
    }
}
