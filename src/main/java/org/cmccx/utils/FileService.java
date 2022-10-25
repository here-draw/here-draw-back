package org.cmccx.utils;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class FileService {

    private static final Tika tika = new Tika();

    public boolean validateFile(InputStream inputStream) throws IOException {
        List<String> validFiletype = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/svg");

        String mimeType = tika.detect(inputStream);

        boolean isValid = validFiletype.stream().anyMatch(filetype -> filetype.equalsIgnoreCase(mimeType));

        return isValid;
    }
}
