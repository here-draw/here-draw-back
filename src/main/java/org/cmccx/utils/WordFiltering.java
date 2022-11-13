package org.cmccx.utils;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class WordFiltering {
    private final Set<String> word = new HashSet<String>(){
        {
            add("씨발");
            add("fuck");
        }
    };

    public boolean checkWord(String message) {
        return word.stream().anyMatch(message::contains);
    }
    public boolean blankCheck(String message) {
        String convertedMessage = message.replace(" ", "");
        return checkWord(convertedMessage);
    }
}
