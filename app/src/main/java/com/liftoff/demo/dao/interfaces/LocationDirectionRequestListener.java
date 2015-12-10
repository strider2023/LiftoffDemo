package com.liftoff.demo.dao.interfaces;

import java.util.HashMap;
import java.util.List;

/**
 * Created by arindamnath on 10/12/15.
 */
public interface LocationDirectionRequestListener {

    void onSuccess(int requestId, List<List<HashMap<String, String>>> directions);
    void onFaliure();
}
