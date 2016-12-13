package com.soluto.exceptionEnrichment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExtraData extends Throwable {
    private Map<String, String> extraData;

    private ExtraData(Map<String, String> extraData) {
        this.extraData = extraData;
    }

    public static ExtraData create() {
        HashMap<String, String> map = new HashMap<>();
        return new ExtraData(map);
    }

    public ExtraData with(String key, String value) {
        extraData.put(key, value);
        return this;
    }

    public ExtraData with(String key, int value) {
        extraData.put(key, Integer.valueOf(value).toString());
        return this;
    }

    public ExtraData with(String key, boolean value) {
        extraData.put(key, Boolean.valueOf(value).toString());
        return this;
    }

    public ExtraData with(String key, long value) {
        extraData.put(key, Long.valueOf(value).toString());
        return this;
    }

    public ExtraData with(String mapKey, Map<String, String> map) {
        for (String key: map.keySet()) {
            extraData.put(mapKey + "." + key, map.get(key));
        }
        return this;
    }

    public ExtraData mergeWith(ExtraData extraData) {
        for (String key: extraData.buildDictionary().keySet()) {
            this.extraData.put(key, extraData.buildDictionary().get(key));
        }
        return this;
    }

    public ExtraData mergeWith(Map<String, String> extraDataDictionary) {
        for (String key: extraDataDictionary.keySet()) {
            this.extraData.put(key, extraDataDictionary.get(key));
        }
        return this;
    }

    public Map<String, String> buildDictionary(){
        return extraData;
    }

    public static Throwable enrichedException(Throwable throwable, ExtraData extraData) {
        Throwable currentCause = throwable;
        do {
            if (currentCause instanceof ExtraData) {
                ((ExtraData)currentCause).mergeWith(extraData);
                return throwable;
            }
            if (currentCause.getCause() == null) {
                try {
                    currentCause.initCause(extraData);
                }
                catch (IllegalStateException _) {

                }
                return throwable;
            }
            currentCause = currentCause.getCause();
        }
        while (true);
    }

    public static ExtraData getExtraData(Throwable throwable) {
        Throwable currentCause = throwable;
        while (currentCause != null) {
            if (currentCause instanceof ExtraData) {
                return (ExtraData) currentCause;
            }
            else {
                currentCause = currentCause.getCause();
            }
        }
        return ExtraData.create();
    }

    public static ExtraData withCorrelationId(ExtraData extraData) {
        if (!(extraData.buildDictionary().containsKey("extraDataCorrelationId"))) {
            extraData.with("extraDataCorrelationId", UUID.randomUUID().toString());
        }
        return extraData;
    }

    public static String getCorrelationId(ExtraData extraData) {
        if (extraData.buildDictionary().containsKey("extraDataCorrelationId")) {
            return extraData.buildDictionary().get("extraDataCorrelationId");
        }
        return null;
    }
}
