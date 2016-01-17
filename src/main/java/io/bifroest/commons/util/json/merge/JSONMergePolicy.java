package io.bifroest.commons.util.json.merge;

import org.json.JSONObject;

public interface JSONMergePolicy {

	JSONObject merge( JSONObject first, JSONObject second );

}
