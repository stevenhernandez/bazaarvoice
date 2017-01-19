import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Steven on 1/15/2017.
 */
public class ColorEventDeserializer implements JsonDeserializer<EventContainer>{

    @Override
    public EventContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        EventContainer eventContainer = new EventContainer();
        eventContainer.setColorEventList(new ArrayList<>());
        for(Map.Entry entry : obj.entrySet()) {
            ColorEvent colorEvent = new ColorEvent();
            colorEvent.setEventId(entry.getKey().toString());
            colorEvent.setColorItem(new Gson().fromJson(entry.getValue().toString(), ColorItem.class));
            eventContainer.getColorEventList().add(colorEvent);
        }

        return  eventContainer;
    }
}
