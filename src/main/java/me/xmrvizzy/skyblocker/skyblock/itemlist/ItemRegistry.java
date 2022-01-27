package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class ItemRegistry {
    private static final String ITEM_REPO_URI = "https://github.com/KonaeAkira/NotEnoughUpdates-REPO.git";

    private static final String ITEM_REPO_DIR = "./config/skypixel/items-repo/";
    private static final String ITEM_LIST_DIR = ITEM_REPO_DIR + "items/";
    private static final String CONSTANTS_DIR = ITEM_REPO_DIR + "constants/";
    private static final String PETNUMS_FILE = CONSTANTS_DIR + "petnums.json";

    private static final JsonParser JSON_PARSER = new JsonParser();

    protected static SortedSet<Entry> registry = new TreeSet<Entry>();
    protected static JsonObject petNums;

    // TODO: make async
    public static void init() {
        updateItemRepo();
        try {
            petNums = JSON_PARSER.parse(Files.readString(Paths.get(PETNUMS_FILE))).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        importItemFiles();
    }

    private static void updateItemRepo() {
        if (!Files.isDirectory(Paths.get(ITEM_REPO_DIR))) {
            try {
                Git.cloneRepository()
                        .setURI(ITEM_REPO_URI)
                        .setDirectory(new File(ITEM_REPO_DIR))
                        .setBranchesToClone(Arrays.asList("refs/heads/master"))
                        .setBranch("refs/heads/master")
                        .call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Git.open(new File(ITEM_REPO_DIR)).pull().call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void importItemFiles() {
        File dir = new File(ITEM_LIST_DIR);
        File[] files = dir.listFiles();
        for (File file : files) {
            String path = ITEM_LIST_DIR + "/" + file.getName();
            try {
                String fileContent = Files.readString(Paths.get(path));
                JsonObject json = JSON_PARSER.parse(fileContent).getAsJsonObject();
                registry.add(new Entry(json));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

