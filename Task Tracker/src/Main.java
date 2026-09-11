import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException{
        ArrayList<Task> tasks = new ArrayList<Task>();

        Path filePath = Path.of("tasks.json");
        if (Files.exists(filePath)) {
            System.out.println("it exists");
            String json = Files.readString(filePath);
            System.out.println(json);
            Files.writeString(filePath, json);
        }
        if (args[0].equals("add")) {
            Task task = new Task(tasks.size() + 1, args[1], "todo", "today", "today");


            tasks.add(task);


            System.out.println(tasks.size());
        }
        if (args[0].equals("list")) {

            for (Task t : tasks) {
                System.out.println(t.id);
                System.out.println(t.description);
                System.out.println(t.status);
            }
        }
    }
}
