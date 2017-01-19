import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by Steven on 1/15/2017.
 */
public class ColorEventSerializer implements JsonSerializer<ColorEvent> {
    @Override
    public JsonElement serialize(ColorEvent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add(src.getEventId(), new Gson().toJsonTree(src.getColorItem()));
        return object;
    }
}
