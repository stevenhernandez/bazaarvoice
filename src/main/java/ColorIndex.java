import com.google.gson.Gson;
import org.vvcephei.occ_map.OCCHashMap;
import org.vvcephei.occ_map.VersionConflictException;

import java.util.*;

/**
 * Created by Steven on 1/18/2017.
 */
public class ColorIndex {

    private Map<String, ColorItem> eventIndex = new OCCHashMap<>();
    private Map<String, Set<String>> colorIndex = new HashMap<>();

    public void put (String id, ColorItem colorItem) {
        try {

            if(eventIndex.containsKey(id) && !colorIndex.get(colorItem.getColor()).contains(id)) {
                colorIndex.get(eventIndex.get(id).getColor()).remove(id);
            }
            eventIndex.put(id, colorItem);
            if (!colorIndex.containsKey(colorItem.getColor())) {
                Set<String> colorDocumentList = new HashSet<>();
                colorDocumentList.add(colorItem.getId());
                colorIndex.put(colorItem.getColor(), colorDocumentList);
            } else {
                colorIndex.get(colorItem.getColor()).add(colorItem.getId());

            }
        } catch (VersionConflictException ex) {
            System.out.print("Throwing out outdated version");
        }
    }

    public void listenForQueries() {
        Scanner scanner = new Scanner(System.in);
        do  {
            System.out.println("Enter an event id to return event document or a color to list all documents of that color and '0' to exit");
            String line = scanner.nextLine();
            if (line.equals("0")) {
                break;
            } else {
                if (eventIndex.containsKey(line)) {
                    System.out.println(new Gson().toJson(eventIndex.get(line)));
                } else {
                    List<ColorItem> colorList = new ArrayList<>();
                    if (colorIndex.containsKey(line)) {
                        for (String id : colorIndex.get(line)) {
                            colorList.add(eventIndex.get(id));
                        }
                    }
                    System.out.println(new Gson().toJson(colorList));
                }
            }
        } while (true);
    }
}
