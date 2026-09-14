import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main {

    static Path filePath = Path.of("tasks.json");

    public static void main(String[] args) throws IOException {

        if (!Files.exists(filePath)) {
            Files.writeString(filePath, "[]");
        }

        ArrayList<Task> tasks = loadTasks();

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        String command = args[0];

        if (command.equals("add")) {

            if (args.length < 2) {
                System.out.println("Please enter a task description.");
                return;
            }

            int newId = getNextId(tasks);
            String now = LocalDateTime.now().toString();

            Task task = new Task(
                    newId,
                    args[1],
                    "todo",
                    now,
                    now
            );

            tasks.add(task);
            saveTasks(tasks);

            System.out.println("Task added successfully.");
            System.out.println("ID: " + task.id);

        } else if (command.equals("update")) {

            if (args.length < 3) {
                System.out.println("Usage: update <id> <description>");
                return;
            }

            int id = Integer.parseInt(args[1]);
            boolean found = false;

            for (Task task : tasks) {
                if (task.id == id) {
                    task.description = args[2];
                    task.updatedAt = LocalDateTime.now().toString();
                    found = true;
                    break;
                }
            }

            if (found) {
                saveTasks(tasks);
                System.out.println("Task updated successfully.");
            } else {
                System.out.println("Task not found.");
            }

        } else if (command.equals("delete")) {

            if (args.length < 2) {
                System.out.println("Usage: delete <id>");
                return;
            }

            int id = Integer.parseInt(args[1]);
            boolean removed = false;

            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).id == id) {
                    tasks.remove(i);
                    removed = true;
                    break;
                }
            }

            if (removed) {
                saveTasks(tasks);
                System.out.println("Task deleted successfully.");
            } else {
                System.out.println("Task not found.");
            }

        } else if (command.equals("mark-in-progress")) {

            if (args.length < 2) {
                System.out.println("Usage: mark-in-progress <id>");
                return;
            }

            int id = Integer.parseInt(args[1]);
            markStatus(tasks, id, "in-progress");

        } else if (command.equals("mark-done")) {

            if (args.length < 2) {
                System.out.println("Usage: mark-done <id>");
                return;
            }

            int id = Integer.parseInt(args[1]);
            markStatus(tasks, id, "done");

        } else if (command.equals("list")) {

            if (args.length == 1) {
                listTasks(tasks);
            } else if (args[1].equals("done")) {
                listByStatus(tasks, "done");
            } else if (args[1].equals("todo")) {
                listByStatus(tasks, "todo");
            } else if (args[1].equals("in-progress")) {
                listByStatus(tasks, "in-progress");
            } else {
                System.out.println("Unknown list option.");
            }

        } else {
            System.out.println("Unknown command.");
        }
    }

    static int getNextId(ArrayList<Task> tasks) {

        int maxId = 0;

        for (Task task : tasks) {
            if (task.id > maxId) {
                maxId = task.id;
            }
        }

        return maxId + 1;
    }

    static void markStatus(ArrayList<Task> tasks, int id, String status)
            throws IOException {

        boolean found = false;

        for (Task task : tasks) {
            if (task.id == id) {
                task.status = status;
                task.updatedAt = LocalDateTime.now().toString();
                found = true;
                break;
            }
        }

        if (found) {
            saveTasks(tasks);
            System.out.println("Task status updated.");
        } else {
            System.out.println("Task not found.");
        }
    }

    static void listTasks(ArrayList<Task> tasks) {

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        for (Task task : tasks) {
            printTask(task);
        }
    }

    static void listByStatus(ArrayList<Task> tasks, String status) {

        boolean found = false;

        for (Task task : tasks) {
            if (task.status.equals(status)) {
                printTask(task);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No tasks found.");
        }
    }

    static void printTask(Task task) {

        System.out.println(
                task.id + " - " +
                        task.description +
                        " [" + task.status + "]"
        );
    }

    static void saveTasks(ArrayList<Task> tasks) throws IOException {

        StringBuilder json = new StringBuilder();

        json.append("[\n");

        for (int i = 0; i < tasks.size(); i++) {

            Task task = tasks.get(i);

            json.append("  {\n");
            json.append("    \"id\": ").append(task.id).append(",\n");
            json.append("    \"description\": \"")
                    .append(escapeJson(task.description))
                    .append("\",\n");
            json.append("    \"status\": \"")
                    .append(task.status)
                    .append("\",\n");
            json.append("    \"createdAt\": \"")
                    .append(task.createdAt)
                    .append("\",\n");
            json.append("    \"updatedAt\": \"")
                    .append(task.updatedAt)
                    .append("\"\n");
            json.append("  }");

            if (i < tasks.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]");

        Files.writeString(filePath, json.toString());
    }

    static ArrayList<Task> loadTasks() throws IOException {

        ArrayList<Task> tasks = new ArrayList<>();

        String json = Files.readString(filePath).trim();

        if (json.equals("[]")) {
            return tasks;
        }

        int position = 0;

        while (true) {

            int objectStart = json.indexOf("{", position);

            if (objectStart == -1) {
                break;
            }

            int objectEnd = json.indexOf("}", objectStart);

            if (objectEnd == -1) {
                break;
            }

            String object = json.substring(objectStart, objectEnd + 1);

            int id = getIntValue(object, "id");
            String description = getStringValue(object, "description");
            String status = getStringValue(object, "status");
            String createdAt = getStringValue(object, "createdAt");
            String updatedAt = getStringValue(object, "updatedAt");

            Task task = new Task(
                    id,
                    description,
                    status,
                    createdAt,
                    updatedAt
            );

            tasks.add(task);

            position = objectEnd + 1;
        }

        return tasks;
    }

    static int getIntValue(String json, String key) {

        String search = "\"" + key + "\":";
        int start = json.indexOf(search);

        if (start == -1) {
            return 0;
        }

        start += search.length();

        int end = json.indexOf(",", start);

        if (end == -1) {
            end = json.indexOf("}", start);
        }

        String value = json.substring(start, end).trim();

        return Integer.parseInt(value);
    }

    static String getStringValue(String json, String key) {

        String search = "\"" + key + "\":";
        int start = json.indexOf(search);

        if (start == -1) {
            return "";
        }

        start += search.length();

        int firstQuote = json.indexOf("\"", start);
        int secondQuote = json.indexOf("\"", firstQuote + 1);

        return json.substring(firstQuote + 1, secondQuote);
    }

    static String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}