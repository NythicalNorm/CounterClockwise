package com.blockninja.counterclockwise;

import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.api.VsApi;

public class ApiGetter {
    // vsApi import fails in kotlin for who knows why, so we get it from java
    public static VsApi vsApi = ValkyrienSkies.api();
}
