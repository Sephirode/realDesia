package desia.story;

import desia.io.Io;
import desia.ui.ConsoleUi;

public class StoryService {

    private final Io io;
    private final StoryRepository repo;

    public StoryService(Io io, StoryRepository repo) {
        this.io = io;
        this.repo = repo;
    }

    public void printStory(String key) {
        ConsoleUi.clearConsole();
        ConsoleUi.printHeading(repo.get(key), 1);
        io.anythingToContinue();
    }
}
