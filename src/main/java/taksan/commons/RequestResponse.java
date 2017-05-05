package taksan.commons;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

public class RequestResponse implements Serializable {
	private static final long serialVersionUID = -9121943094727486673L;
	public final int responseCode;
	public final String content;
	public final Map<String, List<String>> headers;

	@SuppressWarnings("unchecked")
	public RequestResponse(int status, String content, Map<String, List<String>> headers) {
		this.responseCode = status;
		this.content = content;
		this.headers = UnmodifiableMap.decorate(headers);
	}
	
	public JSON getContentsJson() {
		return JSONSerializer.toJSON(content);
	}
}
