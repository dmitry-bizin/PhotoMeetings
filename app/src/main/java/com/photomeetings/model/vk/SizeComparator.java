package com.photomeetings.model.vk;

import java.util.Comparator;

public class SizeComparator implements Comparator<Size> {

    @Override
    public int compare(Size size1, Size size2) {
        return size1.getType().compareTo(size2.getType());
    }

}
