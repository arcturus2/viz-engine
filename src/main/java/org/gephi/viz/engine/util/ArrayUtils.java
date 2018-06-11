package org.gephi.viz.engine.util;

import com.jogamp.opengl.util.GLBuffers;

public class ArrayUtils {

    /**
     * Inspired in https://stackoverflow.com/questions/32452025/fastest-way-to-create-new-array-with-length-n-and-fill-it-by-repeating-a-given-a
     *
     * You must ensure there is enough length in the array to copy.
     *
     * @param arr Array object, unsafe to use something that is not an array!
     * @param offset Offset in the array to start repeating
     * @param elementsCount Elements count from the offset position to repeat
     * @param times Times the elements should be repeated after the offset (including existing data)
     * @return Next index
     */
    public static int repeat(Object arr, int offset, int elementsCount, int times) {
        int newLength = times * elementsCount;

        for (long last = elementsCount; last != 0 && last < newLength; last <<= 1) {
            System.arraycopy(arr, offset, arr, offset + (int) last, (int) (Math.min(last << 1, newLength) - last));
        }

        return offset + elementsCount * times;
    }

    public static float[] ensureCapacity(float[] buffer, int elements) {
        final int capacity = buffer.length;
        if (capacity < elements) {
            int newElementsCapacity = GLBuffers.getNextPowerOf2(elements);

            System.out.println("Growing float buffer from " + capacity + " to " + newElementsCapacity + " elements");
            float[] newBuffer = new float[newElementsCapacity];

            System.arraycopy(buffer, 0, newBuffer, 0, capacity);
            return newBuffer;
        } else {
            return buffer;
        }
    }
    
    public static float[] ensureCapacityNoCopy(float[] buffer, int elements) {
        final int capacity = buffer.length;
        if (capacity < elements) {
            int newElementsCapacity = GLBuffers.getNextPowerOf2(elements);

            System.out.println("Growing float buffer from " + capacity + " to " + newElementsCapacity + " elements");
            return new float[newElementsCapacity];
        } else {
            return buffer;
        }
    }
}
