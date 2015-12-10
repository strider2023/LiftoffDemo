package com.liftoff.demo.dao.interfaces;

/**
 * Created by arindamnath on 09/12/15.
 */
public interface LocationRequestListener {

    void onSuccess(int id, double longitude, double latitude, String address, String country);
    void onFailure(int id);
}
