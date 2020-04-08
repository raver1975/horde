package com.klemstinegroup.wub.system;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.TrackSearchRequest;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.Track;

import java.util.List;

/**
 * Created by Paul on 3/4/2017.
 */
public class RandomSong {
    static String[] wordsList = new String[]{
            "Help",
                    "Love",
                    "Hate",
                    "Desperate",
                    "Open",
                    "Close",
                    "Baby",
                    "Girl",
                    "Yeah",
                    "Whoa",
                    "Start",
                    "Finish",
                    "Beginning",
                    "End",
                    "Fight",
                    "War",
                    "Running",
                    "Want",
                    "Need",
                    "Fire",
                    "Myself",
                    "Alive",
                    "Life",
                    "Dead",
                    "Death",
                    "Kill",
                    "Different",
                    "Alone",
                    "Lonely",
                    "Darkness",
                    "Home",
                    "Gone",
                    "Break",
                    "Heart",
                    "Floating",
                    "Searching",
                    "Dreaming",
                    "Serenity",
                    "Star",
                    "Recall",
                    "Think",
                    "Feel",
                    "Slow",
                    "Speed",
                    "Fast",
                    "World",
                    "Work",
                    "Miss",
                    "Stress",
                    "Please",
                    "More",
                    "Less",
                    "only",
                    "World",
                    "Moving",
                    "lasting",
                    "Rise",
                    "Save",
                    "Wake",
                    "Over",
                    "High",
                    "Above",
                    "Taking",
                    "Go",
                    "Why",
                    "Before",
                    "After",
                    "Along",
                    "See",
                    "Hear",
                    "Feel",
                    "Change",
                    "Body",
                    "Being",
                    "Soul",
                    "Spirit",
                    "God",
                    "Angel",
                    "Devil",
                    "Demon",
                    "Believe",
                    "Away",
                    "Everything",
                    "Shared",
                    "Something",
                    "Everything",
                    "Control",
                    "Heart",
                    "Away",
                    "Waiting",
                    "Loyalty",
                    "Shared",
                    "Remember",
                    "Yesterday",
                    "Today",
                    "Tomorrow",
                    "Fall",
                    "Memories",
                    "Apart",
                    "Time",
                    "Forever",
                    "Breath",
                    "Lie",
                    "Sleep",
                    "Inside",
                    "Outside",
                    "Catch",
                    "Be",
                    "Pretending"
};

    public static Track getRandom(Api api) {
        int numofWords = (int) (Math.random() * 3 + 1);
        String s = "";
        for (int i = 0; i < numofWords; i++) {
            s += wordsList[((int) (Math.random() * wordsList.length))] + " ";
        }
        s = s.substring(0, s.length() - 1);
        System.out.println("searcing for: "+s);
        final TrackSearchRequest request = api.searchTracks(s).market("US").build();

        try {
            final Page<Track> trackSearchResult = request.get();
            System.out.println("I got " + trackSearchResult.getTotal() + " results!");
            List<Track> list = trackSearchResult.getItems();
            return list.get((int) (Math.random()*list.size()));
        } catch (Exception e) {
            System.out.println("Something went wrong!" + e.getMessage());
        }
        return getRandom(api);
    }
}
