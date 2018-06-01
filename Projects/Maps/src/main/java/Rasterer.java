import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    final double rootlrlon = MapServer.ROOT_LRLON;
    final double rootullon = MapServer.ROOT_ULLON;
    final double rootlrlat = MapServer.ROOT_LRLAT;
    final double rootullat = MapServer.ROOT_ULLAT;
    final double diff = rootlrlon - rootullon;
    String[][] images;
    double rasterullon;
    double rasterlrlon;
    double rasterlrlat;
    double rasterullat;
    double additionlon;
    double additionlat;
    int numbertitles;

    public Rasterer() {

    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     * can also be interpreted as the length of the numbers in the image
     * string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     */


    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        System.out.println(params);
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double w = params.get("w");
        double h = params.get("h");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double londpp = (lrlon - ullon) / w;


        boolean querysuccess = validater(lrlon, ullon, ullat, lrlat);
        int depth = depthcalculator(londpp, w);
        numbertitles = (int) java.lang.Math.pow(2, depth);

        additionlon = Math.abs(MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / Math.pow(2, depth);
        additionlat = Math.abs(MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / Math.pow(2, depth);

        if (querysuccess) {
            int minlong = minlongitude(ullon, depth);
            int maxnlong = maxlongitude(lrlon, ullon, depth);
            int minlat = minlatitude(lrlat, depth);
            int maxlat = maxlatitude(ullat, depth);
            images = imagesfinder(minlat, maxlat, minlong, maxnlong, depth);
        }

        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", images);
        results.put("raster_ul_lon", rasterullon);
        results.put("raster_ul_lat", rasterullat);
        results.put("raster_lr_lon", rasterlrlon);
        results.put("raster_lr_lat", rasterlrlat);
        results.put("depth", depth);
        results.put("query_success", querysuccess);
        return results;
    }

    public int minlongitude(double ullong, int depth) {
        int counter = (int) Math.floor(Math.abs((ullong - rootullon)) / additionlon);
        rasterullon = rootullon + counter * additionlon;
        if (ullong < rootullon) {
            rasterullon = rootullon;
            counter = 0;
        }
        return counter;
    }

    public int maxlongitude(double lrlong, double ulong, int depth) {
        int counter = (int) Math.ceil(Math.abs((lrlong - rootullon)) / additionlon);
        rasterlrlon = rootullon + counter * additionlon;
        if (lrlong > rootlrlon) {
            rasterlrlon = rootlrlon;
            counter = (int) Math.pow(2, depth);
        }
        return counter;
    }

    public int minlatitude(double lrlat, int depth) {
        int counter = (int) Math.ceil((Math.abs(lrlat - rootullat)) / additionlat);
        rasterlrlat = rootullat - (counter) * additionlat;
        if (lrlat < rootlrlat) {
            rasterlrlat = rootlrlat;
            counter = (int) Math.pow(2, depth);
        }
        return counter;
    }

    public int maxlatitude(double ullrat, int depth) {
        int counter = (int) Math.floor((Math.abs(ullrat - rootullat)) / additionlat);
        rasterullat = rootullat - counter * additionlat;
        if (ullrat > rootullat) {
            rasterullat = rootullat;
            counter = 0;
        }
        return counter;
    }

    private boolean validater(double lrlon, double ullon, double ullat, double lrlat) {
        boolean a = false;
        if (ullon > rootlrlon) {
            return a;
        }
        if (lrlon < rootullon) {
            return a;
        }
        if (ullat < rootlrlat) {
            return a;
        }
        if (lrlat > rootullat) {
            return a;
        }
        return true;
    }

    public String[][] imagesfinder(int startla, int endla, int startlo, int endlo, int depth) {
        int difflon = endlo - startlo;
        int difflat = startla - endla;
        String[][] img = new String[difflat][difflon];
        for (int j = startlo; j < startlo + difflon; j++) {
            for (int i = endla; i < endla + difflat; i++) {
                img[i - endla][j - startlo] = "d" + depth + "_x" + j + "_y" + i + ".png";
            }
        }
        return img;
    }

    public int depthcalculator(double desiredlondopp, double width) {
        double differnce = diff;
        int level = 0;
        while (desiredlondopp < differnce / MapServer.TILE_SIZE) {
            level += 1;
            differnce = differnce / 2;
        }
        if (level > 7) {
            return 7;
        }
        if (level < 0) {
            return 0;
        }
        return level;
    }
}

