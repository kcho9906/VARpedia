package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.imageio.ImageIO;
import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

public class FlickrImageExtractor {

    public static String getAPIKey(String key) throws Exception {
        String config = System.getProperty("user.dir")
                + System.getProperty("file.separator")+ "flickr-api-keys.txt";

        File file = new File(config);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;
        while ( (line = br.readLine()) != null ) {
            if (line.trim().startsWith(key)) {
                br.close();
                return line.substring(line.indexOf("=")+1).trim();
            }
        }
        br.close();
        throw new RuntimeException("Couldn't find " + key +" in config file "+file.getName());
    }

    public static int downloadImages(File file, int numImages) {
        try {
            String query = file.getName();
            String apiKey = getAPIKey("apiKey");
            String sharedSecret = getAPIKey("sharedSecret");

            Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

            int resultsPerPage = numImages;
            int page = 0;

            PhotosInterface photos = flickr.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(query);

            PhotoList<Photo> results = photos.search(params, resultsPerPage, page);
            if (results.getTotal() > 0) {

                for (Photo photo : results) {
                    try {
                        BufferedImage image = photos.getImage(photo, Size.LARGE);
                        String filename = query.trim().replace(' ', '-') + "-" + System.currentTimeMillis() + "-" + photo.getId() + ".jpg";
                        File outputfile = new File(file.getPath(),  filename);
                        ImageIO.write(image, "jpg", outputfile);
                    } catch (FlickrException fe) {
                        System.err.println("Ignoring image " + photo.getId() + ": " + fe.getMessage());
                    }
                }
            }
            return results.size();


        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
