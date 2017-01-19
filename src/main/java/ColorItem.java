import java.util.Map;

/**
 * Created by Steven on 1/15/2017.
 */
public class ColorItem extends EmoEntity{

    private String color;
    private String text;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static ColorItem mapColorItem (Map<String, Object> valueMap) {
        ColorItem colorItem = new ColorItem();
        colorItem = (ColorItem) colorItem.mapEmoEntity(valueMap, colorItem);
        colorItem.setColor(valueMap.get("color").toString());
        colorItem.setText(valueMap.get("text").toString());
        return colorItem;
    }

}
