package com.haeram.safebrowsing.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by haeram@gmail.com on 2017-07-27.
 */

public class RlmObjUrl extends RealmObject {
    @PrimaryKey
    private String url;
    private boolean checked;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
