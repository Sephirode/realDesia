package desia.loader;

import desia.io.Io;
import desia.progress.ChapterRepository;
import desia.progress.GameSession;

public class GameLoad {
    public GameSession gameLoad(Io io, DataLoader loader, ChapterRepository chapterRepo) {
        SaveService save = new SaveService(io);
        return save.load(loader, chapterRepo);
    }
}
