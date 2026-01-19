package desia.story;

import desia.Game;
import desia.io.Io;

public class StoryService {

    Io io = new Io();

    private final StoryRepository repo;

    public StoryService(StoryRepository repo) {
        this.repo = repo;
    }

    public void printStory(String key) {
        Game gm = new Game();
        gm.clearConsole();
        gm.printHeading(repo.get(key), 1);
        io.anythingToContinue();

        /*System.out.println();
        System.out.println(repo.get(key));
        System.out.println();*/
    }
}
