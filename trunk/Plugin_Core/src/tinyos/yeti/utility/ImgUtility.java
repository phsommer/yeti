/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.utility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * from https://bugs.eclipse.org/bugs/show_bug.cgi?id=70949
 * @author dcg
 *
 */
public class ImgUtility {

    public static ImageData downSample(Image image) {
        ImageData data = image.getImageData();
        if (!data.palette.isDirect && data.depth <= 8) return data;

        // compute a histogram of color frequencies
        Map<RGB, ColorCounter> freq = new HashMap<RGB, ColorCounter>();
        int width = data.width;
        int[] pixels = new int[width];
        int[] maskPixels = new int[width];
        for (int y = 0, height = data.height; y < height; ++y) {
            data.getPixels(0, y, width, pixels, 0);
            for (int x = 0; x < width; ++x) {
                RGB rgb = data.palette.getRGB(pixels[x]);
                ColorCounter counter = freq.get(rgb);
                if (counter == null) {
                    counter = new ColorCounter();
                    counter.rgb = rgb;
                    freq.put(rgb, counter);
                }
                counter.count++;
            }
        }

        // sort colors by most frequently used
        ColorCounter[] counters = new ColorCounter[freq.size()];
        freq.values().toArray(counters);
        Arrays.sort(counters);

        // pick the most frequently used 256 (or fewer), and make a palette
        ImageData mask = null;
        if (data.transparentPixel != -1 || data.maskData != null) {
            mask = data.getTransparencyMask();
        }
        int n = Math.min(256, freq.size());
        RGB[] rgbs = new RGB[n + (mask != null ? 1 : 0)];
        for (int i = 0; i < n; ++i) rgbs[i] = counters[i].rgb;
        if (mask != null) {
            rgbs[rgbs.length - 1] = data.transparentPixel != -1 ? 
                    data.palette.getRGB(data.transparentPixel) : new RGB(255, 255, 255);
        }
        PaletteData palette = new PaletteData(rgbs);

        // create a new image using the new palette:
        //   for each pixel in the old image, look up the best matching 
        //   index in the new palette
        ImageData newData = new ImageData(width, data.height, 8, palette);
        if (mask != null) newData.transparentPixel = rgbs.length - 1;
        for (int y = 0, height = data.height; y < height; ++y) {
            data.getPixels(0, y, width, pixels, 0);
            if (mask != null) mask.getPixels(0, y, width, maskPixels, 0);
            for (int x = 0; x < width; ++x) {
                if (mask != null && maskPixels[x] == 0) {
                    pixels[x] = rgbs.length - 1;
                }
                else {
                    RGB rgb = data.palette.getRGB(pixels[x]);
                    pixels[x] = closest(rgbs, n, rgb);
                }
            }
            newData.setPixels(0, y, width, pixels, 0);
        }
        return newData;
    }

    static int closest(RGB[] rgbs, int n, RGB rgb) {
        int minDist = 256*256*3;
        int minIndex = 0;
        for (int i = 0; i < n; ++i) {
            RGB rgb2 = rgbs[i];
            int da = rgb2.red - rgb.red;
            int dg = rgb2.green - rgb.green;
            int db = rgb2.blue - rgb.blue;
            int dist = da*da + dg*dg + db*db;
            if (dist < minDist) {
                minDist = dist;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private static class ColorCounter implements Comparable<ColorCounter> {
        RGB rgb;
        int count;

        public int compareTo( ColorCounter o) {
            return o.count - count;
        }
    }
}

