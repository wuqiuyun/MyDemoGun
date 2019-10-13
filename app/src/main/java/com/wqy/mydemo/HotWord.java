package com.wqy.mydemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/10/13 0013.
 */

public class HotWord {
    String trans;
    List<String> words;
    int size;
    String name;
    int number;
    int state;

    @Override
    public String toString() {
        return "HotWord{" +
                "trans='" + trans + '\'' +
                ", words=" + words +
                ", size=" + size +
                ", name='" + name + '\'' +
                ", number=" + number +
                ", state=" + state +
                '}';
    }
}
