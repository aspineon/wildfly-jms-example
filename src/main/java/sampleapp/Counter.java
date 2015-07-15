package sampleapp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * Created by eduda on 13.7.2015.
 */
@Named
@ApplicationScoped
public class Counter {

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
